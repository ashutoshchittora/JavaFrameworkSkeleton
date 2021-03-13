package com.student.env.pageObject;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.*;
import com.google.common.base.Function;
import com.beust.jcommander.Strings;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.student.env.utilities.TimeOutUtil;

import groovy.time.Duration;

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

	public void waitForElementToBeVisible(WebElement elementToCheck) throws Exception {
		int count = 0;
		// wait for max 60 seconds
		try {
			do {
				if (elementToCheck.isDisplayed() && elementToCheck.isEnabled()) {
					break;
				} else {
					count++;
				}
			} while (count < 20);

		} catch (Exception e) {
			WebDriverWait wait = new WebDriverWait(driver, 60);
			java.time.Duration duration = java.time.Duration.ofSeconds(2);
			wait.pollingEvery(duration);
			wait
					.ignoreAll(Arrays.asList(StaleElementReferenceException.class, ElementNotVisibleException.class));
			WebElement elementLookUp = waitForElement.until(ExpectedConditions.visibilityOf(elementToCheck));
			if (elementLookUp == null) {
				throw new Exception("Element not found ->" + e.getMessage());
			}
		}

	}

	public void waitForElementToBeClickable(WebElement elementToCheck, int iTimeOut) throws Exception {
		int count = 0;
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);

		try {
			do {
				TimeOutUtil.sleepFor(1000);
				if (elementToCheck.isDisplayed() && elementToCheck.isEnabled()) {
					break;
				} else {
					count++;
				}
			} while (count < iTimeOut);

		} catch (Exception e) {

			if (iTimeOut < 3) {
				iTimeOut = 3;
			}

			WebDriverWait wait = new WebDriverWait(driver, iTimeOut);
			java.time.Duration duration = java.time.Duration.ofMillis(500);
			wait.pollingEvery(duration);
			WebElement elementLookUp = wait.until(ExpectedConditions.elementToBeClickable(elementToCheck));
			if (elementLookUp == null) {
				throw new Exception("Element not found ->" + e.getMessage());
			}
		}
		driver.manage().timeouts().implicitlyWait(TimeOutUtil.getUiTimeOut(), TimeUnit.MILLISECONDS);

	}

	public boolean isElementExists(WebElement elementToCheck, int timeOut, String label) {
		WebElement elementLookUp = null;
		long currentImplicitWait = TimeOutUtil.getUiTimeOut();
		driver.manage().timeouts().implicitlyWait(2000, TimeUnit.MILLISECONDS);
		WebDriverWait wait = new WebDriverWait(driver, timeOut);
		java.time.Duration duration = java.time.Duration.ofMillis(timeOut / 2);
		try {
			wait.pollingEvery(duration);
			wait.withTimeout(java.time.Duration.ofMillis(currentImplicitWait));
			wait.ignoreAll(Arrays.asList(NoSuchElementException.class, ElementNotFoundException.class,
					StaleElementReferenceException.class));
			elementLookUp = wait.until(ExpectedConditions.visibilityOf(elementToCheck));
		} catch (Exception e) {
			LOGGER.info("Element not found ->" + e.getMessage());
		}
		driver.manage().timeouts().implicitlyWait(currentImplicitWait, TimeUnit.MILLISECONDS);
		return elementLookUp == null ? false : true;
	}

	public boolean isElementExists(WebElement elementToCheck, int timeOut) {
		return isElementExists(elementToCheck, timeOut, "");
	}

	public boolean isElementExists(String elementLocator, int timeOut) {
		WebElement elementLookUp = null;
		try {
			driver.manage().timeouts().implicitlyWait(timeOut, TimeUnit.MILLISECONDS);
			elementLookUp = driver.findElement(By.xpath(elementLocator));
		} catch (Exception e) {
			LOGGER.info("Element not found ->" + e.getMessage());
		}

		driver.manage().timeouts().implicitlyWait(TimeOutUtil.getUiTimeOut(), TimeUnit.MILLISECONDS);
		return elementLookUp == null ? false : true;
	}

	public boolean isElementVisible(WebElement elementToCheck, int timeOut) {
		WebElement elementLookUp = null;
		long currentTimeOut = TimeOutUtil.getUiTimeOut();
		driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS);
		try {
			WebDriverWait wait = new WebDriverWait(driver, timeOut);
			elementLookUp = wait.pollingEvery(java.time.Duration.ofMillis(200))
					.until(ExpectedConditions.refreshed(ExpectedConditions.visibilityOf(elementToCheck)));
		} catch (Exception e) {
			LOGGER.info("Element not found ->" + e.getMessage());
		}
		driver.manage().timeouts().implicitlyWait(currentTimeOut, TimeUnit.MILLISECONDS);
		return elementLookUp == null ? false : true;
	}

	public boolean isElementEnabled(WebElement elementToCheck, int timeOut) {
		WebElement elementLookUp = null;
		long currentTimeOut = TimeOutUtil.getUiTimeOut();
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
		WebDriverWait wait = new WebDriverWait(driver, timeOut);
		java.time.Duration duration = java.time.Duration.ofMillis(timeOut / 2);
		try {

			wait.pollingEvery(duration);
			wait.ignoreAll(Arrays.asList(NoSuchElementException.class, StaleElementReferenceException.class,
					ElementNotFoundException.class));
			elementLookUp = wait.until(ExpectedConditions.elementToBeClickable(elementToCheck));
		} catch (Exception e) {
			LOGGER.info("Element not found ->" + e.getMessage());
		}
		driver.manage().timeouts().implicitlyWait(currentTimeOut, TimeUnit.MILLISECONDS);
		return elementLookUp == null ? false : true;
	}

	public boolean isElementExists(By byLocator, int timeOut) {
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
		WebDriverWait wait = new WebDriverWait(driver, timeOut);
		WebElement elementLookUp = wait.until(ExpectedConditions.presenceOfElementLocated(byLocator));
		driver.manage().timeouts().implicitlyWait(TimeOutUtil.getUiTimeOut(), TimeUnit.MILLISECONDS);
		return elementLookUp == null ? false : true;
	}

	public Wait<WebDriver> createWebDriverWait(WebElement wbE, int timeOut, By byLocator) {
		ArrayList<Class<? extends Exception>> possibleExceptions = new ArrayList();
		possibleExceptions.add(NoSuchElementException.class);
		possibleExceptions.add(StaleElementReferenceException.class);
		java.time.Duration duration = java.time.Duration.ofMillis(timeOut / 2);
		Wait<WebDriver> waitForElement = new FluentWait<WebDriver>(driver).ignoreAll(possibleExceptions)
				.pollingEvery(duration).withMessage("timedOut.NoElementFound" + (wbE == null ? byLocator : wbE));

		return waitForElement;
	}

	public boolean waitForElement(WebElement wbE, int timeOut) {
		Wait<WebDriver> createWebDriverWait = createWebDriverWait(wbE, timeOut, null);
		Function<WebDriver, WebElement> isElementFound = (driver) -> {
			WebElement w = driver.findElement(By.tagName(wbE.getTagName()));
			return w;
		};

		WebElement founfWebElement = createWebDriverWait.until(isElementFound);
		return founfWebElement != null ? true : false;
	}

	public boolean waitForElement(By byLocator, int timeOut) {
		Wait<WebDriver> createWebDriverWait = createWebDriverWait(null, timeOut, byLocator);
		Function<WebDriver, WebElement> isElementFound = (driver) -> {
			WebElement w = driver.findElement(byLocator);
			return w;
		};

		WebElement founfWebElement = createWebDriverWait.until(isElementFound);
		return founfWebElement != null ? true : false;

	}

	public void typeKeySlowly(WebElement wbE, String textToType) throws Exception {
		waitForPageToLoad();
		wbE.click();
		waitForPageToLoad();
		wbE.clear();
		TimeOutUtil.sleepFor(300);
		for (int i = 0; i < textToType.length(); i++) {
			TimeOutUtil.sleepFor(500, true);
			if (Character.isWhitespace(textToType.charAt(i))) {
				wbE.sendKeys(" ");
			} else {
				wbE.sendKeys(String.valueOf(textToType.charAt(i)));
			}

		}

	}

	public void enterText(WebElement wbE, String textToType) throws Exception {
		if (Strings.isStringEmpty(textToType))
			return;

		((JavascriptExecutor) driver).executeScript("argument[0].scrollIntoView(true);", wbE);
		waitForPageToLoad();
		wbE.clear();
		waitForPageToLoad();
		wbE.sendKeys(textToType);
		waitForPageToLoad();
		wbE.sendKeys(Keys.TAB);
		waitForPageToLoad();
	}

	public void scrollAndClick(WebElement wbE) throws Exception {
		((JavascriptExecutor) driver).executeScript("argument[0].scrollIntoView(true);", wbE);
		waitForPageToLoad();
		wbE.click();
		waitForPageToLoad();
	}
	
	public void scrollToView(WebElement wbE) throws Exception {
		((JavascriptExecutor) driver).executeScript("argument[0].scrollIntoView(true);", wbE);
		waitForPageToLoad();
		
	}
	
	public void clickElement(String elementLocator){
		List<WebElement> lsElement = driver.findElements(By.xpath(elementLocator));
		driver.manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
		for(WebElement w : lsElement){
			try {
				if(w.isDisplayed()){
					w.click();
				}
			} catch (Exception e) {
				LOGGER.info("Element not found ->" + e.getMessage());
			}
		}
		driver.manage().timeouts().implicitlyWait(TimeOutUtil.getUiTimeOut(), TimeUnit.MILLISECONDS);
	}
	

}
