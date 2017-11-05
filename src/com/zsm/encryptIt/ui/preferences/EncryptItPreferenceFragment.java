package com.zsm.encryptIt.ui.preferences;


import java.security.GeneralSecurityException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
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

	private static final String KEY_EXPORT_ENABLE = "KEY_EXPORT_ENABLE";

	private static final int REQUEST_CODE_PASSWORD_EXPORT = 101;
	
	private CheckBoxPreference mPrefBackupTypes[]
				= new CheckBoxPreference[Preferences.KEY_BACKUP_FILE_TYPES.length];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.std_preferences);
		initEnableExport();
		initBackupType();
		initChangePassword();
	}

	private void initBackupType() {
		int count = mPrefBackupTypes.length;
		BackupTypeChangeListener l = new BackupTypeChangeListener();
		for( int i = 0; i < count; i++ ) {
			mPrefBackupTypes[i]
				= (CheckBoxPreference) findPreference( 
											Preferences.KEY_BACKUP_FILE_TYPES[i] );
			
			mPrefBackupTypes[i].setOnPreferenceChangeListener( l );
		}
		
		String key = Preferences.getInstance().getBackupFilesType();
		updateBackupType(key);
	}

	private void updateBackupType(String key) {
		for( CheckBoxPreference p : mPrefBackupTypes ) {
			p.setChecked( p.getKey().equals(key) );
		}
	}

	private void initChangePassword() {
		findPreference( "CHANGE_PASSWORD" )
			.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					try {
						PasswordPromptParameter passwordPromptParam
							= new PasswordPromptParameter(
									PasswordPromptParameter.REQUEST_CODE_CHANGE_PASSWORD,
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

	private void initEnableExport() {
		findPreference( KEY_EXPORT_ENABLE )
			.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference preference,
												  Object newValue) {
					boolean value = (boolean)newValue;
					if( !value ) {
						Preferences.getInstance().setExportEnable( false );
						return true;
					}
					try {
						PasswordPromptParameter passwordPromptParam
							= new PasswordPromptParameter(
									REQUEST_CODE_PASSWORD_EXPORT,
									getActivity().getApplicationContext(),
									EncryptItPreferenceFragment.this );
						EncryptItApplication.getPasswordHandler()
							.promptPassword( passwordPromptParam );
						
					} catch (GeneralSecurityException e) {
						// Any error makes the application quit
						Log.e( e, "Show prompt password activity for enable "
								  + "export failed!" );
						getActivity().finish();
					}
					return false;
				}
			} );
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch( requestCode ) {
			case PasswordPromptParameter.REQUEST_CODE_CHANGE_PASSWORD:
				doChangePassword(resultCode, data);
				break;
			case REQUEST_CODE_PASSWORD_EXPORT:
				doEnableExport( resultCode );
			default:
				break;
		}
	}
	
	private void doEnableExport(int resultCode) {
		Preferences.getInstance().setExportEnable( false );
		switch ( resultCode ) {
			case Activity.RESULT_OK:
				CheckBoxPreference p
					= (CheckBoxPreference) findPreference( KEY_EXPORT_ENABLE );
				p.setChecked( true );
				Preferences.getInstance().setExportEnable( true );
				break;
			case PasswordPromptParameter.TOO_MUCH_TIMES_TO_TRY:
				finishAffinity();
				break;
			case Activity.RESULT_CANCELED:
			default:
				break;
		}
	}

	public void doChangePassword(int resultCode, Intent intent) {
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

	private final class BackupTypeChangeListener
							implements OnPreferenceChangeListener {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean value = (boolean)newValue;
			if( !value ) {
				// Selected a checked type, keep it selected
				return false;
			}
			String key = preference.getKey();
			updateBackupType(key);
			Preferences.getInstance().setBackupFileType( key );
			return true;
		}
	}

}
