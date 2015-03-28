package com.zsm.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.zsm.log.Log;

public enum KeyManager {

	instance;		// Singleton of this class
	
	private static final int KEY_SIZE = 128;		// bits
	private static final String KEY_ALGORITHM = "AES";
	private static final String KEYSTORE_YTPE = KeyStore.getDefaultType();
	private static final String KEYSTORE_FILE_NAME = "293178926451020105";
	private static final String KEY_FOR_PRIMARYKEY = "primary_key";
	private static final String KEY_FOR_SECONDARYKEY = "secondary_key";
	
	private String path;

	private File ksFile;

	private KeyStore keystore;
	private char[] ksPassword;
	
	private KeyManager() {
		
	}
	
	public static KeyManager getInstance() {
		return instance;
	}
	
	/**
	 * Initialize the key manager. In this method, a the keys will be loaded,
	 * if the key store exists. Otherwise, a new empty key store will be created.
	 * The key store type is 'KEYSTORE_YTPE' of the constant in this class.
	 * When the key store is stored, it will be stored in the ksPath parameter
	 * that passed in. A key manager can only be initialized once.
	 * 
	 * @param ksPath key store path
	 * @param ksPassword password to protect the key store.
	 * @throws IOException if a problem occurred while reading from the stream
	 * @throws KeyStoreException if the key store type were not supported
	 * @throws CertificateException if the key store password is invalid
	 * @throws NoSuchAlgorithmException  if the required algorithm is not 
	 * 									 available when read the key store 
	 */
	public void initialize( String ksPath, char[] ksPassword )
					throws IOException, KeyStoreException, CertificateException,
							NoSuchAlgorithmException {
		if( path != null ) {
			throw new IllegalStateException( "KeyStore has been initialized!" );
		}
		path = ksPath;
		ksFile = new File( path + "/" + KEYSTORE_FILE_NAME );
		
		this.ksPassword = ksPassword;
		loadKeys();
	}
	
	private void loadKeys()
			throws KeyStoreException, CertificateException, IOException, 
					NoSuchAlgorithmException {
		
		if( keystore != null ) {
			return;
		}
		keystore = KeyStore.getInstance( KEYSTORE_YTPE );
		Log.d( "Type of keystore. ", KEYSTORE_YTPE );
		
		InputStream is = null;
		try {
			is = new FileInputStream( ksFile );
		} catch ( IOException e ) {
			Log.d( "KeyStore file not found, an empty inputstream will be used." );
		}
		try {
			keystore.load( is, ksPassword );
		} finally {
			if( is != null ) {
				is.close();
				is = null;
			}
		}
	}

	/**
	 * Check if the key with the given alias exist.
	 * 
	 * @return true, when the key exist
	 * @throws KeyStoreException if this KeyStore is not initialized.
	 */
	public boolean primaryKeyExists( ) throws KeyStoreException {
		return keystore.containsAlias(KEY_FOR_PRIMARYKEY);
	}

	/**
	 * Check if the key with the given alias exist.
	 * 
	 * @return true, when the key exist
	 * @throws KeyStoreException if this KeyStore is not initialized.
	 */
	public boolean secondaryKeyExists( ) throws KeyStoreException {
		return keystore.containsAlias( KEY_FOR_SECONDARYKEY);
	}
	
	/**
	 * Get the specified key. If this key does not exist, a new key will be
	 * generated. And the new key will be stored after it is generated. 
	 * The algorithm is specified with KEY_ALGORITHM
	 * 
	 * @param password to protect the key. And for most case, this password
	 * 					will be the password for whole system.
	 * @return the key
	 * @throws KeyStoreException if this KeyStore is not initialized
	 * @throws UnrecoverableKeyException if the password is invalid
	 * @throws NoSuchAlgorithmException	if the specified key algorithm is 
	 * 									not available by any provider.
	 * @throws CertificateException  if an exception occurred while storing the
	 * 								 certificates of this KeyStore.
	 * @throws IOException when store the key store failed
	 */
	public Key getPrimaryKey( char[] password )
			throws KeyStoreException, UnrecoverableKeyException,
				   NoSuchAlgorithmException, CertificateException, IOException {
		
		return getKey(password, KEY_FOR_PRIMARYKEY);
	}

	/**
	 * Get the specified key. If this key does not exist, a new key will be
	 * generated. And the new key will be stored after it is generated. 
	 * The algorithm is specified with KEY_ALGORITHM
	 * 
	 * @param password to protect the key. And for most case, this password
	 * 					will be the password for whole system.
	 * @return the key
	 * @throws KeyStoreException if this KeyStore is not initialized
	 * @throws UnrecoverableKeyException if the password is invalid
	 * @throws NoSuchAlgorithmException	if the specified key algorithm is 
	 * 									not available by any provider.
	 * @throws CertificateException  if an exception occurred while storing the
	 * 								 certificates of this KeyStore.
	 * @throws IOException when store the key store failed
	 */
	public Key getSecondaryKey( char[] password )
			throws KeyStoreException, UnrecoverableKeyException,
				   NoSuchAlgorithmException, CertificateException, IOException {
		
		return getKey(password, KEY_FOR_SECONDARYKEY);
	}
	
	/**
	 * Change the password of specified key. The new password will be stored 
	 * after it is generated.
	 * 
	 * @param oldPassword the old password to recover the key
	 * @param newPassword the new password to protect the key
	 * @throws KeyStoreException if this KeyStore is not initialized
	 * @throws UnrecoverableKeyException if the old password is invalid
	 * @throws NoSuchAlgorithmException	if the specified key algorithm is 
	 * 									not available by any provider.
	 * @throws CertificateException  if an exception occurred while storing the
	 * 								 certificates of this KeyStore.
	 * @throws IOException when store the key store failed
	 */
	public void changePrimaryKeyPassword( char[] oldPassword,
									      char[] newPassword )
					throws KeyStoreException, UnrecoverableKeyException,
						   NoSuchAlgorithmException, CertificateException,
						   IOException, KeyManagementException {
		
		changePassword(KEY_FOR_PRIMARYKEY, oldPassword, newPassword);
	}

	public void changeSecondaryKeyPassword( char[] ksPassword, char[] oldPassword,
											char[] newPassword )
					throws KeyStoreException, UnrecoverableKeyException,
						   NoSuchAlgorithmException, CertificateException,
						   IOException, KeyManagementException {
		
		changePassword(KEY_FOR_SECONDARYKEY, oldPassword, newPassword);
	}
	
	private void changePassword( String alias, char[] oldPassword,
								 char[] newPassword )
					throws KeyStoreException, CertificateException,
							NoSuchAlgorithmException, IOException,
							KeyManagementException, UnrecoverableKeyException {
		
		if (keystore.containsAlias(alias)) {
			Key key = keystore.getKey(alias, oldPassword);
			keystore.setKeyEntry(alias, key, newPassword, null);
			storeKeys();
		} else {
			throw new KeyManagementException("Key not found! Alias is " + alias);
		}
	}

	private Key getKey(char[] password, String alias)
			throws KeyStoreException, CertificateException, IOException,
			NoSuchAlgorithmException, UnrecoverableKeyException {
		
		if( keystore.containsAlias( alias ) ) {
			return keystore.getKey( alias, password);
		} else {
			Key key = generateKey();
			keystore.setKeyEntry(alias, key, password, null);
			storeKeys( );
			return key;
		}
	}

	private Key generateKey() throws NoSuchAlgorithmException {
		KeyGenerator keygen = KeyGenerator.getInstance(KEY_ALGORITHM);
		keygen.init(KEY_SIZE);
		SecretKey key = keygen.generateKey();
		return key;
	}

	private void storeKeys()
					throws KeyStoreException, NoSuchAlgorithmException,
							CertificateException, IOException {
		
		OutputStream os = null;
		try {
			os = new FileOutputStream( ksFile );
		} catch ( IOException e ) {
			Log.e( e, "KeyStore file cannot be written." );
			throw new KeyStoreException( e );
		}
		
		try {
			keystore.store( os, ksPassword );
		} finally {
			if( os != null ) {
				os.close();
				os = null;
			}
		}
	}

	/**
	 * Remove the key store.
	 */
	public void removeKeyStore() {
		ksFile.delete();
	}
	
	/**
	 * Backup the whole key store to the specified file.
	 * @param file the target file
	 * @throws IOException error occurred when copying
	 */
	public void backupKeyStore( File file ) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(ksFile);
			output = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} finally {
			input.close();
			output.close();
		}
	}
}
