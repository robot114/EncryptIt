package com.zsm.security;

public class LengthPasswordPolicy extends PasswordPolicy {

	public enum LengthResult implements Result{
		TOO_SHORT, TOO_LONG
	}
	
	private int min;
	private int max;
	
	public LengthPasswordPolicy( int minLength, int maxLength ) {
		min = minLength;
		max = maxLength;
	}
	
	public LengthPasswordPolicy( int minLength ) {
		min = minLength;
		max = Integer.MAX_VALUE;
	}
	
	@Override
	public Result check(char[] password) {
		if( password.length < min ) {
			return LengthResult.TOO_SHORT;
		} else if ( password.length > max ) {
			return LengthResult.TOO_LONG;
		}
		return GoodResult.GOOD;
	}

}
