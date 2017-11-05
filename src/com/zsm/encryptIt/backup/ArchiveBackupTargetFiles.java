package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;

import com.zsm.encryptIt.backup.BackupTargetFilesConsts.BACKUP_TARGET_KEY;
import com.zsm.encryptIt.backup.BackupableConst.BACKUPABLES_KEY;
import com.zsm.log.Log;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.android.DocumentFileUtilities;


public class ArchiveBackupTargetFiles extends BackupTargetFiles {

	private static final String EXT = "eiz";
	
	private static final String[] MIME_TYPES = { "application/" + EXT };
	private static final FileExtensionFilter[] EXT_FILTER
				= { new FileExtensionFilter(EXT, "Packaged backup file") };
	private static final Set<BACKUP_TARGET_KEY> TARGET_KEY_SET
				= new HashSet<BACKUP_TARGET_KEY>();
	private static final Hashtable<BACKUP_TARGET_KEY, String> TARGET_EXTS_MAP
				= new Hashtable<BACKUP_TARGET_KEY, String>();
	private static final Hashtable<BACKUPABLES_KEY, String> SRC_TO_TARGET_EXTS_MAP
				= new Hashtable<BACKUPABLES_KEY, String>();

	private ZipEntryOutputStream mOutStream;
	
	static {
		TARGET_KEY_SET.add( BACKUP_TARGET_KEY.KEY_ARCHIVED );
		TARGET_EXTS_MAP.put( BACKUP_TARGET_KEY.KEY_ARCHIVED, EXT );
		SRC_TO_TARGET_EXTS_MAP.put(BACKUPABLES_KEY.KEY_KEY, EXT);
		SRC_TO_TARGET_EXTS_MAP.put(BACKUPABLES_KEY.KEY_DATABASE, EXT);
	}

	@Override
	Set<BACKUP_TARGET_KEY> getTargetKeySet() {
		return TARGET_KEY_SET;
	}

	@Override
	public Hashtable<BACKUP_TARGET_KEY, String> getTargetExtsMap() {
		return TARGET_EXTS_MAP;
	}

	@Override
	BACKUP_TARGET_KEY getTargetKeyFromBackupableKey(BACKUPABLES_KEY key) {
		return BACKUP_TARGET_KEY.KEY_ARCHIVED;
	}

	@Override
	public Hashtable<BACKUPABLES_KEY, String> getSrcToTargetExtsMap() {
		return SRC_TO_TARGET_EXTS_MAP;
	}

	@Override
	public boolean isToMultiFiles() {
		return false;
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
		if( acceptedByExtensionFilter(fileName)) {
			return fileName;
		}
		
		String defaultExtension = EXT_FILTER[0].getDefaultExtension();
		if( defaultExtension.length() > 0 ) {
			return fileName + "." + defaultExtension;
		} else {
			return fileName;
		}
	}

	@Override
	protected boolean validInputStream(InputStream in) {
		if( !MagicHeader.checkHeader(in) ) {
			return false;
		}
		
		try( ZipInputStream zin = new ZipInputStream( in ) ) {

			Set<BACKUPABLES_KEY> set
				= Collections.synchronizedSet(EnumSet.noneOf(BACKUPABLES_KEY.class));
			
			ZipEntry ze = null;
			while( ( ze = zin.getNextEntry() ) != null ) {
				BACKUPABLES_KEY key = BACKUPABLES_KEY.valueOf( ze.getName() );
				if( key == null ) {
					Log.w( "Invalid zip entry: ", ze.getName() );
					return false;
				}
				set.add(key);
			}
			
			return set.size() == BACKUPABLES_KEY.values().length;
		} catch (IOException e) {
			Log.w( e, "Iterate the zip file failed!" );
			return false;
		}
	}

	@Override
	public BackupOutputStream openOutputStream(Context context, BACKUPABLES_KEY key,
										 	   Uri targetUri, String prefix,
										 	   char[] password,
										 	   boolean firstForThisTask )
					throws IOException, GeneralSecurityException {

		if( firstForThisTask && mOutStream != null ) {
			throw new IOException( "The previous outputStream is not closed all!" );
		}
		
		if( firstForThisTask ) {
			ContentResolver cr = context.getContentResolver();
			String fn = composeFileNameBySourceKey(prefix, key);
			Uri uri = DocumentFileUtilities.getChildUri(targetUri, fn);
			if( !DocumentFileUtilities.documentExists(context, uri) ) {
				uri = DocumentsContract.createDocument( cr, targetUri, MIME_TYPE, fn );
			}
			
			OutputStream passOut
				= wrapByPassword(password, cr.openOutputStream(uri) );
			MagicHeader.outputHeader(passOut);
			mOutStream = new ZipEntryOutputStream( passOut );
		}
		
		mOutStream.putNextEntry( new ZipEntry( key.name() ) );
		
		return mOutStream;
	}

	@Override
	public InputStream openInputStream(Context context, BACKUPABLES_KEY key,
									   DocumentFile backupTarget,
									   char[] password)
					throws IOException, GeneralSecurityException {

		ContentResolver cr = context.getContentResolver();
		InputStream in
			= wrapByPassword(password, cr.openInputStream(backupTarget.getUri()) );
		
		if( !MagicHeader.checkHeader(in) ) {
			in.close();
			return null;
		}
		
		ZipEntryInputStream zin = new ZipEntryInputStream( in );
		ZipEntry entry;
		while( ( entry = zin.getNextEntry() ) != null
			   && !entry.getName().equals( key.name() ) ) {
			
			zin.closeEntry();
			Log.d( "Entry is ", entry.getName() );
		}
		
		if( entry == null ) {
			Log.e( "Entry not found: ", key );
			zin.close();
			return null;
		}
		
		return zin;
	}

	@Override
	public long getTotalSize(Hashtable<BACKUP_TARGET_KEY, DocumentFile> files) {
		return files.get( BACKUP_TARGET_KEY.KEY_ARCHIVED ).length()
					- MagicHeader.length();
	}
	
	private class ZipEntryOutputStream extends BackupOutputStream {

		private ZipEntry mCurrentEntry;

		public ZipEntryOutputStream(OutputStream os) {
			super( new ZipOutputStream( os ) );
		}

		public void putNextEntry(ZipEntry zipEntry) throws IOException {
			((ZipOutputStream)getOutputStream()).putNextEntry(zipEntry);
			mCurrentEntry = zipEntry;
		}

		@Override
		void close(CLOSE_TYPE closeType) throws IOException {
			Log.d( "ZipOutputStream is to be closed", "close type", closeType,
				   "entry", mCurrentEntry );
			((ZipOutputStream)getOutputStream()).closeEntry();
			mCurrentEntry = null;
			if( closeType != CLOSE_TYPE.NORMAL_ONE ) {
				getOutputStream().close();
			}
		}
	}

	private class ZipEntryInputStream extends ZipInputStream {

		public ZipEntryInputStream(InputStream stream) {
			super(stream);
		}

		@Override
		public void close() throws IOException {
			closeEntry();
			super.close();
		}
	}
}
