package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Backupable {

	InputStream openBackupSrcInputStream() throws IOException;
	
	OutputStream openRestoreTargetOutputStream() throws IOException;

	/**
	 * 
	 * @return display name of the backupable entry. MUST be unique for each
	 * 			entry. This value will be used to identify this entry in
	 * 			the archived target file 
	 */
	String displayName();
	
	long size();
	
	boolean backupToLocal() throws IOException;
	
	boolean restoreFromLocalBackup() throws IOException;

	void reopen() throws Exception;
}
