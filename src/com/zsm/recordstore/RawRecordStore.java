package com.zsm.recordstore;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.sql.RowId;
import java.util.HashMap;

import android.support.annotation.NonNull;

import com.zsm.log.Log;
import com.zsm.util.Converter;
import com.zsm.util.NumberUtil;

/**
 * This abstract class represents a raw records set in which all the records match the
 * condition given when it is opened. A raw record store is the one, in which each
 * row has only two item, the id and the data. How to explain the meaning of the
 * data is coded in the client. 
 * Each row has a unique identification assigned by the record store. The id will
 * not change once the record created. The id has no actual meaning. The records
 * are not ordered by the ids. And the id is different to the index of the record. 
 * 
 * @author zsm
 *
 */
public abstract class RawRecordStore implements Closeable {

	public final static String META_DATA_TABLE_NAME = "meta_data";
	public final static String RAW_DATA_TABLE_NAME = "raw_data";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_DATA = "data";
	public static final String COLUMN_CREATE = "created";
	public static final String COLUMN_MODIFY = "modified";
	public static final String COLUMN_KEY = "key";
	
	static final String RAW_COLUMNS_ALL
			= COLUMN_ID + ", " + COLUMN_DATA + ", " + COLUMN_CREATE
				+ ", " + COLUMN_MODIFY;
	protected static final String[] RAW_COLUMNS
		= { COLUMN_ID, COLUMN_DATA, COLUMN_CREATE, COLUMN_MODIFY };

	public final static String CREATE_RAW_TABLE_SQL
		= "create table " + RAW_DATA_TABLE_NAME + " ("
		  + COLUMN_ID + " integer primary key autoincrement, "
		  + COLUMN_CREATE + " datetime, "
		  + COLUMN_MODIFY + " datetime, "
		  + COLUMN_DATA + " VARBINARY)";
	
	static final String META_COLUMNS_ALL = COLUMN_KEY + ", " + COLUMN_DATA;
	static final String[] META_COLUMNS = { COLUMN_KEY, COLUMN_DATA };
	
	public final static String CREATE_META_TABLE_SQL
					= "create table if not exists " + META_DATA_TABLE_NAME + " ("
					  + COLUMN_KEY + " text primary key, "
					  + COLUMN_DATA + " VARBINARY)";
	
	private final static String KEY_DATA_VERSION = "DataVersion";
	private final static int DEFAULT_DATA_VERSION = 1;
	
	private static final String SELECTION_KEY = COLUMN_KEY + " = ?";
	
	final private RecordStore recordStore;

	private boolean closeTogether;
	
	/**
	 * Construct the raw record store with a record store. The record store MUST 
	 * have a table names as {@link #code TABLE_NAME}.
	 * And the table MUST be created by the SQL statement {@link #code CREATE_SQL}. 
	 * 
	 * @param rs, the inner record restore with the special structure.
	 * @param closeTogether, whether close the inner record store when the raw
	 * 		  record store close.
	 * @return the generated record store
	 */
	public RawRecordStore( @NonNull RecordStore rs, boolean closeTogether ) {
		recordStore = rs;
		this.closeTogether = closeTogether;
		initConvertor();
	}
	
	/**
	 * Construct the raw record store with a record store. The record store MUST 
	 * have a table names as {@link #code TABLE_NAME}.
	 * And the table MUST be created by the SQL statement {@link #code CREATE_SQL}.
	 * <p>The inner record store will be closed when the raw record store closes
	 * 
	 * @param rs, the inner record restore with the special structure.
	 * @param closeTogether, whether close the inner record store when the raw
	 * 		  record store close.
	 * @return the generated record store
	 */
	public RawRecordStore( @NonNull RecordStore rs ) {
		this( rs, true );
	}
	
	protected RawRecordStore(Object db, boolean readOnly) {
		recordStore = generateRecordStore( db, readOnly );
		closeTogether = true;
		initConvertor();
	}
	
	private void initConvertor() {
		recordStore.addConverter(RAW_DATA_TABLE_NAME, getConverter(RAW_DATA_TABLE_NAME));
		recordStore.addConverter(META_DATA_TABLE_NAME, getConverter(META_DATA_TABLE_NAME));
	}

	/**
	 * Generate the record store for this raw record store. The generated 
	 * record store will only have a table named as {@link #code TABLE_NAME}.
	 * And the table will be created by the SQL statement {@link #code CREATE_SQL}.
	 * 
	 * @param db the concrete database instance
	 * @param readOnly, whether the record store is read only.
	 * @return the generated record store
	 */
	protected abstract RecordStore generateRecordStore(Object db, boolean readOnly);

	/**
	 * Is this record store read only. For the read only record store, rows 
	 * <b>CANNOT</b> be deleted or modified.
	 * 
	 * @return true, if the record store is read only
	 */
	public boolean isReadOnly() {
		return recordStore.isReadOnly();
	}

	@Override
	public void close() {
		recordStore.commit();
		if( closeTogether && recordStore.isOpen() ) {
			recordStore.close();
		}
	}
	
	public boolean isOpen() {
		return recordStore.isOpen();
	}
	
	public int getDataVersion() {
		return getIntMetaData( KEY_DATA_VERSION, DEFAULT_DATA_VERSION );
	}

	public void upgradeDataVersion( int newVersion ) {
		int version = getDataVersion();
		if( version > newVersion ) {
			throw new RecordStoreException( "New data version (" + newVersion
								+ ") less than the current version ("
								+ version + ")!" );
		}
		putMetaData(KEY_DATA_VERSION, newVersion);
	}
	
	public AbstractMetaDataCursor queryMetaData( String key ) {
		
		RecordStoreCursor cursor
			= recordStore.newCursor( META_DATA_TABLE_NAME, META_COLUMNS,
								 	 SELECTION_KEY, new String[]{ key },
								 	 null,/*group by*/ null/* having */,
								 	 COLUMN_DATA + " DESC", null );
		
		if( cursor != null && cursor.getCount() < 1 ) {
			cursor.close();
			cursor = null;
		}
		
		if( cursor != null ) {
			return new RawRecordStoreMetaCursor( this, cursor, true );
		}
		return null;
	}

	public byte[] getMetaData( String key ) {
		AbstractMetaDataCursor cursor = queryMetaData( key );
		if( cursor == null ) {
			return null;
		}
		
		byte[] data = cursor.getData();
		cursor.close();
		
		return data;
	}
	
	public int getIntMetaData( String key, int defaultValue ) {
		AbstractMetaDataCursor cursor = queryMetaData( key );
		if( cursor == null ) {
			return defaultValue;
		}
		
		byte[] data = cursor.getData();
		cursor.close();
		if( data == null || data.length < 1 ) {
			return defaultValue;
		}
		
		return NumberUtil.byteArrayToInt(data);
	}
	
	public void putMetaData( String key, int value ) {
		byte[] bytes = NumberUtil.intToByteArray(value);
		putMetaData(key, bytes);
	}

	public void putMetaData(String key, byte[] bytes) {
		AbstractMetaDataCursor cursor = queryMetaData( key );
		
		HashMap<String, byte[]> data = new HashMap<String, byte[]>();
		data.put(key, bytes );
		if( cursor == null ) {
			recordStore.add(META_DATA_TABLE_NAME, data);
		} else {
			cursor.close();
			recordStore.update( META_DATA_TABLE_NAME, SELECTION_KEY,
							   	new String[] { key }, bytes );
		}
	}
	
	/**
	 * Remove the record with the specified id. If the record does not exist,
	 * nothing will happen.
	 * 
	 * @param id the record with this id will be removed
	 * @return count of records removed. It should be 0 or 1 here.
	 * @throws RecordStoreNotOpenException if the record store is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 */
	public int remove(RowId id)
			throws RecordStoreNotOpenException, InvalidOperationException {
		
		checkReadOnly();
		
		checkAndBeginTranscation( RecordStore.OPERATION_DELETE );
		String where = COLUMN_ID + "=" + id;
		int num = recordStore.remove(RawRecordStore.RAW_DATA_TABLE_NAME, where, null );
		recordStore.commit();
		Log.d( "Record with specified id removed.", "id", id,
			   "removed record num", num );
		
		return num;
	}

	/**
	 * Remove the record at the cursor. If the record does not exist,
	 * nothing will happen.
	 * 
	 * @param cursor at which the record will be removed
	 * @return count of records removed. It should be 0 or 1 here.
	 * @throws RecordStoreNotOpenException if the record store is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 */
	public int remove( AbstractRawCursor cursor )
			throws RecordStoreNotOpenException, InvalidOperationException {
		
		return remove( cursor.currentId() );
	}

	/**
	 * Add a new row and return a cursor points to the new row. <b>The cursor may
	 * be limited to just the new row. That means it cannot move.</b>
	 * 
	 * <p><b>CAUTION: This method of the subclass MUST invoke the method 
	 * newCursor( Cursor ) to make the new cursor managed.</b>
	 * 
	 * @param data data to be added into the table
	 * @return cursor points to the new row
	 * @throws RecordStoreNotOpenException if the record store is closed
	 * @throws InvalidOperationException If the record store is read only.
	 */
	public AbstractRawCursor add( byte[] data )
			throws RecordStoreNotOpenException, InvalidOperationException {
		
		checkAndBeginTranscation( RecordStore.OPERATION_ADD );
		RowId id = recordStore.add(RAW_DATA_TABLE_NAME, data );
		recordStore.commit();
		
		return cursorForId(id);
	}

	/**
	 * Update the record at the cursor. If the record does not exist,
	 * InvalidOperationException should be thrown. 
	 * 
	 * @param cursor at which the record will be updated
	 * @param data values to update. The structure and the meaning are
	 * 				 implementation defined.
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 */
	public void update( AbstractRawCursor cursor, Object data )
			throws RecordStoreNotOpenException, InvalidOperationException {
		
		update( cursor.currentId(), data );
	}

	/**
	 * Update the record with the special id. If the record does not exist,
	 * InvalidOperationException should be thrown. 
	 * 
	 * @param id with which the record will be updated
	 * @param data values to update. The structure and the meaning are
	 * 				 implementation defined.
	 * @return number of rows be updated
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 * @throws NoSuchRecordException if the record with the id does not exist
	 */
	public int update( RowId id, Object data )
			throws RecordStoreNotOpenException, InvalidOperationException,
					NoSuchRecordException {
		checkReadOnly();
		
		checkAndBeginTranscation( RecordStore.OPERATION_UPDATE );
		String where = COLUMN_ID + "=" + id;
		int num = recordStore.update(RawRecordStore.RAW_DATA_TABLE_NAME, where, null,
									 getConverter(RAW_DATA_TABLE_NAME).convert(data) );
		recordStore.commit();
		Log.d( "Record with specified id removed.", "id", id,
			   "removed record num", num );
		
		return num;
	}

	/**
	 * Get an InputStream for a current cursor. The input stream will stand at the
	 * original point if the cursor moves. The input stream gotten by this method
	 * will <b>NOT</b> check the state of the record store or the record at the
	 * cursor. So if the record store closed or the record removed, the reading 
	 * behavior of this input stream is defined by the implementation of the driver.
	 * Whether the modification of the record will be notified to the
	 * input stream is defined by the implementation of the driver.
	 * 
	 * @param cursor by which the input stream returned
	 * @return an instance of DataInputStream or an instance of its subclass
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 */
	public RecordStoreInputStream getInputStream(AbstractRawCursor cursor)
			throws RecordStoreNotOpenException {
		
		return new RecordStoreInputStream( getArrayInputStream(cursor), cursor );
	}

	/**
	 * Get an InputStream for a row with special id. The input stream gotten by
	 * this method will <b>NOT</b> check the state of the record store. So if the
	 * record store closed or the record removed, the reading behavior of this
	 * input stream is defined by the implementation of the driver.
	 * Whether the modification of the record will be notified to the
	 * input stream is defined by the implementation of the driver.
	 * 
	 * @param id the input stream of the record with this id returned
	 * @return an instance of DataInputStream or an instance of its subclass
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws NoSuchRecordException if the record with this id does not exist
	 */
	public RecordStoreInputStream getInputStream(RowId id)
			throws RecordStoreNotOpenException, NoSuchRecordException {
		
		AbstractRawCursor c = cursorForId(id);
		return new RecordStoreInputStream( getArrayInputStream(c), c, true );
	}

	/**
	 * Get a SAFE InputStream for a current cursor. The input stream will stand at the
	 * original point if the cursor moves. If the record store is closed, a 
	 * RecordStoreNotOpenException will be thrown for any reading by this input stream.
	 * If the record of the input stream removed, a NoSuchRecordException will be
	 * thrown for any reading. Whether the modification of the record will be
	 * notified to the input stream is defined by the implementation of the driver.
	 * 
	 * @param cursor by which the input stream returned
	 * @return an instance of RecordStoreSafeInputStream or an instance of its subclass
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 */
	public RecordStoreSafeInputStream getSafeInputStream(AbstractRawCursor cursor)
			throws RecordStoreNotOpenException {
		
		return new RecordStoreSafeInputStream( getArrayInputStream(cursor),
											   recordStore, cursor);
	}

	/**
	 * Get a SAFE InputStream for a record with special id. If the record store
	 * is closed, a RecordStoreNotOpenException will be thrown for any reading
	 * by this input stream. If the record of the input stream removed, a
	 * NoSuchRecordException will be thrown for any reading. Whether the 
	 * modification of the record will be notified to the input stream is 
	 * defined by the implementation of the driver.
	 * 
	 * @param id the input stream of the record with this id returned
	 * @return an instance of RecordStoreSafeInputStream or an instance of its subclass
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws NoSuchRecordException if the record with this id does not exist
	 */
	public RecordStoreSafeInputStream getSafeInputStream(RowId id)
			throws RecordStoreNotOpenException, NoSuchRecordException {
		
		AbstractRawCursor c = cursorForId(id);
		return new RecordStoreSafeInputStream( getArrayInputStream(c),
											   recordStore, c, true );
	}

	/**
	 * Get an OutputStream for a current cursor. The output stream will stand at the
	 * original point if the cursor moves. The output stream gotten by this method
	 * will <b>NOT</b> check the state of the record store or the record at the
	 * cursor. So if the record store closed or the record removed, the writing 
	 * behavior of this output stream is defined by the implementation of the driver.
	 * Whether the modification of the record will be notified to the
	 * output stream is defined by the implementation of the driver.
	 * 
	 * @param cursor by which the output stream returned
	 * @return an instance of RecordStoreOutputStream or an instance of its subclass
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 */
	public RecordStoreOutputStream getOutputStream(AbstractRawCursor cursor)
			throws RecordStoreNotOpenException, InvalidOperationException {
		
		checkReadOnly();
		
		return new RecordStoreOutputStream( this, cursor.currentId() );
	}
	/**
	 * Get an OutputStream for the record with special id. The output stream 
	 * gotten by this method will <b>NOT</b> check the state of the record 
	 * store or the record at the cursor. So if the record store closed or 
	 * the record removed, the writing behavior of this output stream is defined
	 * by the implementation of the driver. Whether the modification of the 
	 * record will be notified to the output stream is defined by the 
	 * implementation of the driver.
	 * 
	 * @param id the input stream of the record with this id returned
	 * @return an instance of RecordStoreOutputStream or an instance of its subclass
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 * @throws NoSuchRecordException if the record with this id does not exist
	 */
	public RecordStoreOutputStream getOutputStream(RowId id)
			throws RecordStoreNotOpenException, InvalidOperationException,
					NoSuchRecordException {
		
		return new RecordStoreOutputStream( this, id );
	}

	/**
	 * Get a SAFE OutputStream for a current cursor. The output stream will stand at the
	 * original point if the cursor moves. If the record store is closed, a 
	 * RecordStoreNotOpenException will be thrown for any writing to this stream.
	 * If the record of the stream removed, a NoSuchRecordException will be
	 * thrown for any writing. Whether the modification of the record will be
	 * notified to the stream is defined by the implementation of the driver.
	 * 
	 * @param cursor by which the output stream returned
	 * @return an instance of RecordStoreOutputStream or an instance of its subclass
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 */
	public RecordStoreOutputStream getSafeOutputStream(AbstractRawCursor cursor)
			throws RecordStoreNotOpenException, InvalidOperationException {
		
		checkReadOnly();
		return new RecordStoreSafeOutputStream( this, cursor.currentId() );
	}

	/**
	 * Get a SAFE OutputStream for a record with special id. If the record store
	 * is closed, a RecordStoreNotOpenException will be thrown for any writing 
	 * to this stream. If the record of the stream removed, a NoSuchRecordException
	 * will be thrown for any writing. Whether the modification of the record will be
	 * notified to the stream is defined by the implementation of the driver.
	 * 
	 * @param id the input stream of the record with this id returned
	 * @return an instance of RecordStoreOutputStream or an instance of its subclass
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 * @throws InvalidOperationException If the record store is read only.
	 * @throws NoSuchRecordException if the record with this id does not exist
	 */
	public RecordStoreOutputStream getSafeOutputStream( RowId id )
			throws RecordStoreNotOpenException, InvalidOperationException,
					NoSuchRecordException {
		
		return new RecordStoreSafeOutputStream( this, id );
	}

	protected abstract Converter getConverter( String tableName );
	
	/**
	 * Get a cursor of a record with the id.
	 * 
	 * @param id of the record
	 * @return the cursor
	 * @throws RecordStoreNotOpenException if the record store is closed
	 */
	public AbstractRawCursor newCursor( RowId id )
				throws RecordStoreNotOpenException {
		
		return newCursor( id == null ? null : COLUMN_ID + "=" + id, null, null,
						  null, null, null );
	}
	
	/**
	 * Generate a cursor instance. Each cursor generated from the same record store
	 * is independent to the others. When the record store is closed, the cursors
	 * are invalid. The modification of the RecordStore will affect the the cursor.
	 * For example, the moveToNext method will return false, when the cursor points
	 * to the record just before the last one, and the last one is removed.</br>
	 * 
	 * <p>The cursor should be closed when it is useless.
	 * 
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
	public AbstractRawCursor newCursor( String selection,
										   String[] selectionArgs,
	        						       String groupBy,
	        						       String having,
	        						       String orderBy,
	        						       Object parameter )
			throws RecordStoreNotOpenException {
				
		return new RawRecordStoreCursor( this, selection, selectionArgs,
										 groupBy, having, orderBy, parameter );
	}
	
	protected RecordStore getRecordStore() {
		return recordStore;
	}

	void closeCursor(RawRecordStoreCursor cursor) {
		recordStore.closeCursor(cursor.getInnerCursor());
	}

	void closeCursor(RawRecordStoreMetaCursor cursor) {
		recordStore.closeCursor(cursor.getInnerCursor());
	}

	protected void checkReadOnly() {
		if( isReadOnly() ) {
			throw new IllegalStateException( "Record store is read only!" );
		}
	}

	protected void checkAndBeginTranscation( int op ) {
		recordStore.checkAndBeginTransaction(op);
	}
	
	private ByteArrayInputStream getArrayInputStream(AbstractRawCursor cursor) {
		
		byte[] data = cursor.getData();
		
		ByteArrayInputStream bais
			= new ByteArrayInputStream( data, 0, data.length );
		return bais;
	}

	private AbstractRawCursor cursorForId(RowId id) {
		AbstractRawCursor c = newCursor(id);
		c.moveToFirst();
		return c;
	}

}
