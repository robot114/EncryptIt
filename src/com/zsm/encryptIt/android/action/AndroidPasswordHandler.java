package com.zsm.encryptIt.android.action;

import java.security.GeneralSecurityException;
import java.security.Key;

import android.content.Context;
import android.content.Intent;

import com.zsm.encryptIt.action.KeyAction;
import com.zsm.encryptIt.ui.LoginActivity;
import com.zsm.encryptIt.ui.PasswordActivity;
import com.zsm.security.PasswordHandler;

public class AndroidPasswordHandler implements PasswordHandler {
	
	@Override
	public void promptPassword(Object context) throws GeneralSecurityException {
		PasswordPromptParameter param = (PasswordPromptParameter)context;
		
		Intent intent;
		Context appContext = param.getAppContext();
		if( KeyAction.getInstance().keyExist() ) {
			intent = new Intent( appContext, LoginActivity.class );
		} else {
			intent = new Intent( appContext, PasswordActivity.class );
			intent.putExtra(PasswordActivity.KEY_TYPE, PasswordActivity.TYPE_INIT);
		}
		
		param.getParent()
			.startActivityForResult( intent, param.getRequestCode() );
	}

	@Override
	public Key getKey(Object context) {
		PasswordPromptParameter param = (PasswordPromptParameter)context;
		Intent intent = param.getData();
		
		Key key = (Key)intent.getSerializableExtra(KEY_KEY);
		return key;
	}

	@Override
	public void promptChangePassword(Object context) throws GeneralSecurityException {
		PasswordPromptParameter param = (PasswordPromptParameter)context;
		Intent intent = new Intent( param.getAppContext(), PasswordActivity.class );
		intent.putExtra(PasswordActivity.KEY_TYPE, PasswordActivity.TYPE_CHANGE);
		
		param.getParent()
			.startActivityForResult( intent, param.getRequestCode() );
	}

}
