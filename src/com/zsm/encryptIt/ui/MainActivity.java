package com.zsm.encryptIt.ui;

import java.security.GeneralSecurityException;
import java.security.Key;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.action.KeyAction;
import com.zsm.encryptIt.android.action.AndroidKeyActor;
import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.log.Log;
import com.zsm.security.PasswordHandler;

public class MainActivity extends ProtectedActivity {

	protected static final int SHOW_FOR_EDIT = 3;
	protected static final int SHOW_FOR_DELETE = 4;
	
	static final int ENCRYPT_IT_ID = 0;
	
	// The key manager and actor is global static, so this flag must be global static
	static private boolean enviromentInitialized = false;
	
	private ClearableEditor clearableEditor;
	
	private View editorLayout;
	private View listLayout;
	private View buttonLayout;

	private ToDoListFragment listFragment;

	public MainActivity() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initEnviroment();
		
		if( !promptPassword( ) ) {
			return;
		}
		
		setContentView( R.layout.main );
		
		FragmentManager fm = getFragmentManager();
		listFragment
			= (ToDoListFragment) fm.findFragmentById(R.id.ToDoListFragment);
		clearableEditor
			= (ClearableEditor) findViewById( R.id.clearableEditor );
		
		listLayout = findViewById( R.id.todoListLayout );
		editorLayout = findViewById( R.id.editorLayout );
		buttonLayout = findViewById( R.id.buttonLayout );
		findViewById( R.id.newItemButton )
			.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					doAdd( );
				}
		} );
		
		findViewById( R.id.button1 ).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changePassword();
			}
		});
		
		clearableEditor.setOnEditorActionListener( new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				doAdd();
				return true;
			}
		} );
		
		setHeightByWindow( );
		
		waitForKeyThenInitList();
	}
	
	private void waitForKeyThenInitList() {
		final Handler handler = new Handler();
		final EncryptItApplication app
			= (EncryptItApplication)getApplicationContext();
		final Thread threadForKey = new Thread( new Runnable(){
			@Override
			public void run() {
				Key key = null;
				try {
					key = waitForKey();
				} catch (InterruptedException e) {
					Log.d( "Waiting for key is interrupted!" );
					return;
				}
				if( key == null || !listFragment.initList(key, handler) ) {
					finish();
				}
				app.threadForKeyStopped();
			}}
		);
		
		app.setThreadForKey(threadForKey);
	}
	
	private Key waitForKey() throws InterruptedException {
		EncryptItApplication context
			= (EncryptItApplication)getApplicationContext();
		
		context.waitForPassword();
		Key key = context.getKey();
		
		return key;
	}
	
	private void initEnviroment() {
		if( enviromentInitialized ) {
			return;
		}
		enviromentInitialized = true;
		try {
			AndroidKeyActor.installInstance( getApplicationContext() );
		} catch (Exception e) {
			Log.e( e, "Install key action failed!" );
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater mi = getMenuInflater();
		mi.inflate( R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if( wasInBackground ) {
			if( promptPassword( ) ) {
				getLoaderManager().restartLoader(ENCRYPT_IT_ID, null, listFragment);
			} else {
				// Password not needed, because the state just comes from onCreate.
				// And in this case, the loader has just loaded, so no restarting.
				return;
			}
		}
	}

	private boolean changePassword() {
		try {
			PasswordPromptParameter passwordPromptParam
				= new PasswordPromptParameter(
					CHANGE_PASSWORD, getApplicationContext(), this );
			EncryptItApplication.getPasswordHandler()
				.promptChangePassword( passwordPromptParam );
			
			return true;
		} catch (GeneralSecurityException e) {
			// Any error makes the application quit
			Log.e( e, "Show prompt password activity failed!" );
			finish();
			return false;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch( item.getItemId() ) {
			case R.id.menuMainChangePassword:
				changePassword();
				return true;
			default:
				break;
		}
		
		return false;
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		int winHeight
			=  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
											   newConfig.screenHeightDp,
											   getResources().getDisplayMetrics());
		setListHeightByScreen(winHeight);
	}

	private void setHeightByWindow() {
		int winHeight = getWindow().getDecorView().getHeight();
		setListHeightByScreen( winHeight - getStatusBarHeight() );
	}

	/**
	 * This method calculates the height excluding the height of status bar.
	 * 
	 * @param screenHeight
	 */
	private void setListHeightByScreen( final int screenHeight ) {
		new Handler().post( new Runnable() {
			@Override
			public void run() {
				int height
					= screenHeight - editorLayout.getHeight()
						- buttonLayout.getHeight()
						- getActionBar().getHeight();
				LayoutParams params
					= new LayoutParams(LayoutParams.MATCH_PARENT, height);
				listLayout.setLayoutParams(params);
			}
		} );
	}
	
	private void doAdd() {
		if( listFragment.doAdd(clearableEditor.getText().toString()) ) {
			clearableEditor.clearText();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if( loginFailed( resultCode ) ) {
			return;
		}
		switch( requestCode ) {
			case PROMPT_PASSWORD:
				doPassword(resultCode, data);
				break;
			case CHANGE_PASSWORD:
				doChangePassword(resultCode, data);
				break;
			case SHOW_FOR_EDIT:
				doEdit(resultCode, data);
				break;
			case SHOW_FOR_DELETE:
				doDelete(resultCode, data);
				break;
		}
	}

	private void doEdit(int resultCode, Intent data) {
		// Modification saved in the detail activity when needed.
		// Here the filter needed to be done event it is cancelled
		// in the detail activity, because it is cancelled, the task
		// may be changed and saved.
		listFragment.filter(clearableEditor.getText());
	}

	private void doDelete(int resultCode, Intent data) {
		switch( resultCode ) {
			case RESULT_OK:
				listFragment.doDelete(
						data.getIntExtra( DetailActivity.KEY_ROW_POSITION, -1 ) );
				listFragment.filter(clearableEditor.getText());
				break;
			default:
				break;
		}
	}

	private void doPassword(int resultCode, Intent intent) {
		switch ( resultCode ) {
			case Activity.RESULT_OK:
				PasswordPromptParameter param
					= new PasswordPromptParameter( PROMPT_PASSWORD,
												   getApplicationContext(),
												   this );
				param.setData( intent );
				Key key = EncryptItApplication.getPasswordHandler().getKey(param);
				
				EncryptItApplication context
					= (EncryptItApplication)getApplicationContext();
				context.setKey( key );
				context.resumeFromWaitForPassword();
				break;
			case Activity.RESULT_CANCELED:
			case SecurityActivity.TOO_MUCH_TIMES_TO_TRY:
				finish();
				break;
			default:
				break;
		}
	}

	public void doChangePassword(int resultCode, Intent intent) {
		switch ( resultCode ) {
			case Activity.RESULT_OK:
				char[] oldPassword
					= intent.getCharArrayExtra(PasswordHandler.KEY_OLD_PASSWORD);
				char[] newPassword
					= intent.getCharArrayExtra(PasswordHandler.KEY_NEW_PASSWORD);
				int id = R.string.changePasswordFailed;
				if( KeyAction.getInstance()
						.changePassword(oldPassword, newPassword) ) {
					
					id = R.string.changePasswordOk;
				}
				promptResult( id );
				break;
			case SecurityActivity.TOO_MUCH_TIMES_TO_TRY:
				finish();
				break;
			case Activity.RESULT_CANCELED:
			default:
				break;
		}
	}

	private void promptResult(int id) {
		Resources r = getResources();
		
		new AlertDialog.Builder(this)
			 .setTitle(r.getString( R.string.app_name )) 
			 .setMessage(r.getString( id ))
			 .setPositiveButton(null, null)
			 .show();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if( hasFocus ) {
			setHeightByWindow();
		}
	}
}
