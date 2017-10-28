package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.net.Uri;

import com.zsm.util.file.FileUtilities;

public class UriBackupTarget extends MagicHeader implements Target {

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
