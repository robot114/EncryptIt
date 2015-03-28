package com.zsm.recordstore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.RowId;

import com.zsm.log.Log;

public class RecordStoreOutputStream extends OutputStream {

	protected RawRecordStore recordStore;
	protected RowId id;
	private boolean dataChanged;
	private ByteArrayOutputStream out;
	private boolean closed;

	public RecordStoreOutputStream( RawRecordStore rs, RowId id ) {
	
		out = new ByteArrayOutputStream();
		recordStore = rs;
		this.id = id;
		
		dataChanged = false;
		closed = false;
	}
	
	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {
		dataChanged = ( count > 0 );
		super.write(buffer, offset, count);
	}

	@Override
	public void write(int oneByte) throws IOException {
		dataChanged = true;
		out.write(oneByte);
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		dataChanged = ( buffer != null && buffer.length > 0 );
		super.write(buffer);
	}

	private void flushToRecordStore() throws IOException {
		if( !dataChanged ) {	// No changing no flushing
			return;
		}
		byte[] data = out.toByteArray();
		
		try {
			recordStore.update(id, data );
			dataChanged = false;
			Log.d( "The data has been update to the db. ",
				   "length: ", data.length, data );
		} catch (RecordStoreException e) {
			Log.e(e);
			throw new IOException( e.getMessage() );
		}
	}

	@Override
	public void close() throws IOException {
		if( closed ) {
			return;
		}
		
		// Actually this is not a stream for the DB, it is a block data. And it 
		// just has only one block. It will just be written to the DB once.
		flushToRecordStore();
		
		recordStore.getRecordStore().commit();
		super.close();
		closed = true;
	}

}
