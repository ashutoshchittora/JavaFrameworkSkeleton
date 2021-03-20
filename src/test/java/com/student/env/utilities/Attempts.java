package com.student.env.utilities;

import java.time.Duration;
import java.util.function.Function;

public class Attempts<T> {

	private static int numOfAttempts;
	private String failWithMessage;
	private T params;
	private Duration waitBetweenAttempts = Duration.ofSeconds(0);
	
	
	public Attempts numOfAttempts(int numOfAttempts){
		Attempts.numOfAttempts=numOfAttempts;		
		return this;
	}
	
	public Attempts failWithMessage(String failWithMessage){
		this.failWithMessage=failWithMessage;		
		return this;
	}
	
	public Attempts withFunctionParameters(T params){
		this.params=params;		
		return this;
	}
	
	public Attempts withWaitBetweenAttempts(Duration waitBetweenAttempts){
		this.waitBetweenAttempts=waitBetweenAttempts;		
		return this;
	}
	
	public <V> void start(Function<? super T,V> targetFunction){
		V response = null;
		
		while (true) {
			try {
				if (numOfAttempts-- > 0) {
					response = targetFunction.apply(params);
					if(Boolean.TRUE.equals(response))
					break;
				} else {
					break;
				}
				Thread.sleep(waitBetweenAttempts.toMillis());
			} catch (Exception e) {
				Thread.interrupted();
			}
		}
		if(response instanceof Throwable){
			throw new AssertionError(failWithMessage, (Throwable)response);
		}
	}
}
