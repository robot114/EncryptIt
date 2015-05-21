package com.zsm.encryptIt.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.log.Log;

public class MultiDetailActivity extends ProtectedActivity implements ModeKeeper {

	private ListFragmentAdapter listFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		Log.d( "MultiDetailActivity to be created!" );
		
		setContentView(R.layout.multi_detail);
		
		FragmentManager fm = getFragmentManager();
		listFragment
			= (ListFragmentAdapter) fm.findFragmentById(R.id.ToDoListFragment);
		listFragment.setModeKeeper( this );
		
		listFragment.setDataListToAdapter( 
				getApp().getUIListOperator().getSelectedDataList() );
		
		setTitle(R.string.detailDeleteSelected);
	}

	@Override
	protected boolean needPromptPassword() {
		return true;
	}

	private EncryptItApplication getApp() {
		return (EncryptItApplication)getApplicationContext();
	}
	
	@Override
	public MODE getMode() {
		return ModeKeeper.MODE.MULTI_DETAIL;
	}

	@Override
	public void switchTo(MODE mode) {
		if( mode != ModeKeeper.MODE.MULTI_DETAIL ) {
			throw new IllegalStateException( "Just MULTI_DETAIL support!" );
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater mi = getMenuInflater();
		mi.inflate( R.menu.delete_selected, menu);
		MenuItem ok = menu.findItem( R.id.menuMultiDetailPositive );
		ok.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				new Builder(MultiDetailActivity.this)
					.setMessage(R.string.confirmDeleteSelectedAgain)
					.setTitle(R.string.app_name)
					.setPositiveButton( android.R.string.yes, 
						   			   	new DialogInterface.OnClickListener() {
						@Override
						public void onClick( DialogInterface dialog,
											 int which) {
							setResult( Activity.RESULT_OK );
							finish();
						}
					} )
					.setNegativeButton( android.R.string.no,
						   			   	new DialogInterface.OnClickListener() {
						@Override
						public void onClick( DialogInterface dialog,
											 int which) {
						}
					} )
					.show();
				return true;
			}
		} );

		MenuItem cancel = menu.findItem( R.id.menuMultiDetailNegative );
		cancel.setOnMenuItemClickListener( new OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				MultiDetailActivity.this.onBackPressed();
				return true;
			}
		} );
		return true;
	}

}
