package com.zsm.encryptIt.ui;

import java.security.GeneralSecurityException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.log.Log;

abstract public class ProtectedActivity extends Activity {

	protected static final int PROMPT_PASSWORD = 1;
	protected static final int CHANGE_PASSWORD = 2;
	
	protected boolean wasInBackground = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setContentVisible( View.VISIBLE );
		EncryptItApplication app = (EncryptItApplication)getApplication();
		wasInBackground = app.wasInBackground;
		app.stopActivityTransitionTimer();
	}

	@Override
	protected void onPause() {
		super.onPause();
		((EncryptItApplication)getApplication()).startActivityTransitionTimer();
		setContentVisible(View.INVISIBLE);
	}

	/**
	 * Hide the content to protect the content as much as possible
	 * 
	 * @param visible whether the content visible
	 */
	private void setContentVisible(int visible) {
		ViewGroup root
			= (ViewGroup)(getWindow().getDecorView()
							.findViewById(android.R.id.content));
		
		root.setVisibility( visible );
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * This method MUST be invoked in onActivityResult of subclass before
	 * handling the result. And if this method return true, the method 
	 * onActivityResult MUST be returned from.
	 * 
	 * @param resultCode same the parameter of onActivityResult
	 * @return true, the result handled and the method onActivityResult 
	 * 			MUST be returned from; false, the subclass MUST continue to 
	 * 			handle the result
	 */
	final protected boolean loginFailed(int resultCode) {
		if( resultCode == LoginActivity.LOGIN_FAILED ) {
			setResult( LoginActivity.LOGIN_FAILED );
			finish();
			return true;
		}
		
		return false;
	}

	protected boolean promptPassword() {
		try {
			PasswordPromptParameter passwordPromptParam
				= new PasswordPromptParameter(
					PROMPT_PASSWORD, getApplicationContext(), this );
			EncryptItApplication.getPasswordHandler()
				.promptPassword( passwordPromptParam );
			
			return true;
		} catch (GeneralSecurityException e) {
			// Any error makes the application quit
			Log.e( e, "Show prompt password activity failed!" );
			setResult( LoginActivity.LOGIN_FAILED );
			finish();
			return false;
		}
	}
}
