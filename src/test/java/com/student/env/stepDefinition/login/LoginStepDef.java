package com.student.env.stepDefinition.login;

import com.student.env.appSetting.Setup;
import com.student.env.pageObject.LoginPageObject;
import com.student.env.testDataAccess.ScenarioContext;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import junit.framework.Assert;

import org.apache.log4j.Logger;

import java.lang.invoke.MethodHandles;

import org.apache.log4j.LogManager;

public class LoginStepDef extends Setup {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	LoginPageObject loginPage;

	public void initLoginPage() {
		loginPage = new LoginPageObject(driver);
	}

	@Given("^I open the application$")
	public void i_open_app() throws Throwable {
		initialize();
	}

	@When("^I enter the (.*) and (.*)$")
	public void i_enter_login_credentials(String usrName, String pwd) throws Throwable {
		initLoginPage();
		loginPage.enterLoginCredential(usrName, pwd);
		ScenarioContext.setUsername(usrName);
		ScenarioContext.setPassword(pwd);
	}

	@Then("^I verify login is successful$")
	public void loginIsSuccess() throws Throwable {
		Assert.assertTrue(loginPage.welcomeUser.getText().toString().contains(ScenarioContext.getUsername(),"Different user is logged in as intended ...");
		LogOff();
	}

}
