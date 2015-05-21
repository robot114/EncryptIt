package com.zsm.encryptIt.app;

import java.io.File;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javax.crypto.NoSuchPaddingException;

import android.app.Activity;
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
import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.encryptIt.ui.ActivityOperator;
import com.zsm.encryptIt.ui.MainActivity;
import com.zsm.encryptIt.ui.preferences.Preferences;
import com.zsm.log.FileLog;
import com.zsm.log.Log;
import com.zsm.recordstore.RecordStoreManager;
import com.zsm.recordstore.driver.android.sqlite.SQLiteDriver;
import com.zsm.security.LengthPasswordPolicy;
import com.zsm.security.PasswordHandler;
import com.zsm.security.PasswordPolicy;

public class EncryptItApplication extends Application {

	private static final int MAX_LOG_FILE_LENGTH = 1024*1024*1024;

    public static final String ANDROID_LOG = "AndroidLog";
	public static final String FILE_LOG = "FileLog";
	public static final String DEFAULT_LOG = ANDROID_LOG;

	private long maxActivityTransitionTimeMs = 5000;
    
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

	private Activity mainActivity;

	public EncryptItApplication() {
		Log.setGlobalLevel( Log.LEVEL.DEBUG );
		
		Log.install( ANDROID_LOG, new AndroidLog( "EncryptIt" ) );
		Log.setLevel( ANDROID_LOG, Log.LEVEL.DEBUG );
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		installFileLogRetry();
		Log.setLevel(FILE_LOG, Log.LEVEL.DEBUG);
		RecordStoreManager.getInstance()
			.setDefaultDriver( new SQLiteDriver(this) );
		
		passwordHandler = new AndroidPasswordHandler();
		initPaswordPolicy();
		
		Preferences.init(this);
		
		Log.setGlobalLevel( Preferences.getInstance().getLogLevel() );
		if( Preferences.getInstance().getLogChannels().contains( FILE_LOG ) ) {
			installFileLog();
			installFileLogRetry();
		}
	}

	private void installFileLog() {
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
	
	private void installFileLogRetry() {
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
	}

	private void initPaswordPolicy() {
		passwordPolicy = new LengthPasswordPolicy( 8 );
		
		passwordPolicy.setUnknownResult( R.string.passwordUnknownResult );
		
		passwordPolicy.putResultString(
				LengthPasswordPolicy.LengthResult.TOO_SHORT,
				R.string.passwordTooShort);
	}

	public void setMainActivity( MainActivity a ) {
		mainActivity = a;
	}
	
	public Activity getMainActivity() {
		return mainActivity;
	}
	
	public void startActivityTransitionTimer() {
		if( maxActivityTransitionTimeMs == 0 ) {
			return;
		}
		
	    activityTransitionTimer = new Timer();
	    activityTransitionTimerTask = new TimerTask() {
	        public void run() {
	        	EncryptItApplication.this.wasInBackground = true;
	        }
	    };

	    activityTransitionTimer
	    	.schedule(activityTransitionTimerTask,
	    			  Preferences.getInstance().getLockAppTimeInMs());
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

	public void resumeProtectedActivity(ActivityOperator ao,
										boolean needPromptPassword) {
		
		boolean inBg = wasInBackground;
		stopActivityTransitionTimer();
		
		Log.d( "For resuming.", "activity", this, "wasInBackground",
				inBg, "needPromptPassword", needPromptPassword );
		if( inBg && needPromptPassword ) {
			promptPassword( ao );
		}
	}
	
	public boolean promptPassword(ActivityOperator ao) {
		try {
			PasswordPromptParameter passwordPromptParam
				= new PasswordPromptParameter(
						PasswordPromptParameter.PROMPT_PASSWORD,
						getApplicationContext(), ao );
			EncryptItApplication.getPasswordHandler()
				.promptPassword( passwordPromptParam );
			
			return true;
		} catch (GeneralSecurityException e) {
			// Any error makes the application quit
			Log.e( e, "Show prompt password activity failed!" );
//			ao.setResult( PasswordPromptParameter.LOGIN_FAILED );
//			ao.finish();
			ao.finishAffinity();
			return false;
		}
	}

}
