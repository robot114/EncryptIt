package com.zsm.encryptIt.app;

import java.io.File;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javax.crypto.NoSuchPaddingException;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.telephony.TelephonyManager;

import com.zsm.driver.android.log.AndroidLog;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.SystemParameter;
import com.zsm.encryptIt.action.ItemListActor;
import com.zsm.encryptIt.android.action.AndroidItemListOperator;
import com.zsm.encryptIt.android.action.AndroidPasswordHandler;
import com.zsm.log.FileLog;
import com.zsm.log.Log;
import com.zsm.recordstore.RecordStoreManager;
import com.zsm.recordstore.driver.android.sqlite.SQLiteDriver;
import com.zsm.security.LengthPasswordPolicy;
import com.zsm.security.PasswordHandler;
import com.zsm.security.PasswordPolicy;

public class EncryptItApplication extends Application {

    private static final int MAX_LOG_FILE_LENGTH = 1024*1024*1024;

	public static final String FILE_LOG = "FileLog";

	private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 5000;
    
    private Timer activityTransitionTimer;
    private TimerTask activityTransitionTimerTask;
    public boolean wasInBackground;
    
    private static PasswordHandler passwordHandler;
	private static PasswordPolicy passwordPolicy;
	private Semaphore promptPasswordSemaphore = new Semaphore(0);
	private Key key;
	private Thread threadForKey;
	private ItemListActor itemActioner;

	private AndroidItemListOperator uiListOperator;

	public EncryptItApplication() {
		Log.setGlobalLevel( Log.DEBUG );
		
		Log.install( "AndroidLog", new AndroidLog( "EncryptIt" ) );
		Log.setLevel( "AndroidLog", Log.DEBUG );
		
		String logFileName
			= Environment.getExternalStorageDirectory()
				+ "/EncryptId/log/EncryptIt.log";
		try {
			Log.install( FILE_LOG, new FileLog( logFileName,
												MAX_LOG_FILE_LENGTH ));
		} catch (Exception e) {
			Log.e( "Install log failed!", "id", FILE_LOG,
					"file name", logFileName );
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if( !Log.isIinstalled(FILE_LOG) ) {
			ContextWrapper cw = new ContextWrapper(this);
			File directory = cw.getDir("log", Context.MODE_PRIVATE);
			String logFileName = directory.getAbsolutePath() +"/EncryptIt.log";
			try {
				Log.install( FILE_LOG, new FileLog( logFileName,
													MAX_LOG_FILE_LENGTH ) );
			} catch (Exception e) {
				Log.e( "Install log failed!", "id", FILE_LOG,
						"file name", logFileName );
			}
		}
		Log.setLevel(FILE_LOG, Log.DEBUG);
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
	    activityTransitionTimer = new Timer();
	    activityTransitionTimerTask = new TimerTask() {
	        public void run() {
	        	EncryptItApplication.this.wasInBackground = true;
	        }
	    };

	    this.activityTransitionTimer.schedule(activityTransitionTimerTask,
	                                           MAX_ACTIVITY_TRANSITION_TIME_MS);
	}

	public void stopActivityTransitionTimer() {
	    if (activityTransitionTimerTask != null) {
	        activityTransitionTimerTask.cancel();
	        activityTransitionTimerTask = null;
	    }

	    if (activityTransitionTimer != null) {
	        activityTransitionTimer.cancel();
	        activityTransitionTimer = null;
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
	
	public void setUIListOperator( AndroidItemListOperator o ) {
		if( uiListOperator != null && o != null ) {
			throw new IllegalStateException( 
				"ItemListOperator has been set, but not cleared."
				+ " Set it to null first!" );
		}
		uiListOperator = o;
	}
	
	public AndroidItemListOperator getUIListOperator() {
		return uiListOperator;
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
