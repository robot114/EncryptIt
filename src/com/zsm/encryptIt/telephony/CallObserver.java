package com.zsm.encryptIt.telephony;

import com.zsm.log.Log;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class CallObserver extends ContentObserver {

	private Context mContext;
	private TelephonyBase mTelephonyBase;

	public CallObserver(Context context, TelephonyBase tb, Handler handler) {
		super(handler);
		mContext = context;
		mTelephonyBase = tb;
	}

	@Override
	public void onChange(boolean selfChange, Uri uri) {
		String outgoingCall = mTelephonyBase.getOutgoingCall();
		if( outgoingCall != null ) {
			TelephonyLogUtilities.deleteLastOutgoingCall(mContext, outgoingCall );
			mTelephonyBase.setOutgoingCall(null);
			Log.d( "Log for last security call cleaned" );
		}
	}

}
