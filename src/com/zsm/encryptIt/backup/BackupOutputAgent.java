package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;

public interface BackupOutputAgent {
	OutputStream openOutputStream() 
		throws FileNotFoundException, NoSuchAlgorithmException,
			   NoSuchPaddingException, InvalidKeySpecException,
			   IOException;

	String displayName();
	
}
