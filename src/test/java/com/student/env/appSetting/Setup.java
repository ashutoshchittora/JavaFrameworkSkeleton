package com.student.env.appSetting;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.util.Strings;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.student.env.pageObject.LoginPageObject;
import com.student.env.stepDefinition.baseStep.BackgroundStepDefs;
import com.student.env.testHelper.PageObjectsLoad;
import com.student.env.utilities.TimeOutUtil;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;

public class Setup {
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	public static WebDriver driver;

	// just add LoginPageObject for all different users and applications
	static LoginPageObject login;
	private static boolean bAppLogin = false;
	private static String projectPath;
	private static boolean bLogin = false;

	public Setup() {
		projectPath = System.getProperty("user.dir");
	}

	public static void initialize() throws Exception {
		teardown();
		int iAttempt = 0;

		do {
			try {
				TestBase Get = TestBase.getInstance();
				System.setProperties("webdriver.chrome.driver", Get.Browser());
				initChromeBrowser();
				driver.manage().timeouts().implicitlyWait(TimeOutUtil.getUiTimeOut(), TimeUnit.MILLISECONDS);
				driver.navigate().to(Get.loginUrl());

				waitForPageToLoad();

			} catch (org.openqa.selenium.TimeoutException e) {
				String snapshotFilename = "TimeoutExceptionWhileLoggingIn" + "_"
						+ UUID.randomUUID().toString().replace("-", "");
				LOGGER.debug("Encountered timeout exception while connecting to test env - screenshot -> "
						+ BackgroundStepDefs.takeSnapshot(snapshotFilename));
				teardown();
				iAttempt++;
				continue;
			}

			break;
		} while (iAttempt < 3);
	}

	public static void initChromeBrowser(){
		int iAttempt =0;
		 do {
			try {
				/*TestBase Get = TestBase.getInstance();
				System.setProperties("webdriver.chrome.driver",Get.Browser());
				WebDriverManager.chromedriver().setup();
				*/
				
				ChromeOptions options = new ChromeOptions();
				/*if(!(Strings.isNullOrEmpty(System.getenv("TeamCity.WebDriver.Resolution")) && 
						Strings.isNullOrEmpty(System.getenv("TeamCity.WebDriver.Headless"))) ){
					
					options.addArguments(System.getenv("TeamCity.WebDriver.Resolution"));
					options.addArguments(System.getenv("TeamCity.WebDriver.Headless"));
					LOGGER.info("WebDriver Mode - [HEADLESS]");
				}*/
				
				options.addArguments("--start-maximized");
				// below code to fix "Please protect ports used by chromedriver and realted test framework tto prevent access by malicious code"
				options.addArguments("--whitelist-ip *");
				options.addArguments("proxy-server='direct://'");
				options.addArguments("--proxy-bypass-list=*");
				options.addArguments("--ignore-certificate-errors");
				driver = new ChromeDriver(options);
				
				if(driver!=null){
					break;
				}
			} catch (Exception e) {
				LOGGER.debug("chrome init failed - retying ->" +  ++iAttempt);
				LOGGER.debug(" Exeception details : "+e.getMessage());
			}
		} while (iAttempt<3);
		 
		if(driver!=null){
			driver.manage().timeouts().implicitlyWait(TimeOutUtil.getUiTimeOut(), TimeUnit.MILLISECONDS);
		}

	public static void Login() throws Exception {
		TestBase Credentials = TestBase.getInstance();
		login = new LoginPageObject(driver);
		login.enterLoginCredentials(Credentials.Username(), Credential s.Password());
		// SmokeTestLoadTime.startLogger();
		waitForPageToLoad();
		// SmokeTestLoadTime.endLogger();
		// SmokeTestLoadTime.logTime("Login","wait till SignIn button gets
		// clicked");
		bLogin = true;
	}

	public static void LogOff() {

		try {
			if (!bAppLogin)
				return;

			bAppLogin = false;
			login = new LoginPageObject(driver);
			waitForPageToLoad();
			WaitForElementToBeClickable(login.logOut);
			login.logOutBtnClick();
			waitForPageToLoad();

		} catch (Throwable e) {
			LOGGER.error(e.getMessage());
		}

	}

	public static String takeSnapShot(String fileName) throws Exception {
		if(driver != null){
			fileName = fileName.replaceAll("[/:*?<>|\\\\]","");
			TakesScreenshot scrShot = (TakesScreenshot)driver;
			File srcFile = scrShot.getScreenshotAs(OutputType.FILE);
			String filePath = projectPath + "\\artifacts\\" + fileName + ".png";
			File destFile = new File(filePath);
			FileUtils.copyFile(srcFile,destFile);
			return filePath;
			
		}
		return null;
	}
	
	public static void waitForPageToLoad(){
		new WebDriverWait(driver, 5).until(new PageObjectsLoad());		
	}
	
	public static void waitForPageToLoad(int time){
		new WebDriverWait(driver, time).until(new PageObjectsLoad());		
	}

	public static WebElement WaitForElementToBeVisible(WebElement oWebElement){
		oWebElement = new WebDriverWait(driver, TimeOutUtil.getUiTimeOut()).until(ExpectedConditions.visibilityOf(oWebElement));
		return oWebElement;
	}
	
	public static WebElement WaitForElementToBeClickable(WebElement oWebElement){
		oWebElement = new WebDriverWait(driver, TimeOutUtil.getUiTimeOut()).until(ExpectedConditions.elementToBeClickable(oWebElement));
		return oWebElement;
	}
	
	public static void WaitForElementToBeClickable(WebElement oWebElement , int intTimeOut){
		oWebElement = new WebDriverWait(driver, intTimeOut).until(ExpectedConditions.elementToBeClickable(oWebElement));
	}
	
	
	public static boolean isElementVisible(WebElement wWebElement , int intTimeOut){
		WebElement oWebElement = null;
		try {
			oWebElement = new WebDriverWait(driver, intTimeOut).until(ExpectedConditions.visibilityOf(oWebElement));
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}
		return oWebElement==null?false : true;
	}
	
	public static void clickBtnWithJSFailOver(WebElement elementToClick) throws Exception {
		try {
			elementToClick.click();
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
			throw new Exception("Element not found hence element not clicked is -> "+elementToClick);			
		} catch(WebDriverException e) {
			((JavascriptExecutor)driver).executeScript("arguments[0].click();", elementToClick);
			LOGGER.error("The standard click is not successful , attempting with JS executor");
		}
	}
	
	public static void teardown(){
		if(driver!=null){
			
			try {
				LogOff();
			} catch (Exception e) {
				e.printStackTrace();
			}
			driver.manage().deleteAllCookies();
			driver.quit();
			driver =null;
		}
	}
	
}
