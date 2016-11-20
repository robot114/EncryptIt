package com.zsm.encryptIt.telephony;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zsm.android.ui.TextAutoResizeWatcher;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.ui.ProtectedActivity;
import com.zsm.log.Log;

public class SecurityDialerActivity extends ProtectedActivity {

	private static final String LONG_CLICK_SEPERATOR = "(";
	public final static String ACTION_CALL = "com.zsm.security.ACTION_CALL";
	public final static String ACTION_DIAL = "com.zsm.security.ACTION_DIAL";
	
	public static final String KEY_PHONE_NUMBER = "KEY_PHONE_NUMBER";
	public static final String KEY_TITLE_RESID = "KEY_TITLE_RESID";
	
	public static final String TEL_SCHEME = "tel";
	private EditText mNumberText;
	private int mNumberTextHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.security_dialer );
		mNumberText = (EditText)findViewById( R.id.textPhoneNumber );
		mNumberText
			.addTextChangedListener( 
					new TextAutoResizeWatcher( this, mNumberText, 20 ) );
		
		setTitle( );
		
		initNumberButton(R.id.button0);
	}

	private void initNumberButton(int btnId) {
		Button button = (Button)findViewById( btnId );
		CharSequence text = button.getText();
		SpannableString ss = new SpannableString( text );
		ss.setSpan(new RelativeSizeSpan(.5f), 1, text.length(), 0); // set size
		ss.setSpan(new ForegroundColorSpan(Color.GRAY), 1, text.length(), 0);// set color
		button.setText(ss);
		button.setOnLongClickListener( new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String text = ((Button)v).getText().toString();
				int index = text.indexOf( LONG_CLICK_SEPERATOR );
				CharSequence ch
					= ((Button)v).getText().subSequence(index+1, index+2).toString();
				insertNumberToCurrentPosition(ch);
				return true;
			}
		} );
	}

	private void setTitle() {
		int titleResId = getIntent().getIntExtra( KEY_TITLE_RESID, 0 );
		if( titleResId == 0 ) {
			titleResId = forCall() 
					  ? R.string.activityTitleDialerCall 
						: R.string.activityTitleDialer;
		}
		
		setTitle( titleResId );
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if( mNumberTextHeight == 0 ) {
			mNumberTextHeight = mNumberText.getHeight();
			mNumberText.setMinHeight( mNumberTextHeight );
			mNumberText.setMaxHeight( mNumberTextHeight );
			
			// Set the number after the height of the text view is fixed
			// Otherwise the height of the view will be small when the
			// number from outside is too long
			Uri uri = getIntent().getData();
			if( uri != null && TEL_SCHEME.equals(uri.getScheme()) ) {
				String pn = uri.getSchemeSpecificPart();
				mNumberText.setText( pn );
				mNumberText.setSelection( pn.length() );
			}
			
		}
	}

	public void onClickKey( View v ) {
		CharSequence ch = ((Button)v).getText().subSequence(0, 1).toString();
		insertNumberToCurrentPosition(ch);
	}

	private void insertNumberToCurrentPosition(CharSequence ch) {
		int cursorPosition = mNumberText.getSelectionStart();
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
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void onOK( View v ) {
		Editable number = mNumberText.getText();
		if( number == null || number.length() == 0 ) {
			Toast.makeText( getApplicationContext(), R.string.promptNoPhoneNumber,
							Toast.LENGTH_LONG )
				 .show();
			mNumberText.requestFocus();
			return;
		}
		if( forCall() ) {
			doSecurityCall();
		} else {
			doDial();
		}
	}

	private void doDial() {
		Intent intent = new Intent( Intent.ACTION_PICK );
		intent.putExtra( KEY_PHONE_NUMBER, mNumberText.getText().toString() );
		setResult( RESULT_OK, intent );
		finish();
	}

	private boolean forCall() {
		String action = getIntent().getAction();
		return ( action != null && !action.equals( ACTION_DIAL ));
	}
	
	private void doSecurityCall() {
		String number = mNumberText.getText().toString();
		TelephonyBase app = (TelephonyBase)getApplication();
		app.setOutgoingCall( number );
		
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent
				.setData( Uri.fromParts( TEL_SCHEME, number, "" ) );
			startActivity(callIntent);
			Log.d( "Start dialer" );
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
