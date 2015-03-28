package com.zsm.recordstore.driver.android.sqlite;

import java.sql.RowId;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import com.zsm.log.Log;
import com.zsm.recordstore.RecordStore;
import com.zsm.recordstore.RecordStoreCursor;
import com.zsm.recordstore.RecordStoreNotOpenException;

public class SQLiteDriverCursor extends RecordStoreCursor implements Cursor {

	final private FILED_TYPE[] DRIVER_TYPE_TO_MINE
		= { FILED_TYPE.NULL, FILED_TYPE.INTEGER, FILED_TYPE.FLOAT,
			FILED_TYPE.STRING, FILED_TYPE.BLOB };
	
	private Cursor cursorDriver;

	private String groupBy;
	private String having;
	private String order;
	
	public SQLiteDriverCursor(RecordStore rs,
							  String tables, String[] columns,
							  String where, String[] whereArgs,
							  String groupBy, String having,
							  String order ) {
		super(rs, tables, columns, where, whereArgs);
		this.groupBy = groupBy;
		this.having = having;
		this.order = order;
		
		cursorDriver = generateCursorDriver();
	}
	
	@Override
	public void close()  throws RecordStoreNotOpenException {
		super.close();
		cursorDriver.close();
	}

	@Override
	public boolean moveToFirst() throws RecordStoreNotOpenException {
		checkOpenState();
		return cursorDriver.moveToFirst();
	}

	@Override
	public boolean moveToNext() throws RecordStoreNotOpenException {
		checkOpenState();
		return cursorDriver.moveToNext();
	}

	@Override
	public boolean moveToPosition(int position)
						throws RecordStoreNotOpenException {
		
		checkOpenState();
		return cursorDriver.moveToPosition((int) position);
	}

	@Override
	public boolean currentExist() throws RecordStoreNotOpenException {
		checkOpenState();
		// TODO: 
		return true;
	}

	@Override
	public byte[] getBlob(int columnIndex) {
		checkOpenState();
		return cursorDriver.getBlob(columnIndex);
	}

	@Override
	public String getString(int columnIndex) {
		checkOpenState();
		return cursorDriver.getString(columnIndex);
	}

	@Override
	public void copyStringToBuffer(int columnIndex, char[] buffer) {
		checkOpenState();
		cursorDriver.copyStringToBuffer(columnIndex,
										new CharArrayBuffer( buffer ) );
	}

	@Override
	public short getShort(int columnIndex) {
		checkOpenState();
		return cursorDriver.getShort(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) {
		checkOpenState();
		return cursorDriver.getInt(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) {
		checkOpenState();
		return cursorDriver.getLong(columnIndex);
	}

	@Override
	public float getFloat(int columnIndex) {
		checkOpenState();
		return cursorDriver.getFloat(columnIndex);
	}

	@Override
	public double getDouble(int columnIndex) {
		checkOpenState();
		return cursorDriver.getDouble(columnIndex);
	}

	@Override
	public FILED_TYPE getColumnType(int columnIndex) {
		checkOpenState();
		int type = cursorDriver.getType(columnIndex);
		
		if( type < 0 || type >= DRIVER_TYPE_TO_MINE.length ) {
			return FILED_TYPE.NULL;
		}
		
		return DRIVER_TYPE_TO_MINE[type];
	}

	@Override
	public boolean isNull(int columnIndex) {
		checkOpenState();
		return cursorDriver.isNull(columnIndex);
	}

	@Override
	public int getColumnIndex(String columnName) {
		checkOpenState();
		return cursorDriver.getColumnIndex(columnName);
	}

	@Override
	public String getColumnName(int columnIndex) {
		checkOpenState();
		return cursorDriver.getColumnName(columnIndex);
	}

	@Override
	public void updateCursor(int op, RowId id) {
		checkOpenState();
		
		int position = cursorDriver.getPosition();
		cursorDriver = generateCursorDriver();
		
		int count = cursorDriver.getCount();
		position = position > count ? count : position;
		cursorDriver.moveToPosition(position);
		
		Log.d( "The cursor is updated. ", this, op, id );
	}

	private Cursor generateCursorDriver() {
		SQLiteDatabase db = (SQLiteDatabase)(getRecordStore().getDatabase());
		Log.d( "start query and generate a cursor.", "db", db,
				"table", tables, "columns", columns, "where", where,
				"whereArgs", whereArgs, "groupBy", groupBy, "having", having,
				"order", order );
		return db.query( tables, columns, where, whereArgs, groupBy, having, order );
	}

	@Override
	public int getCount() {
		return cursorDriver.getCount();
	}

	@Override
	public int getPosition() {
		return cursorDriver.getPosition();
	}

	@Override
	public boolean move(int offset) {
		return cursorDriver.move(offset);
	}

	@Override
	public boolean moveToLast() {
		return cursorDriver.moveToLast();
	}

	@Override
	public boolean moveToPrevious() {
		return cursorDriver.moveToPrevious();
	}

	@Override
	public boolean isFirst() {
		return cursorDriver.isFirst();
	}

	@Override
	public boolean isLast() {
		return cursorDriver.isLast();
	}

	@Override
	public boolean isBeforeFirst() {
		return cursorDriver.isBeforeFirst();
	}

	@Override
	public boolean isAfterLast() {
		return cursorDriver.isAfterLast();
	}

	@Override
	public int getColumnIndexOrThrow(String columnName)
			throws IllegalArgumentException {
		return cursorDriver.getColumnIndexOrThrow(columnName);
	}

	@Override
	public String[] getColumnNames() {
		return cursorDriver.getColumnNames();
	}

	@Override
	public int getColumnCount() {
		return cursorDriver.getColumnCount();
	}

	@Override
	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
		cursorDriver.copyStringToBuffer(columnIndex, buffer);
	}

	@Override
	public int getType(int columnIndex) {
		return cursorDriver.getType(columnIndex);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void deactivate() {
		cursorDriver.deactivate();
	}

	@Override
	@Deprecated
	public boolean requery() {
		return cursorDriver.requery();
	}

	@Override
	public void registerContentObserver(ContentObserver observer) {
		cursorDriver.registerContentObserver(observer);
	}

	@Override
	public void unregisterContentObserver(ContentObserver observer) {
		cursorDriver.unregisterContentObserver(observer);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		cursorDriver.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		cursorDriver.unregisterDataSetObserver(observer);
	}

	@Override
	public void setNotificationUri(ContentResolver cr, Uri uri) {
		cursorDriver.setNotificationUri(cr, uri);
	}

	@SuppressLint("NewApi")
	@Override
	public Uri getNotificationUri() {
		return cursorDriver.getNotificationUri();
	}

	@Override
	public boolean getWantsAllOnMoveCalls() {
		return cursorDriver.getWantsAllOnMoveCalls();
	}

	@Override
	public Bundle getExtras() {
		return cursorDriver.getExtras();
	}

	@Override
	public Bundle respond(Bundle extras) {
		return cursorDriver.respond(extras);
	}

}
