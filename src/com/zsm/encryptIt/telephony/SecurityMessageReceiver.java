package com.zsm.encryptIt.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import com.zsm.log.Log;

public class SecurityMessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if( !Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals( intent.getAction() ) ) {
			Log.w( "Invalid action", intent.getAction() );
			return;
		}
		SmsMessage[] sms = Telephony.Sms.Intents.getMessagesFromIntent(intent);
		if( sms == null || sms.length < 1 ) {
			Log.w( "No pdus", sms );
			return;
		}

		StringBuffer buf = new StringBuffer();
		for( int i = 0; i < sms.length; i++ ) {
			buf.append( sms[i].getDisplayMessageBody() );
		}
		
		SmsMessage message = sms[0];
		Intent messageIntent = new Intent( context, SecurityMessageActivity.class );
		messageIntent.setAction( SecurityMessageActivity.ACTION_RECEIVE_SMS );
		messageIntent.putExtra( SecurityMessageActivity.KEY_MESSAGE,
								buf.toString() );
		messageIntent.putExtra( SecurityMessageActivity.KEY_NUMBER, 
								message.getOriginatingAddress() );
		messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity( messageIntent );
	}

}
