package com.zsm.persistence;

import java.sql.RowId;
import java.util.Date;

import com.zsm.recordstore.AbstractRawCursor;
import com.zsm.recordstore.RecordStoreNotOpenException;

public class PersistenceCursor extends AbstractRawCursor {

	final private AbstractRawCursor innerCursor;
	
	public PersistenceCursor( AbstractRawCursor cursor ) {
		innerCursor = cursor;
	}
	
	@Override
	public RowId currentId() {
		return innerCursor.currentId();
	}

	@Override
	public byte[] getData() {
		byte[] data = innerCursor.getData();
		return data;
	}

	@Override
	public Date getCreated() {
		return innerCursor.getCreated();
	}

	@Override
	public Date getLastModified() {
		return innerCursor.getLastModified();
	}

	@Override
	public int getCount() {
		return innerCursor.getCount();
	}

	@Override
	public boolean moveToFirst() throws RecordStoreNotOpenException {
		innerCursor.moveToFirst();		// The firs row is the magic data
		return innerCursor.moveToNext();
	}

	@Override
	public boolean moveToNext() throws RecordStoreNotOpenException {
		return innerCursor.moveToNext();
	}

	@Override
	public boolean moveToPosition(int position)
			throws RecordStoreNotOpenException {
		
		// The first row is the magic data
		return innerCursor.moveToPosition(position+1);
	}

	@Override
	public boolean currentExist() throws RecordStoreNotOpenException {
		return innerCursor.currentExist();
	}

	@Override
	public void updateCursor(int op, RowId id) {
		innerCursor.updateCursor(op, id);
	}

	@Override
	public boolean isNull(int columnIndex) {
		return innerCursor.isNull(columnIndex);
	}

	@Override
	public int getColumnIndex(String columnName) {
		return innerCursor.getColumnIndex(columnName);
	}

	@Override
	public String getColumnName(int columnIndex) {
		return innerCursor.getColumnName(columnIndex);
	}

}
