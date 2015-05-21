package com.zsm.encryptIt.ui.preferences;


import java.security.GeneralSecurityException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.action.KeyAction;
import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.ui.ActivityOperator;
import com.zsm.encryptIt.ui.UIUtility;
import com.zsm.log.Log;
import com.zsm.security.PasswordHandler;

public class EncryptItPreferenceFragment extends PreferenceFragment
				implements ActivityOperator {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.std_preferences);
		Preference p = findPreference( "CHANGE_PASSWORD" );
		p.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				try {
					PasswordPromptParameter passwordPromptParam
						= new PasswordPromptParameter(
								PasswordPromptParameter.CHANGE_PASSWORD,
								getActivity().getApplicationContext(),
								EncryptItPreferenceFragment.this );
					EncryptItApplication.getPasswordHandler()
						.promptChangePassword( passwordPromptParam );
					
					return true;
				} catch (GeneralSecurityException e) {
					// Any error makes the application quit
					Log.e( e, "Show prompt password activity failed!" );
					getActivity().finish();
					return false;
				}
			}
		} );
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch( requestCode ) {
			case PasswordPromptParameter.CHANGE_PASSWORD:
				doChangePassword(resultCode, data);
				break;
			default:
				break;
		}
	}
	
	public void doChangePassword(int resultCode, Intent intent) {
		Log.d( resultCode );
		switch ( resultCode ) {
			case Activity.RESULT_OK:
				char[] oldPassword
					= intent.getCharArrayExtra(PasswordHandler.KEY_OLD_PASSWORD);
				char[] newPassword
					= intent.getCharArrayExtra(PasswordHandler.KEY_NEW_PASSWORD);
				int id = R.string.changePasswordFailed;
				if( KeyAction.getInstance()
						.changePassword(oldPassword, newPassword) ) {
					
					id = R.string.changePasswordOk;
				}
				UIUtility.promptResult( getActivity(), id );
				break;
			case PasswordPromptParameter.TOO_MUCH_TIMES_TO_TRY:
				finishAffinity();
				break;
			case Activity.RESULT_CANCELED:
			default:
				break;
		}
	}

	@Override
	public void finishAffinity() {
		getActivity().finishAffinity();
	}
}
