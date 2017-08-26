package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.zsm.log.Log;

public class UriInputAgent implements BackupInputAgent {

	private ContentResolver mContentResolver;
	private Uri mUri;

	public UriInputAgent( ContentResolver cr, Uri uri ) {
		mContentResolver = cr;
		mUri = uri;
	}
	
	@Override
	public InputStream openBackupInputStream() throws FileNotFoundException {
		return mContentResolver.openInputStream(mUri);
	}

	@Override
	public long size() {
		try ( Cursor c = mContentResolver.query(mUri, null, null, null, null) ) {
			if( c == null || c.getCount() != 1 || !c.moveToFirst() ) {
				Log.w( "Sth. wrong in quering uri. ", mUri, "cursor", c,
					   "count", c == null ? 0 : c.getCount() );
				return 0;
			}
			
			
			int colIndex = c.getColumnIndex( OpenableColumns.SIZE );
			if( colIndex < 0 ) {
				Log.w( "No size column in the query result for uri. ", mUri );
				return 0;
			}
			return c.getLong( colIndex );
		}
	}

}
