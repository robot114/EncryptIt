package com.zsm.encryptIt.ui.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.preference.PreferenceManager;

public class Preferences {

	private static final String KEY_BACKUP_SECURITY_URI = "KEY_BACKUP_SECURITY_URI";
	private static final String KEY_LAST_BACKUP_PATH = "KEY_LAST_BACKUP_PATH";
	private static final String KEY_LOCK_APP_TIME = "LOCK_APP_TIME";
	private static final String KEY_MAIN_LIST_EXPANDABLE = "MAIN_LIST_EXPANDABLE";
	private static final String KEY_MAX_KEYSTORE_TEMP_FILE_NUM
									= "KEY_MAX_KEYSTORE_TEMP_FILE_NUM";
	private static final String KEY_LAST_KEYSTORE_TEMP_FILE_NUM
									= "KEY_LAST_KEYSTORE_TEMP_FILE_NUM";
	
	private static final String DEFAULT_LOCK_APP_TIME = "5";
	
	static private Preferences instance;
	
	final private SharedPreferences preferences;
	
	private StackTraceElement[] stackTrace;
	
	private Preferences( Context context ) {
		preferences
			= PreferenceManager
				.getDefaultSharedPreferences( context );
		
	}
	
	static public void init( Context c ) {
		if( instance != null ) {
			throw new IllegalStateException( "Preference has been initialized! "
											 + "Call getInitStackTrace() to get "
											 + "the initlization place." );
		}
		instance = new Preferences( c );
		instance.stackTrace = Thread.currentThread().getStackTrace();
	}
	
	static public Preferences getInstance() {
		return instance;
	}
	
	public StackTraceElement[] getInitStackTrace() {
		return stackTrace;
	}
	
	public void registerOnSharedPreferenceChangeListener(
					OnSharedPreferenceChangeListener listener) {
		
		preferences.registerOnSharedPreferenceChangeListener(listener);
	}
	
	public int getLockAppTimeInMs() {
		return Integer.parseInt( 
					preferences.getString( KEY_LOCK_APP_TIME,
										   DEFAULT_LOCK_APP_TIME ) )*1000;
	}

	public boolean getMainListExpandable() {
		// TODO: add ui
		return preferences.getBoolean( KEY_MAIN_LIST_EXPANDABLE, true );
	}

	public boolean exportAsXml() {
		return preferences.getBoolean( "KEY_EXPORT_XML", false);
	}

	public String getLastBackupPath() {
		return preferences.getString( KEY_LAST_BACKUP_PATH, null);
	}

	public void setLastBackupPath(String filePath) {
		preferences.edit().putString(KEY_LAST_BACKUP_PATH, filePath).commit();
	}

	public Uri getBackupSecurityUri() {
		String uriStr = preferences.getString( KEY_BACKUP_SECURITY_URI, null );
		if( uriStr != null ) {
			return Uri.parse(uriStr);
		}
		return null;
	}

	public void setSecurityBackupUri(Uri uri) {
		preferences
			.edit().putString(KEY_BACKUP_SECURITY_URI, uri.toString() )
			.commit();
	}

	public int getLastTempKeyStoreFileNumber() {
		return preferences.getInt( KEY_LAST_KEYSTORE_TEMP_FILE_NUM, -1);
	}

	public void setLastTempKeyStoreFileNumber( int num ) {
		preferences.edit().putInt( KEY_LAST_KEYSTORE_TEMP_FILE_NUM, num).commit();
	}

	public int getTempKeyStoreFileMaxNumber() {
		return preferences.getInt( KEY_MAX_KEYSTORE_TEMP_FILE_NUM, 10);
	}

}
