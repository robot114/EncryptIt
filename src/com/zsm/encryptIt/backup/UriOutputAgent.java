package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import com.zsm.log.Log;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class UriOutputAgent implements BackupOutputAgent {

	private ContentResolver mContentResolver;
	private Uri mUri;

	public UriOutputAgent( ContentResolver cr, Uri uri ) {
		mContentResolver = cr;
		mUri = uri;
	}
	
	@Override
	public OutputStream openOutputStream() throws FileNotFoundException {
		return mContentResolver.openOutputStream(mUri);
	}

	@Override
	public String displayName() {
		String result = null;
		if (mUri.getScheme().equals("content")) {
			try( Cursor cursor
					= mContentResolver.query(mUri, null, null, null, null) ) {
				
				if ( cursor != null && cursor.moveToFirst() ) {
					int columnIndex
						= cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
					
					result = cursor.getString(columnIndex);
				}
			} catch( Exception e ) {
				Log.w( e, "Get display name failed: ", mUri );
			}
		}
		if (result == null) {
			result = mUri.getLastPathSegment();
			int cut = result.lastIndexOf('/');
			if (cut != -1) {
				result = result.substring(cut + 1);
			}
		}
		return result;
	}

}
