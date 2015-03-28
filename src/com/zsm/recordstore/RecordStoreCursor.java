package com.zsm.recordstore;

public abstract class RecordStoreCursor extends AbstractCursor {

	final private RecordStore recordStore;
	
	final protected String tables;
	final protected String[] columns;
	final protected String where;
	final protected String[] whereArgs;

	/*
     * Values returned by {@link #getType(int)}.
     * These should be consistent with the corresponding types defined in CursorWindow.h
     */
	public enum FILED_TYPE { NULL, INTEGER, FLOAT, STRING, BLOB };
    
	protected RecordStoreCursor(RecordStore rs, String tables, String[] columns,
								String where, String[] whereArgs ) {
		super();
		this.tables = tables;
		this.columns = columns;
		this.where = where;
		this.whereArgs = whereArgs;
		recordStore = rs;
	}

	protected RecordStore getRecordStore() {
		return recordStore;
	}
	
	public String getTables() {
		return tables;
	}

	public String[] getColumns() {
		return columns;
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
	}
	
	/**
	 * Check the state of the cursor and its record store
	 */
	@Override
	protected void checkOpenState() {
		super.checkOpenState();
		
		if( getRecordStore() != null && !getRecordStore().isOpen() ) {
			throw new RecordStoreNotOpenException( "RecordStore is not open" );
		}
	}
	
    /**
     * Returns the value of the requested column as a byte array.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null or the column type is not a blob type is
     * implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a byte array.
     */
	public abstract byte[] getBlob(int columnIndex);

    /**
     * Returns the value of the requested column as a String.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null or the column type is not a string type is
     * implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a String.
     */
	public abstract String getString(int columnIndex);
    
    /**
     * Retrieves the requested column text and stores it in the buffer provided.
     * If the buffer size is not sufficient, a new char buffer will be allocated 
     * and assigned to CharArrayBuffer.data
     * @param columnIndex the zero-based index of the target column.
     *        if the target column is null, return buffer
     * @param buffer the buffer to copy the text into. 
     */
	public abstract void copyStringToBuffer(int columnIndex, char[] buffer);
    
    /**
     * Returns the value of the requested column as a short.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null, the column type is not an integral type, or the
     * integer value is outside the range [<code>Short.MIN_VALUE</code>,
     * <code>Short.MAX_VALUE</code>] is implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a short.
     */
	public abstract short getShort(int columnIndex);

    /**
     * Returns the value of the requested column as an int.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null, the column type is not an integral type, or the
     * integer value is outside the range [<code>Integer.MIN_VALUE</code>,
     * <code>Integer.MAX_VALUE</code>] is implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as an int.
     */
	public abstract int getInt(int columnIndex);

    /**
     * Returns the value of the requested column as a long.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null, the column type is not an integral type, or the
     * integer value is outside the range [<code>Long.MIN_VALUE</code>,
     * <code>Long.MAX_VALUE</code>] is implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a long.
     */
	public abstract long getLong(int columnIndex);

    /**
     * Returns the value of the requested column as a float.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null, the column type is not a floating-point type, or the
     * floating-point value is not representable as a <code>float</code> value is
     * implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a float.
     */
	public abstract float getFloat(int columnIndex);

    /**
     * Returns the value of the requested column as a double.
     *
     * <p>The result and whether this method throws an exception when the
     * column value is null, the column type is not a floating-point type, or the
     * floating-point value is not representable as a <code>double</code> value is
     * implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a double.
     */
	public abstract double getDouble(int columnIndex);

    /**
     * Returns data type of the given column's value.
     * The preferred type of the column is returned but the data may be converted to other types
     * as documented in the get-type methods such as {@link #getInt(int)}, {@link #getFloat(int)}
     * etc.
     *<p>
     * Returned column types are
     * <ul>
     *   <li>{@link #FIELD_TYPE_NULL}</li>
     *   <li>{@link #FIELD_TYPE_INTEGER}</li>
     *   <li>{@link #FIELD_TYPE_FLOAT}</li>
     *   <li>{@link #FIELD_TYPE_STRING}</li>
     *   <li>{@link #FIELD_TYPE_BLOB}</li>
     *</ul>
     *</p>
     *
     * @param columnIndex the zero-based index of the target column.
     * @return column value type
     */
	public abstract FILED_TYPE getColumnType(int columnIndex);

}
