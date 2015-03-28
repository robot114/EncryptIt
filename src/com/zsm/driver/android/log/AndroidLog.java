package com.zsm.driver.android.log;

import java.io.BufferedReader;
import java.io.IOException;

import com.zsm.log.DummyReader;
import com.zsm.log.Log;

public class AndroidLog extends Log {
	
	private static final int DELTA_OF_TWO_LEVEL
		= android.util.Log.DEBUG - Log.DEBUG;
	
	private String tag;

	public AndroidLog() {
		this( "AndroidLog" );
	}
	
	public AndroidLog( String tag ) {
		this.tag = tag;
	}
	
	@Override
	protected BufferedReader createReader() throws IOException {
		return new DummyReader();
	}

	@Override
	protected void print(Throwable t, Object message, int level)
			throws IOException {

		if( level + DELTA_OF_TWO_LEVEL >= getLevel() ) {
			// By default, log of other level does not display
			android.util.Log.e( tag, "" + message, t );
		} else {
			androidLog( t, message, level );
		}
	}

	private void androidLog( Throwable t, Object message, int level ) {
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
		// TODO: clear the logs
	}

}
