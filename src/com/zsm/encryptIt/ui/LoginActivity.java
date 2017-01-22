package com.zsm.encryptIt.ui;

import java.security.Key;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.security.PasswordHandler;

public class LoginActivity extends SecurityActivity {

	final public static String KEY_PASSWORD_TYPE = "KEY_PASSWORD_TYPE";
	final public static int TYPE_LOGIN = 1;
	final public static int TYPE_PROMPT = 2;
	
	private RelativeLayout buttonLayout;
	private VisiblePassword passwordView;
	private Button okButton;
	private Button cancelButton;
	private int passwordType;
	
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
		
		okButton = (Button)findViewById( R.id.loginOkButton );
		okButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				doLogin(passwordView.getPassword().toCharArray());
			}
		} );
		cancelButton = (Button)findViewById( R.id.loginCancelButton );
		cancelButton.setOnClickListener( new OnClickListener() {
	
			@Override
			public void onClick(View v) {
				int result
					= passwordType == TYPE_LOGIN 
					  ? PasswordPromptParameter.LOGIN_FAILED 
						: RESULT_CANCELED;
					  
				setResult( result );
				finish();
			}
		} );
		
		buttonLayout = (RelativeLayout)findViewById( R.id.loginButtonLayout );
	}

	@Override
	protected void onResume() {
		super.onResume();
		passwordType = getIntent().getIntExtra( KEY_PASSWORD_TYPE, TYPE_LOGIN );
		switch( passwordType ) {
			case TYPE_PROMPT:
				okButton.setText( R.string.login );
				cancelButton.setText( R.string.quit );
				break;
			case TYPE_LOGIN:
			default:
				okButton.setText( android.R.string.ok );
				cancelButton.setText( android.R.string.cancel );
				break;
		}
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
			setResult( PasswordPromptParameter.LOGIN_FAILED );
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

	@Override
	public void onBackPressed() {
		setResult( PasswordPromptParameter.LOGIN_FAILED );
		super.onBackPressed();
	}

	@Override
	protected boolean needPromptPassword() {
		return false;
	}

}
