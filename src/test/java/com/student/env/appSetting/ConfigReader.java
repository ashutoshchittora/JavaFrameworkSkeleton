package com.student.env.appSetting;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Optional;

public class ConfigReader {

	protected static Properties configProp = null;
	protected static String filename = "Config.properties";

	static {

		try {
			configProp = new Properties();
			String buildEnvironment = System.getProperty("TC_BUILD_ENV");
			InputStream input = new FileInputStream(getConfigPropertyFile(filename));
			configProp.load(input);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String getProjectRootFolder() {
		// Path root = FileSystems.getDefault().getPath("").toAbsolutePath();
		// Path filepath = Paths.get(root.toString(),"src","main","resources",
		// filename);
		String path = System.getProperty("user.dir");
		return path;

	}

	/*
	 * public static String getConfigPropertyFile(Optional<String> fileName){
	 * String s= fileName.isPresent()?fileName.get() : filename; Path filePath =
	 * Paths.get(getProjectRootFolder(), "src","test","resources",s); return
	 * filePath.toAbsolutePath().toString();
	 * 
	 * }
	 */

	public static String getConfigPropertyFile(String... fileName) {
		String s = (fileName != null) ? fileName[0] : filename;
		Path filePath = Paths.get(getProjectRootFolder(), "src", "test", "resources", s);
		return filePath.toAbsolutePath().toString();

	}

	public static String getTestDataUploadFolderPath() {
		Path filePath = Paths.get(getProjectRootFolder(), "src", "test", "resources", "TestData", "upload");
		return filePath.toAbsolutePath().toString();
	}

	public static String getTestDataDownloadFolderPath() {
		Path filePath = Paths.get(getProjectRootFolder(), "src", "test", "resources", "TestData", "download");
		return filePath.toAbsolutePath().toString();
	}

	public static String getStorageConnectionString() {
		return configProp.getProperty(getEnvironment() + "azure.student.storageConnectionString");
	}

	public static String getTestRunType() {
		return configProp.getProperty("TestRunType");
	}

	public static String getEnvironment() {
		return configProp.getProperty("Environemnt");
	}

	public static String TestDatabase() {
		return configProp.getProperty(getEnvironment() + ".test.servername");
	}

	public String getBrowserType() {

		return configProp.getProperty("browser");
	}

	public String getWebdriverFolder() {

		return configProp.getProperty("webDriverFolder");
	}

	public String loginUrl() {

		return configProp.getProperty(getEnvironment() + ".LOGIN.url");
	}

	public String getUsername() {

		return configProp.getProperty(getEnvironment() + ".LOGIN.username");
	}

	public String Password() {

		return configProp.getProperty(getEnvironment() + ".LOGIN.password");
	}

	public static String getStudentUri() {

		return configProp.getProperty(getEnvironment() + ".student.failed.uri");
	}
}
