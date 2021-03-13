package com.student.env.testHelper;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.testng.util.Strings;

import com.student.env.stepDefinition.baseStep.BackgroundStepDefs;
import com.student.env.utilities.TimeOutUtil;

public class PageObjectsLoad implements ExpectedCondition<Boolean> {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	String js = "return document.readyState";

	@Override
	public Boolean apply(WebDriver driver) {
		Object result;
		JavascriptExecutor jsDriver = (JavascriptExecutor) driver;

		try {
			result = jsDriver.executeScript(js).equals("complete");
		} catch (Exception e) {
			String sFileName = isAlertPresent(driver);
			if (Strings.isNotNullAndNotEmpty(sFileName)) {
				LOGGER.info("SCREENSHOT CAN BE FOUND IN THE ARTIFACT FOLDER WITH PATH -> " + sFileName);
				LOGGER.info("app wait for page load occured due to JS exception as ->" + e.getMessage());

				List<StackTraceElement> stackTraceList = Arrays.asList(e.getStackTrace());
				stackTraceList.forEach(st -> {
					LOGGER.error(st.toString());
				});
				ArrayList<String> studentUiLogsList = new ArrayList<>();
				LocalDateTime ldt = LocalDateTime.now();

				// In an event of exception will pill logs from studentUi app
				LogEntries logEntries = driver.manage().logs().get("browser");
				LOGGER.info("================== Application Console Logs ================== ");
				List<LogEntry> json = logEntries.toJson();
				json.forEach(lgE -> {
					studentUiLogsList.add(lgE.toJson().toString());
					LOGGER.info("App log ->" + lgE.toJson());
				});
				LOGGER.info("=================================== ");

				// Push the log file to target folder
				String timeStamp = ldt.toLocalTime().toString().replaceAll(":", "-");
				timeStamp = timeStamp.substring(0, timeStamp.lastIndexOf("."));
				UUID uuid = UUID.randomUUID();
				String logFileName = "AppLog_" + ldt.toLocalDate().toString() + "-" + timeStamp
						+ uuid.toString().replace("-", "") + ".log";
				try {
					FileUtils.writeLines(Paths.get("target", logFileName).toFile(), studentUiLogsList);
				} catch (IOException e2) {
					LOGGER.info(e.getMessage());
				}

				TimeOutUtil.sleepFor(Duration.ofSeconds(1));
				result = jsDriver.executeScript(js).equals("complete");
			}

		}

		return Boolean.TRUE.equals(result);

	}

	private String isAlertPresent(WebDriver driver) {
		String sFilePath = "";
		try {
			String snapShotFileName = "snapWithPotentialAlert" + "_" + UUID.randomUUID().toString().replace("-", "");
			Alert alert = driver.switchTo().alert();
			LOGGER.info(alert.getText());
			BackgroundStepDefs.takeSnapShot(snapShotFileName);
			alert.accept();
			driver.switchTo().defaultContent();
		} catch (Exception e) {
			LOGGER.info(e.getMessage());
		}

		return sFilePath;

	}

}
