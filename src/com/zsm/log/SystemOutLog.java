/**
 * 
 */
package com.zsm.log;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * @author zsm
 *
 */
public class SystemOutLog extends Log {

    /**
	 * Return a dummy reader which can read nothing actually.
	 */
	@Override
	public BufferedReader createReader() throws IOException {
		return new DummyReader();
	}

	@Override
	protected void print(Throwable t, Object message, LEVEL level)
			throws IOException {
		
		if( level.compareTo( LEVEL.WARNING ) >= 0 ) {
			System.err.println( message );
			if( t == null ) {
				t = new Exception();
			}
			t.printStackTrace();
		} else {
			System.out.println( message );
		}
	}

    /**
     * Do nothing.
	 */
	@Override
	public void clearContent() throws IOException {
	}

	@Override
	protected void uninstall() throws IOException {
		// Do nothing
	}

	@Override
	public String toString() {
		return "Logs out by System.out";
	}

}
