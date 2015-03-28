package com.zsm.recordstore;

import java.io.Closeable;
import java.sql.RowId;

/**
 * The subclass of this one should wrap a cursor instance of the concrete database.
 * 
 * @author zsm
 *
 */
public abstract class AbstractCursor implements Closeable {
	
	private boolean closed;
	private boolean keepUpdated;
	
	protected AbstractCursor( ) {
		closed = false;
		keepUpdated = true;
	}
	
	/**
	 * Whether keep this cursor notified when the record store change.
	 * By default, it is notified.
	 * <p>When this flag change from false to true, the cursor may do
	 * a update.
	 *  
	 * @param keepUpdated true, keep the cursor notified when the record
	 * 					  store change
	 */
	public void setKeepUpdated( boolean keepUpdated ) {
		if( keepUpdated && !this.keepUpdated ) {
			updateCursor( 0, null );
		}
		this.keepUpdated = keepUpdated;
	}
	
	public boolean isKeepUpdated( ) {
		return keepUpdated;
	}
	
	/**
	 * Close the cursor. For a closed cursor, any invocation by it or
	 * use it will throws RecordStoreNotOpenException, except this one.
	 * If the cursor has been close, nothing will happen.
	 * 
	 */
	public void close() throws RecordStoreNotOpenException {
		closed = true;
	}

	/**
	 * Check if the cursor is closed.
	 * 
	 * @return true, if it is closed.
	 */
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * Get how may rows in the cursor set
	 * 
	 * @return rows count
	 */
	abstract public int getCount();

	/**
	 * Get columns of this cursor
	 *  
	 * @return columns of this cursor
	 */
	abstract public String[] getColumnNames();
	
	/**
	 * Move the cursor to the first record. When a cursor is return by RecordStore,
	 * it should at the position of the first record.
	 * 
	 * @return true if the result set is <b>NOT</b> empty
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 */
	abstract public boolean moveToFirst() throws RecordStoreNotOpenException;
	
	/**
	 * Move the cursor to the next record. When the method moveToFirst called, then
	 * this method called, the cursor should point to the second one.
	 * 
	 * @return true if the cursor is not at the last record
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 */
	abstract public boolean moveToNext() throws RecordStoreNotOpenException;

	/**
	 * Move the cursor to a special index in the result set.
	 * 
	 * @return true if there is such a record
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 */
	abstract public boolean moveToPosition( int position )
			throws RecordStoreNotOpenException;

	/**
	 * Check if the current record exists.
	 * 
	 * @return true if the current record exists
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 */
	abstract public boolean currentExist() throws RecordStoreNotOpenException;

	/**
	 * Update this cursor when the record store id modified.
	 * 
	 * @param op Which operations were taken. If it is 0, then the operations
	 * 				were not cared. It can be one of the values,
	 * 				{@link #code RecordStore.OPERATION_ADD},
	 * 				{@link #code RecordStore.OPERATION_DELETE},
	 * 				{@link #code RecordStore.OPERATION_UPDATE}. Or composition of them.
	 * @param id of the record is affected. null, when more than one record is affect
	 * 				or no record is affected at all. When the affected record is not
	 * 				sure, it will be null too.
	 */
	abstract public void updateCursor( int op, RowId id );

	/**
	 * Check the state of the cursor and its record store
	 */
	protected void checkOpenState() {
		if( isClosed() ) {
			throw new RecordStoreNotOpenException( "Cursor is closed" );
		}
	}

	/**
	 * Returns <code>true</code> if the value in the indicated column is null.
	 *
	 * @param columnIndex the zero-based index of the target column.
	 * @return whether the column value is null.
	 */
	public abstract boolean isNull(int columnIndex);

	/**
	 * Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
	 * If you expect the column to exist use {@link #getColumnIndexOrThrow(String)} instead, which
	 * will make the error more clear.
	 *
	 * @param columnName the name of the target column.
	 * @return the zero-based column index for the given column name, or -1 if
	 * the column name does not exist.
	 * @see #getColumnIndexOrThrow(String)
	 */
	public abstract int getColumnIndex(String columnName);

	/**
	 * Returns the column name at the given zero-based column index.
	 *
	 * @param columnIndex the zero-based index of the target column.
	 * @return the column name for the given column index.
	 */
	public abstract String getColumnName(int columnIndex);
}
