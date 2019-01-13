package com.zsm.recordstore.driver.android.sqlite;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.zsm.log.Log;
import com.zsm.recordstore.RawRecordStore;
import com.zsm.recordstore.RecordStoreException;

public class SQLiteRawOpenHelper extends SQLiteOpenHelper {
	
	private static final int VERSION_1 = 1;
	private static final int VERSION_2 = 2;
	private static final int CURRENT_VERSION = VERSION_2;
	
	public SQLiteRawOpenHelper(Context context, String name,
							   CursorFactory factory,
							   boolean createIfNecessary ) {
		
		super(context, name, factory, CURRENT_VERSION);
		
		if( !createIfNecessary && !doesDatabaseExist( context, name ) ) {
			throw new RecordStoreException( "DataBase " + name + " not exist!" );
		}
	}

	static boolean doesDatabaseExist(Context context, String dbName) {
	    File dbFile = context.getDatabasePath(dbName);
	    return dbFile.exists();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL( RawRecordStore.CREATE_RAW_TABLE_SQL );
		createMetaTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if( oldVersion == VERSION_1 && newVersion > VERSION_1 ) {
			// MetaTable is created since version 2
			createMetaTable(db);
		}
		if( newVersion <= VERSION_2 ) {
			Log.d( "Upgraded", "oldVersion", oldVersion, "newVersion", newVersion );
			// No more need to be done for version 2
			return;
		}
		Log.w( "Upgrade from version " + oldVersion + " to " + newVersion
			   + ". All datas are destroyed!" );
		
		deleteAllTables(db);
		
		onCreate( db );
	}

	private void deleteAllTables(SQLiteDatabase db) {
		db.execSQL( "DROP TABLE IF EXISTS " + RawRecordStore.RAW_DATA_TABLE_NAME );
		deleteMetaTable(db);
	}

	private void deleteMetaTable(SQLiteDatabase db) {
		db.execSQL( "DROP TABLE IF EXISTS " + RawRecordStore.META_DATA_TABLE_NAME );
	}

	private void createMetaTable( SQLiteDatabase db ) {
		db.execSQL( RawRecordStore.CREATE_META_TABLE_SQL );
		Log.d( "Meta data table created." );
	}

	@Override
	public synchronized void close() {
		Log.d( "DB Helper closed!" );
		super.close();
	}

}
