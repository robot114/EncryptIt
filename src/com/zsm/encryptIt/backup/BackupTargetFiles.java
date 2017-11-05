package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Hashtable;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.zsm.encryptIt.SystemParameter;
import com.zsm.encryptIt.backup.BackupTargetFilesConsts.BACKUP_TARGET_KEY;
import com.zsm.encryptIt.backup.BackupableConst.BACKUPABLES_KEY;
import com.zsm.log.Log;
import com.zsm.util.file.FileExtensionFilter;

/**
 * This class and its subclass describe the relationship from the Backupable
 * sources to the target files, including which Backupable source or which 
 * Backupable sources to which target or targets.
 * 
 * This class and its subclass also give the method the check if the existing
 * targets are valid by check its format, which is defined here
 * 
 * @author zsm
 *
 */
abstract public class BackupTargetFiles {
	
	protected static final String MIME_TYPE = "application/octet-stream";
	
	final private Hashtable<BACKUP_TARGET_KEY, String> mFileNameTable
				= new Hashtable<BACKUP_TARGET_KEY, String>( );
	
	private String mPrefix;
	
	abstract Set<BACKUP_TARGET_KEY> getTargetKeySet();

	abstract public Hashtable<BACKUP_TARGET_KEY, String> getTargetExtsMap();
	
	abstract public Hashtable<BACKUPABLES_KEY, String> getSrcToTargetExtsMap();
	
	abstract BACKUP_TARGET_KEY getTargetKeyFromBackupableKey( BACKUPABLES_KEY key );
	
	abstract public boolean isToMultiFiles();
	
	abstract public String[] getMimeTypes();
	
	abstract public FileExtensionFilter[] getExtensionFilter();
	
	/**
	 * When {@link isToMultiFiles} is true:<br>
	 * The original fileName will be returned, if the fileName is NOT accepted
	 * by any filter retrieved by {@link getExtensionFilter}.<br>
	 * Or, the fileName removed extension will be retrieved, if it is not accepted.
	 * When {@link isToMultiFiles} is true:<br>
	 * The original fileName will be retrieved, if it accepted by a filter.<br>
	 * Or, the default extension of the first filter will be appended 
	 * @param fileName
	 * @return
	 */
	abstract public String normalizeFileName( String fileName );
	
	abstract public long getTotalSize( Hashtable<BACKUP_TARGET_KEY, DocumentFile> files );

	abstract protected boolean validInputStream(InputStream in);
	
	/**
	 * Open a output stream for the backup target.
	 * @param context TODO
	 * @param key For which source to open the output stream
	 * @param targetPathUri the path uri
	 * @param prefix Name, without the extension, of the target. For the 
	 * 			target whose {@link isToMultiFiles} is false, this one is useless
	 * @param password password to protect the output. When it is null,
	 * 			no protection
	 * @param firstForThisTask Whether to open the first output stream for
	 * 			one backup task. For the target whose {@link isToMultiFiles} is
	 * 			true this method will return a OutputStream that can be appended
	 * 			to when {@link firstForThisTask} is true.
	 * 
	 * @return output stream to store the backup target. If {@link password} is
	 * 			not null, the output stream will be a encrypt one
	 * @throws IOException
	 * @throws GeneralSecurityException 
	 */
	abstract public BackupOutputStream openOutputStream(
										   Context context,
										   BACKUPABLES_KEY key,
										   Uri targetPathUri,
										   String prefix,
										   char[] password,
										   boolean firstForThisTask )
					throws IOException, GeneralSecurityException;

	/**
	 * Open an input stream to read the backup target
	 * 
	 * @param context
	 * @param key For which backupable to open the input stream
	 * @param backupTargetFile the backup target file of the {@link key}
	 * @param password password to protect the backup target files. When it is null,
	 * 			no protection
	 * 
	 * @return input stream to store the backup target. If {@link password} is
	 * 			not null, the input stream will be a encrypt one
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public abstract InputStream openInputStream(Context context,
												BACKUPABLES_KEY key,
												DocumentFile backupTargetFile,
												char[] password)
					throws IOException, GeneralSecurityException;

	public Hashtable<BACKUP_TARGET_KEY, String> getFileNames( String prefix ) {
		if( !prefix.equals( mPrefix ) ) {
			updateFileNames( mFileNameTable, prefix );
			mPrefix = prefix;
		}
		
		return mFileNameTable;
	}

	private void updateFileNames(
			Hashtable<BACKUP_TARGET_KEY, String> fileNames, String prefix) {

		Hashtable<BACKUP_TARGET_KEY, String> exts = getTargetExtsMap();
				
		for( BACKUP_TARGET_KEY e : getTargetKeySet() ) {
			fileNames.put( 
				e, BackupTargetFilesConsts.composeFileName( prefix, exts.get(e) ) );
		}
	}
	
	String composeFileNameByTargetKey( String prefix, BACKUP_TARGET_KEY key ) {
		return getFileNames(prefix).get(key);
	}
	
	String composeFileNameBySourceKey( String prefix, BACKUPABLES_KEY key ) {
		return BackupTargetFilesConsts.composeFileName(
							prefix, getSrcToTargetExtsMap().get(key) );
	}
	
	public boolean acceptedByExtensionFilter( String fileName ) {
		
		FileExtensionFilter[] filters = getExtensionFilter();
		for( FileExtensionFilter f : filters ) {
			if( f.acceptByName(fileName)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the target files are in the target uri.
	 * @param context 
	 * @param target target path uri. For the BackupTargetFiles whose
	 * 			{@link isToMultiFiles()} is true, it is the path uri
	 * 			for the target files;
	 * 			otherwise, it is the target file uri itself.
	 * @param prefix prefix of the backup target files' name. For the
	 * 			BackupTargetFiles whose {@link isToMultiFiles()} IS NOT
	 * 			true, it is useless and can be null. 
	 * @param valids, output parameter to store all the valid targets
	 * @param missed, output parameter to store all the missed targets' name
	 * 
	 * @return vector contains the existing files
	 */
	public void checkExistingTargetFile( Context context, Uri target, String prefix,
				 	Hashtable<BACKUP_TARGET_KEY, DocumentFile> existings,
				 	Hashtable<BACKUP_TARGET_KEY, String> missed) {
		
		existings.clear();
		missed.clear();
		
		Hashtable<BACKUP_TARGET_KEY, String> fileNames = getFileNames(prefix);
		DocumentFile dir = DocumentFile.fromTreeUri(context, target);
		for (BACKUP_TARGET_KEY key : getTargetKeySet()) {
			String fn = fileNames.get(key);
			final DocumentFile file = dir.findFile(fn);
			if ( fileExist( file ) ) {
				existings.put(key, file);
			} else {
				missed.put(key, fn);
			}
		}
	}

	private boolean fileExist(DocumentFile file) {
		return file != null && file.exists() && file.isFile();
	}

	/**
	 * Check if the target files are in the target uri and valid.
	 * @param context 
	 * @param target target path uri. For the BackupTargetFiles whose
	 * 			{@link isToMultiFiles()} is true, it is the path uri
	 * 			for the target files;
	 * 			otherwise, it is the target file uri itself.
	 * @param prefix prefix of the backup target files' name. For the
	 * 			BackupTargetFiles whose {@link isToMultiFiles()} IS NOT
	 * 			true, it is useless and can be null. 
	 * @param password password of target files when they were backuped.
	 * 			Can be null, if no password used
	 * @param valids output parameter to store all the valid targets
	 * @param invalids output parameter to store all the invalid targets,
	 * 			including files not existing, with bad format and invalid
	 * 			for the {@link password}
	 * 
	 * @return vector contains the existing files
	 */
	public void checkTargetFileValid( Context context, Uri target, String prefix,
						char[] password,
						Hashtable<BACKUP_TARGET_KEY, DocumentFile> valids,
						Hashtable<BACKUP_TARGET_KEY, String> invalids) {
		
		valids.clear();
		invalids.clear();
		
		checkExistingTargetFile(context, target, prefix, valids, invalids);
		validTargetFiles( context, password, valids, invalids );
	}

	private void validTargetFiles( Context context, char[] password,
					Hashtable<BACKUP_TARGET_KEY, DocumentFile> valids,
					Hashtable<BACKUP_TARGET_KEY, String> invalids) {

		for( BACKUP_TARGET_KEY key : getTargetKeySet() ) {
			DocumentFile file = valids.get(key);
			if( file != null ) {
				if( !validFile( context, password, file ) ) {
					valids.remove(key);
					invalids.put( key, file.getName() );
				}
			}
		}
	}

	protected boolean validFile(Context context, char[] password, DocumentFile file) {
		if( file ==  null ) {
			return false;
		}
		
		ContentResolver cr = context.getContentResolver();
		try( InputStream in
				= wrapByPassword(password, cr.openInputStream( file.getUri() ) ) ) {
			
			boolean result = validInputStream( in );
			Log.d( "Valid file's result. ", "file", file.getName(), "result", result );
			return result;
		} catch (IOException | GeneralSecurityException e) {
			Log.e( e, "Failed to valid the file: ", file );
			return false;
		}
	}
	
	protected boolean hasPassword(char[] password) {
		return password != null && password.length > 0;
	}
	
	protected InputStream wrapByPassword( char[] password, InputStream in )
								throws GeneralSecurityException, IOException {
		
		if( hasPassword( password ) ) {
			return SystemParameter.getPasswordBasedInOutDecorator(
					password ).wrapInputStream(in);
		} else {
			return in;
		}
	}

	protected OutputStream wrapByPassword(char[] password, OutputStream out)
								throws GeneralSecurityException, IOException {

		if (hasPassword(password)) {
			return SystemParameter.getPasswordBasedInOutDecorator(password)
					.wrapOutputStream(out);
		} else {
			return out;
		}
	}

}
