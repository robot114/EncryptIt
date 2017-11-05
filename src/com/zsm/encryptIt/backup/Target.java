package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

public interface Target {
	
	OutputStream openOutputStream( String mode )
			throws GeneralSecurityException, IOException;
	
	void outputHeader( OutputStream out ) throws IOException;

	String displayName();
	
}
