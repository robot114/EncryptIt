package com.zsm.encryptIt.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.RowId;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import com.zsm.encryptIt.EncryptItPersistence;
import com.zsm.encryptIt.R;
import com.zsm.log.Log;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.persistence.Persistence;
import com.zsm.recordstore.AbstractMetaDataCursor;
import com.zsm.recordstore.AbstractRawCursor;
import com.zsm.recordstore.LongRowId;

public class EncryptItContentProvider extends ContentProvider {

	private static final String PATH_BACKUP = "backup";
	private static final String PATH_FROM_LOCAL = PATH_BACKUP+"/fromlocal";
	private static final String PATH_TO_LOCAL = PATH_BACKUP+"/tolocal";
	
	private static final String PATH_DB_OPERATION = "database";
	private static final String PATH_REOPEN_DATABASE = PATH_DB_OPERATION+"/reopen";
	private static final String PATH_CLOSE_DATABASE = PATH_DB_OPERATION+"/close";

	public static final String KEY_DATA = "DATA";
	
	private static final String ITEMS = "todoitems";
	private static final String ITEM_TYPE = "/vnd.zsm.todoitem";
	private static final String ITEM_TYPE_ALL
				= ContentResolver.CURSOR_DIR_BASE_TYPE+ITEM_TYPE;
	private static final String ITEM_TYPE_SINGLE 
				= ContentResolver.CURSOR_ITEM_BASE_TYPE+ITEM_TYPE;
	
	private static final String META_DATA = "metadata";
	public static final String PARAMETER_KEY = "key";

	private static final String TYPE_BACKUP 
		= ContentResolver.CURSOR_ITEM_BASE_TYPE+"/backup";

	private static final int CODE_ALL = 1;
	private static final int CODE_SINGLE = 2;
	
	private static final int CODE_META_DATA = 20;
	
	private static final int CODE_BACKUP = 10;
	private static final int CODE_BACKUP_TO_LOCAL = 11;
	private static final int CODE_RESTORE_FROM_LOCAL = 12;
	private static final int CODE_CLOSE_DATABASE = 13;
	private static final int CODE_REOPEN_DATABASE = 14;
	
	private static final String[] OPENABLE_COLUMNS = new String[] {
		OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
	};

	private static UriMatcher uriMatcher;
	private static Uri contentUri;
	private static Uri metaDataUri;
	private static Uri backupUri;
	private static Uri backupToLocalUri;
	private static Uri restoreFromLocalUri;
	private static Uri closeUri;
	private static Uri reopenUri;
	
	private Persistence persistence;

	private Boolean initialized = false;

	@Override
	public boolean onCreate() {
		if( uriMatcher == null ) {
			uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
			uriMatcher.addURI( getUri(), ITEMS, CODE_ALL );
			uriMatcher.addURI( getUri(), ITEMS+"/#", CODE_SINGLE );
			
			uriMatcher.addURI( getUri(), META_DATA, CODE_META_DATA );
			
			uriMatcher.addURI( getUri(), PATH_TO_LOCAL, CODE_BACKUP_TO_LOCAL );
			uriMatcher.addURI( getUri(), PATH_FROM_LOCAL, CODE_RESTORE_FROM_LOCAL );
			uriMatcher.addURI( getUri(), PATH_BACKUP, CODE_BACKUP );
			uriMatcher.addURI( getUri(), PATH_CLOSE_DATABASE, CODE_CLOSE_DATABASE );
			uriMatcher.addURI( getUri(), PATH_REOPEN_DATABASE, CODE_REOPEN_DATABASE );
			
			contentUri = Uri.parse( "content://" + getUri() + "/" + ITEMS );
			metaDataUri = Uri.parse( "content://" + getUri() + "/" + META_DATA );
			backupUri = Uri.parse( "content://" + getUri() + "/" + PATH_BACKUP );
			backupToLocalUri = Uri.parse( "content://" + getUri() + "/" + PATH_TO_LOCAL );
			restoreFromLocalUri = Uri.parse( "content://" + getUri() + "/" + PATH_FROM_LOCAL );
			reopenUri = Uri.parse( "content://" + getUri() + "/" + PATH_REOPEN_DATABASE );
			closeUri = Uri.parse( "content://" + getUri() + "/" + PATH_CLOSE_DATABASE );
		}
		return true;
	}

	private String getUri() {
		return getContext().getResources()
					.getString(R.string.contentProviderUri);
	}	

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		
		Log.d( "start query storage.", "uri", uri );
		synchronized(initialized) {
			try {
				if( !initialized && !initPersistence() ) {
					return null;
				}
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				Log.e( e, "Initialize persistence failed!" );
				return null;
			}
		}
		
        int matchCode = uriMatcher.match(uri);
        
        if( matchCode == CODE_BACKUP ) {
        	return queryFile(matchCode, uri, projection, selection,
        					 selectionArgs, sortOrder);
        }
        
        if( matchCode == CODE_META_DATA ) {
        	return queryMetaData(uri);
        }
        
        RowId id = null;
		if(matchCode == CODE_SINGLE){
            id = getIdFromUri(uri);
        }

        AbstractRawCursor rsc = persistence.query( id );
        Cursor c = new AndroidWrappedRawCursor( rsc );
        c.setNotificationUri(getContext().getContentResolver(), uri);
        
		Log.d( "query storage ok.", "uri", uri, "cursor", rsc, "id", id );
        return c;
	}

	private Cursor queryMetaData(Uri uri) {
		String key = getKeyFromUri( uri );
		if( key == null ) {
			Log.w( "No parameter 'key': ", uri );
			return null;
		}
		
		AbstractMetaDataCursor rmc = persistence.queryMetaData(key);
		Cursor c = new AndroidWrappedMetaCursor( rmc );
		c.setNotificationUri(getContext().getContentResolver(), uri);
		Log.d( "query meta data ok.", "uri", uri, "cursor", rmc, "key", key );
		return c;
	}

	private boolean initPersistence()
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		try {
			// With EncryptItPersistence, the data between the provider and the
			// solver are encrypted. Thus make it safer.
			persistence = new EncryptItPersistence( );
			persistence.open();
		} catch (BadPersistenceFormatException | IOException e) {
			Log.e( e, "Cannot initialize the stream decorator!" );
			return false;
		}
		
		initialized = true;
		return true;
	}
	
	private Cursor queryFile(int matchCode, Uri uri, String[] projection,
							 String selection, String[] selectionArgs,
							 String sortOrder) {
		
		String[] columns = projection == null ? OPENABLE_COLUMNS : projection;
		
		MatrixCursor cursor = new MatrixCursor( columns, 1 );
		switch( matchCode ) {
			case CODE_BACKUP:
				cursor.newRow()
					.add( OpenableColumns.DISPLAY_NAME, "Backup for Database")
					.add( OpenableColumns.SIZE, persistence.getBackupSize() );
				break;
			default:
				throw new IllegalArgumentException( "Unsupport file uri: " + uri );
		}
		
		return cursor;
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		
		if( uriMatcher.match(uri) != CODE_BACKUP ) {
			throw new IllegalArgumentException( "Unsupport file uri: " + uri );
		}
		return persistence.openFileDescriptorForBackup(mode);
	}

	@Override
	public String getType(Uri uri) {
		switch( uriMatcher.match(uri) ) {
			case CODE_ALL:
				return ITEM_TYPE_ALL;
			case CODE_SINGLE:
				return ITEM_TYPE_SINGLE;
			case CODE_BACKUP:
				return TYPE_BACKUP;
			default:
				throw new IllegalArgumentException( "Unsupport uri: " + uri );
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch(uriMatcher.match(uri)){
			case CODE_SINGLE:
			case CODE_ALL:
				break;
			default:
				throw new IllegalArgumentException( "Unsupport insert uri: " + uri );
		}
		
		byte[] data = values.getAsByteArray( KEY_DATA );
		RowId id = null;
		try {
			id = (RowId)persistence.add(data);
		} catch (IOException e) {
			Log.e( e, "Add item failed!", data );
		}
		Uri newedUri = null;
		if( id != null ) {
			Uri.Builder builder = contentUri.buildUpon();
			newedUri = builder.appendEncodedPath(id.toString()).build();
			notifyResolverChange(uri);
		}
		return newedUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = -1;
		
		switch( uriMatcher.match(uri) ) {
			case CODE_SINGLE:
				try {
					count = persistence.remove(getIdFromUri(uri));
				} catch (IOException e) {
					Log.e( e, "Remove item failed: ", uri );
				}
				break;
			case CODE_ALL:
				persistence.clear();
				break;
			default:
				throw new IllegalArgumentException( "Unsupport delete uri: " + uri );
				
		}
		
		notifyResolverChange(uri);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		
		Log.d( "To update. ", "uri", uri, "data", values );
		switch( uriMatcher.match(uri) ) {
			case CODE_SINGLE:
				return updateSingle(uri, values);
			case CODE_BACKUP_TO_LOCAL:
				return backupToLocal();
			case CODE_RESTORE_FROM_LOCAL:
				return restoreFromLocal();
			case CODE_CLOSE_DATABASE:
				return closeDatabase();
			case CODE_REOPEN_DATABASE:
				return reopenDatabase();
			case CODE_ALL:
			default:
				throw new IllegalArgumentException( "Unsupport update uri: " + uri );
				
		}
	}

	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		// TODO Auto-generated method stub
		return super.call(method, arg, extras);
	}

	private int closeDatabase() {
		persistence.close();
		initialized = false;
		notifyResolverChange( getCloseUri() );
		return 1;
	}

	private int reopenDatabase() {
		persistence.close();
		initialized = false;
		try {
			persistence.open();
			notifyResolverChange( getReopenUri() );
		} catch (BadPersistenceFormatException | IOException e) {
			Log.e( e, "Reopen persistence failed!" );
			return 0;
		}
		initialized = true;
		return 1;
	}

	private int restoreFromLocal() {
		try {
			persistence.restoreFromLocal( );
		} catch (FileNotFoundException e) {
			Log.e( "Restore from local failed!" );
			return 0;
		}
		return 1;
	}

	private int backupToLocal() {
		try {
			persistence.backupToLocal();
		} catch (FileNotFoundException e) {
			Log.e( "Backup to local failed!" );
			return 0;
		}
		return 1;
	}

	private int updateSingle(Uri uri, ContentValues values) {
		LongRowId id = getIdFromUri(uri);
		byte[] data = values.getAsByteArray( KEY_DATA );
		try {
			persistence.update(id, data);
		} catch ( Exception e ) {
			Log.e( e, "Update record at id failed!", "id", id, "data", data );
			return 0;
		}
		notifyResolverChange(uri);
		return 1;
	}

	private LongRowId getIdFromUri(Uri uri) {
		return new LongRowId( Long.parseLong( uri.getLastPathSegment() ) );
	}

	private String getKeyFromUri(Uri uri) {
		List<String> l = uri.getQueryParameters(PARAMETER_KEY);
		if( l.size() < 1 ) {
			Log.d( "No the 'key' parameter for uri: ", uri );
			return null;
		}
		
		return l.get(0);
	}

	private void notifyResolverChange(Uri uri) {
		getContext().getContentResolver().notifyChange(uri, null);
	}

	public static Uri getContentUri() {
		return contentUri;
	}
	
	public static Uri getMetaDataUri() {
		return metaDataUri;
	}

	public static Uri getBackupUri() {
		return backupUri;
	}

	public static Uri getBackupToLocalUri() {
		return backupToLocalUri;
	}

	public static Uri getRestoreFromLocalUri() {
		return restoreFromLocalUri;
	}

	public static Uri getCloseUri() {
		return closeUri;
	}

	public static Uri getReopenUri() {
		return reopenUri;
	}
}
