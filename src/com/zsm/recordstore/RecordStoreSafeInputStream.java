package com.zsm.recordstore;

import java.io.IOException;
import java.io.InputStream;

public class RecordStoreSafeInputStream extends RecordStoreInputStream{

	
	private RecordStore recordStore;

	public RecordStoreSafeInputStream(InputStream in, RecordStore rs, AbstractCursor c ) {
		super( in, c );
		recordStore = rs;
	}

	public RecordStoreSafeInputStream(InputStream in, RecordStore rs, AbstractCursor c,
								  boolean closeCursorWhenCloseStream ) {
		super( in, c, closeCursorWhenCloseStream );
	}
	
	@Override
	public int read()
		throws IOException, NoSuchRecordException, RecordStoreNotOpenException {
		
		if( recordStore.isOpen() ) {
			throw new RecordStoreNotOpenException( "RecordStore closed!" );
		}
		
		if( !cursor.currentExist() ) {
			throw new NoSuchRecordException(
					"Record at " + cursor + " does not exist!" );
		}
		
		return inputStream.read();
	}

}
