package com.zsm.encryptIt.telephony;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zsm.android.ui.VisiblePassword;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItemV2;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.telephony.SecurityMessager.SmsResultReceiver;
import com.zsm.encryptIt.ui.ProtectedActivity;
import com.zsm.log.Log;

public class SecurityMessageActivity extends ProtectedActivity {

	private static final char NEW_LINE = '\n';
	private static final String UNTITLED = "Untitled";

	private abstract class InnerSmsResultReceiver implements SmsResultReceiver {
		protected int mCurrentMessagePart;
		
		private void resetPartCounter() {
			mCurrentMessagePart = 0;
		}
	}
	
	public static final String KEY_NUMBER = "KEY_NUMBER";
	public static final String KEY_MESSAGE = "KEY_MESSAGE";
	public static final String KEY_TITLE = "KEY_TITLE";
	public static final String KEY_MESSAGE_TOTAL_NUMBER = "MESSAGE_TOTAL_NUMBER";
	
	public static final String ACTION_RECEIVE_SMS = "com.zsm.security.sms.RECEIVE";
	public static final String ACTION_SEND_SMS = "com.zsm.security.sms.SEND";
	
	private static final int REQUEST_FOR_PHONE_NUMBER = 1001;

	private CheckBox mTitleCheckBox;
	private EditText mTitleView;
	private EditText mMessageView;
	private TextView mReciptientView;
	private VisiblePassword mPasswordView;
	private ImageView mButtonAction;
	
	private boolean mSendSms;
	private String mOriginalTitle;
	private String mOriginalMessage;
	private InnerSmsResultReceiver mSendReceiver;
	private InnerSmsResultReceiver mDeliverReceiver;

	public SecurityMessageActivity() {
		super();
		mSendReceiver = new InnerSmsResultReceiver() {
			@Override
			public void onReceive(int result, Intent intent) {
				int resId;
				if( result == Activity.RESULT_OK ) {
					resId = R.string.promptSendMessageOK;
				} else {
					resId = R.string.promptSendMessageFail;
				}
				promptMessageStatus(resId, ++mCurrentMessagePart, intent);
			}
		};
		
		mDeliverReceiver = new InnerSmsResultReceiver() {
			@Override
			public void onReceive(int result, Intent intent) {
				promptMessageStatus(R.string.promptDeliverMessage,
									++mCurrentMessagePart, intent);
			}
		};
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.security_message);
		
		Intent data = getIntent();
		String action = data.getAction();
		mSendSms = ACTION_SEND_SMS.equals(action);
		
		mOriginalTitle = data.getStringExtra( KEY_TITLE );
		mTitleView = (EditText)findViewById( R.id.editTextTitle );
		mTitleView.setText( mOriginalTitle == null ? UNTITLED : mOriginalTitle );
		
		mTitleCheckBox = (CheckBox)findViewById( R.id.checkBoxTitle );
		mTitleCheckBox.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				
				enableTitleView(isChecked);
				if( !mSendSms ) {
					handleDecodeMessage();
				}
			}
		} );
		enableTitleView( mTitleCheckBox.isChecked() );
		
		mMessageView = (EditText)findViewById( R.id.editTextMessage );
		mOriginalMessage = data.getStringExtra( KEY_MESSAGE );
		mMessageView.setText(mOriginalMessage);
		mMessageView.setEnabled( mSendSms );
		
		mReciptientView = (TextView)findViewById( R.id.textViewReciptient );
		String number = data.getStringExtra( KEY_NUMBER );
		mReciptientView.setText(number);
		
		mPasswordView = (VisiblePassword)findViewById( R.id.viewPassword );
		mPasswordView.addTextChangedListener( new PasswordChangeWatcher() );
		
		initActionButton();
		
		if( !mSendSms ) {
			handleDecodeMessage();
		}
	}

	private void initActionButton() {
		mButtonAction = (ImageView)findViewById( R.id.imageViewAction );
		int imageId;
		int imageHintId;
		if( mSendSms ) {
			imageId = R.drawable.message;
			imageHintId = R.string.hintSendMessageImage;
		} else {
			imageId = R.drawable.save;
			imageHintId = R.string.hintSaveMessageImage;
		}
		mButtonAction.setImageResource( imageId );
		mButtonAction.setContentDescription( getString( imageHintId ) );
	}
	
	@Override
	protected boolean needPromptPassword() {
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if( requestCode == REQUEST_FOR_PHONE_NUMBER ) {
			if( resultCode == RESULT_OK ) {
				String number
					= data.getStringExtra( SecurityDialerActivity.KEY_PHONE_NUMBER );
				mReciptientView.setText(number);
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void onNumber( View v ) {
		Intent intent = new Intent( this, SecurityDialerActivity.class );
		intent.setAction(SecurityDialerActivity.ACTION_DIAL);
		intent.putExtra( SecurityDialerActivity.KEY_TITLE_RESID,
						 R.string.activityTitleDialerMessage );
		String numberString = mReciptientView.getText().toString();
		if( numberString.length() > 0 ) {
			intent.setData( Uri.fromParts( SecurityDialerActivity.TEL_SCHEME,
										   numberString, "" ) );
		}
		intent.putExtra( SecurityDialerActivity.KEY_PHONE_NUMBER,
						 numberString );
		startActivityForResult( intent, REQUEST_FOR_PHONE_NUMBER );
	}
	
	public void onAction( View v ) {
		if( mSendSms ) {
			onSend();
		} else {
			onSave();
		}
	}
	
	public void onSend( ) {
		String title = mTitleView.getText().toString();
		String detail = mMessageView.getText().toString();
		doMessage( title, detail, getIntent() );
	}
	
	private void doMessage( String title, String detail, Intent data ) {
		final String message = composeMessage(title, detail);
		
		if( message == null ) {
			Log.d( "Nothing to send" );
			Toast.makeText(getApplicationContext(), R.string.promptNothingToSend,
						   Toast.LENGTH_LONG ).show();
			return;
		}
		
		final String number = mReciptientView.getText().toString();
		if( emptyText(number) ) {
			Log.w( "Invalid phone number, nothing sent" );
			Toast.makeText(getApplicationContext(), R.string.promptNoPhoneNumber,
					   Toast.LENGTH_LONG ).show();
			return;
		}
		
		TelephonyBase app = (TelephonyBase)getApplication();
		app.setOutgoingSms(number);
		char[] password = mPasswordView.getPassword().toCharArray();
		mSendReceiver.resetPartCounter();
		mDeliverReceiver.resetPartCounter();
		if( password.length > 0 ) {
			if( SecurityMessager.getInstance()
					.sendSms(number, message, password, mSendReceiver,
							 mDeliverReceiver) ) {
				
				finish();
			} else {
				Toast.makeText(this, R.string.promptEncodeMessageFailed,
						   	   Toast.LENGTH_LONG )
					 .show();
			}
		} else {
			new AlertDialog.Builder( this )
				.setIcon( android.R.drawable.ic_dialog_alert )
				.setMessage( R.string.promptNoPasswordForMessage )
				.setPositiveButton( android.R.string.yes, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SecurityMessager.getInstance()
							.sendSms(number, message, mSendReceiver,
									 mDeliverReceiver);
						finish();
					}
				})
				.setNegativeButton( android.R.string.no, null )
				.show();
		}
	}

	private String composeMessage(String title, String detail) {
		String message = null;
		if( !emptyText(title) && mTitleCheckBox.isChecked() ) {
			message = title + ( emptyText(detail) ? "" : ( NEW_LINE + detail ) );
		} else if( !emptyText(detail) ) {
			message = detail;
		}
		return message;
	}

	private boolean emptyText(final String message) {
		return message == null || message.length() == 0;
	}

	private void promptMessageStatus(int resId, int nth, Intent intent) {
		int totalNumber = intent.getIntExtra( KEY_MESSAGE_TOTAL_NUMBER, 1 );
		String text
			= getResources().getString(resId, nth, totalNumber );
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		Log.d( "Security message status", text );
	}

	public void onSave( ) {
		String title = mTitleView.getText().toString();
		try {
			decodeMessage();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException | IOException e) {
			
			Toast.makeText( this, R.string.promptDecodeFailed, Toast.LENGTH_LONG )
				 .show();
			mPasswordView.requestFocus();
			return;
		}

		if( emptyText( title ) ) {
			doSave();
		} else {
			doSave(title);
			finish();
		}
	}

	private void doSave() {
		final EditText editTitle = new EditText(this);
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder
			.setTitle( R.string.app_name )
			.setMessage( R.string.promptSaveMessageTitle )
			.setView( editTitle )
			.setPositiveButton( android.R.string.ok, null )
			.setNegativeButton( android.R.string.cancel, null );
		
		final AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener( new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				initTitleDlgButton(editTitle, alertDialog);
			}
		} );
		
		alertDialog.show();
	}

	private void initTitleDlgButton(final EditText editTitle,
									final AlertDialog titleDialog) {
		titleDialog
			.getButton( DialogInterface.BUTTON_POSITIVE )
			.setOnClickListener( new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					enterTitle(editTitle);
				}
			} );
	}

	private void enterTitle(final EditText editTitle) {
		String newTitle = editTitle.getText().toString().trim();
		if( emptyText( newTitle ) ) {
			int[] location = new int[2];
			editTitle.getLocationInWindow(location);
			Toast t = Toast.makeText( SecurityMessageActivity.this,
									  R.string.promptNoTitle,
									  Toast.LENGTH_LONG );
				 t.setGravity( Gravity.CENTER, location[0], location[1]/2);
				 t.show();
		} else {
			doSave( newTitle );
			finish();
		}
	}

	private void doSave(String title) {
		WhatToDoItemV2 item = new WhatToDoItemV2( title );
		item.setDetail( mMessageView.getText().toString() );
		
		((EncryptItApplication)getApplicationContext()).getUIListOperator()
			.doAddToDataAndView( item );
	}
	
	private void handleDecodeMessage() {
		String text;
		try {
			text = decodeMessage();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException | IOException e) {
			
			mMessageView.setText( mOriginalMessage );
			return;
		}
		
		if( mTitleCheckBox.isChecked() ) {
			int index = text.indexOf( NEW_LINE );
			if( index > 0 ) {
				mTitleView.setText( text.substring( 0, index ) );
				mMessageView.setText( text.substring( index + 1 ) );
			} else {
				mTitleView.setText( text );
				mMessageView.setText( "" );
			}
		} else {
			mTitleView.setText( "" );
			mMessageView.setText( text );
		}
	}

	private String decodeMessage()
				throws NoSuchAlgorithmException, NoSuchPaddingException,
					   InvalidKeySpecException, IOException {
		
		String text = mOriginalMessage;
		String password = mPasswordView.getPassword();
		if( password.length() > 0 ) {
			mTitleCheckBox.setEnabled( false );
			text = SecurityMessager.getInstance()
				.decodeMessage(mOriginalMessage, password);
		}
		mTitleCheckBox.setEnabled( true );
		return text;
	}

	private void enableTitleView(boolean isChecked) {
		mTitleView.setEnabled( isChecked && mSendSms );
	}

	private final class PasswordChangeWatcher implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if( !mSendSms ) {
				handleDecodeMessage();
			}
		}

	}

}
