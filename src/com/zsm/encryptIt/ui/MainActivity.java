package com.zsm.encryptIt.ui;

import java.security.GeneralSecurityException;
import java.security.Key;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.zsm.encryptIt.AndroidItemListOperator;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.action.KeyAction;
import com.zsm.encryptIt.android.action.AndroidKeyActor;
import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.log.Log;
import com.zsm.security.PasswordHandler;

public class MainActivity extends ProtectedActivity implements ModeKeeper {

	protected static final int SHOW_FOR_EDIT = 3;
	protected static final int SHOW_FOR_DELETE = 4;
	
	public static final int ENCRYPT_IT_ID = 0;
	
	// The key manager and actor is global static, so this flag must be global static
	static private boolean enviromentInitialized = false;
	
	private ClearableEditor clearableEditor;
	private View newItemButton;
	
	private View editorLayout;
	private View listLayout;
	private View buttonLayout;

	private FragmentAdapter listFragment;
	private AndroidItemListOperator operator;
	
	private MODE mode;
	
	private MenuItem menuItemSelectedCount;
	private MenuItem menuItemSelectAll;

	public MainActivity() {
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initEnviroment();
		
		if( !promptPassword( ) ) {
			return;
		}
		
		mode = MODE.BROWSE;
		setContentView( R.layout.main );
		
		FragmentManager fm = getFragmentManager();
		listFragment
			= (ToDoListFragment) fm.findFragmentById(R.id.ToDoListFragment);
		listFragment.setModeKeeper( this );
		listFragment.registerListDataSetObserver( new ListDataSetObserver() );
		
		clearableEditor
			= (ClearableEditor) findViewById( R.id.clearableEditor );
		
		listLayout = findViewById( R.id.todoListLayout );
		editorLayout = findViewById( R.id.editorLayout );
		buttonLayout = findViewById( R.id.buttonLayout );
		newItemButton = findViewById( R.id.newItemButton );
		newItemButton.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					doAdd( );
				}
		} );
		
		clearableEditor.addTextChangedListener( new EditorListener() );
		
		clearableEditor.setOnEditorActionListener( new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				doAdd();
				return true;
			}
		} );
		
		operator
			= new AndroidItemListOperator( getApplicationContext(),
										   getLoaderManager(),
										   listFragment );
		
		listFragment.setListOperator( operator );
		
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
				if( key == null || !operator.initList(key, handler) ) {
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
		if( mode == MODE.BROWSE ) {
			mi.inflate( R.menu.main_browse, menu);
		} else {
			mi.inflate( R.menu.main_edit, menu);
		}
		menuItemSelectAll = menu.findItem( R.id.menuSelectAll );
		menuItemSelectedCount = menu.findItem( R.id.menuSelectedCount );
		updateSelectedCount();
		return true;
	}

	private void updateSelectedCount() {
		String fmt = getResources().getString( R.string.menuSelectedCount );
		String str = String.format( fmt, operator.getSelectedCount() );
		menuItemSelectedCount.setTitle( str );
		boolean allSelected
			= operator.getSelectedCount() == operator.getShownCount();
		menuItemSelectAll.setTitle( allSelected 
									? R.string.menuUnselectAll
									: R.string.menuSelectAll);
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
			case R.id.menuMainEditMode:
				switchTo( MODE.EDIT );
				return true;
			case R.id.menuMainEditDone:
				switchTo( MODE.BROWSE );
				return true;
			case R.id.menuSelectAll:
				boolean toSelectAll
					= operator.getSelectedCount() < operator.getShownCount();
				operator.selectAll( toSelectAll );
				updateSelectedCount();
				return true;
			case R.id.menuSelectReverse:
				operator.selectReverse();
				updateSelectedCount();
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
		if( operator.doAdd(clearableEditor.getText().toString()) ) {
			clearableEditor.clearText();
		}
	}

	protected boolean needPromptPassword() {
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if( checkLoginFailed( resultCode ) ) {
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
		operator.filter(clearableEditor.getText());
	}

	private void doDelete(int resultCode, Intent data) {
		switch( resultCode ) {
			case RESULT_OK:
				operator.doDelete(
						data.getIntExtra( DetailActivity.KEY_ROW_POSITION, -1 ) );
				operator.filter(clearableEditor.getText());
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
	
	@Override
	public MODE getMode() {
		return this.mode;
	}

	@Override
	public void switchTo(MODE mode) {
		this.mode = mode;
		// Clear all selection to avoid delete by mistake
		operator.selectAll( false );
		// In selectAll listFragment.notifyDataSetChanged() will be called,
		// so no need to call it again

		boolean isBrowseMode = ( mode == MODE.BROWSE );
		newItemButton.setVisibility( isBrowseMode ? View.VISIBLE : View.INVISIBLE );
		clearableEditor.setIMEOption( isBrowseMode
										? EditorInfo.IME_ACTION_NONE 
										: EditorInfo.IME_ACTION_GO );
		invalidateOptionsMenu();
	}
	
	private final class EditorListener implements TextWatcher {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			
			operator.filter( s );
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	}
	
	private final class ListDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			updateSelectedCount();
		}
	}
}
