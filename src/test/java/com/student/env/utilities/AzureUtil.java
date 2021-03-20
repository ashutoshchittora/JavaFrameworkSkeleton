package com.student.env.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Assert;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.servicebus.ServiceBusConfiguration;
import com.microsoft.windowsazure.services.servicebus.ServiceBusContract;
import com.microsoft.windowsazure.services.servicebus.ServiceBusService;
import com.microsoft.windowsazure.services.servicebus.models.BrokeredMessage;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveMessageOptions;
import com.microsoft.windowsazure.services.servicebus.models.ReceiveSubscriptionMessageResult;
import com.microsoft.windowsazure.services.servicebus.models.SubscriptionInfo;
import com.opencsv.CSVReader;
import com.student.env.appSetting.ConfigReader;

import java.util.List;
import java.util.Map;

public class AzureUtil implements UtilityConstants {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	private static String downloadLocation = ConfigReader.getTestDataDownloadFolderPath();
	private static File blobDir = new File(downloadLocation);
	public static final String CONNECTION_STRING = ConfigReader.getStorageConnectionString();

	CloudFileClient fileClient = null;
	CloudFileShare fileShare = null;
	File localFile = null;
	CloudBlobClient blobClient = null;
	CloudBlobContainer blobContainer = null;
	CloudStorageAccount account = null;

	public void pushFileToAzure(String sourceLocalFilePath, String azureFileShareLocationPath) throws Exception {
		CloudFileDirectory rootDir, refDir = null;

		try {
			localFile = new File(sourceLocalFilePath);
			account = CloudStorageAccount.parse(CONNECTION_STRING);
			fileClient = account.createCloudFileClient();
			String[] splitPath = azureFileShareLocationPath.split("\\\\");
			for (String token : splitPath) {
				String rootDirectoryReference = token;
				fileShare = fileClient.getShareReference(rootDirectoryReference);
				rootDir = fileShare.getRootDirectoryReference();
			}

			// upload local file to root dir
			CloudFile cloudFile = refDir.getFileReference(localFile.getName());
			cloudFile.uploadFromFile(localFile.getAbsolutePath());
			LOGGER.info("successfully uploaded the file ...");

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Exception occured while uploading the file ...");
		}
	}

	public static void createBlockBlobCsvFile(String fileName) throws Exception {
		List<String> csvFileEntries = Arrays.asList("Student_id,Student_name,Student_age,Student_marks");
		File csvFile = new File(fileName);
		CSVParser.writeOrAppendToCsv(csvFile.getAbsoluteFile().toString(), csvFileEntries, false);

	}

	/**
	 * Convert Azure file storage & download all the csv files of a given date
	 * and convert to one CSV with proper header. Then transform csv to JSON.
	 */

	public static String downloadBlockBlobsFromAzureAndConvertToJson(String prodContainer, String storageConnection,
			int[] dateArray, boolean flag) throws Throwable {

		int currentYear = dateArray[0];
		int currentMonth = dateArray[1];
		int currentDay = dateArray[2];
		File blobDir = new File(downloadLocation);
		int i = 0;

		if (dateArray.length < 2) {
			throw new Exception("provide valid format as {yyyy,mm,dd} ");
		}

		if (blobDir.exists()) {
			File[] listFiles = blobDir.listFiles();
			for (File f : listFiles) {
				FileUtils.deleteQuietly(f);
			}
		} else {
			blobDir.mkdir();
		}

		String blockBlobJSONFile = downloadLocation + File.separatorChar + "blockBlob.json";
		String blobCsvFileName = downloadLocation + File.separatorChar + "blockBlobFile.csv";
		createBlockBlobCsvFile(blobCsvFileName);

		CloudStorageAccount storageAcc = CloudStorageAccount.parse(storageConnection);
		CloudBlobClient cloudBlobClient = storageAcc.createCloudBlobClient();
		CloudBlobDirectory blobContainer = cloudBlobClient.getContainerReference("studentdata")
				.getDirectoryReference("files/" + prodContainer);

		List<CloudBlob> list = new ArrayList<CloudBlob>();
		for (ListBlobItem l : blobContainer.listBlobs()) {
			list.add((CloudBlob) l);
		}

		Collections.sort(list,
				(o1, o2) -> o2.getProperties().getLastModified().compareTo(o1.getProperties().getLastModified()));

		for (CloudBlob b : list) {
			Date blockBlobDate = b.getProperties().getLastModified();
			LocalDate currentDate = LocalDate.of(currentYear, currentMonth, currentDay);
			Instant instant = blockBlobDate.toInstant();
			LocalDate blockBlobLocalDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
			if (currentDate.equals(blockBlobLocalDate)) {
				String blobFileName = b.getName().contains("/") ? b.getName().split("/")[2] : b.getName();
				LOGGER.info("file download -> " + blobFileName);

				b.download(new FileOutputStream(new File(downloadLocation + File.separatorChar + blobFileName)));
				i++;
				if (flag && i > 2) {
					break;
				}
			}
			LOGGER.info(" downloaded" + i + "blob csv files to" + downloadLocation);

			// lambdas and stream used in parallel processing to read csv files
			// faster.

			java.nio.file.Files.list(Paths.get(downloadLocation)).parallel().forEach((eachFile) -> {
				List<String[]> lsContents = new ArrayList<>();
				try (Reader reader = new FileReader(eachFile.toFile()); CSVReader csvRead = new CSVReader(reader)) {
					String[] nextRec = null;

					while ((nextRec = csvRead.readNext()) != null) {
						lsContents.add(nextRec);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					CSVParser.writeRecordsToCSV(blobCsvFileName, lsContents, true);
				} catch (Exception e2) {
					LOGGER.error(
							"did not write to block csv file" + eachFile.getFileName().toString() + e2.getMessage());
				}
			});
			LOGGER.info("converted all block blob csv to one CSV" + blobCsvFileName);

		}
		// convert the blobCsvFileName to JSON file
		JsonParser.convertCSVToJSONFileAndReturnFile(blobCsvFileName, blockBlobJSONFile);
		return blockBlobJSONFile;
	}

	public static String downloadBlockBlobsFromAzureAndConvertToJSON(String productCont, String StorConnStr, String day,
			boolean flag) throws Exception {

		int year, month, todaysDate;
		if (day.toLowerCase() == "yesterday") {
			LocalDate yesterdayDate = LocalDate.now().minusDays(1);
			year = yesterdayDate.getYear();
			month = yesterdayDate.getMonthValue();
			todaysDate = yesterdayDate.getDayOfMonth();
		} else {
			year = LocalDate.now().getYear();
			month = LocalDate.now().getMonthValue();
			todaysDate = LocalDate.now().getDayOfMonth();
		}

		int[] date = { year, month, todaysDate };

		return downloadBlockBlobsFromAzureAndConvertToJSON(productCont, StorConnStr, day, flag);
	}

	public <StorageFile> void DownloadFromAzureStorage(String localFilePath, String azureFileShareLocatioPAth)
			throws Exception {
		try {
			account = CloudStorageAccount.parse(CONNECTION_STRING);
			blobClient = account.createCloudBlobClient();
			blobContainer = blobClient.getContainerReference("StudentArchive");
			for (ListBlobItem i : blobContainer.listBlobs()) {
				if (i instanceof CloudBlob) {
					CloudBlob b = (CloudBlob) i;
					b.download(new FileOutputStream(downloadLocation + b.getName()));
				}
			}
			LOGGER.info("file downloaded successfully ...");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Exception while uploading the file ...");
		}

	}

	public static boolean postMessageToServiceBustTopic(String filePath, String topic, String subscriber)
			throws InterruptedException {
		boolean success = false;

		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication(
				ConfigReader.getServiceBusNameSpace(), ConfigReader.getSeviceBusUsername(),
				ConfigReader.getServiceBusPassword, ".servicebus.windows.net");

		ServiceBusContract serviceBusContract = ServiceBusService.create(config);
		File file = new File(filePath);
		Assert.assertTrue(file.exists());

		try {
			byte[] encoded = Files.readAllBytes(file.toPath());
			String inputMsg = new String(encoded, "UTF-8");
			BrokeredMessage bkMsg = new BrokeredMessage(inputMsg);
			bkMsg.setContentType("text/plain");
			serviceBusContract.sendTopicMessage(topic, bkMsg);
			success = true;

		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return success;
	}

	public static boolean postStudentMessageToServiceBustTopic(String filePath, String topic, String subscriber)
			throws InterruptedException, ServiceException {
		boolean success = false;

		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication(
				ConfigReader.getServiceBusNameSpace(), ConfigReader.getSeviceBusSharedAccessKeyName(),
				ConfigReader.getServiceBusShareDaCCESSkEY, ConfigReader.getServiceBuseEndpoint());
		ServiceBusContract serviceBusContract = ServiceBusService.create(config);

		File file = new File(filePath);
		Assert.assertTrue(file.exists());

		try {
			byte[] encoded = Files.readAllBytes(file.toPath());
			String inputMsg = new String(encoded, "UTF-8");
			BrokeredMessage bkMsg = new BrokeredMessage(inputMsg);
			bkMsg.setContentType("text/plain");
			serviceBusContract.sendTopicMessage(topic, bkMsg);
			success = true;

		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return success;
	}

	public static boolean postMessageToServiceBustWithProperty(String filePath, String topic, String subscriber,
			String prop1, String val1) throws InterruptedException, ServiceException {
		boolean success = false;

		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication(
				ConfigReader.getServiceBusNameSpace(), ConfigReader.getSeviceBusUsername(),
				ConfigReader.getServiceBusPassword(), ".servicebus.windows.net");
		ServiceBusContract serviceBusContract = ServiceBusService.create(config);

		File file = new File(filePath);
		Assert.assertTrue(file.exists());

		try {
			byte[] encoded = Files.readAllBytes(file.toPath());
			String inputMsg = new String(encoded, "UTF-8");
			BrokeredMessage bkMsg = new BrokeredMessage(inputMsg);
			bkMsg.setContentType("text/plain");
			bkMsg.setProperty(prop1, val1);
			serviceBusContract.sendTopicMessage(topic, bkMsg);
			success = true;

		} catch (ServiceException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return success;
	}

	public static List<Map<String,Object>> getServiceBusData(String topic, String subscriber , boolean retry) throws IOException,ServiceException{
		
		int waitTime = SERVICE_BUS_WAITING_TIME;
		List<Map<String,Object>> notificationList;
		int count = 0;
		while(notificationList=serviceBusData(topic, subscriber).size() == 0){
			if(count++ > = SERVICE_BUS_RETRY || !retry){
				break;
			}
			
			LOGGER.info("fetching SB data . Retry : " + count);
			TimeOutUtil.sleepFor(waitTime);
			waitTime = SERVICE_BUS_RETRY_WAITING_TIME;
		}
		
		return notificationList;
	}

	public static List<Map<String, Object>> serviceBusData(String endpoint, String namespace, String username,
			String pwd, String topic, String subscriber) throws IOException, ServiceException {
		Configuration config = ServiceBusConfiguration.configureWithSASAuthentication(namespace, username, pwd,
				endpoint);
		ServiceBusContract serviceBusContract = ServiceBusService.create(config);
		List<SubscriptionInfo> list = serviceBusContract.listSubscriptions(topic).getItems();
		long count = 0;
		for (SubscriptionInfo subs : list) {
			String subName = subs.getName();
			if (subName.equalsIgnoreCase(subscriber)) {
				count = subs.getMessageCount();
			}
		}
		List<Map<String, Object>> notificationList = new ArrayList<>();
		while (notificationList.size() != count) {
			ReceiveSubscriptionMessageResult resultMsg = serviceBusContract.receiveSubscriptionMessage(topic,
					subscriber);
			BrokeredMessage bkMsg = resultMsg.getValue();
			if (bkMsg != null && bkMsg.getMessageId() != null) {
				notificationList.add(ReceiveMessageOptions(bkMsg));
			}
		}

		return notificationList;
	}

}
