package com.zsm.encryptIt.android;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;

import android.content.Context;

import com.zsm.encryptIt.EncryptItPersistence;
import com.zsm.encryptIt.SystemParameter;
import com.zsm.encryptIt.app.EncryptItApplication;

public class AndroidPersistence extends EncryptItPersistence {

	public AndroidPersistence( Context context, Key key )
			throws NoSuchAlgorithmException, NoSuchPaddingException,
					InvalidKeySpecException {
		
		super();
		((EncryptItApplication)context).initEncryptSetting(context, key);
		setInOutDecorator( SystemParameter.getEncryptInOutDecorator() );
	}
}
