package com.zsm.encryptIt.ui.preferences;

import java.util.HashSet;
import java.util.Set;

import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.log.Log;
import com.zsm.log.Log.LEVEL;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Preferences {

	private static final String LOCK_APP_TIME = "LOCK_APP_TIME";
	private static final String MAIN_LIST_EXPANDABLE = "MAIN_LIST_EXPANDABLE";
	
	private static final String LOG_LEVEL = "LOG_LEVEL";
	private static final String LOG_CHANNELS = "LOG_CHANNELS";

	private static final String DEFAULT_LOCK_APP_TIME = "5";
	private static final LEVEL DEFAULT_LOG_LEVEL = Log.LEVEL.ERROR;
	private static final Set<String> DEFAULT_LOG_CHANNELS;
	
	static {
		DEFAULT_LOG_CHANNELS = new HashSet<String>();
		DEFAULT_LOG_CHANNELS.add( EncryptItApplication.DEFAULT_LOG );
	}

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
					preferences.getString( LOCK_APP_TIME,
										   DEFAULT_LOCK_APP_TIME ) )*1000;
	}

	public boolean getMainListExpandable() {
		return preferences.getBoolean( MAIN_LIST_EXPANDABLE, true );
	}

	public LEVEL getLogLevel() {
		return Log.LEVEL.valueOf( 
				preferences.getString( LOG_LEVEL, DEFAULT_LOG_LEVEL.name() ) );
	}

	public Set<String> getLogChannels() {
		Set<String> c
			= preferences.getStringSet( LOG_CHANNELS, DEFAULT_LOG_CHANNELS );
		c.add( EncryptItApplication.ANDROID_LOG );
		return c;
	}
}
