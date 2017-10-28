package com.zsm.encryptIt.action;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public abstract class KeyAction {
	
	private static KeyAction instance;
	
	protected static void setInstance( KeyAction instance ) {
		KeyAction.instance = instance;
	}
	
	public static KeyAction getInstance() {
		return instance;
	}

	/**
	 * Initialize the actor. It can only be initialized once. In this method
	 * the context and the handler passed in will be store for later use.
	 * 
	 * @param context context for later use
	 * @throws GeneralSecurityException initialized failed. If the initialization
	 * 									took place before, this exception will
	 * 									be thrown either.
	 * @throws IOException some io error occurred
	 */
	abstract public void initialize( Object context )
			throws GeneralSecurityException, IOException;
	
	/**
	 * Reinitialize the actor. It will be invoked when the key store is replaced.
	 * The context passed when the method {@link initialize} invoked, will be
	 * reused, and need not to pass here.
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 */
	abstract public void reinitialize()
				throws KeyStoreException, CertificateException,
					   NoSuchAlgorithmException, IOException;
	
	/**
	 * Check if the key exists. If it does not exist, the invoker should prompt
	 * the user set password.
	 * 
	 * @return true, if the key exists; false, otherwise
	 */
	abstract public boolean keyExist();
	
	/**
	 * Get the key from key store. It may prompt the user inputing or initializing
	 * the password when necessary.
	 * 
	 * @param keyPassword to get the key
	 * @return key to encrypt or dencrypt. null, when the password is invalid. 
	 * @throws GeneralSecurityException when some unrecoverable errors occurred
	 */
	abstract public Key getKey( char[] keyPassword ) throws GeneralSecurityException;
	
	/**
	 * Change the password.
	 * 
	 * @param oldKeyPassword password current usd
	 * @param newKeyPassword password to be set
	 * @return true, when success
	 */
	abstract public boolean changePassword( char[] oldKeyPassword,
											char[] newKeyPassword );

}
