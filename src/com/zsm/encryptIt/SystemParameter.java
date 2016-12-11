package com.zsm.encryptIt;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;

import com.zsm.log.Log;
import com.zsm.persistence.KeyPasswordInOutDecorator;
import com.zsm.persistence.InOutDecorator;
import com.zsm.persistence.PasswordInOutDecorator;

public class SystemParameter {

	private static final String SYMMETRIC_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final String PBE_ALGORITHM = "PBEWithMD5AndDES";

    private static final byte keyIv[] = {
        (byte)0x91, (byte)0x07, (byte)0x36, (byte)0x6B,
        (byte)0x88, (byte)0xB3, (byte)0x1C, (byte)0x7C,
        (byte)0x18, (byte)0x8D, (byte)0x8E, (byte)0xBB,
        (byte)0x84, (byte)0xA7, (byte)0x0A, (byte)0x72,
    };
    
	private static final byte[] pbeSalts = new byte[] {
        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
    };
	
	private static InOutDecorator inOutDecorator;
	
	/**
	 * This method can make all the persistence on different platform having
	 * the same security algorithm.
	 * 
	 * @param key key to encrypt and unencrypt. It is different for each instance
	 * 			of this application
	 * @param password of the pbekey. For different platform, the password is
	 * 			 different
	 * @throws NoSuchAlgorithmException if the JCA providers do not support
	 * 			 the algorithm
	 * @throws NoSuchPaddingException if the  JCA providers do not support
	 * 			 the padding algorithm
	 * @throws InvalidKeySpecException if the key is invalid
	 */
	static public void initEncryptSetting( Key key, char[] password )
					throws NoSuchAlgorithmException, NoSuchPaddingException,
							InvalidKeySpecException {
		
		if( inOutDecorator != null ) {
			Log.w( "The InOutDecorator has been initialized!"
				   + " The old one will be used!");
			
			return;
		}
		inOutDecorator
			= new KeyPasswordInOutDecorator( SYMMETRIC_ALGORITHM,
											 PBE_ALGORITHM,
											 key,
											 keyIv,
											 password,
											 pbeSalts,
											 20 );
	}
	
	/**
	 * Return the stream decorator generated by {@link #code initEncryptSetting}
	 * @return stream decorator generated by {@link #code initEncryptSetting}
	 */
	static public InOutDecorator getEncryptInOutDecorator() {
		return inOutDecorator;
	}
	
	static public InOutDecorator getPasswordBasedInOutDecorator( char[] password )
					throws NoSuchAlgorithmException, NoSuchPaddingException, 
							InvalidKeySpecException {
		
		return new PasswordInOutDecorator( PBE_ALGORITHM, password, pbeSalts, 20 );
	}
}
