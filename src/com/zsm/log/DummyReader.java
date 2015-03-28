package com.zsm.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

final public class DummyReader extends BufferedReader {

	/**
	 * @param systemOutLog
	 */
	public DummyReader() {
		super( new Reader(){

			@Override
			public void close() throws IOException {
			}

			@Override
			public int read(char[] buffer, int offset, int count)
					throws IOException {
				return -1;
			}} );
	}

	@Override
	public void close() throws IOException {
	}
}