package com.zsm.encryptIt.android.action;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.zsm.encryptIt.action.KeyAction;
import com.zsm.log.Log;
import com.zsm.security.KeyManager;

public class AndroidKeyActor extends KeyAction {
	
	private static char[] ksPassword;
	
	private String ksPath;
	private Context context;
	
	private AndroidKeyActor(){
	}
	
	public static void installInstance( Context context )
							throws GeneralSecurityException, IOException {
		
		if( ksPassword != null ) {
			// Initialized
			Log.d( "Key store's password has been initialized!" );
			return;
		}
		TelephonyManager mngr
			= (TelephonyManager) ((Context) context).getSystemService(
					Context.TELEPHONY_SERVICE);
		
		ksPassword = mngr.getDeviceId().toCharArray();
		KeyAction.setInstance( new AndroidKeyActor() );
		KeyAction.getInstance().initialize( context );
	}

	@Override
	public void initialize(Object context)
					throws GeneralSecurityException, IOException {
		
		this.context = (Context)context;
		
		ksPath = (this.context).getApplicationInfo().dataDir;
		
		KeyManager.getInstance().initialize( ksPath, ksPassword );
	}
	
	@Override
	public void reinitialize( )
					throws KeyStoreException, CertificateException,
						   NoSuchAlgorithmException, IOException {
		
		KeyManager.getInstance().reopen();
	}
	
	@Override
	public boolean keyExist() {
		try {
			return KeyManager.getInstance().primaryKeyExists();
		} catch (KeyStoreException e) {
			Log.w( e, "Cannot know if the key exists. The key will be done as no existed!" );
			return false;
		}
	}

	@Override
	public Key getKey( char[] keyPassword ) throws GeneralSecurityException {
		Key key = null;
		try {
			key = KeyManager.getInstance().getPrimaryKey(keyPassword);
		} catch ( UnrecoverableKeyException e ) {
			Log.w( "Invalid password!" );
			return null;
		} catch (IOException e) {
			Log.e(e, "Cannot get key from key store!");
			throw new GeneralSecurityException( e );
		}
		
		return key;
	}

	@Override
	public boolean changePassword(char[] oldKeyPassword, char[] newKeyPassword) {
		try {
			KeyManager.getInstance()
				.changePrimaryKeyPassword(oldKeyPassword, newKeyPassword);
			
			return true;
		} catch (UnrecoverableKeyException | KeyManagementException
				| KeyStoreException | NoSuchAlgorithmException
				| CertificateException | IOException e) {
			
			Log.e( e, "Change password failed!" );
			return false;
		}
	}

}
