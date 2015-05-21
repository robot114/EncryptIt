package com.zsm.encryptIt.ui;

import java.security.GeneralSecurityException;
import java.security.Key;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.action.KeyAction;
import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.log.Log;

abstract public class SecurityActivity extends ProtectedActivity {

	protected int passwordAllowToTry;

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
				setResult(PasswordPromptParameter.TOO_MUCH_TIMES_TO_TRY);
			} else {
				Log.w( "Invalid password!" );
				UIUtility.promptResult( this, R.string.invalidPassword );
			}
		}
		
		return key;
	}

	protected boolean passwordTriedTooMuch() {
		return passwordAllowToTry <= 0;
	}

}
