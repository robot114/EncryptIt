package com.zsm.persistence;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;

public class KeyPasswordInOutDecorator implements InOutDecorator {

	private KeyInOutDecorator mKeyInOutDecorator;
	private PasswordInOutDecorator mPasswordInOutDecorator;
	
	public KeyPasswordInOutDecorator( String symmetricAlgorithm,
									   String pbeAlgorithm,
									   Key key,
									   byte[] keyIv,
									   char[] password,
									   byte[] pbeSalt,
									   int pbeIterationCount )
				throws NoSuchAlgorithmException, NoSuchPaddingException,
							InvalidKeySpecException {
		
		mKeyInOutDecorator = new KeyInOutDecorator(symmetricAlgorithm, key, keyIv );
		mPasswordInOutDecorator
			= new PasswordInOutDecorator(pbeAlgorithm, password, pbeSalt,
										 pbeIterationCount);
	}
	
	@Override
	public InputStream wrapInputStream(InputStream in) throws IOException {
		InputStream keyIn = mKeyInOutDecorator.wrapInputStream(in);
		return mPasswordInOutDecorator.wrapInputStream(keyIn);
	}

	@Override
	public DataOutputStream wrapOutputStream(OutputStream out) throws IOException {
		
		OutputStream keyOut = mKeyInOutDecorator.wrapOutputStream(out);
		return mPasswordInOutDecorator.wrapOutputStream(keyOut);
	}

	@Override
	public byte[] encode(byte[] data) throws IOException {
		
		byte[] keyData = mKeyInOutDecorator.encode(data);
		return mPasswordInOutDecorator.encode(keyData);
	}

	@Override
	public byte[] decode(byte[] data) throws IOException {
		byte[] keyData = mKeyInOutDecorator.decode(data);
		return mPasswordInOutDecorator.decode(keyData);
	}
}
