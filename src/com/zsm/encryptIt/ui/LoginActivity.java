package com.zsm.encryptIt.ui;

import java.security.Key;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.security.PasswordHandler;

public class LoginActivity extends SecurityActivity {

	final public static String KEY_LOGIN_TYPE = "KEY_LOGIN_TYPE";
	private RelativeLayout buttonLayout;
	private View okButton;
	private VisiblePassword passwordView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.login );
		
		passwordAllowToTry
			= ((EncryptItApplication)getApplication()).maxPasswordRetries();
		
		passwordView = (VisiblePassword)findViewById( R.id.loginPassword );
		passwordView.setOnEditorActionListener( new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				doLogin( passwordView.getPassword().toCharArray() );
				return true;
			}
		} );
		
		okButton = findViewById( R.id.loginOkButton );
		okButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				doLogin(passwordView.getPassword().toCharArray());
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
		
		buttonLayout = (RelativeLayout)findViewById( R.id.loginButtonLayout );
	}

	private void doLogin(final char[] password) {
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setButtonLayoutHeight();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if( hasFocus ) {
			setButtonLayoutHeight();
		}
	}
	
	private void setButtonLayoutHeight() {
		LayoutParams params = (LayoutParams) buttonLayout.getLayoutParams();
		params.height
			= okButton.getHeight() + passwordView.getHeight() + params.topMargin;
		buttonLayout.setLayoutParams(params);
	}

	protected boolean needPromptPassword() {
		return false;
	}

}
