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
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import com.zsm.log.Log;

public class KeyPasswordInOutDecorator implements InOutDecorator {

	private Key key;
	private char[] password;
	private Key pbeKey;
	private PBEParameterSpec pbeParamSpec;
	private AlgorithmParameterSpec keyParamSpec;
	private String symmetricAlgorithm;
	private String pbeAlgorithm;
	
	public KeyPasswordInOutDecorator( String symmetricAlgorithm,
									   String pbeAlgorithm,
									   Key key,
									   byte[] keyIv,
									   char[] password,
									   byte[] pbeSalt,
									   int pbeIterationCount )
				throws NoSuchAlgorithmException, NoSuchPaddingException,
							InvalidKeySpecException {
		
		this.key = key;
		this.password = Arrays.copyOf(password, password.length);
		this.symmetricAlgorithm = symmetricAlgorithm;
		this.pbeAlgorithm = pbeAlgorithm;
		
		keyParamSpec = new IvParameterSpec( keyIv );
		
		pbeParamSpec = new PBEParameterSpec(pbeSalt, pbeIterationCount);
		pbeKey = generatePBEKey( pbeAlgorithm );
	}
	
	private Key generatePBEKey( String pbeAlgorithm )
				throws NoSuchAlgorithmException, InvalidKeySpecException {
	
		PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
		SecretKeyFactory keyFac = SecretKeyFactory.getInstance(pbeAlgorithm);
		return keyFac.generateSecret(pbeKeySpec);
	}
	
	@Override
	public InputStream wrapInputStream(InputStream in) throws IOException {
		Cipher keyInCipher;
		Cipher pbeInCipher;
		
		try {
			keyInCipher = getInKeyCipher();
			pbeInCipher = getInPbeCipher();
		} catch (InvalidKeyException | InvalidAlgorithmParameterException
				 | NoSuchAlgorithmException | NoSuchPaddingException e) {
			
			Log.e( e, "Error when wrap to the key input stream" );
			throw new IOException( e );
		}
		InputStream keyIn = new CipherInputStream(in, keyInCipher);
		
		return new CipherInputStream(keyIn, pbeInCipher);
	}

	@Override
	public DataOutputStream wrapOutputStream(OutputStream out)
				throws IOException {
		
		Cipher keyOutCipher;
		Cipher pbeOutCipher;
		try {
			keyOutCipher = getOutKeyCipher();
			pbeOutCipher = getOutPbeCipher();
		} catch (InvalidKeyException | InvalidAlgorithmParameterException
				 | NoSuchAlgorithmException | NoSuchPaddingException e) {
			
			Log.e( e, "Error when wrap to the key output stream" );
			throw new IOException( e );
		}
		
		OutputStream keyOut = new CipherOutputStream(out, keyOutCipher);
		
		return new DataOutputStream(new CipherOutputStream(keyOut, pbeOutCipher));
	}

	private Cipher getInPbeCipher()
				throws NoSuchAlgorithmException, NoSuchPaddingException,
					   InvalidKeyException, InvalidAlgorithmParameterException {
		
		Cipher pbeInCipher;
		pbeInCipher = Cipher.getInstance(pbeAlgorithm);
		pbeInCipher.init( Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec );
		return pbeInCipher;
	}

	private Cipher getInKeyCipher()
				throws NoSuchAlgorithmException, NoSuchPaddingException,
					   InvalidKeyException, InvalidAlgorithmParameterException {
		
		Cipher keyInCipher;
		keyInCipher = Cipher.getInstance(symmetricAlgorithm);
		keyInCipher.init( Cipher.DECRYPT_MODE, key, keyParamSpec);
		return keyInCipher;
	}

	private Cipher getOutPbeCipher()
				throws NoSuchAlgorithmException, NoSuchPaddingException,
					   InvalidKeyException, InvalidAlgorithmParameterException {
		
		Cipher pbeOutCipher;
		pbeOutCipher = Cipher.getInstance(pbeAlgorithm);
		pbeOutCipher.init( Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec );
		return pbeOutCipher;
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
			byte[] keyData = keyCipher.doFinal(data);
			Cipher pbeCipher = getOutPbeCipher();
			return pbeCipher.doFinal( keyData );
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
			byte[] keyData = keyCipher.doFinal(data);
			Cipher pbeCipher = getInPbeCipher();
			return pbeCipher.doFinal( keyData );
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException
				| IllegalBlockSizeException | BadPaddingException e) {
			
			Log.e( e, "Error happen when decode the data" );
			throw new IOException( e );
		}
	}
}
