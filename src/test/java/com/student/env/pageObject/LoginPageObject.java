package com.student.env.pageObject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPageObject extends BasePageObject {
	
	public LoginPageObject(WebDriver driver){
		super(driver);
		PageFactory.initElements(driver, this);
	}
	
	@FindBy(xpath = "//*[@id='loginPanel']/form/div[1]/input")
	public WebElement UserName;
	
	@FindBy(xpath = "//*[@id='loginPanel']/form/div[2]/input")
	public WebElement Password;
	
	@FindBy(xpath = "//*[@id='loginPanel']/form/div[3]/input")
	public WebElement logIn;
	
	@FindBy(xpath = "//*[@id='leftPanel']/ul/li[8]/a")
	public WebElement logOut;
	
	@FindBy(xpath = "//*[@id='loginPanel']/p[2]/a")
	public WebElement register;
	
	@FindAll({
		@FindBy(xpath = "//*[@id='loginPanel']/p[2]/a"),
		@FindBy(xpath = "//*[@id='loginPanel']/p[2]/a")
	})
	public WebElement registerOption;
	
	
	@FindBy(xpath = "//*[@id='leftPanel']/p")
	public WebElement welcomeUser;
	
	
	public void loginBtnClick(){
		logIn.click();
	}
	
	public void logOutBtnClick(){
		logOut.click();
	}
	
	public void enterLoginCredential(String username, String pwd){
		UserName.sendKeys(username);
		Password.sendKeys(pwd);
		loginBtnClick();
	}
	
}
