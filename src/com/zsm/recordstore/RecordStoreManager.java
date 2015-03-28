package com.zsm.recordstore;

import java.io.FileNotFoundException;

public class RecordStoreManager {

	private static final RecordStoreManager instance = new RecordStoreManager();
	
	private Driver defaultDriver;
	
	static public RecordStoreManager getInstance() {
		return instance;
	}
	
	public void setDefaultDriver( Driver d ) {
		defaultDriver = d;
	}
	
	public boolean recordStoreExist( String name ) {
		checkDefaultDriver();
		return defaultDriver.recordStoreExists(name);
	}

	public boolean rename(String name, String newFullPathName)
				throws FileNotFoundException {
		checkDefaultDriver();
		return defaultDriver.moveRecordStore( name, newFullPathName );
	}

	public void deleteRecordStore( String name ) {
		checkDefaultDriver();
		defaultDriver.deleteRecordStore( name );
	}

	public String getFullPathName(String name) {
		checkDefaultDriver();
		return defaultDriver.getRecordStorePathName( name );
	}
	
	public RawRecordStore openRawRecordStore( String name,
			  								  boolean createIfNecessary,
			  								  boolean readOnly ) {

		checkDefaultDriver();
		return defaultDriver.openRawRecordStore( name, createIfNecessary, false );
	}

	private void checkDefaultDriver() {
		if( defaultDriver == null ) {
			throw new RecordStoreException(
					"Register the default driver with registerDriver first!" );
		}
	}
	
	public RawRecordStore openRawRecordStore( String name,
			  								  boolean createIfNecessary ) {

		return openRawRecordStore( name, createIfNecessary, false );
	}

	public RawRecordStore openRawRecordStore( String name ) {
		return openRawRecordStore( name, true );
	}
}
