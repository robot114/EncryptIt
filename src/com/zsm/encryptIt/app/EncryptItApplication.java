package com.zsm.encryptIt.app;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javax.crypto.NoSuchPaddingException;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.zsm.driver.android.log.AndroidLog;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.SystemParameter;
import com.zsm.encryptIt.action.ItemListActor;
import com.zsm.encryptIt.android.action.AndroidPasswordHandler;
import com.zsm.log.Log;
import com.zsm.recordstore.RecordStoreManager;
import com.zsm.recordstore.driver.android.sqlite.SQLiteDriver;
import com.zsm.security.LengthPasswordPolicy;
import com.zsm.security.PasswordHandler;
import com.zsm.security.PasswordPolicy;

public class EncryptItApplication extends Application {

    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;
    
    private Timer activityTransitionTimer;
    private TimerTask activityTransitionTimerTask;
    public boolean wasInBackground;
    
    private static PasswordHandler passwordHandler;
	private static PasswordPolicy passwordPolicy;
	private Semaphore promptPasswordSemaphore = new Semaphore(0);
	private Key key;
	private Thread threadForKey;
	private ItemListActor itemActioner;

	public EncryptItApplication() {
		Log.install( new AndroidLog( "EncryptIt" ) );
		Log.setLevel(Log.DEBUG);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		RecordStoreManager.getInstance()
			.setDefaultDriver( new SQLiteDriver(this) );
		
		passwordHandler = new AndroidPasswordHandler();
		initPaswordPolicy();
	}
	
	private void initPaswordPolicy() {
		passwordPolicy = new LengthPasswordPolicy( 8 );
		
		passwordPolicy.setUnknownResult( R.string.passwordUnknownResult );
		
		passwordPolicy.putResultString(
				LengthPasswordPolicy.LengthResult.TOO_SHORT,
				R.string.passwordTooShort);
	}

	public void startActivityTransitionTimer() {
	    this.activityTransitionTimer = new Timer();
	    this.activityTransitionTimerTask = new TimerTask() {
	        public void run() {
	        	EncryptItApplication.this.wasInBackground = true;
	        }
	    };

	    this.activityTransitionTimer.schedule(activityTransitionTimerTask,
	                                           MAX_ACTIVITY_TRANSITION_TIME_MS);
	}

	public void stopActivityTransitionTimer() {
	    if (this.activityTransitionTimerTask != null) {
	        this.activityTransitionTimerTask.cancel();
	    }

	    if (this.activityTransitionTimer != null) {
	        this.activityTransitionTimer.cancel();
	    }

	    this.wasInBackground = false;
	}
	
	static public PasswordHandler getPasswordHandler() {
		return passwordHandler;
	}
	
	static public PasswordPolicy getPasswordPolicy() {
		return passwordPolicy;
	}

	public ItemListActor getItemListActor() {
		return itemActioner;
	}
	
	public void setItemListActor(ItemListActor itemListActor) {
		itemActioner = itemListActor;
	}
	
	public void waitForPassword() throws InterruptedException {
		promptPasswordSemaphore.acquire();
	}
	
	public void resumeFromWaitForPassword() {
		promptPasswordSemaphore.release();
	}
	
	public int maxPasswordRetries() {
		return 3;
	}

	public void setKey(Key key) {
		this.key = key;
	}
	
	public Key getKey() {
		return key;
	}

	public void threadForKeyStopped() {
		threadForKey = null;
	}

	public void setThreadForKey(final Thread t) {
		if( threadForKey != null ) {
			// Should be only one thread for a application to waiting for the key
			Log.d( "Thread will be interrupted!", threadForKey );
			threadForKey.interrupt();
		}
		threadForKey = t;
		t.start();
	}

	public void initEncryptSetting(Context context, Key key)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
					InvalidKeySpecException {
		
		TelephonyManager mngr
			= (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		char[] password = mngr.getDeviceId().toCharArray();
		
		SystemParameter.initEncryptSetting(key, password);
	}

}
