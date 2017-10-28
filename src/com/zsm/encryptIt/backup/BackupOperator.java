package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public class BackupOperator implements Target {

	protected final Backupable mSource;
	protected final Target mTarget;

	public BackupOperator( Backupable backupable, Target target ) {
		mSource = backupable;
		mTarget = target;
	}
	
	InputStream openInputStream() throws IOException {
		return mSource.openBackupSrcInputStream();
	}
	
	@Override
	public OutputStream openOutputStream()
					throws GeneralSecurityException, IOException {
		
		return mTarget.openOutputStream();
	}

	@Override
	public String displayName() {
		return mTarget.displayName();
	}
	
	@Override
	public void outputHeader(OutputStream out) throws IOException {
		mTarget.outputHeader(out);
	}

	long size() {
		return mSource.size();
	}
}
