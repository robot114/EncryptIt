package com.zsm.encryptIt.telephony;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.ui.ProtectedActivity;
import com.zsm.log.Log;

public class SecurityMessageActivity extends ProtectedActivity {

	public static final String KEY_NUMBER = "KEY_NUMBER";

	public static final String KEY_MESSAGE = "MESSAGE";
	
	private static final int REQUEST_FOR_PHONE_NUMBER = 1001;

	private EditText mMessageView;

	private TextView mReciptientView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.security_message);
		
		mMessageView = (EditText)findViewById( R.id.editTextMessage );
		Intent data = getIntent();
		String message = data.getStringExtra( KEY_MESSAGE );
		mMessageView.setText(message);
		
		mReciptientView = (TextView)findViewById( R.id.textViewReciptient );
		String number = data.getStringExtra( KEY_NUMBER );
		mReciptientView.setText(number);
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
		finish();
	}
	
	private void doMessage( String message, Intent data ) {
		if( message == null || message.length() == 0 ) {
			Log.d( "No message to send" );
			Toast.makeText(getApplicationContext(), R.string.promptNoMessageToSend,
						   Toast.LENGTH_LONG ).show();
		} else {
			String number = mReciptientView.getText().toString();
			if( number == null || number.length() == 0 ) {
				Log.w( "Invalid phone number, nothing sent" );
				return;
			}
			TelephonyBase app = (TelephonyBase)getApplication();
			app.setOutgoingSms(number);
			SecurityMessager.getInstance().sendSms(number, message);
			Log.d( "Message to be sent" );
		}
	}

	public void onCancel( View v ) {
		setResult( RESULT_CANCELED );
		finish();
	}
}
