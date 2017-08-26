package com.zsm.encryptIt.android;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.RowId;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import com.zsm.encryptIt.EncryptItPersistence;
import com.zsm.encryptIt.R;
import com.zsm.log.Log;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.persistence.Persistence;
import com.zsm.recordstore.AbstractRawCursor;
import com.zsm.recordstore.LongRowId;

public class EncryptItContentProvider extends ContentProvider {

	private static final String PATH_BACKUP = "backup";

	public static final String KEY_DATA = "DATA";
	
	private static final String ITEMS = "todoitems";
	private static final String ITEM_TYPE = "/vnd.zsm.todoitem";
	private static final String ITEM_TYPE_ALL
				= ContentResolver.CURSOR_DIR_BASE_TYPE+ITEM_TYPE;
	private static final String ITEM_TYPE_SINGLE 
				= ContentResolver.CURSOR_ITEM_BASE_TYPE+ITEM_TYPE;
	private static final String TYPE_BACKUP 
		= ContentResolver.CURSOR_ITEM_BASE_TYPE+"/backup";

	private static final int CODE_ALL = 1;
	private static final int CODE_SINGLE = 2;
	private static final int CODE_BACKUP = 3;
	
	private static final String[] OPENABLE_COLUMNS = new String[] {
		OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
	};
	
	private static UriMatcher uriMatcher;
	private static Uri contentUri;
	private static Uri backupUri;
	
	private Persistence persistence;

	private Boolean initialized = false;

	@Override
	public boolean onCreate() {
		if( uriMatcher == null ) {
			uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
			uriMatcher.addURI( getUri(), ITEMS, CODE_ALL );
			uriMatcher.addURI( getUri(), ITEMS+"/#", CODE_SINGLE );
			uriMatcher.addURI( getUri(), PATH_BACKUP, CODE_BACKUP );
			
			contentUri = Uri.parse( "content://" + getUri() + "/" + ITEMS );
			backupUri = Uri.parse( "content://" + getUri() + "/" + PATH_BACKUP );
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
		
        RowId id = null;
        int matchCode = uriMatcher.match(uri);
        if( matchCode == CODE_BACKUP ) {
        	return queryFile(matchCode, uri, projection, selection,
        					 selectionArgs, sortOrder);
        }
        
		if(matchCode == CODE_SINGLE){
            id = getIdFromUri(uri);
        }

        AbstractRawCursor rsc = persistence.query( id );
        Cursor c = new AndroidWrappedRawCursor( rsc );
        c.setNotificationUri(getContext().getContentResolver(), uri);
        
		Log.d( "query storage ok.", "uri", uri, "cursor", rsc, "id", id );
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
		return persistence.openForBackup(mode);
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
				count = persistence.remove(getIdFromUri(uri));
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
			case CODE_ALL:
			default:
				throw new IllegalArgumentException( "Unsupport update uri: " + uri );
				
		}
	}

	private LongRowId getIdFromUri(Uri uri) {
		return new LongRowId( Long.parseLong( uri.getLastPathSegment() ) );
	}

	private void notifyResolverChange(Uri uri) {
		getContext().getContentResolver().notifyChange(uri, null);
	}

	public static Uri getContentUri() {
		return contentUri;
	}

	public static Uri getBackupUri() {
		return backupUri;
	}
}
