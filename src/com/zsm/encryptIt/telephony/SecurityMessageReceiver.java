package com.zsm.encryptIt.telephony;

import com.zsm.log.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

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
		
		Log.d( "Message number is ", sms.length );
		// Only the first message will be handled
		SmsMessage message = sms[0];
		
		Intent messageIntent = new Intent( context, SecurityMessageActivity.class );
		messageIntent.putExtra( SecurityMessageActivity.KEY_MESSAGE,
								message.getDisplayMessageBody() );
		messageIntent.putExtra( SecurityMessageActivity.KEY_NUMBER, 
								message.getOriginatingAddress() );
		messageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity( messageIntent );
	}

}
