package com.zsm.encryptIt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.zsm.log.Log;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.persistence.Persistence;
import com.zsm.util.file.FileUtility;

/**
 * Although, the data of this application in the persistence is encrypted, but
 * the {@code EncryptItPersistence} is considered as a plain persistence.
 * That means the users themselves should decrypt the data after reading and 
 * encrypt the data before writing. Thus can make the remote reading and writing
 * safe. And it also make the data passed between applications safe.
 * 
 * <p>This can be changed by setting the stream decorator with 
 * {@link #code setStreamDecorator}. Some subclass such as 
 * {@link #code AndroidPersistence} enclose the invocation of
 * {@link #code setStreamDecorator} in the constructor.
 * 
 * @author zsm
 *
 */
public class EncryptItPersistence extends Persistence {

	private static final String DATABASE_NAME = "encrypt_it.db";
	// these variables are used to verify the file backup to and restore from
	private static final byte[] MAGIC = "zsm.EncriptyIt.1412".getBytes();
	// these are used to future format changed for backup file
	private static final long VERSION = 0x0001000000000001L;
	private static final byte[] OPTIONS = new byte[32];
	
	public EncryptItPersistence( ) {
		super(DATABASE_NAME, MAGIC, VERSION, OPTIONS);
	}

	@Override
	public void open() throws BadPersistenceFormatException, IOException {
		try {
			super.open();
		} catch (BadPersistenceFormatException e) {
			Log.w( "Check the persistence failed, backup the old one,"
						   + " and create a new one");
			backup();
			clear();
			super.open();
		}
	}
	
	private void backup() {
		String backupName = getFullPathName() + ".bak";
		File bakFile = new File( backupName ).getAbsoluteFile();
		if( bakFile.exists() ) {
			bakFile.delete();
		}
		boolean renameOk = false;
		try {
			renameOk = renameTo( backupName );
		} catch (FileNotFoundException e) {
			Log.w( e, "Rename persistence failed, try to copy" );
		}
		if( !renameOk ) {
			try {
				FileUtility.copyFile( getFullPathName(), backupName );
			} catch (Exception e) {
				Log.w( e, "Cannot copy either, just over write it." );
			}
		}
	}
}
