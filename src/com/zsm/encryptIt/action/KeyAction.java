package com.zsm.encryptIt.action;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;

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
	 * Check if the key exists. If it does not exist, the invoker should prompt
	 * the user set password.
	 * 
	 * @return true, if the key exists; false, otherwise
	 * @throws GeneralSecurityException when cannot know if the key exist
	 */
	abstract public boolean keyExist() throws GeneralSecurityException;
	
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
