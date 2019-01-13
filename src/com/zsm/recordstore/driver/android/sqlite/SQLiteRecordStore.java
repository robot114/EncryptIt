package com.zsm.recordstore.driver.android.sqlite;

import java.sql.RowId;

import com.zsm.log.Log;
import com.zsm.recordstore.InvalidOperationException;
import com.zsm.recordstore.LongRowId;
import com.zsm.recordstore.RecordStore;
import com.zsm.recordstore.RecordStoreCursor;
import com.zsm.recordstore.RecordStoreException;
import com.zsm.recordstore.RecordStoreNotOpenException;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteRecordStore extends RecordStore {

	private SQLiteDatabase database;
	private int operation;

	SQLiteRecordStore( SQLiteDatabase db, boolean readOnly ) {
		// Explicitly requested for a read only database or the concrete database
		// being read only, all these make the record store is read only.
		super( readOnly || db.isReadOnly() );
		database = db;
	}
	
	@Override
	public SQLiteDatabase getDatabase() {
		return database;
	}

	@Override
	public int getVersion() {
		return database.getVersion();
	}
	
	@Override
	public void close() throws RecordStoreException {
		commit();
		super.close();
		database.close();
	}

	@Override
	public boolean isOpen() {
		return database.isOpen();
	}

	/**
	 * @param columns is useless for this method in SQLiteRawRecordStore,
	 * 					 as the columns are fixed
	 * @param parameter is useless for this method in SQLiteRawRecordStore
	 */
	@Override
	public RecordStoreCursor newCursor( String tables, String[] columns,
			  				 			String selection, String[] selectionArgs,
			  				 			String groupBy, String having,
			  				 			String orderBy, Object parameter )
			throws RecordStoreNotOpenException {
		
		if( !isOpen() ) {
			throw new RecordStoreNotOpenException( "RecordStore is not opened!" );
		}
		
		RecordStoreCursor c
			= new SQLiteDriverCursor( this, tables, columns,
									  selection, selectionArgs, groupBy,
									  having, orderBy );
		
		manageCursor( c );
		return c;
	}

	@Override
	public int remove(String table, String selection, String[] selectionArgs)
			throws RecordStoreNotOpenException, InvalidOperationException {

		checkReadOnly();
		
		checkAndBeginTransaction( RecordStore.OPERATION_DELETE );
		int num = database.delete(table, selection, selectionArgs );
		Log.d( "Record with specified id removed.", "cursor", table,
			   "removed record num", num );
		
		return num;
	}

	@Override
	public RowId add( String table, Object data )
			throws RecordStoreNotOpenException, InvalidOperationException {
		
		checkReadOnly();
		
		ContentValues values = (ContentValues) getConverter( table ).convert( data );
		checkAndBeginTransaction( RecordStore.OPERATION_ADD );
		long id = database.insert(table, null, values);
		Log.d( "New values added.", "id", id, "values", values );
		
		return new LongRowId( id );
	}

	@Override
	public int update(String tables, String selection, String[] selectionArgs, Object data)
			throws RecordStoreNotOpenException, InvalidOperationException {
		
		return database.update(tables,
							   (ContentValues)getConverter( tables ).convert(data),
							   selection,
							   selectionArgs );
	}

	@Override
	protected void checkAndBeginTransaction( int op ) {
		if( !database.inTransaction() ) {
			database.beginTransaction();
			operation = 0;
			Log.d( "Transaction began!" );
		}
		operation |= op;
	}

	@Override
	public void commit() {
		if( database.inTransaction() ) {
			database.setTransactionSuccessful();
			database.endTransaction();
			Log.d( "Transaction commit!" );
			
			// Update the cursor if necessary
			updateCursors( operation, null );
			operation = 0;
		}
	}

}
