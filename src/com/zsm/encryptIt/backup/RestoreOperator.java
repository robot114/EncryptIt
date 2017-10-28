package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public class RestoreOperator implements Source {

	protected final Source mSource;
	protected final Backupable mTarget;
	
	public RestoreOperator( Source s, Backupable t ) {
		mSource = s;
		mTarget = t;
	}

	@Override
	public InputStream openInputStream()
				throws GeneralSecurityException, IOException {
		return mSource.openInputStream();
	}

	public OutputStream openOutputStream() throws IOException {
		return mTarget.openRestoreTargetOutputStream();
	}

	public String displayName() {
		return mTarget.displayName();
	}

	@Override
	public long size() {
		return mSource.size();
	}

	public boolean renameForRestore() throws IOException {
		return mTarget.backupToLocal();
	}

	public boolean restoreFromRename() throws IOException {
		return mTarget.restoreFromLocalBackup();
	}

	@Override
	public boolean checkHeader(InputStream in) {
		return mSource.checkHeader(in);
	}
	
	public void reopenTarget() throws Exception {
		mTarget.reopen();
	}
}
