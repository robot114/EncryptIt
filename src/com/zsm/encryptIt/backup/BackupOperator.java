package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public class BackupOperator {

	protected final Backupable mSource;
	protected final Target mTarget;

	public BackupOperator( Backupable backupable, Target target ) {
		mSource = backupable;
		mTarget = target;
	}
	
	InputStream openInputStream() throws IOException {
		return mSource.openBackupSrcInputStream();
	}
	
	OutputStream openOutputStream()
					throws GeneralSecurityException, IOException {
		
		return mTarget.openOutputStream();
	}

	String displayName() {
		return mTarget.displayName();
	}
	
	long size() {
		return mSource.size();
	}
}
