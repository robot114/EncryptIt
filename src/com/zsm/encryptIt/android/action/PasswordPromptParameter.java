package com.zsm.encryptIt.android.action;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class PasswordPromptParameter {

	private int requestCode;
	private Context appContext;
	private Activity parent;
	private Intent data;
	
	public PasswordPromptParameter( int requestCode, Context appContext,
									Activity parent ) {
		
		this.requestCode = requestCode;
		this.appContext = appContext;
		this.parent = parent;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public Context getAppContext() {
		return appContext;
	}

	public Activity getParent() {
		return parent;
	}

	public Intent getData() {
		return data;
	}

	public void setData(Intent data) {
		this.data = data;
	}
}
