package com.student.env.utilities;

import java.lang.invoke.MethodHandles;
import java.time.Duration;

import static com.jayway.restassured.RestAssured.*;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.testng.util.Strings;

import com.student.env.appSetting.ConfigReader;

import cucumber.api.java.en.Given;

public class InterfaceResponse {
	
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	
	public static JSONObject getStudentApiDetails (String studentId){
		String apiUriPath = String.format(ConfigReader.getStudentUri()+ "get-api-service/vi/studentId/%s", studentId);
		String response = getData(apiUriPath);
		return new JSONObject(response);
	}
	
	private static String getData(String apiUriPath){
		String response;
		LOGGER.info("About to call "+ apiUriPath + " to retrieve data");
		// checking for Student details at the interval of 15 sec for 1 min
		int iAttempt =0;
		int iMaxAttempt = 12;
		do{
			response = given().contentType("application/json").when().get(apiUriPath).then().extract().body().asString();
			iAttempt++;
			if(!Strings.isNullOrEmpty(response) && !response.contains("error")){
				break;
			}
			TimeOutUtil.sleepFor(Duration.ofSeconds(15));
			
		}while(iAttempt<iMaxAttempt)
			
		if(Strings.isNullOrEmpty(response) ){
			String failureReason = "Details not available in student server =>" +apiUriPath;
			assertTrue(false,failureReason);
		}
		return response;
	}
}
