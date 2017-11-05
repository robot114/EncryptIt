package com.zsm.encryptIt.backup;

import java.util.Hashtable;

import com.zsm.encryptIt.backup.BackupableConst.BACKUPABLES_KEY;
import com.zsm.encryptIt.ui.preferences.Preferences;
import com.zsm.log.Log;

public class BackupTargetFilesConsts {

	/**
	 * The only keys to index the backupable. All hashtables of backup target should be
	 * Hashtable<BACKUP_TARGET_KEY, ?>
	 * 
	 * @author zsm
	 *
	 */
	public enum BACKUP_TARGET_KEY { KEY_KEY, KEY_DATABASE, KEY_ARCHIVED }
	
	static final Hashtable<String, Class<? extends BackupTargetFiles>>
		BACKUP_FILES_CLASS_TABLE
			= new Hashtable<String, Class<? extends BackupTargetFiles>>();
	
	static {
		BACKUP_FILES_CLASS_TABLE.put( 
			Preferences.KEY_BACKUP_MULTI_FILES, MultiBackupTargetFiles.class );
		BACKUP_FILES_CLASS_TABLE.put(
			Preferences.KEY_BACKUP_ARCHIVE, ArchiveBackupTargetFiles.class );
	}

	private static BackupTargetFiles mBackupFiles;

	private BackupTargetFilesConsts() {
		
	}
	
	public static BackupTargetFiles getBackupTargetFilesInstance() {
		String type = Preferences.getInstance().getBackupFilesType();
		Class<? extends BackupTargetFiles> cls = BACKUP_FILES_CLASS_TABLE.get(type);
		if( mBackupFiles == null || !mBackupFiles.getClass().equals( cls )) {
			updateBackupFilesInstance(cls);
		}
		
		return mBackupFiles;
	}

	public static void updateBackupFilesInstance(String type) {
		updateBackupFilesInstance( BACKUP_FILES_CLASS_TABLE.get(type) );
	}

	private static void updateBackupFilesInstance(
							Class<? extends BackupTargetFiles> cls) {
		
		try {
			mBackupFiles = (BackupTargetFiles) cls.newInstance( );
			
		} catch (InstantiationException | IllegalAccessException
				 | NullPointerException | IllegalArgumentException e) {
			
			// type may be null, so cls may be null
			Log.e( e, "Failed to instance the BackupFiles: ", cls );
			if( mBackupFiles == null ) {
				mBackupFiles = getDefaultInstance();
			}
		}
	}
	
	private static BackupTargetFiles getDefaultInstance() {
		return new ArchiveBackupTargetFiles();
	}
	
	public static String composeFileName(String name, String ext) {
		return name + "." + ext;
	}
	
	static BACKUP_TARGET_KEY fromBackupablesKey( BACKUPABLES_KEY key ) {
		return BACKUP_TARGET_KEY.valueOf( key.name() );
	}

}
