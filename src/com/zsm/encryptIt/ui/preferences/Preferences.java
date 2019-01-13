package com.zsm.encryptIt.ui.preferences;

import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.zsm.encryptIt.backup.BackupTargetFilesConsts;

public class Preferences {

	private static final String KEY_BACKUP_TYPE = "KEY_BACKUP_TYPE";
	private static final String KEY_BACKUP_SECURITY_URI = "KEY_BACKUP_SECURITY_URI";
	private static final String KEY_LAST_EXPORT_PATH = "KEY_LAST_EXPORT_PATH";
	private static final String KEY_LOCK_APP_TIME = "LOCK_APP_TIME";
	private static final String KEY_MAIN_LIST_EXPANDABLE = "MAIN_LIST_EXPANDABLE";
	private static final String KEY_MAX_KEYSTORE_TEMP_FILE_NUM
									= "KEY_MAX_KEYSTORE_TEMP_FILE_NUM";
	private static final String KEY_LAST_KEYSTORE_TEMP_FILE_NUM
									= "KEY_LAST_KEYSTORE_TEMP_FILE_NUM";
	
	public static final String KEY_BACKUP_ARCHIVE = "KEY_BACKUP_ARCHIVE";
	public static final String KEY_BACKUP_MULTI_FILES = "KEY_BACKUP_MULTI_FILES";
	static final String[] KEY_BACKUP_FILE_TYPES = new String[]{
		KEY_BACKUP_MULTI_FILES, KEY_BACKUP_ARCHIVE
	};
	
	private static final String DEFAULT_LOCK_APP_TIME = "5";
	
	static private Preferences instance;
	
	final private SharedPreferences preferences;
	private boolean mExportEnable;
	
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
		return preferences.getBoolean( KEY_MAIN_LIST_EXPANDABLE, true );
	}

	public boolean exportAsXml() {
		return preferences.getBoolean( "KEY_EXPORT_XML", false);
	}

	public Uri getLastExportPath() {
		return getUriPreference( KEY_LAST_EXPORT_PATH );
	}

	public void setLastExportPath(Uri pathUri) {
		preferences
			.edit()
			.putString(KEY_LAST_EXPORT_PATH, pathUri.toString())
			.commit();
	}

	public Uri getBackupSecurityUri() {
		return getUriPreference(KEY_BACKUP_SECURITY_URI);
	}

	private Uri getUriPreference(String key) {
		String uriStr = preferences.getString( key, null );
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

	public String getBackupFilesType() {
		return preferences.getString( KEY_BACKUP_TYPE, Preferences.KEY_BACKUP_ARCHIVE );
	}

	public void setBackupFileType( String type ) {
		if( !Arrays.asList( KEY_BACKUP_FILE_TYPES ).contains(type) ) {
			throw new IllegalArgumentException( "Invalid backup type: " + type );
		}
		
		BackupTargetFilesConsts.updateBackupFilesInstance(type);
		
		preferences.edit().putString( KEY_BACKUP_TYPE, type ).commit();
	}
	
	public boolean getExportEnable() {
		return mExportEnable;
	}
	
	public void setExportEnable( boolean enable ) {
		mExportEnable = enable;
	}
}
