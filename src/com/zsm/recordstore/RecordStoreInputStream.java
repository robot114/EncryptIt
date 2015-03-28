package com.zsm.recordstore;

import java.io.IOException;
import java.io.InputStream;

public class RecordStoreInputStream extends InputStream {

	protected InputStream inputStream;
	protected AbstractCursor cursor;
	private boolean closeCursorWhenCloseStream;

	public RecordStoreInputStream(InputStream in, AbstractCursor c ) {
		this( in, c, false );
	}

	public RecordStoreInputStream(InputStream in, AbstractCursor c,
								  boolean closeCursorWhenCloseStream ) {
		super();
		inputStream = in;
		cursor = c;
		this.closeCursorWhenCloseStream = closeCursorWhenCloseStream;
	}
	
	@Override
	public int read()
		throws IOException, NoSuchRecordException, RecordStoreNotOpenException {
		
		return inputStream.read();
	}

	@Override
	public void close() throws IOException {
		if( closeCursorWhenCloseStream ) {
			cursor.close();
		}
		super.close();
	}

}
