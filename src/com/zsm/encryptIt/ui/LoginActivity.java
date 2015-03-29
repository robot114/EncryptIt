package com.zsm.encryptIt.ui;

import java.security.Key;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.security.PasswordHandler;

public class LoginActivity extends SecurityActivity {

	final public static String KEY_LOGIN_TYPE = "KEY_LOGIN_TYPE";
	final public static int LOGIN_LOGIN = 1;
	final public static int LOGIN_UNLOCK = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.login );
		
		passwordAllowToTry
			= ((EncryptItApplication)getApplication()).maxPasswordRetries();
		
		final TextView passwordTextView
			= (TextView)findViewById( R.id.loginPasswordTextView );
		passwordTextView.setOnEditorActionListener( new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				doLogin( passwordTextView );
				return true;
			}
		} );
		
		findViewById( R.id.loginOkButton )
			.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				doLogin(passwordTextView);
			}
		} );
		findViewById( R.id.loginCancelButton )
			.setOnClickListener( new OnClickListener() {
	
			@Override
			public void onClick(View v) {
				LoginActivity.this.setResult( LOGIN_FAILED );
				finish();
			}
		} );
	}

	private void doLogin(final TextView passwordTextView) {
		char[] password = passwordTextView .getText().toString().toCharArray();
		Key key = checkPasswordAndGetKey( password );
		if( key != null ) {
			Intent intent = new Intent( Intent.ACTION_PICK );
			intent.putExtra( PasswordHandler.KEY_KEY, key );
			setResult(RESULT_OK, intent);
			finish();
			return;
		} else if( passwordTriedTooMuch() ) {
			setResult( LOGIN_FAILED );
			finish();
			return;
		} else {
			// Can try more times
		}
	}
}
