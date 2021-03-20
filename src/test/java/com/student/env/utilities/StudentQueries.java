package com.student.env.utilities;

import java.lang.invoke.MethodHandles;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.student.env.appSetting.TestBase;

public class StudentQueries {
	
	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	static TestBase testBase;	
	private static StringBuilder studentQuery = null;
	
	public static String getStidentName(String studentName){
		return studentName;
	}
	

}
