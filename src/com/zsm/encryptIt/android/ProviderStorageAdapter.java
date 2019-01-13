package com.zsm.encryptIt.android;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.sql.RowId;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.zsm.encryptIt.ItemCompator;
import com.zsm.encryptIt.SystemParameter;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.WhatToDoItemV2;
import com.zsm.encryptIt.action.ItemStorageAdapter;
import com.zsm.io.SerializedObjectInputStream;
import com.zsm.log.Log;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.persistence.InOutDecorator;
import com.zsm.recordstore.AbstractMetaDataCursor;
import com.zsm.recordstore.AbstractRawCursor;
import com.zsm.recordstore.LongRowId;
import com.zsm.util.file.FileUtilities;

public class ProviderStorageAdapter implements ItemStorageAdapter {

	private ContentResolver contentSolver;
	final static private Uri CONTENT_URI = EncryptItContentProvider.getContentUri();
	private InOutDecorator decorator;

	public ProviderStorageAdapter( Context context ) {
		contentSolver = context.getContentResolver();
		decorator = SystemParameter.getEncryptInOutDecorator();
	}
	
	@Override
	public void clear() {
		contentSolver.delete(CONTENT_URI, null, null);
	}

	@Override
	public AbstractRawCursor query() {
		return (AbstractRawCursor)contentSolver.query(
									CONTENT_URI, null, null, null, null);
	}

	@Override
	public byte[] getMetaData(String key) {
		AbstractMetaDataCursor c
			= (AbstractMetaDataCursor) contentSolver.query(
					EncryptItContentProvider.getMetaDataUri(),
					null, EncryptItContentProvider.PARAMETER_KEY + "=?",
					new String[] { key }, null);

		if( c == null ) {
			return null;
		}
		
		return c.getData();
	}

	@Override
	public WhatToDoItemV2 read(AbstractRawCursor cursor)
			throws ClassNotFoundException, IOException, BadPersistenceFormatException {
		
		byte[] data = cursor.getData();	// Encoded data
		Object obj = null;
		try( InputStream in = new ByteArrayInputStream( data );
				ObjectInputStream serIn
					= new ObjectInputStream(decorator.wrapInputStream(in));	// decode
				) {
			
			obj = serIn.readObject();
		} catch ( OptionalDataException | ClassCastException e ) {
			Log.e( e, "Bad persistence data format. ", "id", cursor.currentId() );
			throw new BadPersistenceFormatException( e );
		}
		WhatToDoItemV2 item = ItemCompator.toLastVersionItem(obj);
		item.setContext( cursor.currentId() );
		return item;
	}

	@Override
	public void remove(RowId rowId) {
		Uri uriWithId = Uri.withAppendedPath(CONTENT_URI, rowId.toString());
		contentSolver.delete(uriWithId, null, null);
	}

	@Override
	public RowId add(WhatToDoItemV2 item) throws IOException {
		Log.d( "The following is the data to be added: ", item );
		RowId id = null;
		ContentValues values = putItemIntoContent( item );
		Uri uri = contentSolver.insert(CONTENT_URI, values );
		id = new LongRowId( Long.parseLong( uri.getLastPathSegment() ) );
		Log.d( "Data added to the content provider as a byte array!" );
		
		return id;
	}

	@Override
	public void update(RowId rowId, WhatToDoItemV2 item) throws IOException {
		Log.d( "The following is the data to be updated: ", item );
		ContentValues values = putItemIntoContent( item );
		Uri uriWithId = Uri.withAppendedPath(CONTENT_URI, rowId.toString());
		contentSolver.update(uriWithId, values, null, null);
	}
	
	private ContentValues putItemIntoContent( WhatToDoItemV2 item ) throws IOException {
		ByteArrayOutputStream aos = new ByteArrayOutputStream();
		ObjectOutputStream serOut = null;
		ContentValues values = null;
		try {
			// encode
			serOut = new ObjectOutputStream( decorator.wrapOutputStream( aos ) );
			serOut.writeObject( item );
			serOut.close();		// flush
			serOut = null;
			values = new ContentValues();
			values.put( EncryptItContentProvider.KEY_DATA, aos.toByteArray());
		} finally {
			if( serOut != null ) {
				serOut.close();
				serOut = null;
			}
			aos.close();
		}
		
		return values;
	}

	@Override
	public void close() {
	}

	@Override
	public InputStream openBackupSrcInputStream() throws IOException {
		return contentSolver.openInputStream( 
					EncryptItContentProvider.getBackupUri() );
	}

	@Override
	public OutputStream openRestoreTargetOutputStream() throws IOException {
		return contentSolver.openOutputStream(
					EncryptItContentProvider.getBackupUri() );
	}

	@Override
	public String displayName() {
		return "Database";
	}

	@Override
	public long size() {
		return FileUtilities.sizeFromUri( contentSolver,
				  	EncryptItContentProvider.getBackupUri() );
	}

	@Override
	public boolean backupToLocal() {
		int count
			= contentSolver.update( EncryptItContentProvider.getBackupToLocalUri(),
									null, null, null );
		return count == 1;
	}

	@Override
	public boolean restoreFromLocalBackup() {
		int count
			= contentSolver.update( EncryptItContentProvider.getRestoreFromLocalUri(),
									null, null, null );
		return count == 1;
	}

	@Override
	public void reopen() {
		contentSolver
			.update( EncryptItContentProvider.getReopenUri(), null, null, null );
	}

}
