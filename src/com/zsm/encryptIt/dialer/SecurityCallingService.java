package com.zsm.encryptIt.dialer;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;

public class SecurityCallingService extends Service {

	private ServiceBinder mBinder;
	private AbstractPhoneCallReceiver mReceiver;
	private CallObserver mCallLogObserver;

	public final class ServiceBinder extends Binder {
		public SecurityCallingService getService() {
		    return SecurityCallingService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		if( mBinder == null ) {
			mBinder = new ServiceBinder();
		}
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
//		registerPhoneStateReceiver();
		
		mCallLogObserver
			= new CallObserver(this, (CallBase) getApplication(), new Handler());
		getContentResolver()
			.registerContentObserver( CallLog.Calls.CONTENT_URI, true,
									  mCallLogObserver );
	}

	private void registerPhoneStateReceiver() {
		mReceiver = new SecurityPhoneCallReceiver( );
		IntentFilter filter
			= new IntentFilter( AudioManager.ACTION_AUDIO_BECOMING_NOISY );
		registerReceiver( mReceiver, filter);
		filter
			= new IntentFilter( "android.intent.action.PHONE_STATE" );
		registerReceiver( mReceiver, filter);
		filter
			= new IntentFilter( Intent.ACTION_NEW_OUTGOING_CALL );
		registerReceiver( mReceiver, filter);
	}

	@Override
	public void onDestroy() {
		unregisterPhoneStateReceiver();
		
		if( mCallLogObserver != null ) {
			getContentResolver().unregisterContentObserver(mCallLogObserver);
			mCallLogObserver = null;
		}
		
		super.onDestroy();
	}

	private void unregisterPhoneStateReceiver() {
		if( mReceiver != null ) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
	}

}
