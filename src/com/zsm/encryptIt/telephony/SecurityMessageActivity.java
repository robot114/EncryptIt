package com.zsm.encryptIt.telephony;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zsm.android.ui.VisiblePassword;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.telephony.SecurityMessager.SmsResultReceiver;
import com.zsm.encryptIt.ui.ProtectedActivity;
import com.zsm.log.Log;

public class SecurityMessageActivity extends ProtectedActivity {

	private abstract class InnerSmsResultReceiver implements SmsResultReceiver {
		protected int mCurrentMessagePart;
		
		private void resetPartCounter() {
			mCurrentMessagePart = 0;
		}
	}
	
	public static final String KEY_NUMBER = "KEY_NUMBER";
	public static final String KEY_MESSAGE = "MESSAGE";
	public static final String KEY_MESSAGE_TOTAL_NUMBER = "MESSAGE_TOTAL_NUMBER";
	
	public static final String ACTION_RECEIVE_SMS = "com.zsm.security.sms.RECEIVE";
	public static final String ACTION_SEND_SMS = "com.zsm.security.sms.SEND";
	
	private static final int REQUEST_FOR_PHONE_NUMBER = 1001;

	private EditText mMessageView;
	private TextView mReciptientView;
	private boolean mSendSms;
	private VisiblePassword mPasswordView;
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
		
		mMessageView = (EditText)findViewById( R.id.editTextMessage );
		Intent data = getIntent();
		mOriginalMessage = data.getStringExtra( KEY_MESSAGE );
		mMessageView.setText(mOriginalMessage);
		
		mReciptientView = (TextView)findViewById( R.id.textViewReciptient );
		String number = data.getStringExtra( KEY_NUMBER );
		mReciptientView.setText(number);
		
		String action = data.getAction();
		mSendSms = ACTION_SEND_SMS.equals(action);
		
		mPasswordView = (VisiblePassword)findViewById( R.id.backupPassword );
		mPasswordView.addTextChangedListener( new PasswordChangeWatcher() );
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
	
	public void onSend( View v ) {
		String message = mMessageView.getText().toString();
		doMessage( message, getIntent() );
	}
	
	private void doMessage( final String message, Intent data ) {
		if( message == null || message.length() == 0 ) {
			Log.d( "No message to send" );
			Toast.makeText(getApplicationContext(), R.string.promptNoMessageToSend,
						   Toast.LENGTH_LONG ).show();
		} else {
			final String number = mReciptientView.getText().toString();
			if( number == null || number.length() == 0 ) {
				Log.w( "Invalid phone number, nothing sent" );
				return;
			}
			
			TelephonyBase app = (TelephonyBase)getApplication();
			app.setOutgoingSms(number);
			char[] password = mPasswordView.getPassword().toCharArray();
			mSendReceiver.resetPartCounter();
			mDeliverReceiver.resetPartCounter();
			if( password.length > 0 ) {
				if( !SecurityMessager.getInstance()
						.sendSms(number, message, password, mSendReceiver,
								 mDeliverReceiver) ) {
					
					Toast.makeText(this, R.string.promptEncodeMessageFailed,
								   Toast.LENGTH_LONG )
						 .show();
				} else {
					finish();
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
	}

	private void promptMessageStatus(int resId, int nth, Intent intent) {
		int totalNumber = intent.getIntExtra( KEY_MESSAGE_TOTAL_NUMBER, 1 );
		String text
			= getResources().getString(resId, nth, totalNumber );
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
		Log.d( "Security message status", text );
	}

	public void onCancel( View v ) {
		setResult( RESULT_CANCELED );
		finish();
	}
	
	private final class PasswordChangeWatcher implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			if( !mSendSms ) {
				String password = s.toString();
				String text
					= SecurityMessager.getInstance()
						.decodeMessage(mOriginalMessage, password);
				mMessageView.setText( text );
			}
		}

	}

}
