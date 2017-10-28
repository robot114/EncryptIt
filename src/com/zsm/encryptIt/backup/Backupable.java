package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Backupable {

	InputStream openBackupSrcInputStream() throws IOException;
	
	OutputStream openRestoreTargetOutputStream() throws IOException;

	String displayName();
	
	long size();
	
	boolean backupToLocal() throws IOException;
	
	boolean restoreFromLocalBackup() throws IOException;

	void reopen() throws Exception;
}
