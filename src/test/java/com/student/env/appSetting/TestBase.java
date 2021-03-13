package com.student.env.appSetting;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.common.base.Strings;

public class TestBase extends ConfigReader {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	/*private static class SingletonHolder {
		public static final TestBase instance = new TestBase();
	}*/
	
	public static TestBase getInstance(){		
		//public static final TestBase instance = new TestBase()
		final TestBase instance = new TestBase();
		return instance;
	}
	
	public static String getBrowserVersion(){
		String cmdString = "powershell.exe ([version](Get-Item (Get-ItemProperty 'HKLM:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe').'(Default)').VersionInfo.ProductVersion).Major";
		String versionInfo = "";
		String line = null;
		try {
			Process pr = Runtime.getRuntime().exec(cmdString);
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			while ((line = input.readLine()) != null) {
				pr.waitFor();
				if(Strings.isNullOrEmpty(line)) continue;
				versionInfo = line;		
			}
			LOGGER.info("Chrome Version -> "+versionInfo);
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.info("powershell chromeVersion cmd failed ...");
		}
		
		return versionInfo;				
	}
	
	public String Browser(){
		int version = Integer.parseInt(getBrowserVersion().trim());
		String driverPath = getWebdriverFolder();
		if(version<=83){
			return driverPath + "chromedriver76.exe";
		}else if(version==84){
			return driverPath + "chromedriver84.exe";
		}else if(version==85){
			return driverPath + "chromedriver85.exe";
		}		
		
		return driverPath + "chromedriver76.exe";
	}
	
	
}
