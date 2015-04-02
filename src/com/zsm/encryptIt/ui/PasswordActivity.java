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

	private VisiblePassword oldPasswordView;
	private VisiblePassword newPasswordView;
	private VisiblePassword confirmPasswordView;
	private TextView hintTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.prompt_password );
		
		passwordAllowToTry
			= ((EncryptItApplication)getApplication()).maxPasswordRetries();
		
		newPasswordView = (VisiblePassword)findViewById( R.id.promptNewPassword );
		confirmPasswordView = (VisiblePassword)findViewById( R.id.promptConfirmPassword );
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
		newPasswordView.addTextChangedListener( tw );
		confirmPasswordView.addTextChangedListener(tw);
		
		oldPasswordView = (VisiblePassword)findViewById( R.id.promptOldPassword );
		final Button cancelButton = (Button)findViewById( R.id.promptCancelButton );
		
		Intent intent = getIntent();
		final int type = intent.getIntExtra(KEY_TYPE, -1);
		if( type == TYPE_INIT ) {
			oldPasswordView.setVisibility( View.INVISIBLE );
			newPasswordView.setLabel( R.string.promptPassword );
			newPasswordView.requestFocus();
			cancelButton.setText( R.string.quit );
		} else if( type == TYPE_CHANGE ) {
			oldPasswordView.setVisibility( View.VISIBLE );
			newPasswordView.setLabel( R.string.promptNewPasswordLabel );
			oldPasswordView.requestFocus();
			cancelButton.setText( android.R.string.cancel );
		} else {
			IllegalArgumentException e = new IllegalArgumentException();
			Log.e(e, "No valid type for me!" );
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
		
		confirmPasswordView.setOnEditorActionListener( new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				doPassword(oldPasswordView, type);
				return true;
			}
		} );
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if( hasFocus ) {
			alignLabel();
		}
	}

	private void alignLabel() {
		int labelWidth
			= Math.max( newPasswordView.getLabelWidth(),
						confirmPasswordView.getLabelWidth() );
		labelWidth = Math.max( labelWidth, oldPasswordView.getLabelWidth() );
		newPasswordView.setLabelWidth(labelWidth);
		confirmPasswordView.setLabelWidth(labelWidth);
		oldPasswordView.setLabelWidth(labelWidth);
	}
	
	private int isPasswordSafity() {
		String passwordNew = newPasswordView.getPassword();
		String passwordConfirm = confirmPasswordView.getPassword();
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

	private void doPassword(final VisiblePassword oldPasswordView, final int type) {
		int strId = isPasswordSafity();
		if( strId > 0 ) {
			promptResult( strId );
			return;
		}
		char[] password = null;
		if( type == TYPE_INIT ) {
			password = newPasswordView.getPassword().toCharArray();
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
			password = oldPasswordView.getPassword().toCharArray();
			if( checkPasswordAndGetKey( password ) != null ) {
				Intent intent = new Intent( Intent.ACTION_PICK );
				intent.putExtra( PasswordHandler.KEY_OLD_PASSWORD,
								 password );
				intent.putExtra( PasswordHandler.KEY_NEW_PASSWORD, 
								 newPasswordView.getPassword().toCharArray() );
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
