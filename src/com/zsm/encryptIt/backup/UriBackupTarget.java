package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import com.zsm.log.Log;
import com.zsm.util.file.FileUtilities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class UriBackupTarget implements Target {

	private ContentResolver mContentResolver;
	private Uri mUri;

	public UriBackupTarget( ContentResolver cr, Uri uri ) {
		mContentResolver = cr;
		mUri = uri;
	}
	
	@Override
	public OutputStream openOutputStream() throws FileNotFoundException {
		return mContentResolver.openOutputStream(mUri);
	}

	@Override
	public String displayName() {
		return FileUtilities.displayNameFromUri(mContentResolver, mUri);
	}

}
