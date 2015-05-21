package com.zsm.encryptIt.android.action;

import com.zsm.encryptIt.ui.ActivityOperator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class PasswordPromptParameter {

	public static final int TOO_MUCH_TIMES_TO_TRY = Activity.RESULT_FIRST_USER + 1;
	public static final int INITIALIZE_PASSWORD_FAILED = TOO_MUCH_TIMES_TO_TRY+2;
	public static final int LOGIN_FAILED = TOO_MUCH_TIMES_TO_TRY+1;
	
	public static final int PROMPT_PASSWORD = 1;
	public static final int CHANGE_PASSWORD = 2;
	
	private int requestCode;
	private Context appContext;
	private ActivityOperator operator;
	private Intent data;
	
	public PasswordPromptParameter( int requestCode, Context appContext,
									ActivityOperator operator ) {
		
		this.requestCode = requestCode;
		this.appContext = appContext;
		this.operator = operator;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public Context getAppContext() {
		return appContext;
	}

	public ActivityOperator getOperator() {
		return operator;
	}

	public Intent getData() {
		return data;
	}

	public void setData(Intent data) {
		this.data = data;
	}
}
