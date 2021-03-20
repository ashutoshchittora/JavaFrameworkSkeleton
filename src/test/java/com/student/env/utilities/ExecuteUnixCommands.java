package com.student.env.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;

import cucumber.api.Scenario;

public class ExecuteUnixCommands {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	public static String user = "Student";
	public static String password = "pwd123";
	public static String host = "1.1.1.1";
	public static String privateKey = "Student_rivate_KEY";

	public static int exitStatus;
	public static int port = Integer.parseInt("8080");

	public static String studentBox_connect_sshClinet_checkOutputFile(String fileName) throws Exception {

		StringBuffer fileContent = new StringBuffer();
		BufferedReader buffReader = null;
		InputStream iStream = null;

		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			LOGGER.info("session created ...");
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publicKey,keyboard-interactive,password");
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			LOGGER.info("session connected ...");

			Channel channel = session.openChannel("sftp");
			channel.connect();

			ChannelSftp sftpChannel = (ChannelSftp) channel;
			String cmdCheckOutputFile = "/orabin/app/oracle/StudentProduct/retailSchool/batch/RETLForStudent/data/"
					+ fileName;
			iStream = sftpChannel.get(cmdCheckOutputFile);

			if (iStream != null) {
				LOGGER.info("file data is available as below :");
				buffReader = new BufferedReader(new InputStreamReader(iStream), 1);
				String line = "";
				while ((line = buffReader.readLine()) != null) {
					fileContent.append(line);
					fileContent.append("\n");
				}
			} else {
				LOGGER.info("File data not available ...");
			}
			session.disconnect();
		} catch (Exception e) {
			LOGGER.error("exception during unix box connection" + e.getMessage());
		} finally {
			if (iStream != null)
				iStream.close();
			if (buffReader != null)
				buffReader.close();
		}

		return fileContent.toString();
	}

	public static void studentBox_connect_sshClinet_checkOutputFileExists(String fileName, int count)
			throws IOException, JSchException, SftpException {

		int nol = 0;
		BufferedReader buffReader = null;
		InputStream in = null;

		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			LOGGER.info("session created ...");
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publicKey,keyboard-interactive,password");
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			LOGGER.info("session connected ...");

			Channel channel = session.openChannel("sftp");
			channel.connect();

			ChannelSftp sftpChannel = (ChannelSftp) channel;
			String cmdCheckOutputFile = "/orabin/app/oracle/StudentProduct/retailSchool/batch/RETLForStudent/data/"
					+ fileName;
			in = sftpChannel.get(cmdCheckOutputFile);

			if (in != null) {
				LOGGER.info("file data is available as below :");
				buffReader = new BufferedReader(new InputStreamReader(in), 1);
				String line = "";
				while ((line = buffReader.readLine()) != null) {
					nol++;
				}

				LOGGER.info("nol ::::" + nol);
				LOGGER.info("count::::" + count);
				if (nol != count) {
					throw new AssertionError("content mismatch");
				}
			} else {
				LOGGER.info("File data not available ...");
			}
			session.disconnect();
		} catch (IOException e) {
			LOGGER.error("exception during unix box connection" + e.getMessage());
		}

	}

	public static boolean compareList(ArrayList<String> uiList, ArrayList<String> studentList) {
		boolean notFound = false;
		for (int i = 0; i < uiList.size(); i++) {
			if (studentList.contains(uiList.get(i))) {

			} else {
				notFound = true;
				break;
			}
		}
		return notFound;
	}

	public static boolean CheckFilePresentInTargetPath(String FileContains) {
		try {

			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			LOGGER.info("session created ...");
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publicKey,keyboard-interactive,password");
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			LOGGER.info("session connected ...");

			Channel channel = session.openChannel("sftp");
			channel.connect();

			ChannelSftp sftpChannel = (ChannelSftp) channel;
			String targetPath = "/student/oracle/UAT/data/outbound//Integration/StudentBag/pending";
			sftpChannel.cd(targetPath);

			Vector fileslist = sftpChannel.ls(targetPath);
			for (int i = 0; i < fileslist.size(); i++) {
				LsEntry entry = (LsEntry) fileslist.get(i);
				if (entry.getFilename().contains(FileContains)) {
					LOGGER.info("file reached target folder & ready for pick-up :" + entry.getFilename());
					return true;
				}
			}

		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}

		return false;
	}

	public static String studenBox_connect_sshClient_checkOutputInBlobFile(String fileName)
			throws IOException, StorageException, URISyntaxException, InterruptedException {
		String downloadBlobAsText = null;
		String blobSignature = "";
		String sContainerName = "";
		String sDirectoryName = "";
		CloudBlobClient blobClient = new CloudBlobClient(new URI(blobSignature));
		CloudBlobContainer oBlocContainer = blobClient.getContainerReference(sContainerName);
		String blobName = "";
		String expectedBlobName = sDirectoryName + "/" + fileName;

		// checking if container has a blob present
		if (oBlocContainer.exists()) {
			CloudBlobDirectory dirRef = oBlocContainer.getDirectoryReference(sDirectoryName);

			// Listing blobs in container and adding all blob in blobList
			Iterator<ListBlobItem> it = dirRef.listBlobs().iterator();
			while (it.hasNext()) {
				CloudBlockBlob blob = (CloudBlockBlob) (it.next());
				blobName = blob.getName();
				if (blobName.equalsIgnoreCase(expectedBlobName)) {
					downloadBlobAsText = blob.downloadText("UTF-8", null, null, null);
				}
			}
		}
		TimeOutUtil.sleepFor(5000);

		return downloadBlobAsText;
	}

	public static void connect_sshClinet_checkOutputFileContent(String fileName, String scenario) throws IOException {

		StringBuffer fileContent = new StringBuffer();
		BufferedReader buffReader = null;
		InputStream iStream = null;
		XSSFSheet studentData = ExcelUtil.getExcelTestData("StudentData", "StudentData.xlsx").getSheet("Add_Student");
		XSSFSheet studenDataDetails = ExcelUtil.getExcelTestData("StudentData", "StudentData.xlsx")
				.getSheet("Add_Detail");

		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			LOGGER.info("session created ...");
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publicKey,keyboard-interactive,password");
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			LOGGER.info("session connected ...");

			Channel channel = session.openChannel("sftp");
			channel.connect();

			ChannelSftp sftpChannel = (ChannelSftp) channel;
			String cmdCheckOutputFile = "";
			if (scenario.equalsIgnoreCase("studentGrade")) {
				LOGGER.info(cmdCheckOutputFile);
				cmdCheckOutputFile = "/orabin/app/oracle/StudentProduct/retailSchool/batch/priceStudent/archive/"
						+ fileName;
			}
			iStream = sftpChannel.get(cmdCheckOutputFile);

			if (iStream != null) {
				LOGGER.info("file data is available as below :");
				buffReader = new BufferedReader(new InputStreamReader(iStream), 1);
				String line = "";
				String found = "False";
				while ((line = buffReader.readLine()) != null) {
					fileContent.append(line);
					if (line.contains(studentData.getRow(1).getCell(0).toString())) {
						found = "True";
						if (line.contains(studenDataDetails.getRow(1).getCell(1).toString())) {
							throw new AssertionError("found deleted differentiator");
						}
					}

				}
				if (found.equalsIgnoreCase("false")) {
					throw new AssertionError("Could not find student data");
				}
			} else {
				LOGGER.info("file data not avaialble ... ");
			}
			session.disconnect();
		} catch (Exception e) {
			LOGGER.error("exception during unix box connection" + e.getMessage());
		} finally {
			if (iStream != null)
				iStream.close();
			if (buffReader != null)
				buffReader.close();
		}

	}

	public static void CheckBlobFileContent(String fileName)
			throws IOException, StorageException, URISyntaxException, InterruptedException {

		String downloadBlobAsText = null;
		String blobSignature = "APBlobUriName()";
		String sContainerName = "APBlobContainerName()";
		String sDirectoryName = "APBlobDirName()";
		CloudBlobClient blobClient = new CloudBlobClient(new URI(blobSignature));
		CloudBlobContainer oBlocContainer = blobClient.getContainerReference(sContainerName);
		String blobName = "";
		String expectedBlobName = sDirectoryName + "/" + fileName;
		BufferedReader buffReader = null;
		InputStream iStream = null;
		XSSFSheet studentData = ExcelUtil.getExcelTestData("studentdata", "studentData.xlsx").getSheet("Add_Student");
		XSSFSheet studentDataDetails = ExcelUtil.getExcelTestData("studentdata", "studentData.xlsx")
				.getSheet("Add_Detail");

		// checking if container has a blob present
		if (oBlocContainer.exists()) {
			CloudBlobDirectory dirRef = oBlocContainer.getDirectoryReference(sDirectoryName);

			// Listing blobs in container and adding all blob in blobList
			Iterator<ListBlobItem> it = dirRef.listBlobs().iterator();
			while (it.hasNext()) {
				CloudBlockBlob blob = (CloudBlockBlob) (it.next());
				blobName = blob.getName();
				if (blobName.equalsIgnoreCase(expectedBlobName)) {
					downloadBlobAsText = blob.downloadText("UTF-8", null, null, null);
				}
			}
		}

		TimeOutUtil.sleepFor(5000);
		iStream = new ByteArrayInputStream(downloadBlobAsText.getBytes(StandardCharsets.UTF_8.name()));

		if (iStream != null) {
			LOGGER.info("file data is available as below :");
			buffReader = new BufferedReader(new InputStreamReader(iStream), 1);
			String line = "";
			String found = "False";
			while ((line = buffReader.readLine()) != null) {
				if (line.contains(studentData.getRow(1).getCell(0).toString())) {
					found = "True";
					if (line.contains(studentDataDetails.getRow(1).getCell(1).toString())) {
						throw new AssertionError("found deleted differentiator");
					}
				}

			}
			if (found.equalsIgnoreCase("false")) {
				throw new AssertionError("Could not find student data");
			}
		}
	}

	public static void Connect_SshClient_delete_outputFile(String fileName) throws IOException {

		InputStream iStream = null;

		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			LOGGER.info("session created ...");
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publicKey,keyboard-interactive,password");
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			LOGGER.info("session connected ...");

			Channel channel = session.openChannel("sftp");
			channel.connect();

			ChannelSftp sftpChannel = (ChannelSftp) channel;
			String cmdCheckOutputFile = "/orashare/app/oracle/StudentProduct/retailSchool/batch/RETLForStudent/data/"
					+ fileName;

			iStream = sftpChannel.get(cmdCheckOutputFile);

			if (iStream != null) {
				sftpChannel.rm(cmdCheckOutputFile);
				;
			} else {
				LOGGER.info("File not available to delete ...");
			}
			session.disconnect();
		} catch (Exception e) {
			LOGGER.error("exception during unix box connection" + e.getMessage());
		}

	}

	public static boolean Connect_SshClient_checkOutputFile(String fileName, int studentId) throws IOException {

		BufferedReader buffReader = null;
		InputStream iStream = null;
		boolean found = false;

		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			LOGGER.info("session created ...");
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publicKey,keyboard-interactive,password");
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			LOGGER.info("session connected ...");

			Channel channel = session.openChannel("sftp");
			channel.connect();

			ChannelSftp sftpChannel = (ChannelSftp) channel;
			String cmdCheckOutputFile = "/orabin/app/oracle/StudentProduct/retailSchool/batch/RETLForStudent/data/"
					+ fileName;
			iStream = sftpChannel.get(cmdCheckOutputFile);
			if (iStream != null) {

				LOGGER.info("file data is available as below :");
				buffReader = new BufferedReader(new InputStreamReader(iStream), 1);
				String line = "";
				while ((line = buffReader.readLine()) != null) {
					if (line.contains(String.valueOf(studentId))) {
						found = true;
					}
				}
			} else {
				LOGGER.info("File data not available ...");
			}
			session.disconnect();
		} catch (Exception e) {
			LOGGER.error("exception during unix box connection" + e.getMessage());
		} finally {
			if (iStream != null)
				iStream.close();
			if (buffReader != null)
				buffReader.close();
		}

		return found;

	}

	public static String checkOutputFileContent(String path, String where, String actionType) {
		StringBuffer fileContent = new StringBuffer();
		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			LOGGER.info("session created ...");
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			config.put("PreferredAuthentications", "publicKey,keyboard-interactive,password");
			session.setPassword(password);
			session.setConfig(config);
			session.connect();
			LOGGER.info("session connected ...");

			Channel channel = session.openChannel("sftp");
			channel.connect();

			ChannelSftp sftpChannel = (ChannelSftp) channel;
			if (actionType.equalsIgnoreCase("ListFiles")) {
				sftpChannel.cd(path);
				Vector fileList = sftpChannel.ls(path);

				for (int i = 0; i < fileList.size(); i++) {
					LsEntry entry = (LsEntry) fileList.get(i);
					fileContent.append(entry.getFilename());
					fileContent.append("\n");
				}
			} else if (actionType.equalsIgnoreCase("fileContent")) {
				InputStream in = sftpChannel.get(path);
				if (in != null) {
					BufferedReader bfRead = new BufferedReader(new InputStreamReader(in), 1);
					String line = "";
					while ((line = bfRead.readLine()) != null) {
						fileContent.append(line);
						fileContent.append("\n");
					}
					in.close();
					bfRead.close();
				} else {
					throw new AssertionError("file data is not present", null);
				}
			}
			sftpChannel.disconnect();
			channel.disconnect();
			session.disconnect();
		} catch (Exception e) {
			e.getMessage();
		}

		return fileContent.toString();
	}

}
