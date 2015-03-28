package com.zsm.recordstore;

import java.sql.RowId;
import java.util.Date;

public class RawRecordStoreCursor extends AbstractRawCursor {

	final private RawRecordStore recordStore;
	final private RecordStoreCursor innerCursor;
	final protected int idColumnIndex;
	final protected int dataColumnIndex;
	final protected int createColumnIndex;
	final protected int modifyColumnIndex;
	final protected boolean closeInnerTogether;

	/**
	 * Create a raw record store cursor from an existing instance of 
	 * {@link #code RecordStoreCursor}
	 * 
	 * @param rs to which, the cursor attached
	 * @param cursor the existing record store cursor
	 * @param closeInnerTogether whether close the record store cursor when close
	 * 							 this one
	 */
	protected RawRecordStoreCursor( RawRecordStore rs, RecordStoreCursor cursor,
								 	boolean closeInnerTogether ) {
		super();
		recordStore = rs;
		innerCursor = cursor;
		
		idColumnIndex = cursor.getColumnIndex( RawRecordStore.COLUMN_ID );
		dataColumnIndex = cursor.getColumnIndex( RawRecordStore.COLUMN_DATA );
		createColumnIndex = cursor.getColumnIndex( RawRecordStore.COLUMN_CREATE );
		modifyColumnIndex = cursor.getColumnIndex( RawRecordStore.COLUMN_MODIFY );
		this.closeInnerTogether = closeInnerTogether;
	}
	
	/**
	 * Create a raw record store cursor from ground.
	 * 
	 * @param rs to which, the cursor attached
	 * @param selection A filter declaring which rows to return, formatted as an
     *            SQL WHERE clause (excluding the WHERE itself). Passing null
     *            will return all rows for the given table.
	 * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in order that they
     *         appear in the selection. The values will be bound as Strings.
	 * @param groupBy A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
	 * @param having A filter declare which row groups to include in the cursor,
     *            if row grouping is being used, formatted as an SQL HAVING
     *            clause (excluding the HAVING itself). Passing null will cause
     *            all row groups to be included, and is required when row
     *            grouping is not being used.
	 * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
	 * @param parameter used to be create the cursor. The structure and meaning are 
	 * 					implementation-defined
	 * @param cursorMover cursor mover to move the cursor
	 */
	RawRecordStoreCursor( RawRecordStore rs, String selection,
			   			  String[] selectionArgs, String groupBy,
			   			  String having, String orderBy, Object parameter ) {
		super();
		recordStore = rs;
		innerCursor
			= recordStore.getRecordStore().newCursor(
								RawRecordStore.TABLE_NAME, RawRecordStore.COLUMNS,
								selection, selectionArgs, groupBy, having,
								orderBy, parameter );
;
		
		idColumnIndex = innerCursor.getColumnIndex( RawRecordStore.COLUMN_ID );
		dataColumnIndex = innerCursor.getColumnIndex( RawRecordStore.COLUMN_DATA );
		createColumnIndex = innerCursor.getColumnIndex( RawRecordStore.COLUMN_CREATE );
		modifyColumnIndex = innerCursor.getColumnIndex( RawRecordStore.COLUMN_MODIFY );
		closeInnerTogether = true;
	}
	
	/**
	 * Close the cursor. In this method, the record store will be notified.
	 * For a closed cursor, any invocation by it or use it will throws 
	 * RecordStoreNotOpenException, except this one. If the cursor has
	 * been close, nothing will happen.
	 * 
	 */
	@Override
	public void close() throws RecordStoreNotOpenException {
		super.close();
		if( recordStore != null ) {
			recordStore.closeCursor( this );
		}
		if( closeInnerTogether ) {
			innerCursor.close();
		}
	}
	
	@Override
	public int getCount() {
		return innerCursor.getCount();
	}
	
	AbstractCursor getInnerCursor() {
		return innerCursor;
	}
	
	/**
	 * Check the state of the cursor and its record store
	 */
	@Override
	protected void checkOpenState() {
		super.checkOpenState();
		
		if( !getRecordStore().isOpen() ) {
			throw new RecordStoreNotOpenException( "RecordStore is not open" );
		}
	}
	
	public RawRecordStore getRecordStore() {
		return recordStore;
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
	public RowId currentId() {
		return new LongRowId( innerCursor.getInt(idColumnIndex) );
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
