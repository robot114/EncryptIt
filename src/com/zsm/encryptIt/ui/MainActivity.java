package com.zsm.encryptIt.ui;

import java.security.Key;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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

import com.zsm.android.ui.ClearableEditor;
import com.zsm.driver.android.log.LogActivity;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.android.action.AndroidItemListOperator;
import com.zsm.encryptIt.android.action.AndroidKeyActor;
import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.ui.preferences.Preferences;
import com.zsm.encryptIt.ui.preferences.PreferencesActivity;
import com.zsm.encryptIt.ui.preferences.SecurityAdvancedPreferencesActivity;
import com.zsm.log.Log;

public class MainActivity extends ProtectedActivity
				implements ModeKeeper, ActivityOperator {

	private static final String SHOW_LOG = "show log!";
	
	protected static final int SHOW_FOR_EDIT = 3;
	protected static final int SHOW_FOR_DELETE = 4;
	protected static final int SHOW_FOR_DELETE_SELECTED = 5;
	
	public static final int ENCRYPT_IT_ID = 0;
	
	// The key manager and actor is global static, so this flag must be global static
	static private boolean enviromentInitialized = false;
	
	private ClearableEditor clearableEditor;
	private View newItemButton;
	
	private View editorLayout;
	private View listLayout;
	private View buttonLayout;

	private ListFragmentAdapter listFragment;
	private AndroidItemListOperator operator;
	
	private MODE mode;
	
	private MenuItem menuItemSelectedCount;
	private MenuItem menuItemSelectAll;
	private ListDataSetObserver listDataObserver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d( "The MainActivity is to be created", this );
		initEnviroment();
		
		getApp().setMainActivity(this);
		
		mode = MODE.BROWSE;
		if( Preferences.getInstance().getMainListExpandable() ) {
			setContentView( R.layout.main_expandable );
		} else {
			setContentView( R.layout.main );
		}
		
		FragmentManager fm = getFragmentManager();
		listFragment
			= (ListFragmentAdapter) fm.findFragmentById(R.id.ToDoListFragment);
		listFragment.setModeKeeper( this );
		
		clearableEditor
			= (ClearableEditor) findViewById( R.id.clearableEditor );
		
		listLayout = findViewById( R.id.todoListLayout );
		editorLayout = findViewById( R.id.editorLayout );
		buttonLayout = findViewById( R.id.buttonLayout );
		newItemButton = findViewById( R.id.newItemButton );
		newItemButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = clearableEditor.getText().toString();
				
				if( text.equals( SHOW_LOG ) ) {
					Intent intent
						= new Intent( MainActivity.this, SecurityLogActivity.class );
					intent.putExtra( LogActivity.KEY_PREFERENCE_ACTIVITY,
									 SecurityAdvancedPreferencesActivity.class);
					MainActivity.this.startActivity( intent );
					return;
				}
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
		
		listFragment.setDataListToAdapter( operator.getDataList() );
		getApp().setUIListOperator(operator);
		
		setHeightByWindow( );
		
		if( !(getApp().promptPassword( this ) ) ) {
			return;
		}
		waitForKeyThenInitList();
	}
	
	private void waitForKeyThenInitList() {
		Log.d( "Waiting for the key..." );
		final Handler handler = new Handler();
		final EncryptItApplication app = getApp();
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
		EncryptItApplication context = getApp();
		
		context.waitForPassword();
		Key key = context.getKey();
		
		return key;
	}

	private EncryptItApplication getApp() {
		return (EncryptItApplication)getApplicationContext();
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
		listDataObserver = new ListDataSetObserver();
		listFragment.registerListDataSetObserver( listDataObserver );

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
	
	private void deleteSelected() {
		Intent intent = new Intent( this, MultiDetailActivity.class );
		startActivityForResult( intent, SHOW_FOR_DELETE_SELECTED );
	}
	
	@Override
	public void onBackPressed() {
		new Builder(this)
			.setMessage(R.string.promptExit)
			.setTitle(R.string.app_name)
			.setPositiveButton( android.R.string.yes, 
				   			   	new DialogInterface.OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog,
									 int which) {
					MainActivity.super.onBackPressed();
				}
			} )
			.setNegativeButton( android.R.string.no, null )
			.setCancelable( false )
			.create()
			.show();
	}

	@Override
	protected void onDestroy() {
		Log.d( "MainActivity to be destoried" );
		getApp().stopActivityTransitionTimer();
		if( listDataObserver != null ) {
			listFragment.unregisterListDataSetObserver( listDataObserver );
			listDataObserver = null;
		}
		operator.closeStorage();
		getApp().setUIListOperator(null);
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch( item.getItemId() ) {
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
			case R.id.menuDeleteSelected:
				deleteSelected();
				updateSelectedCount();
				return true;
			case R.id.menuPreferences:
				Intent intent = new Intent( this, PreferencesActivity.class );
				startActivity( intent );
				break;
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
		Log.d( "MainActivity Result: ", "requestCode", requestCode, "resultCode",
			   resultCode );
		super.onActivityResult(requestCode, resultCode, data);
		if( checkLoginFailed( resultCode ) ) {
			return;
		}
		switch( requestCode ) {
			case PasswordPromptParameter.PROMPT_PASSWORD:
				doPassword(resultCode, data);
				break;
			case SHOW_FOR_EDIT:
				doEdit(resultCode, data);
				break;
			case SHOW_FOR_DELETE:
				doDelete(resultCode, data);
				break;
			case SHOW_FOR_DELETE_SELECTED:
				doDeleteSelected( resultCode, data );
				break;
		}
	}

	private void doEdit(int resultCode, Intent data) {
		// Modification saved in the detail activity when needed.
		// Here the filter needed to be done event it is cancelled
		// in the detail activity, because it is cancelled, the task
		// may be changed and saved.
		refilter();
	}

	private void refilter() {
		operator.filter(clearableEditor.getText());
		operator.notifyDataSetChanged();
	}

	private void doDelete(int resultCode, Intent data) {
		switch( resultCode ) {
			case RESULT_OK:
				operator.doDelete(
						data.getIntExtra( DetailActivity.KEY_ROW_POSITION, -1 ) );
				refilter();
				break;
			default:
				break;
		}
	}

	private void doDeleteSelected(int resultCode, Intent data) {
		switch( resultCode ) {
			case RESULT_OK:
				operator.doDeleteSelected( );
				refilter();
				break;
			default:
				break;
		}
	}

	private void doPassword(int resultCode, Intent intent) {
		switch ( resultCode ) {
			case Activity.RESULT_OK:
				PasswordPromptParameter param
					= new PasswordPromptParameter( 
							PasswordPromptParameter.PROMPT_PASSWORD,
							getApplicationContext(),
							this );
				param.setData( intent );
				Key key = EncryptItApplication.getPasswordHandler().getKey(param);
				
				EncryptItApplication context = getApp();
				context.setKey( key );
				context.resumeFromWaitForPassword();
				break;
			case Activity.RESULT_CANCELED:
			case PasswordPromptParameter.TOO_MUCH_TIMES_TO_TRY:
				finish();
				break;
			default:
				break;
		}
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
		return mode;
	}

	@Override
	public void switchTo(MODE mode) {
		if( mode != MODE.BROWSE && mode != MODE.EDIT ) {
			throw new IllegalStateException( 
						"Just MODE.BROWSE and MODE.EDIT support!" );
		}
		
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
