package com.zsm.encryptIt.backup;

import android.net.Uri;

public class BackupParameter {

	final Uri mTargetUri;
	final String mPrefix;
	final char[] mPassword;
	final BackupTargetFiles mTargetFiles;

	public BackupParameter( Uri targetUri, String prefix, char[] password,
							BackupTargetFiles btf ) {
		
		mTargetUri = targetUri;
		mPrefix = prefix;
		mPassword = password;
		mTargetFiles = btf;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append( super.toString() )
			.append( "[ targetUri: " )
			.append( mTargetUri.toString() )
			.append( ", prefix: " )
			.append( mPrefix )
			.append( ", targetFiles: " )
			.append( mTargetFiles )
			.append( "]" );
		return buffer.toString();
	}
}
