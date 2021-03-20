package com.student.env.utilities;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import com.student.env.appSetting.ConfigReader;

import org.testng.*;

public class FtpService {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	Channel channel = null;
	Session session = null;
	JSch ssh = new JSch();
	ChannelSftp sftp = null;

	private String sftpHost;
	private String sftpPort;
	private String sftpUser;
	private String sftpPass;
	private String sftpWorkingDir;

	public FtpService(String sftpHost, String sftpPort, String sftpUser, String sftpPass, String sftpWorkingDir) {
		this.sftpHost = sftpHost;
		this.sftpPort = sftpPort;
		this.sftpUser = sftpUser;
		this.sftpPass = sftpPass;
		this.sftpWorkingDir = sftpWorkingDir;
	}

	public FtpService(String sftpHost, String sftpPort, String sftpUser, String sftpPass) {
		this.sftpHost = sftpHost;
		this.sftpPort = sftpPort;
		this.sftpUser = sftpUser;
		this.sftpPass = sftpPass;
	}

	public FtpService(FtpBuilder ftpBuilder) {
		this.sftpHost = ftpBuilder.sftpHost;
		this.sftpPort = ftpBuilder.sftpPort;
		this.sftpUser = ftpBuilder.sftpUser;
		this.sftpPass = ftpBuilder.sftpPass;
		this.sftpWorkingDir = ftpBuilder.sftpWorkingDir;
	}

	public FtpService() {
		super();
	}

	public static FtpService getStudentTeacherDetails(String workingDir) {
		FtpService ftpService = new FtpService();
		ftpService.sftpHost = "1.1.1.10";
		ftpService.sftpPass = "pass";
		ftpService.sftpPort = "22";
		ftpService.sftpUser = "studentUser";
		ftpService.sftpWorkingDir = workingDir.isEmpty() ? workingDir : "/usr/bin/student/details/";

		return ftpService;
	}

	public ChannelSftp connectSession() {
		session = oracleStudentFileShareSession();
		session.connect();
		channel = session.openChannel("sftp");
		channel.connect();
		sftp = (ChannelSftp) channel;
		return sftp;
	}

	public void disconnectSession() {
		channel.disconnect();
		session.disconnect();
	}

	// Builder pattern used

	public static class FtpBuilder {
		private String sftpHost;
		private String sftpPort;
		private String sftpUser = "student";
		private String sftpPass = "22";
		private String sftpWorkingDir;

		FtpBuilder() {
		}

		public FtpBuilder setSftpHost(String sftpHost) {
			this.sftpHost = sftpHost;
			return this;
		}

		public FtpBuilder setSftpPort(String sftpPort) {
			this.sftpPort = sftpPort;
			return this;
		}

		public FtpBuilder setSftpUser(String sftpUser) {
			this.sftpUser = sftpUser;
			return this;
		}

		public FtpBuilder setSftpPass(String sftpPass) {
			this.sftpPass = sftpPass;
			return this;
		}

		public FtpBuilder setSftpWorkingDir(String sftpWorkingDir) {
			this.sftpWorkingDir = sftpWorkingDir;
			return this;
		}

		public FtpService build() {
			return new FtpService(this);
		}

	}

	public void removeFileFromServer() throws Throwable {
		try {

			sftp = connectSession();

			// get list of files
			Vector<ChannelSftp.LsEntry> v = (Vector<ChannelSftp.LsEntry>) sftp.ls(sftpWorkingDir);
			Enumeration<LsEntry> e = v.elements();
			sftp.cd(sftpWorkingDir);

			// remove files one ata time
			while (e.hasMoreElements()) {
				ChannelSftp.LsEntry entry = (LsEntry) e.nextElement();
				if (entry.getFilename().startsWith(".")) {
					continue;
				}
				sftp.rm(entry.getFilename());
			}
			disconnectSession();

		} /*
			 * catch (JSchException e) { e.printStackTrace(); }
			 */
		catch (SftpException e) {
			e.printStackTrace();
		}
	}

	public void addFileToServer(String filePath, String sftpwrkngDir) {

		try {
			sftp = connectSession();
			sftp.cd(sftpwrkngDir);
			FileInputStream fileToUpload = new FileInputStream(new File(filePath));
			String currentTime = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			String fileNameToUpload = "studentFile" + currentTime;
			sftp.put(fileToUpload, fileNameToUpload, sftp.OVERWRITE);
			disconnectSession();

		} // catch (JSchException e) { e.printStackTrace(); }
		catch (SftpException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void downloadFilesFromServer(String sDownloadFilesToPath, String cdDir, boolean lastDayModifiedFilesOnly,
			boolean withPrivateKey) {

		try {
			sftp = connectSession();
			sftp.cd(cdDir);

			// get list of files
			Vector<ChannelSftp.LsEntry> v = null;
			Date currDate = new Date();
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			Enumeration<LsEntry> e = null;
			String regexSearch = dateFormat.format(currDate);

			if (lastDayModifiedFilesOnly) {
				LocalDate localDate = LocalDate.now();
				LocalDate minusDays = localDate.minusDays(1);
				regexSearch = minusDays.toString().replaceAll("-", "");
			}

			v = sftp.ls(cdDir + "/*" + regexSearch + "*");
			e = v.elements();

			// download file one at a time
			while (e.hasMoreElements()) {
				ChannelSftp.LsEntry entry = (LsEntry) e.nextElement();
				if (entry.getFilename().startsWith(".")) {
					continue;
				}
				sftp.get(entry.getFilename(), sDownloadFilesToPath);
				LOGGER.info("downloading the file ->" + entry.getFilename());
			}
			disconnectSession();

		} // catch (JSchException e) { e.printStackTrace(); }
		catch (SftpException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processXmlFilesListAndConvertToJson(File xmlFilesDir, String jsonFile, String... rootNodes)
			throws Exception {
		
	try {
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(jsonFile));
		bWriter.write("[");
		// do not search recursivley , just the main folder
		Collection<File> listFiles = FileUtils.listFiles(xmlFilesDir, new String[] {"xml"}, false);
		LinkedList<File> fileList = (LinkedList<File>)listFiles;
		boolean lastCharIsComma = false;
		
		//read each file and add the content to StudentMarks main Json file
		for (int i = 0; i < fileList.size(); i++) {
			File eachFile = fileList.get(i);
			String sContent = "";
			try {
				BufferedReader br = new BufferedReader(new FileReader(eachFile));
				String currentLine = "";
				while( (currentLine = br.readLine()) !=null){
					sContent += currentLine;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("unable to read this file ->"+eachFile.getAbsolutePath());
			}
			
			// if xml is empty do not process and skip it
			if(sContent.isEmpty()){
				continue;
			}
			
			// read till end of rootNode array and overwrite the endJsonObj
			JSONObject jsonObj = XML.toJSONObject(sContent.replaceAll("&", ""));
			JSONObject endJsonObj = jsonObj;
			
			try {
				for(String rootNodeTag : rootNodes){
					endJsonObj = endJsonObj.getJSONObject(rootNodeTag);
				}
			} catch (Exception e) {
				continue;
				// moving to net file in case if many files are getting dumped at the same time ...
			}
			try {
				bWriter.write(endJsonObj.toString());
				lastCharIsComma = false;
				// don't write comma on the last value of the list
				if(i!=fileList.size()-1){
					lastCharIsComma = true;
					bWriter.write(",");
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (lastCharIsComma) {
			bWriter.write("{}");
			
		bWriter.write("]"); // end Array
		}
	} catch (Exception e) {
		e.printStackTrace();
	}

	private Session oracleStudentFileShareSession() throws Throwable {

		ConfigReader conf = new ConfigReader();
		String privateKey = conf.get_UNIX_PrivateKey();
		String user = conf.get_UNIX_Username();
		String pwd = conf.get_UNIX_Password();
		String host = conf.get_UNIX_Host();
		int port = Integer.parseInt(conf.get_UNIX_port());

		Properties config = new Properties();

		session = ssh.getSession(user, host, port);
		config.put("StrictHostKeyChecking", "no");
		config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
		session.setPassword(pwd);
		session.setConfig(config);
		return session;
	}

	public void checkFileExistsOnserver(String expectedFile, String cdDir, boolean lastDayModifiedFilesOnly,
			boolean withPrivateKey) {

		boolean found = false;

		try {
			sftp = connectSession();
			sftp.cd(cdDir);

			// get list of files
			Vector<ChannelSftp.LsEntry> v = null;
			Enumeration<LsEntry> e = null;
			Date currDate = new Date();
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

			if (lastDayModifiedFilesOnly) {
				cal.add(Calendar.DATE, -1);

			}

			v = sftp.ls(cdDir + "/*" + expectedFile + "*");
			e = v.elements();

			// remove one file at a time
			List<String> fileNameList = new ArrayList<String>();

			while (e.hasMoreElements()) {
				ChannelSftp.LsEntry entry = (LsEntry) e.nextElement();
				if (entry.getFilename().contains(expectedFile)) {
					fileNameList.add(entry.getFilename());
					found = true;
				}
			}
			if (found) {
				LOGGER.info("file found ->" + expectedFile);
			}

			AssertJUnit.assertTrue(found);
			disconnectSession();

		} // catch (JSchException e) { e.printStackTrace(); }
		catch (SftpException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void downloadFilesFromServerWithSearch(String tempFileDownloadFolder, String searchKey, String ftpDirPath,
			String fileExtension, String date, boolean withPrivateKey) throws Throwable {

		ChannelExec channelExec = null;
		try {
			session = oracleStudentFileShareSession();
			session.connect();
			channelExec = (ChannelExec) session.openChannel("exec");

			String cmdToExecute = "grep -l" + searchKey + " " + ftpDirPath + "/*" + date.replaceAll("-", "") + "*"
					+ fileExtension + " | tail -n1";
			((ChannelExec) channelExec).setCommand(cmdToExecute);
			channelExec.connect();
			InputStream in = channelExec.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			int index = 0;

			// data containers for files
			ArrayList<String> fileList = new ArrayList<>();
			ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();

			// create a temp folder instead garbaging testDataFolder in
			// workspace ...

			while ((line = reader.readLine()) != null) {
				String fileName = line.substring(line.lastIndexOf('/') + 1);
				fileList.add(fileName);
				LOGGER.info("found the file");
				channelSftp.get(line, tempFileDownloadFolder); // download the
																// file
			}
			int exitStatus = channelExec.getExitStatus();
			if (exitStatus < 0) {
				LOGGER.error("SOMETHING WEN WRONG WITH COMMAND ... no files downloaded");
			} else if (exitStatus > 0) {
				LOGGER.error("Commad EXECUTED , BUT no files downloaded");
			} else if (exitStatus == 0) {
				LOGGER.error("no files downloaded");
			} else {
				LOGGER.info("found the raaw file to temp folder");
			}
			channelExec.disconnect();
			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteOldFiles(String sPath, String sFilesType, int days) throws Throwable {
		session = oracleStudentFileShareSession();
		String cmd = String.format("find %s -name \"%s\" -mtime +%s -exec rm {} +", sPath, sFilesType, days);

		try {
			session.connect();
			Channel channelExec = session.openChannel("exec");
			((ChannelExec) channelExec).setCommand(cmd);

			InputStream in = channelExec.getInputStream();
			channelExec.connect();
			channelExec.disconnect();
			session.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void uploadFilesToSftpServer(String localFilePath, List<String> files) {
		try {

			sftp = connectSession();
			sftp.cd(sftpWorkingDir);
			for (String file : files) {
				FileInputStream fileToUpload = new FileInputStream(
						new File(Paths.get(localFilePath, file).normalize().toString()));
				sftp.put(fileToUpload, file, ChannelSftp.OVERWRITE);
			}
			LOGGER.info("all files uplaoded ...");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			disconnectSession();
		}
	}
}
