package com.zsm.encryptIt.android;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.RowId;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.zsm.encryptIt.EncryptItPersistence;
import com.zsm.encryptIt.R;
import com.zsm.log.Log;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.persistence.Persistence;
import com.zsm.recordstore.AbstractRawCursor;
import com.zsm.recordstore.LongRowId;

public class EncryptItContentProvider extends ContentProvider {

	public static final String KEY_DATA = "DATA";
	
	private static final String ITEMS = "todoitems";
	private static final String ITEM_TYPE = "/vnd.zsm.todoitem";
	private static final String ITEM_TYPE_ALL
				= ContentResolver.CURSOR_DIR_BASE_TYPE+ITEM_TYPE;
	private static final String ITEM_TYPE_SINGLE 
				= ContentResolver.CURSOR_ITEM_BASE_TYPE+ITEM_TYPE;

	private static final int ALL = 1;

	private static final int SINGLE = 2;
	
	private static UriMatcher uriMatcher;
	private static Uri contentUri;
	
	private Persistence persistence;

	private Boolean initialized = false;

	@Override
	public boolean onCreate() {
		if( uriMatcher == null ) {
			uriMatcher = new UriMatcher( UriMatcher.NO_MATCH );
			uriMatcher.addURI( getUri(), ITEMS, ALL );
			uriMatcher.addURI( getUri(), ITEMS+"/#", SINGLE );
			
			contentUri = Uri.parse( "content://" + getUri() + "/" + ITEMS );
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
        if((uriMatcher.match(uri)) == SINGLE){
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
	
	@Override
	public String getType(Uri uri) {
		switch( uriMatcher.match(uri) ) {
			case ALL:
				return ITEM_TYPE_ALL;
			case SINGLE:
				return ITEM_TYPE_SINGLE;
			default:
				throw new IllegalArgumentException( "Unsupport uri: " + uri );
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch(uriMatcher.match(uri)){
			case SINGLE:
			case ALL:
				break;
			default:
				throw new IllegalArgumentException( "Unsupport uri: " + uri );
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
			case SINGLE:
				count = persistence.remove(getIdFromUri(uri));
				break;
			case ALL:
				persistence.clear();
				break;
			default:
				throw new IllegalArgumentException( "Unsupport uri: " + uri );
				
		}
		
		notifyResolverChange(uri);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		
		Log.d( "To update. ", "uri", uri, "data", values );
		switch( uriMatcher.match(uri) ) {
			case SINGLE:
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
			case ALL:
			default:
				throw new IllegalArgumentException( "Unsupport uri: " + uri );
				
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

}
