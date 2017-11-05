package com.zsm.encryptIt.backup;

import java.util.Hashtable;

import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.log.Log;
import com.zsm.security.KeyManager;

public class BackupableConst {

	/**
	 * The only keys to index the backupable. All hashtables of backupables should be
	 * Hashtable<BACKUPABLES_KEY, ?>
	 * 
	 * @author zsm
	 *
	 */
	public enum BACKUPABLES_KEY { KEY_KEY, KEY_DATABASE }
	
	private static Hashtable<BACKUPABLES_KEY, Backupable> BACKUPABLES;;
	
	private BackupableConst() {
	}
	
	static Hashtable<BACKUPABLES_KEY, Backupable>
				getBackupables( EncryptItApplication app ) {
		
		if( BACKUPABLES == null ) {
			BACKUPABLES
				= new Hashtable<BACKUPABLES_KEY, Backupable>(
									BACKUPABLES_KEY.values().length );
			
			BACKUPABLES.put( BACKUPABLES_KEY.KEY_KEY, KeyManager.getInstance() );
		}

		BACKUPABLES.put(
				BACKUPABLES_KEY.KEY_DATABASE,
				app.getItemListController().getItemStorageAdapter() );
		
		return BACKUPABLES;
	}

	public static boolean undoRestore(EncryptItApplication app) {
		Hashtable<BACKUPABLES_KEY, Backupable> backupables
				= BackupableConst.getBackupables( app );
		
		boolean undo = true;
		for( BACKUPABLES_KEY key : BACKUPABLES_KEY.values() ) {
			Backupable backupable = backupables.get(key);
			try {
				if( !backupable.restoreFromLocalBackup() ) {
					undo = false;
				}
			} catch (Exception e) {
				Log.w(e, "Restore from the renamed locally failed: ", backupable);
				undo = false;
			}
		}
		
		// Do not reopen the backupable untill all are restored
		for( BACKUPABLES_KEY key : BACKUPABLES_KEY.values() ) {
			Backupable backupable = backupables.get(key);
			try {
				backupable.reopen();
			} catch (Exception e) {
				Log.w(e, "Reopen from the renamed locally failed: ", backupable);
				undo = false;
			}
		}
		
		return undo;
	}
	
	public static String toString(
			Hashtable<BACKUPABLES_KEY, Backupable> backupables, String deli ) {
		
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for( BACKUPABLES_KEY key : BACKUPABLES_KEY.values() ) {
			if( !first ) {
				builder.append( deli );
			}
			first = false;
			builder.append( backupables.get(key).displayName() );
		}
		
		return builder.toString();
	}

}
