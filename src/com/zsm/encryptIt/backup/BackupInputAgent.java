package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface BackupInputAgent {
	InputStream openBackupInputStream() throws FileNotFoundException;
	long size();
}
