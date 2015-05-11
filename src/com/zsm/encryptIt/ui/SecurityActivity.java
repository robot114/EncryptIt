package com.zsm.encryptIt.ui;

import java.security.GeneralSecurityException;
import java.security.Key;

import android.app.AlertDialog;
import android.content.res.Resources;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.action.KeyAction;
import com.zsm.log.Log;

abstract public class SecurityActivity extends ProtectedActivity {

	static final int TOO_MUCH_TIMES_TO_TRY = RESULT_FIRST_USER + 1;
	public static final int LOGIN_FAILED = TOO_MUCH_TIMES_TO_TRY+1;
	static final int INITIALIZE_PASSWORD_FAILED = TOO_MUCH_TIMES_TO_TRY+2;
	
	protected int passwordAllowToTry;

	protected void promptResult(int id) {
		Resources r = getResources();
		
		new AlertDialog.Builder(this)
			 .setTitle(r.getString( R.string.app_name )) 
			 .setMessage(r.getString( id ))
			 .setPositiveButton(android.R.string.ok, null)
			 .show();
	}

	protected Key checkPasswordAndGetKey( char[] password ) {
		Key key = null;
		try {
			key = KeyAction.getInstance().getKey(password);
		} catch (GeneralSecurityException e) {
			Log.e( e, "Error occurred when get key." );
			// Error occurred when get key. Give the user chance to try again
			key = null;
		}
		
		if( key == null ) {
			passwordAllowToTry--;
			if( passwordTriedTooMuch() ) {
				Log.w( "Tried password too much times!" );
				setResult(TOO_MUCH_TIMES_TO_TRY);
			} else {
				Log.w( "Invalid password!" );
				promptResult( R.string.invalidPassword );
			}
		}
		
		return key;
	}

	protected boolean passwordTriedTooMuch() {
		return passwordAllowToTry <= 0;
	}

}
