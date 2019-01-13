package com.zsm.encryptIt.android;

import com.zsm.recordstore.AbstractMetaDataCursor;
import com.zsm.recordstore.RawRecordStore;

public class AndroidWrappedMetaCursor extends android.database.AbstractCursor {

	private AbstractMetaDataCursor innerCursor;

	public AndroidWrappedMetaCursor( AbstractMetaDataCursor cursor ) {
		innerCursor = cursor;
	}
	
	@Override
	public int getCount() {
		return innerCursor.getCount();
	}

	@Override
	public String[] getColumnNames() {
		return innerCursor.getColumnNames();
	}

	@Override
	public byte[] getBlob(int column) {
	    if( innerCursor.getColumnIndex( RawRecordStore.COLUMN_DATA ) == column ) {
	    	return innerCursor.getData();
	    }
        throw new UnsupportedOperationException("getBlob is not supported "
        										+ "for this column: "
        										+ innerCursor.getColumnName(column) );
	}

	@Override
	public String getString(int column) {
	    if( innerCursor.getColumnIndex( RawRecordStore.COLUMN_KEY ) == column ) {
	    	return innerCursor.getKey();
	    }
        throw new UnsupportedOperationException("getBlob is not supported "
        										+ "for this column: "
        										+ innerCursor.getColumnName(column) );
	}

	@Override
	public short getShort(int column) {
        throw new UnsupportedOperationException("getShort is not supported");
	}

	@Override
	public int getInt(int column) {
        throw new UnsupportedOperationException("getInt is not supported");
	}

	@Override
	public long getLong(int column) {
        throw new UnsupportedOperationException("getFloat is not supported");
	}

	@Override
	public float getFloat(int column) {
        throw new UnsupportedOperationException("getFloat is not supported");
	}

	@Override
	public double getDouble(int column) {
        throw new UnsupportedOperationException("getDouble is not supported");
	}

	@Override
	public boolean isNull(int column) {
        throw new UnsupportedOperationException("isNull is not supported");
	}

	public AbstractMetaDataCursor getInnerCursor() {
		return innerCursor;
	}
}
