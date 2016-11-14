package com.zsm.encryptIt.dialer;

import java.util.Date;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class SecurityPhoneCallReceiver extends AbstractPhoneCallReceiver {

	@Override
	protected void onIncomingCallReceived(Context ctx, String number, Date start) {
		Toast.makeText(ctx, "Incoming call: " + number, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onOutgoingCallEnded(final Context ctx, final String number, Date start, Date end) {
		CallBase app = (CallBase)ctx.getApplicationContext();
		
		if( number != null && number.equals( app.getOutgoingCall() )) {
			// Finish outgoing call first, then delete the log later. 
			// Otherwise, nothing will be deleted when the log is not
			// added to the database
			new Handler().postDelayed( new Runnable() {
				@Override
				public void run() {
					CallLogUtilities.deleteLastOutgoingCall(ctx, number);
				}
			}, 200 );
		}
		
		app.setOutgoingCall(null);
			
	}

	@Override
	protected void onMissedCall(Context ctx, String number, Date start) {
		// TODO Auto-generated method stub

	}

}
