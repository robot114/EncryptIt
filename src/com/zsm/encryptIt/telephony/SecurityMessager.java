package com.zsm.encryptIt.telephony;

import java.util.ArrayList;

import com.zsm.encryptIt.R;
import com.zsm.log.Log;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SecurityMessager {

	private static final String KEY_MESSAGE_POS = "MESSAGE_POS";
	private static final String KEY_MESSAGE_TOTAL_NUMBER = "MESSAGE_TOTAL_NUMBER";
	private static final String ACTION_SENT_SMS
		= "com.zsm.security.message.ACTION_SENT_SMS";
	private static final String ACTION_DELIVER_SMS
		= "com.zsm.security.message.ACTION_DELIVER_SMS";
	
	private static SecurityMessager instance;
	private static SmsManager mSmsManager;
	private static Context mContext;
	private BroadcastReceiver mSendReceiver;
	private BroadcastReceiver mDeliverReceiver;

	private SecurityMessager( Context context ) {
		mContext = context;
		mSmsManager = SmsManager.getDefault();
		
		mSendReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int resId;
				if( getResultCode() == Activity.RESULT_OK ) {
					resId = R.string.promptSendMessageOK;
				} else {
					resId = R.string.promptSendMessageFail;
				}
				promptMessageStatus(context, resId, intent);
			}
		};
		
		mDeliverReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				promptMessageStatus(context, R.string.promptDeliverMessage, intent);
			}
		};
	}

	static public void initMessager( Context context ) {
		instance = new SecurityMessager( context );
		Log.d( "Security messager initialized" );
	}
	
	static public SecurityMessager getInstance() {
		if( instance == null ) {
			throw new IllegalStateException( "Instance has not been initialized, "+
											  "call initMessager first" );
		}
		
		return instance;
	}

	/**
	 * Send SMS in plain text
	 * 
	 * @param target
	 * @param message
	 */
	public void sendSms( String target, String message ) {
		ArrayList<String> messageArray = mSmsManager.divideMessage(message);
		int size = messageArray.size();
		ArrayList<PendingIntent> spia = new ArrayList<PendingIntent>( size );
		ArrayList<PendingIntent> dpia = new ArrayList<PendingIntent>( size );
		for( int i = 0; i < size; i++ ) {
			spia.add( eventIntent(mContext, ACTION_SENT_SMS, size, i+1) );
			dpia.add( eventIntent(mContext, ACTION_DELIVER_SMS, size, i+1) );
		}
		mSmsManager.sendMultipartTextMessage(target, null, messageArray, spia, dpia);
	}
	
	/**
	 * Build a pending intent to catch message event information
	 * 
	 * @param context
	 * @param action what the event
	 * @param number how many parts of the message
	 * @param nth the nth of this part, begin from 1
	 * @return
	 */
	private PendingIntent eventIntent(Context context, String action,
									  int number, int nth) {
		
		Intent sentIntent = new Intent( action );
		sentIntent.putExtra( KEY_MESSAGE_TOTAL_NUMBER, number );
		sentIntent.putExtra( KEY_MESSAGE_POS, nth );
		return PendingIntent.getBroadcast( context, 0, sentIntent,
									  	   PendingIntent.FLAG_UPDATE_CURRENT );
	}
	
	public void registerReceiver() {
		mContext.registerReceiver(mSendReceiver,  new IntentFilter( ACTION_SENT_SMS ) );
		mContext.registerReceiver(mDeliverReceiver,  new IntentFilter( ACTION_DELIVER_SMS ) );
	}
	
	public void unregisterReceiver() {
		mContext.unregisterReceiver(mSendReceiver);
		mContext.unregisterReceiver(mDeliverReceiver);
	}

	private void promptMessageStatus(Context context, int resId, Intent intent) {
		int pos = intent.getIntExtra( KEY_MESSAGE_POS, 1 );
		int totalNumber = intent.getIntExtra( KEY_MESSAGE_TOTAL_NUMBER, 1 );
		String text = context.getResources().getString(resId, pos, totalNumber );
		Toast.makeText(context, text, Toast.LENGTH_LONG).show();
		Log.d( "Security message status", text );
	}
}
