package com.zsm.recordstore;

import java.io.Closeable;
import java.sql.RowId;
import java.util.Vector;

import com.zsm.util.Converter;


public abstract class RecordStore implements Closeable {
	
	public static final int OPERATION_ADD = 0x1;
	public static final int OPERATION_DELETE = 0x2;
	public static final int OPERATION_UPDATE = 0x4;
	
	private Vector<AbstractCursor> cursorSet = new Vector<AbstractCursor>();
	private boolean readOnly;
	final private Converter converter;

	public RecordStore(boolean readOnly, Converter converter) {
		this.readOnly = readOnly;
		this.converter = converter;
	}

	/**
	 * Is this record store read only. For the read only record store, rows 
	 * <b>CANNOT</b> be deleted or modified.
	 * 
	 * @return true, if the record store is read only
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Close the record store.
	 * 
	 * @throws RecordStoreException
	 */
	synchronized public void close() throws RecordStoreException {
		for( AbstractCursor c : cursorSet ) {
			c.close();
		}
	}

	/**
	 * Check if the record store is open.
	 * 
	 * @return true, if the record store is open.
	 */
	public abstract boolean isOpen();
	
	/**
	 * Get the wrapped database. The type of the database is defined by the concrete
	 * driver.
	 * 
	 * @return the wrapped database
	 */
	public abstract Object getDatabase();
	
	/**
	 * Generate a cursor instance. Each cursor generated from the same record store
	 * is independent to the others. When the record store is closed, the cursors
	 * are invalid. The modification of the RecordStore will affect the the cursor.
	 * For example, the moveToNext method will return false, when the cursor points
	 * to the record just before the last one, and the last one is removed.</br>
	 * 
	 * <p>The cursor should be closed when it is useless.
	 * 
	 * <p><b>CAUTION: This method of the subclass MUST invoke the method 
	 * newCursor( Cursor ) to make the new cursor managed.</b>
	 * @param tables from which tables generate the cursor
	 * @param columns A list of which columns to return. Passing null will
     *            return all columns, which is discouraged to prevent reading
     *            data from storage that isn't going to be used.
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
	 * 
     * @return a cursor, by which to navigate the record store and operate the record.
	 * @throws RecordStoreNotOpenException if the record store is closed
	 */
	public abstract RecordStoreCursor newCursor( String tables,
												 String[] columns,
		            						  	 String selection,
		            						  	 String[] selectionArgs,
		            						     String groupBy,
		            						     String having,
		            						     String orderBy,
		            						     Object parameter )
			throws RecordStoreNotOpenException;
	
	/**
	 * Remove the record at the cursor. If the record does not exist, nothing will happen.
	 * After removing, the cursor passed in will point to the record next to the removed
	 * one.
	 * 
	 * @param table from which table remove the rows
	 * @param selection A filter declaring which rows to return, formatted as an
     *            SQL WHERE clause (excluding the WHERE itself). Passing null
     *            will return all rows for the given table.
	 * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in order that they
     *         appear in the selection. The values will be bound as Strings.
	 * @return count of records removed. It should be 0 or 1 here.
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 */
	public abstract int remove( String table, String selection,
								String[] selectionArgs )
			throws RecordStoreNotOpenException, InvalidOperationException;

	/**
	 * Add a new row and return a cursor points to the new row. <b>The cursor may
	 * be limited to just the new row. That means it cannot move.</b>
	 * 
	 * <p><b>CAUTION: This method of the subclass MUST invoke the method 
	 * newCursor( Cursor ) to make the new cursor managed.</b>
	 * 
	 * @param table name of the table to insert the data
	 * @param data store in the hash map to be added into the table. In the hash
	 * 			map, the keys are the columns' name. And the values in the map
	 * 			are the values to be added into the db
	 * @return id of the new row
	 * 
	 * @throws RecordStoreNotOpenException if the record store is closed
	 * @throws InvalidOperationException If the record store is read only.
	 */
	public abstract RowId add( String table, Object data )
				throws RecordStoreNotOpenException, InvalidOperationException;

	/**
	 * Update the record at the cursor. If the record does not exist,
	 * InvalidOperationException should be thrown. 
	 * 
	 * @param tables at which the record will be updated
	 * @param selection A filter declaring which rows to return, formatted as an
     *            SQL WHERE clause (excluding the WHERE itself). Passing null
     *            will return all rows for the given table.
	 * @param selectionArgs You may include ?s in selection, which will be
     *         replaced by the values from selectionArgs, in order that they
     *         appear in the selection. The values will be bound as Strings.
	 * @param data values to update. The structure and the meaning are
	 * 				 implementation defined.
	 * @return number of rows be updated
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 */
	public abstract int update( String tables, String selection,
								String[] selectionArgs, Object data )
			throws RecordStoreNotOpenException, InvalidOperationException;

	/**
	 * Commits all of the changes made since the last commit or rollback
	 * of the associated transaction. All locks in the database held by
	 * this record store are also relinquished. Calling this operation
	 * on connection objects in auto-commit mode leads to an error.
	 */
	public abstract void commit();
	
	/**
	 * Check the transaction and begin a new one if necessary
	 * 
	 * @param operation which kind of operation, {@link #code OPERATION_ADD},
	 * 		   {@link #code OPERATION_DELETE}, {@link #code OPERATION_UPDATE},
	 */
	protected abstract void checkAndBeginTransaction( int operation );
	
	/**
	 * Notify the record store that the cursor is closed. This method <b>CAN
	 * ONLY BE AND MUST BE CALLED BY CURSOR</b>.
	 * 
	 * @param cursor cursor to be closed.
	 */
	synchronized void closeCursor(AbstractCursor cursor) {
		cursorSet.remove(cursor);
	}
	
	/**
	 * The method is used to make the cursor managed. The method add and newCursor
	 * of the subclass <b>MUST</b> invoke this method to make the new cursor managed.
	 */
	synchronized protected void manageCursor( AbstractCursor c ) {
		cursorSet.add( c );
	}
	
	/**
	 * Update all the cursors which is marked as being kept notified. 
	 * <p><b>This method MUST be invoked after the change is committed.</b>
	 * 
	 * @param op Which operations were taken. If it is 0, then the operations
	 * 				were not cared. It can be one of the values,
	 * 				{@link #code OPERATION_ADD}, {@link #code OPERATION_DELETE},
	 * 				{@link #code OPERATION_UPDATE}. Or composition of them.
	 * @param id of the record is affected. null, when more than one record is affect
	 * 				or no record is affected at all. When the affected record is not
	 * 				sure, it will be null too.
	 */
	synchronized public void updateCursors( int op, RowId id ) {
		for( AbstractCursor c : cursorSet ) {
			if( c.isKeepUpdated() ) {
				c.updateCursor(op, id);
			}
		}
	}

	protected void checkReadOnly() {
		if( isReadOnly() ) {
			throw new InvalidOperationException(
					"Cannot do modification for readonly database!" );
		}
	}
	
	protected Converter getConverter() {
		return converter;
	}

}
