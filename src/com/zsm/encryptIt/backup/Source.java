package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public interface Source {
	InputStream openInputStream() throws GeneralSecurityException, IOException;
	
	long size();
}
