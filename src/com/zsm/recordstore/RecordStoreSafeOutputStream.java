package com.zsm.recordstore;

import java.io.IOException;
import java.sql.RowId;

public class RecordStoreSafeOutputStream extends RecordStoreOutputStream {

	public RecordStoreSafeOutputStream( RawRecordStore rs, RowId id ) {
		super(rs, id);
	}

	@Override
	public void write( int data ) 
		throws IOException, NoSuchRecordException, RecordStoreNotOpenException {
		
		if( recordStore.isOpen() ) {
			throw new RecordStoreNotOpenException( "RecordStore closed!" );
		}
		super.write( data );
	}

}
