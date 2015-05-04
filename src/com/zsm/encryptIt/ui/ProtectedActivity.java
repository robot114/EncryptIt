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
	
	private boolean shouldResume;
	
	/**
	 * Method must be implemented by subclass to indicate whether the login
	 * activity needed, when the application comes back from the background.
	 * For now, for all the activities, this method should return true, 
	 * except the LoginActivity and the PasswordActivity in init mode.
	 * 
	 * @return true, the login activity needed, when the application comes back
	 * 			 from the background.
	 */
	abstract protected boolean needPromptPassword();
	
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
		
		Log.d( "For resuming.", "activity", this, "wasInBackground",
			   wasInBackground, "needPromptPassword", needPromptPassword() );
		shouldResume = true;
		if( wasInBackground && needPromptPassword() ) {
			shouldResume = promptPassword( );
		}
	}

	protected boolean shouldResume() {
		return shouldResume;
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
		if( doLoginFailed( resultCode ) ) {
			Log.w( "Login failed!" );
			return;
		}
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
	final protected boolean doLoginFailed(int resultCode) {
		if( checkLoginFailed(resultCode) ) {
			setResult( LoginActivity.LOGIN_FAILED );
			finish();
			return true;
		}
		
		return false;
	}

	protected boolean checkLoginFailed(int resultCode) {
		return resultCode == LoginActivity.LOGIN_FAILED;
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

	protected int getStatusBarHeight() { 
	      int result = 0;
	      int resourceId
	      	= getResources().getIdentifier(
	      			"status_bar_height", "dimen", "android");
	      
	      if (resourceId > 0) {
	          result = getResources().getDimensionPixelSize(resourceId);
	      } 
	      return result;
	}
}
