package com.zsm.encryptIt.backup;



abstract public class PasswordOperator {

	protected final Backupable mBackupable;
	protected final char[] mPassword;

	protected PasswordOperator( Backupable b, char[] password ) {
		
		mBackupable = b;
		mPassword = password;
	}
	
	protected boolean hasPassword() {
		return mPassword == null || mPassword.length == 0;
	}

}
