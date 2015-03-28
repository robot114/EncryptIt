package com.zsm.security;

import java.util.HashMap;

public class PasswordPolicy {
	
	private HashMap<Result, Object> resultToResource
		= new HashMap<Result, Object>();
	
	private Object unknownResult;
	
	public interface Result{
	}
	
	public enum GoodResult implements Result {
		GOOD
	}

	/**
	 * Check if the password satisfies the policy. The default is no check.
	 * 
	 * @param password the password in plain text
	 * @return checking result. When the check is OK, {@link GoodResult.GOOD}
	 * 			will be returned.
	 */
	public Result check( char[] password ) {
		return GoodResult.GOOD;
	}
	
	public void putResultString( Result r, Object obj) {
		resultToResource.put(r, obj);
	}
	
	public void setUnknownResult( Object unknownResultObj ) {
		this.unknownResult = unknownResultObj;
	}
	
	public Object getResult( Result r ) {
		Object obj = resultToResource.get(r);
		
		return (obj == null) ? unknownResult : obj;
	}
}
