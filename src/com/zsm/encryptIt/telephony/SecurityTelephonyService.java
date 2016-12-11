package com.zsm.encryptIt.telephony;

import com.zsm.log.Log;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.Telephony;

public class SecurityTelephonyService extends Service {

	private ServiceBinder mBinder;
	private AbstractPhoneCallReceiver mCallReceiver;
	private CallObserver mCallLogObserver;
	private SmsObserver mMessageObserver;
	private SecurityMessageReceiver mSmsReceiver;

	public final class ServiceBinder extends Binder {
		public SecurityTelephonyService getService() {
		    return SecurityTelephonyService.this;
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
		
		// Remove the call log by observer the call log content provider,
		// instead of outgoing call state. As the outgoing call may end
		// before the log added
//		registerPhoneStateReceiver();
		
		mCallLogObserver
			= new CallObserver(this, (TelephonyBase) getApplication(), new Handler());
		getContentResolver()
			.registerContentObserver( CallLog.Calls.CONTENT_URI, true,
									  mCallLogObserver );
		
		SecurityMessager.initMessager(getApplicationContext());
		SecurityMessager.getInstance().registerReceiver();
		mMessageObserver
			= new SmsObserver( this, (TelephonyBase) getApplication(), new Handler() );
		getContentResolver()
			.registerContentObserver( Telephony.Sms.CONTENT_URI, true,
									  mMessageObserver );
		
		Log.d( "Security telephony service created!" );
	}

	private void registerPhoneStateReceiver() {
		mCallReceiver = new SecurityPhoneCallReceiver( );
		IntentFilter filter
			= new IntentFilter( AudioManager.ACTION_AUDIO_BECOMING_NOISY );
		registerReceiver( mCallReceiver, filter);
		filter
			= new IntentFilter( "android.intent.action.PHONE_STATE" );
		registerReceiver( mCallReceiver, filter);
		filter
			= new IntentFilter( Intent.ACTION_NEW_OUTGOING_CALL );
		registerReceiver( mCallReceiver, filter);
	}

	@Override
	public void onDestroy() {
		unregisterPhoneStateReceiver();
		
		if( mCallLogObserver != null ) {
			getContentResolver().unregisterContentObserver(mCallLogObserver);
			mCallLogObserver = null;
		}
		
		if( mMessageObserver != null ) {
			getContentResolver().unregisterContentObserver(mMessageObserver);
			mMessageObserver = null;
		}
		SecurityMessager.getInstance().unregisterReceiver();
		if( mSmsReceiver != null ) {
			unregisterReceiver(mSmsReceiver);
		}
		
		super.onDestroy();
	}

	private void unregisterPhoneStateReceiver() {
		if( mCallReceiver != null ) {
			unregisterReceiver(mCallReceiver);
			mCallReceiver = null;
		}
	}

}
