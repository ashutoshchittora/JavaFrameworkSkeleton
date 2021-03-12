package com.student.env.pageObject;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.student.env.utilities.TimeOutUtil;

public class BasePageObject {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	protected WebDriver driver;

	public BasePageObject(WebDriver driver) {
		this.driver = driver;
	}

	public void waitForPageToLoad(int time) {
		new WebDriverWait(driver, time).until(new PageObjectsLoad());
	}

	public void waitForPageToLoad() {
		new WebDriverWait(driver, 240).until(new PageObjectsLoad());
	}

	public WebElement WaitForElementToBeClickable(WebElement oWebElement) {
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
		WebDriverWait wait = new WebDriverWait(driver, TimeOutUtil.getUiTimeOut());
		oWebElement = wait.until(ExpectedConditions.elementToBeClickable(oWebElement));
		driver.manage().timeouts().implicitlyWait(TimeOutUtil.getUiTimeOut(), TimeUnit.MILLISECONDS);
		return oWebElement;
	}

	public void zoomOut(int noOfClicks) throws AWTException {
		int i = noOfClicks;
		Robot robot = new Robot();
		while (i > 0) {
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_MINUS);
			i--;
		}
	}

	public void resetBrowser() {
		WebElement htmlElement = driver.findElement(By.tagName("html"));
		htmlElement.sendKeys(Keys.chord(Keys.CONTROL, "0"));
	}
	
	
	public void  waitForElementToBeVisible() throws Exception {
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
