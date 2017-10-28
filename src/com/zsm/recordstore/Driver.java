package com.zsm.recordstore;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface Driver {

	/**
	 * Check if the database exists.
	 * 
	 * @param name name of the database
	 * @return true, the database exists; false, the database does not exist.
	 */
	boolean recordStoreExists( String name );

	/**
	 * Delete the database. If the database does not exist, nothing will happen.
	 * 
	 * @param name name of the database
	 */
	void deleteRecordStore(String name);
	
	/**
	 * Copy the database to the new path name. If the database does not exist, 
	 * FileNotFoundException will be thrown.
	 * 
	 * @param name name of the database
	 * @param newPathName path and name of the target file
	 * @return true, move successfully
	 * @throws FileNotFoundException record store with name not found
	 * @throws IOException Other IO error occurred
	 */
	void copyRecordStore(String name, String newPathName)
				throws FileNotFoundException, IOException;
	
	/**
	 * Get the path and name of the record store.
	 * 
	 * @param name name of the record store not including location where it stored
	 * @return path and name of the record store
	 */
	String getRecordStorePathName(String name);
	
	/**
	 * Move the database to the new path name. If the database does not exist, 
	 * FileNotFoundException will be thrown.
	 * 
	 * @param name name of the database
	 * @param newPathName path and name of the target file
	 * @return true, move successfully
	 * @throws FileNotFoundException record store with name not found 
	 */
	boolean moveRecordStore(String name, String newPathName)
				throws FileNotFoundException;
	
	/**
	 * Open a normal database. 
	 * 
	 * @param name name of the database
	 * @param createIfNecessary	whether create the database when it does not exist
	 * @param readOnly the database cannot be modified if this is true
	 * @param ch	method onCreate of this create helper will be invoked, when the database
	 * 				is created
	 * @return instance of the data base
	 */
	RecordStore openRecordStore(String name, boolean createIfNecessary,
								boolean readOnly );

	/**
	 * Close a normal database. 
	 * 
	 * @param rs the recordstore to be closed
	 */
	void closeRecordStore(RecordStore rs);

	/**
	 * Open a raw database. A raw data base is the one that each row has tow columns,
	 * one is the id, the other one is a byte[] type raw data. The structure and the
	 * meaning of the raw data is defined by the caller. 
	 * 
	 * @param name name of the database
	 * @param createIfNecessary	whether create the database when it does not exist
	 * @param readOnly the database cannot be modified if this is true
	 * @param ch	method onCreate of this create helper will be invoked, when the database
	 * 				is created
	 * @return instance of the data base
	 */
	RawRecordStore openRawRecordStore(String name, boolean createIfNecessary,
			   						  boolean readOnly);
	
	/**
	 * Close a raw database.
	 * 
	 * @param rs the recordstore to be closed
	 */
	void closeRawRecordStore(RawRecordStore rs);

}
