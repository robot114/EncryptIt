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
	protected void print(Throwable t, Object message, int level)
			throws IOException {
		
		if( level >= WARNING ) {
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

}
