package com.student.env.testDataAccess;

import java.lang.invoke.MethodHandles;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.student.env.utilities.DatabaseConnection;

public class ScenarioContext {
	
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	public static String studentName= "";
	public static String sessionId= "";
	public static String testCaseId="";
	public static String testCaseDescription="";
	public static String scenarioName="";
	public static DatabaseConnection dbConn;
	public static String username="";
	public static String password = ""; 
	public static Integer studentId=0;
	public static List<String> userList = null;
	public static Collection<String> scenarioTags=null;
	public static LocalTime startTimestamp;
	public String featureTitle ="";
	
	
	public static String getStudentName() {
		return studentName;
	}
	public static void setStudentName(String studentName) {
		ScenarioContext.studentName = studentName;
	}
	public static String getSessionId() {
		return sessionId;
	}
	public static void setSessionId(String sessionId) {
		ScenarioContext.sessionId = sessionId;
	}
	public static String getTestCaseId() {
		return testCaseId;
	}
	public static void setTestCaseId(String testCaseId) {
		ScenarioContext.testCaseId = testCaseId;
	}
	public static String getTestCaseDescription() {
		return testCaseDescription;
	}
	public static void setTestCaseDescription(String testCaseDescription) {
		ScenarioContext.testCaseDescription = testCaseDescription;
	}
	public static String getScenarioName() {
		return scenarioName;
	}
	public static void setScenarioName(String scenarioName) {
		ScenarioContext.scenarioName = scenarioName;
	}
	public static DatabaseConnection getDbConn() {
		return dbConn;
	}
	public static void setDbConn(DatabaseConnection dbConn) {
		ScenarioContext.dbConn = dbConn;
	}
	public static String getUsername() {
		return username;
	}
	public static void setUsername(String username) {
		ScenarioContext.username = username;
	}
	public static String getPassword() {
		return password;
	}
	public static void setPassword(String password) {
		ScenarioContext.password = password;
	}
	public static Integer getStudentId() {
		return studentId;
	}
	public static void setStudentId(Integer studentId) {
		ScenarioContext.studentId = studentId;
	}
	public static List<String> getUserList() {
		return userList;
	}
	public static void setUserList(List<String> userList) {
		ScenarioContext.userList = userList;
	}
	public static Collection<String> getScenarioTags() {
		return scenarioTags;
	}
	public static void setScenarioTags(Collection<String> scenarioTags) {
		ScenarioContext.scenarioTags = scenarioTags;
	}
	public static LocalTime getStartTimestamp() {
		return startTimestamp;
	}
	public static void setStartTimestamp(LocalTime startTimestamp) {
		ScenarioContext.startTimestamp = startTimestamp;
	}
	public static Logger getLogger() {
		return LOGGER;
	}
	public String getFeatureTitle() {
		return featureTitle;
	}
	public void setFeatureTitle(String featureTitle) {
		this.featureTitle = featureTitle;
	}
	
	//ScenarioContext utility methods
	
	private void resetScenarioContext(){
		studentId=0;
		studentName="";
	}
	
	
	
}
