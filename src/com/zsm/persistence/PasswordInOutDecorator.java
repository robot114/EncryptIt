package com.zsm.persistence;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import com.zsm.log.Log;

public class PasswordInOutDecorator implements InOutDecorator {

	private Key pbeKey;
	private PBEParameterSpec pbeParamSpec;
	private String pbeAlgorithm;
	
	public PasswordInOutDecorator( String pbeAlgorithm,
									   char[] password,
									   byte[] pbeSalt,
									   int pbeIterationCount )
				throws NoSuchAlgorithmException, NoSuchPaddingException,
							InvalidKeySpecException {
		
		this.pbeAlgorithm = pbeAlgorithm;
		
		pbeParamSpec = new PBEParameterSpec(pbeSalt, pbeIterationCount);
		pbeKey = generatePBEKey( password, pbeAlgorithm );
	}
	
	private Key generatePBEKey( char[] password, String pbeAlgorithm )
				throws NoSuchAlgorithmException, InvalidKeySpecException {
	
		PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
		SecretKeyFactory keyFac = SecretKeyFactory.getInstance(pbeAlgorithm);
		return keyFac.generateSecret(pbeKeySpec);
	}
	
	@Override
	public InputStream wrapInputStream(InputStream in) throws IOException {
		Cipher pbeInCipher;
		
		try {
			pbeInCipher = getInPbeCipher();
		} catch (InvalidKeyException | InvalidAlgorithmParameterException
				 | NoSuchAlgorithmException | NoSuchPaddingException e) {
			
			Log.e( e, "Error when wrap to the key input stream" );
			throw new IOException( e );
		}
		return new CipherInputStream(in, pbeInCipher);
	}

	@Override
	public DataOutputStream wrapOutputStream(OutputStream out)
				throws IOException {
		
		Cipher pbeOutCipher;
		try {
			pbeOutCipher = getOutPbeCipher();
		} catch (InvalidKeyException | InvalidAlgorithmParameterException
				 | NoSuchAlgorithmException | NoSuchPaddingException e) {
			
			Log.e( e, "Error when wrap to the key output stream" );
			throw new IOException( e );
		}
		
		OutputStream keyOut = new CipherOutputStream(out, pbeOutCipher);
		
		return new DataOutputStream(keyOut);
	}

	private Cipher getInPbeCipher()
				throws NoSuchAlgorithmException, NoSuchPaddingException,
					   InvalidKeyException, InvalidAlgorithmParameterException {
		
		Cipher pbeInCipher;
		pbeInCipher = Cipher.getInstance(pbeAlgorithm);
		pbeInCipher.init( Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec );
		return pbeInCipher;
	}

	private Cipher getOutPbeCipher()
				throws NoSuchAlgorithmException, NoSuchPaddingException,
					   InvalidKeyException, InvalidAlgorithmParameterException {
		
		Cipher pbeOutCipher;
		pbeOutCipher = Cipher.getInstance(pbeAlgorithm);
		pbeOutCipher.init( Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec );
		return pbeOutCipher;
	}

	@Override
	public byte[] encode(byte[] data) throws IOException {
		
		try {
			Cipher pbeCipher = getOutPbeCipher();
			return pbeCipher.doFinal( data );
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
			Cipher pbeCipher = getInPbeCipher();
			return pbeCipher.doFinal( data );
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException
				| IllegalBlockSizeException | BadPaddingException e) {
			
			// May bad password
			throw new IOException( e );
		}
	}
}
