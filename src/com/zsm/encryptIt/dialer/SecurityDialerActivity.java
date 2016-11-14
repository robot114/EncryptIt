package com.zsm.encryptIt.dialer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.ui.ProtectedActivity;
import com.zsm.log.Log;

public class SecurityDialerActivity extends ProtectedActivity {

	private static final String TEL_SCHEME = "tel:";
	private EditText mNumberText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.security_dialer );
		mNumberText = (EditText)findViewById( R.id.textPhoneNumber );
		
		String uri = getIntent().getDataString();
		if( uri != null && uri.startsWith(TEL_SCHEME) ) {
			String pn = uri.substring( TEL_SCHEME.length() );
			mNumberText.setText( pn );
			mNumberText.setSelection( pn.length() );
		}
	}

	public void onClickKey( View v ) {
		int cursorPosition = mNumberText.getSelectionStart();
		CharSequence ch = ((Button)v).getText();
		CharSequence number = mNumberText.getText().insert(cursorPosition, ch);
		mNumberText.setText(number);
		mNumberText.setSelection(cursorPosition+1);
	}

	public void onBackspace( View v ) {
		int cursorPosition = mNumberText.getSelectionStart();
		if( cursorPosition > 0 ) {
			CharSequence number
				= mNumberText.getText().delete(cursorPosition-1, cursorPosition);
			mNumberText.setText(number);
			mNumberText.setSelection(cursorPosition-1);
		}
	}
	
	public void onClose( View v ) {
		finish();
	}
	
	public void onCall( View v ) {
		String number = mNumberText.getText().toString();
		CallBase app = (CallBase)getApplication();
		app.setOutgoingCall( number );
		
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData( Uri.parse( TEL_SCHEME + number ) );
			startActivity(callIntent);
		} catch (ActivityNotFoundException activityException) {
			Log.e(activityException, "Failed to call number", number );
		}
		finish();
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean ret = super.dispatchTouchEvent(ev);
		InputMethodManager imm
			= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mNumberText.getWindowToken(), 0);
		
		return ret;
	}

	@Override
	protected boolean needPromptPassword() {
		return true;
	}
}
