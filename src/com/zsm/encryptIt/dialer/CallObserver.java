package com.zsm.encryptIt.dialer;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

public class CallObserver extends ContentObserver {

	private Context mContext;
	private CallBase mCallBase;

	public CallObserver(Context context, CallBase cb, Handler handler) {
		super(handler);
		mContext = context;
		mCallBase = cb;
	}

	@Override
	public void onChange(boolean selfChange, Uri uri) {
		String outgoingCall = mCallBase.getOutgoingCall();
		if( outgoingCall != null ) {
			CallLogUtilities.deleteLastOutgoingCall(mContext, outgoingCall );
			mCallBase.setOutgoingCall(null);
		}
	}

}
