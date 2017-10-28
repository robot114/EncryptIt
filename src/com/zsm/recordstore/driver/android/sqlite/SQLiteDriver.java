package com.zsm.recordstore.driver.android.sqlite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.zsm.log.Log;
import com.zsm.recordstore.Driver;
import com.zsm.recordstore.RawRecordStore;
import com.zsm.recordstore.RecordStore;
import com.zsm.util.file.FileUtilities;

public class SQLiteDriver implements Driver {

	private static final int VERSION = 1;
	
	private Context context;

	public SQLiteDriver( Context context ) {
		this.context = context;
	}
	
	@Override
	public String getRecordStorePathName(String name) {
		return getRecordStoreFile(name).getAbsolutePath();
	}

	@Override
	public boolean recordStoreExists(String name) {
		return SQLiteRawOpenHelper.doesDatabaseExist(context, name);
	}

	@Override
	public void deleteRecordStore(String name) {
	    getRecordStoreFile(name).delete();
	}

	@Override
	public void copyRecordStore(String name, String target)
			throws FileNotFoundException, IOException {
		
		File src = getRecordStoreFile(name);
		FileUtilities.copyFile( src.getPath() + src.getName(), target );
	}
	
	@Override
	public boolean moveRecordStore(String name, String newPathName)
			throws FileNotFoundException {
		
		if( !SQLiteRawOpenHelper.doesDatabaseExist(context, name) ) {
			throw new FileNotFoundException( name + " not found!" );
		}
		File old = getRecordStoreFile(name);
		File target = new File( newPathName );
		target.delete();
		
		return old.renameTo(target);
	}
	
	@Override
	public RawRecordStore openRawRecordStore( String name,
											  boolean createIfNecessary,
											  boolean readOnly ) {
		
		SQLiteOpenHelper helper
			= new SQLiteRawOpenHelper( context, name, null, VERSION,
									   createIfNecessary );
		
		RawRecordStore rs = null;
		if(readOnly) {
			rs = new SQLiteRawRecordStore( helper.getReadableDatabase(),
										   readOnly );
		} else {
			rs = new SQLiteRawRecordStore( helper.getWritableDatabase(),
										   readOnly );
		}
		
		Log.d( "RecordStore opened: ", rs, "name", name );
		return rs;
	}

	@Override
	public RecordStore openRecordStore( String name, boolean createIfNecessary,
										boolean readOnly ) {
		
		return null;
	}

	private File getRecordStoreFile(String name) {
		return context.getDatabasePath(name).getAbsoluteFile();
	}

	@Override
	public void closeRecordStore(RecordStore rs) {
		// TODO Auto-generated method stub
		rs.close();
		Log.d( "RecordStore closed: ", rs );
	}

	@Override
	public void closeRawRecordStore(RawRecordStore rs) {
		// TODO Auto-generated method stub
		rs.close();
		Log.d( "RecordStore closed: ", rs );
	}

}
