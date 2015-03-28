package com.zsm.security;

import java.security.GeneralSecurityException;
import java.security.Key;


/**
 * The interface about the user deal with the password. For a text based
 * implementation, the method will print the text and ask the password.
 * For a GUI implementation, a dialog will pop up and make the user input
 * the password in edit components.
 * <p>In this interface, all the methods should not verify the password.
 * It is the inoker's response to verify it. But it is these methods' response
 * to check if the password satisfies the security policies.
 * 
 * @author zsm
 *
 */
public interface PasswordHandler {

	public static final String KEY_KEY = "KEY";
	public static final String KEY_OLD_PASSWORD = "PASSWORD_OLD";
	public static final String KEY_NEW_PASSWORD = "PASSWORD_NEW";
	public static final String PASSWORD_POLICY_KEY = "PASSWORD_POLICY";

	/**
	 * Give the user a prompt to initialize or input the password. The main
	 * thread should be blocked in this method until the user input the password.
	 * After go out of this method the method {@link getKey} can be invoked 
	 * to return the key. If any error occurred, this method will be terminated
	 * either. 
	 * 
	 * @param context context for password input
	 * @throws GeneralSecurityException error occurred when the user 
	 * 					inputs the password or the system checks the password
	 */
	void promptPassword(Object context) throws GeneralSecurityException;

	/**
	 * Get the key after password is initialized or input.
	 * 
	 * @param context context for to get the key
	 * @return the key, if everything is OK; null, if the user canceled or 
	 * 			some error occurred
	 */
	Key getKey( Object context );
	
	/**
	 * Give the user a prompt to input the current password, and input the new
	 * password.
	 * 
	 * @param context for password change
	 * @throws GeneralSecurityException error occurred when the user 
	 * 					inputs the current password or the system checks the password
	 */
	void promptChangePassword( Object context ) throws GeneralSecurityException;
}
