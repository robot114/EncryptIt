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
	
	public SQLiteRawOpenHelper(Context context, String name,
							   CursorFactory factory, int version,
							   boolean createIfNecessary ) {
		
		super(context, name, factory, version);
		
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
		db.execSQL( RawRecordStore.CREATE_SQL );
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w( "Upgrade from version " + oldVersion + " to " + newVersion
			   + ". All datas are destroyed!" );
		
		db.execSQL( "DROP TABLE IF EXIST " + RawRecordStore.TABLE_NAME );
		
		onCreate( db );
	}

	@Override
	public synchronized void close() {
		Log.d( "DB Helper closed!" );
		super.close();
	}

}
