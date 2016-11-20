package com.zsm.encryptIt.telephony;

import com.zsm.log.Log;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class SmsObserver extends ContentObserver {

	private TelephonyBase mTelephonyBase;
	private Context mContext;

	public SmsObserver(Context context, TelephonyBase tb, Handler handler) {
		super(handler);
		mContext = context;
		mTelephonyBase = tb;
	}

	@Override
	public void onChange(boolean selfChange, Uri uri) {
		String outgoingSms = mTelephonyBase.getOutgoingSms();
		if( outgoingSms != null ) {
			TelephonyLogUtilities.deleteLastOutgoingSms(mContext, outgoingSms );
			mTelephonyBase.setOutgoingSms(null);
			Log.d( "Log for last security call cleaned" );
		}
	}

}
