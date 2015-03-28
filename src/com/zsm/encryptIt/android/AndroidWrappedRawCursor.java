package com.zsm.encryptIt.android;

import com.zsm.recordstore.AbstractRawCursor;
import com.zsm.recordstore.LongRowId;
import com.zsm.recordstore.RawRecordStore;

public class AndroidWrappedRawCursor extends android.database.AbstractCursor {

	private AbstractRawCursor innerCursor;

	public AndroidWrappedRawCursor( AbstractRawCursor cursor ) {
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
        throw new UnsupportedOperationException("getString is not supported");
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
        String columnName = innerCursor.getColumnName(column);
        switch( columnName ) {
        	case RawRecordStore.COLUMN_ID:
        		return ((LongRowId)innerCursor.currentId()).getLongId();
        	case RawRecordStore.COLUMN_CREATE:
        		return innerCursor.getCreated().getTime();
        	case RawRecordStore.COLUMN_MODIFY:
        		return innerCursor.getLastModified().getTime();
        	default:
        		throw new IndexOutOfBoundsException(  );
        }
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

	public AbstractRawCursor getInnerCursor() {
		return innerCursor;
	}
}
