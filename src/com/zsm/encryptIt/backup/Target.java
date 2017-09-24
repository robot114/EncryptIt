package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public interface Target {
	
	OutputStream openOutputStream() throws GeneralSecurityException, IOException;

	String displayName();
	
}
