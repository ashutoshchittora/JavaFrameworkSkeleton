package com.student.env.testRunner;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;


@CucumberOptions(
		features = {"src/test/resources/com/student/env/featureFiles/login/Login.feature"},
		glue = {"com.student.env.stepDefinitions","com.student.env.other.stepDefinitions"},
		plugin = {"json:target/cucumber-report.json"}
		)
public class RunCucumber extends AbstractTestNGCucumberTests {

}
