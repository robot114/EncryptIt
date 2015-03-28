package com.zsm.encryptIt.android;

import java.sql.RowId;
import java.util.Date;

import android.database.Cursor;

import com.zsm.recordstore.AbstractRawCursor;
import com.zsm.recordstore.LongRowId;
import com.zsm.recordstore.RawRecordStore;
import com.zsm.recordstore.RecordStoreNotOpenException;

public class RawWrappedAndroidCursor extends AbstractRawCursor {

	private final int idColumnIndex;
	private final int dataColumnIndex;
	private final int createColumnIndex;
	private final int modifyColumnIndex;

	final private Cursor innerCursor;
	
	public RawWrappedAndroidCursor(Cursor cursor) {
		innerCursor = cursor;
		idColumnIndex = cursor.getColumnIndex( RawRecordStore.COLUMN_ID );
		dataColumnIndex = cursor.getColumnIndex( RawRecordStore.COLUMN_DATA );
		createColumnIndex = cursor.getColumnIndex( RawRecordStore.COLUMN_CREATE );
		modifyColumnIndex = cursor.getColumnIndex( RawRecordStore.COLUMN_MODIFY );
	}

	@Override
	public boolean moveToFirst() throws RecordStoreNotOpenException {
		return innerCursor.moveToFirst();
	}

	@Override
	public boolean moveToNext() throws RecordStoreNotOpenException {
		return innerCursor.moveToNext();
	}

	@Override
	public boolean moveToPosition(int position)
						throws RecordStoreNotOpenException {
		
		return innerCursor.moveToPosition(position);
	}

	@Override
	public boolean currentExist() throws RecordStoreNotOpenException {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void updateCursor(int op, RowId id) {
		innerCursor.requery();
	}

	@Override
	public RowId currentId() {
		return new LongRowId( innerCursor.getLong( idColumnIndex ) );
	}

	@Override
	public byte[] getData() {
		return innerCursor.getBlob(dataColumnIndex);
	}

	@Override
	public Date getCreated() {
		return new Date( innerCursor.getLong(createColumnIndex) );
	}

	@Override
	public Date getLastModified() {
		return new Date( innerCursor.getLong(modifyColumnIndex) );
	}

	@Override
	public int getCount() {
		return innerCursor.getCount();
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
