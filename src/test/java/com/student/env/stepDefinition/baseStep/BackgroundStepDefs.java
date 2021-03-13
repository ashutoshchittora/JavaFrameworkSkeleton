package com.student.env.stepDefinition.baseStep;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.student.env.appSetting.ConfigReader;
import com.student.env.appSetting.Setup;
import com.student.env.testDataAccess.ScenarioContext;
import com.student.env.utilities.TestExecutionTime;

import cucumber.api.java.After;
import cucumber.api.java.Before;

public class BackgroundStepDefs extends Setup {

	private static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	ScenarioContext scenarioContext;

	public BackgroundStepDefs(ScenarioContext sc) {
		scenarioContext = sc;
	}

	static {
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "warn");
	}

	@Before
	public void StartOfEachScenario(cucumber.api.Scenario scenario) throws Throwable {
		TestExecutionTime.startLogger();
		initializeLogger();
		String tempFeatureTitle = "";

		scenarioContext.testcaseDescription = scenario.getId().trim().split(";")[0];
		tempFeatureTitle = testcaseDescription.replace("-", "");

		scenarioContext.scenarioName = scenario.getName();
		scenarioContext.scenarioTags = scenario.getSourceTagNames();
		scenarioContext.testcaseId = tempFeatureTitle.replaceAll("\\D+", "");

		String sMessage = String.format("Stating testcase => %s", scenarioContext.testcaseDescription);
		LOGGER.info("------------------------------------------");
		LOGGER.info(sMessage);
		LOGGER.info("------------------------------------------");
		LOGGER.info("Scenario name is ... " + scenario.getName());
		LOGGER.info("Tage name is ..." + scenario.getSourceTagNames());

		// Feature tilte to Camescase change as below
		String featureTitle = tempFeatureTitle;
		if (tempFeatureTitle.length() > 0) {
			featureTitle = tempFeatureTitle.substring(0, 1).toUpperCase() + tempFeatureTitle.substring(1);
		}
		scenarioContext.setFeatureTitle(featureTitle);

		// Record the start of this execution
		LogScenarioExecutionStarting(scenario);

		// TimeOutUtil.configureTimeOutSettings(ScenarioContext.getScenarioName())
		// for scenario specifc timeout...

	}

	@After
	public void EndOfEachScenario(cucumber.api.Scenario scenario) throws Throwable {
		String sMessage = String.format("%s execution over and test has status = %s",
				scenarioContext.testcaseDescription, scenario.getStatus());
		TestExecutionTime.logTime(scenario.getName().trim().split(";")[0].replace("-", ""));
		LOGGER.info("*****************************************");
		// kill all Chrome instances
		if (scenario.getStatus().equalsIgnoreCase("failed")) {
			String snapshotFilename = scenarioContext.testcaseDescription.replace(' ', '_').replace('<', ' ')
					.replace('>', ' ').replace('-', ' ') + "_" + UUID.randomUUID().toString().replace("-", "");
			takeSnapShot(snapshotFilename);
			LOGGER.info("snapshot can be found at target folder ->" + snapshotFilename + ".png");

		}
		LOGGER.info("*****************************************");
		LogScenarioExecutionEnding(scenario);
		teardown();
		scenarioContext = new ScenarioContext();

	}

	public void initializeLogger() {
		String log4jConfPath = ConfigReader.getConfigPropertyFile("log4j.properties");
		PropertyConfigurator.configure(log4jConfPath);
	}

	public void killChromeInstance() {
		try {
			String cmd = "powershell.exe Get-Process -name chrome* | Stop-Process -Force -ErrorAction SilentlyContinue",
					msg = null, errorMsg = null;
			Process pr = Runtime.getRuntime().exec(cmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
			pr.waitFor();
			while ((msg = input.readLine()) != null) {
				LOGGER.info(msg);
			}
			while ((errorMsg = error.readLine()) != null) {
				LOGGER.info(errorMsg);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void LogScenarioExecutionStarting(cucumber.api.Scenario scenario) {
		String scenarioName = scenario.getName();
		String scenarioVstsId = ParseVstsId(scenarioName);
		String scenarioVstsChildNum = null;

		if (scenarioVstsId != null && scenarioVstsId.contains("_")) {
			String[] splitString = scenarioVstsId.split("_");
			scenarioVstsId = splitString[0];
			scenarioVstsChildNum = splitString[1];

			if (scenarioVstsChildNum != null && scenarioVstsChildNum.length() == 0) {
				scenarioVstsChildNum = null;
			}
		}

		String tagAsCsv = String.join(",", ScenarioContext.scenarioTags);
		String teamCityBuildId = System.getenv("build_number");

		if (teamCityBuildId == null || teamCityBuildId.length() == 0) {
			teamCityBuildId = System.getenv("USERNAME") + "_" + GetJvmStartTime();
		}
		String testPackName = System.getenv("TEAMCITY_BUILDCONF_NAME");
		if (testPackName == null || testPackName.length() == 0) {
			testPackName = "Standalone Run";
		}

	}

	private String SafeQuotedStringOrNull(String value) {
		if (value == null) {
			return "NULL";
		} else {
			return "'" + value.replace("'", "''") + "'";
		}
	}

	private String GetJvmStartTime() {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		if (bean != null) {
			long startTime = bean.getStartTime();
			Date startDate = new Date(startTime);
			return DATE_FORMAT.format(startTime);
		}

		return "";
	}

	private String ParseVstsId(String scenarioOutline) {
		String output = null;
		Pattern pattern = Pattern.compile(".*TC[-_](\\d{6})(_\\d)*.*");
		Matcher matcher = pattern.matcher(scenarioOutline);

		if (matcher.find()) {
			output = matcher.group(1);
			if (matcher.groupCount() >= 2) {
				String childNum = matcher.group(2);
				if (childNum != null && childNum.length() > 1) {
					output += childNum;
				}
			}
		}
		return output;
	}

	private void LogScenarioExecutionEnding(cucumber.api.Scenario scenario) {
		if (!ScenarioContext.getscenarioName().isEmpty()) {
			LOGGER.info("update scenario details in DB with a flag");
		} else {
			LOGGER.debug("skipping update of ScenarioExecution as it is 0");
		}
	}

}
