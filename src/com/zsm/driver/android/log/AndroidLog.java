package com.zsm.driver.android.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.zsm.log.DummyReader;
import com.zsm.log.Log;

public class AndroidLog extends Log {
	
	private String tag;

	public AndroidLog() {
		this( "AndroidLog" );
	}
	
	public AndroidLog( String tag ) {
		this.tag = tag;
	}
	
	@Override
	public BufferedReader createReader() throws IOException {
		try {
			  Process process = Runtime.getRuntime().exec("logcat -d");
			  BufferedReader bufferedReader
			  	= new BufferedReader(
			  			new InputStreamReader(process.getInputStream()));
			  
			  return bufferedReader;
		} catch (IOException e) {
			Log.e( e );
			return new DummyReader();
		}
	}

	@Override
	protected void print(Throwable t, Object message, LEVEL level)
			throws IOException {

		if( level.compareTo( getLevel() ) >= 0 ) {
			// By default, log of other level does not display
			android.util.Log.e( tag, "" + message, t );
		} else {
			androidLog( t, message, level );
		}
	}

	private void androidLog( Throwable t, Object message, LEVEL level ) {
		switch( level ) {
		case INFO:
		 	android.util.Log.i(tag, "" + message, t );
		 	break;
		case DEBUG:
		 	android.util.Log.d(tag, "" + message, t );
		 	break;
		case WARNING:
		 	android.util.Log.w(tag, "" + message, t );
		 	break;
		case ERROR:
		 	android.util.Log.e(tag, "" + message, t );
		 	break;
		default:
			// Unexcepted level, so it may be a terrified error.
			android.util.Log.wtf(tag, "" + message, t );
			break;
	}

	}
	@Override
	public void clearContent() throws IOException {
		Runtime.getRuntime().exec("logcat -c");
	}

	@Override
	protected void uninstall() {
		// Do nothing
	}

	@Override
	public String toReadableString() {
		return "Android System Log";
	}
}
