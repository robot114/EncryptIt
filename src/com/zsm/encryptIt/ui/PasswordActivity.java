package com.zsm.encryptIt.ui;

import java.security.Key;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.log.Log;
import com.zsm.security.PasswordHandler;
import com.zsm.security.PasswordPolicy;

public class PasswordActivity extends SecurityActivity {

	public static final String KEY_TYPE = "PASSWORD_ACTIVITY_TYPE";
	public static final String KEY_PASSWORD = "PASSWORD";
	public static final int TYPE_INIT = 1;
	public static final int TYPE_CHANGE = 2;

	private TextView newTextView;
	private TextView confirmTextView;
	private TextView hintTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.prompt_password );
		
		passwordAllowToTry
			= ((EncryptItApplication)getApplication()).maxPasswordRetries();
		
		newTextView = (TextView)findViewById( R.id.promptNewPasswordTextView );
		confirmTextView = (TextView)findViewById( R.id.promptConfirmPasswordTextView );
		hintTextView = (TextView)findViewById( R.id.promptPasswordHint );
		hintTextView.setText("");
		
		TextWatcher tw = new PasswordTransformationMethod(){
			@Override
			public void afterTextChanged(Editable s) {
				int id = isPasswordSafity();
				if( id > 0 ) {
					hintTextView.setText( getResources().getString(id) );
				} else {
					hintTextView.setText( "" );
				}
			}
		};
		newTextView.addTextChangedListener( tw );
		confirmTextView.addTextChangedListener(tw);
		
		final TextView oldPasswordView
			= (TextView)findViewById( R.id.promptOldPasswordTextView );
		final Button cancelButton = (Button)findViewById( R.id.promptCancelButton );
		
		Intent intent = getIntent();
		final int type = intent.getIntExtra(KEY_TYPE, -1);
		if( type == TYPE_INIT ) {
			findViewById( R.id.promptOldPasswordLabel ).setVisibility( View.INVISIBLE );
			oldPasswordView.setVisibility( View.INVISIBLE );
			((TextView)(findViewById( R.id.promptNewPasswordLabel )))
				.setText( getResources().getString( R.string.promptPassword ) );
			newTextView.requestFocus();
			cancelButton.setText( R.string.quit );
			
		} else if( type == TYPE_CHANGE ) {
			findViewById( R.id.promptOldPasswordLabel ).setVisibility( View.VISIBLE );
			oldPasswordView.setVisibility( View.VISIBLE );
			((TextView)(findViewById( R.id.promptNewPasswordLabel )))
				.setText( getResources().getString( R.string.promptNewPasswordLabel ) );
			oldPasswordView.requestFocus();
			cancelButton.setText( android.R.string.cancel );
		} else {
			IllegalArgumentException e = new IllegalArgumentException();
			Log.e(e, "No valid type give me!" );
			throw e;
		}
		
		findViewById( R.id.promptOkButton )
			.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				doPassword(oldPasswordView, type);
			}

		} );
		cancelButton.setOnClickListener( new OnClickListener() {
	
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		} );
		
		confirmTextView.setOnEditorActionListener( new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				doPassword(oldPasswordView, type);
				return true;
			}
		} );
	}
	
	private int isPasswordSafity() {
		String passwordNew = newTextView.getText().toString();
		String passwordConfirm = confirmTextView.getText().toString();
		char[] password = passwordNew.toCharArray();
		PasswordPolicy.Result res
			= EncryptItApplication.getPasswordPolicy().check( password );
				
		if(  res != PasswordPolicy.GoodResult.GOOD ) {
			int strId
				= (int)EncryptItApplication.getPasswordPolicy()
						.getResult(res);
			return strId;
		}
		
		if( !passwordNew.equals( passwordConfirm ) ) {
			return R.string.promptPasswordNotEqual;
		}
		
		return 0;
	}

	private void doPassword(final TextView oldPasswordView, final int type) {
		int strId = isPasswordSafity();
		if( strId > 0 ) {
			promptResult( strId );
			return;
		}
		char[] password = null;
		if( type == TYPE_INIT ) {
			password = newTextView.getText().toString().toCharArray();
			Key key = checkPasswordAndGetKey( password );
			if( key == null ) {
				Log.e( "Initialize password failed!" );
				setResult( INITIALIZE_PASSWORD_FAILED );
				finish();
				return;
			} else {
				Intent intent = new Intent( Intent.ACTION_PICK );
				intent.putExtra( PasswordHandler.KEY_KEY, key );
				setResult(RESULT_OK, intent);
				finish();
				return;
			}
		} else if( type == TYPE_CHANGE ) {
			password = oldPasswordView.getText().toString().toCharArray();
			if( checkPasswordAndGetKey( password ) != null ) {
				Intent intent = new Intent( Intent.ACTION_PICK );
				intent.putExtra( PasswordHandler.KEY_OLD_PASSWORD,
								 password );
				intent.putExtra( PasswordHandler.KEY_NEW_PASSWORD, 
								 newTextView.getText().toString()
								 	.toCharArray() );
				setResult(RESULT_OK, intent);
				finish();
			} else if( passwordTriedTooMuch() ) {
				setResult( TOO_MUCH_TIMES_TO_TRY );
				finish();
				return;
			} else {
				// Can try more times
			}
		} else {
			IllegalArgumentException e = new IllegalArgumentException();
			Log.e(e, "No valid type give me!" );
			throw e;
		}
	}
}
