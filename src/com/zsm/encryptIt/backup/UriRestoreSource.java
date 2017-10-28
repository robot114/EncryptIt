package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.InputStream;

import android.content.ContentResolver;
import android.net.Uri;

import com.zsm.util.file.FileUtilities;

public class UriRestoreSource extends MagicHeader implements Source {

	private ContentResolver mContentResolver;
	private Uri mUri;

	public UriRestoreSource( ContentResolver cr, Uri uri ) {
		mContentResolver = cr;
		mUri = uri;
	}
	
	@Override
	public InputStream openInputStream() throws FileNotFoundException {
		return mContentResolver.openInputStream(mUri);
	}

	@Override
	public long size() {
		return FileUtilities.sizeFromUri(mContentResolver, mUri);
	}

}
