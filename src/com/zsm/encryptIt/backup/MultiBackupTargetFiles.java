package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;

import com.zsm.encryptIt.backup.BackupTargetFilesConsts.BACKUP_TARGET_KEY;
import com.zsm.encryptIt.backup.BackupableConst.BACKUPABLES_KEY;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.FileUtilities;

public class MultiBackupTargetFiles extends BackupTargetFiles {

	private static final String DB_EXT = "db";
	private static final String KEY_EXT = "key";
	private static final String[] BD_KEY_FILTER = new String[]{DB_EXT, KEY_EXT};
	private static final String[] MIME_TYPES
					= { "application/" + DB_EXT, "application/" + KEY_EXT };
	private static final FileExtensionFilter[] EXT_FILTER
					= { new FileExtensionFilter(BD_KEY_FILTER, "database and key") };
	private static final Set<BACKUP_TARGET_KEY> KEY_SET
					= new HashSet<BACKUP_TARGET_KEY>();
	private static final Hashtable<BACKUP_TARGET_KEY, String> SRC_TO_TARGET_EXTS_MAP
					= new Hashtable<BACKUP_TARGET_KEY, String>();
	private static final Hashtable<BACKUPABLES_KEY, String> EXTS
					= new Hashtable<BACKUPABLES_KEY, String>();
	static {
		KEY_SET.add( BACKUP_TARGET_KEY.KEY_KEY );
		KEY_SET.add( BACKUP_TARGET_KEY.KEY_DATABASE );
		
		SRC_TO_TARGET_EXTS_MAP.put( BACKUP_TARGET_KEY.KEY_KEY, KEY_EXT );
		SRC_TO_TARGET_EXTS_MAP.put( BACKUP_TARGET_KEY.KEY_DATABASE, DB_EXT );
		
		EXTS.put( BACKUPABLES_KEY.KEY_KEY, KEY_EXT );
		EXTS.put( BACKUPABLES_KEY.KEY_DATABASE, DB_EXT );
	}

	@Override
	Set<BACKUP_TARGET_KEY> getTargetKeySet() {
		return KEY_SET;
	}

	@Override
	public Hashtable<BACKUP_TARGET_KEY, String> getTargetExtsMap() {
		return SRC_TO_TARGET_EXTS_MAP;
	}

	@Override
	BACKUP_TARGET_KEY getTargetKeyFromBackupableKey(BACKUPABLES_KEY key) {
		return BackupTargetFilesConsts.fromBackupablesKey(key);
	}

	@Override
	public Hashtable<BACKUPABLES_KEY, String> getSrcToTargetExtsMap() {
		return EXTS;
	}

	@Override
	public boolean isToMultiFiles() {
		return true;
	}

	@Override
	public String[] getMimeTypes() {
		return MIME_TYPES;
	}

	@Override
	public FileExtensionFilter[] getExtensionFilter() {
		return EXT_FILTER;
	}

	@Override
	public String normalizeFileName(String fileName) {
		if( acceptedByExtensionFilter(fileName) ) {
			return FileUtilities.removeExtension(fileName);
		}
		return fileName;
	}

	@Override
	protected boolean validInputStream(InputStream in) {
		return MagicHeader.checkHeader(in);
	}

	@Override
	public BackupOutputStream openOutputStream(Context context,
											   BACKUPABLES_KEY key,
										 	   Uri targetUri, String prefix,
										 	   char[] password,
										 	   boolean firstForThisTask)
						throws IOException, GeneralSecurityException {
		
		ContentResolver cr = context.getContentResolver();
		String fn
			= BackupTargetFilesConsts.composeFileName(
					prefix, getSrcToTargetExtsMap().get(key) );
		
		Uri uri
			= DocumentsContract.createDocument( cr, targetUri, MIME_TYPE, fn );
		
		OutputStream out = wrapByPassword(password, cr.openOutputStream(uri) );
		MagicHeader.outputHeader(out);
		
		return new BackupOutputStream( out ) {

			@Override
			void close(CLOSE_TYPE closeType) throws IOException {
				getOutputStream().close();
			}
			
		};
	}

	@Override
	public InputStream openInputStream(Context context, BACKUPABLES_KEY key,
			   						   DocumentFile backupTarget, char[] password)
						throws IOException, GeneralSecurityException {
		
		ContentResolver cr = context.getContentResolver();
		InputStream in
			= wrapByPassword(password, cr.openInputStream(backupTarget.getUri()) );
		
		if( !MagicHeader.checkHeader(in) ) {
			in.close();
			return null;
		}
		
		return in;
	}

	@Override
	public long getTotalSize(Hashtable<BACKUP_TARGET_KEY, DocumentFile> files) {
		long size = 0;
		
		for( DocumentFile file : files.values() ) {
			size += file.length() - MagicHeader.length();
		}
		
		return size;
	}
}