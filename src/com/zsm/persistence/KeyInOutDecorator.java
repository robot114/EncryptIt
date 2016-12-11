package com.zsm.persistence;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;

import com.zsm.log.Log;

public class KeyInOutDecorator implements InOutDecorator {

	private Key key;
	private AlgorithmParameterSpec keyParamSpec;
	private String symmetricAlgorithm;
	
	public KeyInOutDecorator( String symmetricAlgorithm, Key key, byte[] keyIv )
				throws NoSuchAlgorithmException, NoSuchPaddingException,
							InvalidKeySpecException {
		
		this.key = key;
		this.symmetricAlgorithm = symmetricAlgorithm;
		keyParamSpec = new IvParameterSpec( keyIv );
	}
	
	@Override
	public InputStream wrapInputStream(InputStream in) throws IOException {
		Cipher keyInCipher;
		
		try {
			keyInCipher = getInKeyCipher();
		} catch (InvalidKeyException | InvalidAlgorithmParameterException
				 | NoSuchAlgorithmException | NoSuchPaddingException e) {
			
			Log.e( e, "Error when wrap to the key input stream" );
			throw new IOException( e );
		}
		return new CipherInputStream(in, keyInCipher);
	}

	@Override
	public DataOutputStream wrapOutputStream(OutputStream out)
				throws IOException {
		
		Cipher keyOutCipher;
		try {
			keyOutCipher = getOutKeyCipher();
		} catch (InvalidKeyException | InvalidAlgorithmParameterException
				 | NoSuchAlgorithmException | NoSuchPaddingException e) {
			
			Log.e( e, "Error when wrap to the key output stream" );
			throw new IOException( e );
		}
		
		return new DataOutputStream(new CipherOutputStream(out, keyOutCipher));
	}

	private Cipher getInKeyCipher()
				throws NoSuchAlgorithmException, NoSuchPaddingException,
					   InvalidKeyException, InvalidAlgorithmParameterException {
		
		Cipher keyInCipher;
		keyInCipher = Cipher.getInstance(symmetricAlgorithm);
		keyInCipher.init( Cipher.DECRYPT_MODE, key, keyParamSpec);
		return keyInCipher;
	}

	private Cipher getOutKeyCipher()
				throws NoSuchAlgorithmException, NoSuchPaddingException,
					   InvalidKeyException, InvalidAlgorithmParameterException {
		
		Cipher keyOutCipher;
		keyOutCipher = Cipher.getInstance(symmetricAlgorithm);
		keyOutCipher.init( Cipher.ENCRYPT_MODE, key, keyParamSpec);
		return keyOutCipher;
	}

	@Override
	public byte[] encode(byte[] data) throws IOException {
		
		try {
			Cipher keyCipher = getOutKeyCipher();
			return keyCipher.doFinal(data);
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException
				| IllegalBlockSizeException | BadPaddingException e) {
			
			Log.e( e, "Error happen when encode the data" );
			throw new IOException( e );
		}
	}

	@Override
	public byte[] decode(byte[] data) throws IOException {
		try {
			Cipher keyCipher = getInKeyCipher();
			return keyCipher.doFinal(data);
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException
				| IllegalBlockSizeException | BadPaddingException e) {
			
			Log.e( e, "Error happen when decode the data" );
			throw new IOException( e );
		}
	}
}
