package com.student.env.utilities;

import java.lang.invoke.MethodHandles;
import java.time.Duration;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TimeOutUtil {

	private static final Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	private static final String ThisClassName = "com.student.env.utilities.TimeOutUtil";
	
	private static long  uiTimeOut = 120000;
	private static long  apiTimeOut = 120000;
	private static long  dbTimeOut = 120000;
	public static long  getUiTimeOut(){
		return uiTimeOut;
	}
	
	public static void sleepFor(long timeOut){
		Duration duration = Duration.ofMillis(timeOut);
		sleepFor(duration,false);
	}
	
	public static void sleepFor(long timeOut, boolean bSilentSleep){
		Duration duration = Duration.ofMillis(timeOut);
		sleepFor(duration,false);
	}
	
	public static void sleepFor(Duration duration){
		sleepFor(duration,false);
	}
	
	public static void sleepFor(Duration duration , boolean bSilentSleep) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		int callingMethodStackDetails = 2; 
		if(stackTrace[callingMethodStackDetails].getClassName().equals(ThisClassName)){
			callingMethodStackDetails=3;
		}
		String className = stackTrace[callingMethodStackDetails].getClassName();
		String methodName = stackTrace[callingMethodStackDetails].getMethodName();
		String sleepTimeInfo;
		
		if(duration.getSeconds() > 0){
			sleepTimeInfo = duration.getSeconds() + "seconds ...";			
		} else {
			sleepTimeInfo = duration.toMillis() + "mSeonds ..."	;
		}
		if(!bSilentSleep){
			LOGGER.info("called by"+className+"-"+methodName+"- sleeping for "+sleepTimeInfo);
		}
		
		try {
			Thread.sleep(duration.toMillis());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		
		
		
		
	}
}
