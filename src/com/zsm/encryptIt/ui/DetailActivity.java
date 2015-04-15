package com.zsm.encryptIt.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.zsm.android.wedget.AlertDialogBuilder;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.app.EncryptItApplication;

public class DetailActivity extends ProtectedActivity implements TextWatcher {
	
	static final String KEY_DEATAIL_TEXT = "DEATAIL_TEXT";
	static final String KEY_TRIMED_TASK = "TRIMED_TASK";
	static final String KEY_DETAIL_OK = "DETAIL_OK";
	static final String KEY_DETAIL_TITLE = "DETAIL_TITLE";
	static final String KEY_DETAIL_EDITABLE = "DETAIL_EDITABLE";
	static final String KEY_ROW_POSITION = "ROW_POSITION";
	static final String KEY_ROW_ITEM = "ROW_ITEM";

	private static final DateFormat TIME_FORMAT
		= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
	
	private WhatToDoItem whatToDoItem;
	private int position;

	private String originalTask;
	private String originalDetail;

	private TextView taskText;
	private TextView detailText;

	private AlertDialog.Builder cancelPromptDialogBuilder;
	private MenuItem positiveMenuItem;
	private String newTask;
	private String newDetail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView( R.layout.detail );
		Intent intent = getIntent();
		String title
			= getResources().getString( 
					intent.getIntExtra( KEY_DETAIL_TITLE, R.string.app_name ) );
		setTitle( title );
		
		taskText = (TextView)findViewById( R.id.titleText );
		detailText = (TextView)findViewById( R.id.detailText );
		
		boolean editable = shouldEditable();
		
		taskText.setEnabled( editable );
		taskText.addTextChangedListener( this );
		
		detailText.setEnabled( editable );
		
		TextView created = (TextView)findViewById( R.id.detailCreateTime );
		TextView modified = (TextView)findViewById( R.id.detailModifyTime );
		
		byte[] a = intent.getByteArrayExtra(KEY_ROW_ITEM);
		whatToDoItem = WhatToDoItem.fromByteArray(a, 0);
		position = intent.getIntExtra(KEY_ROW_POSITION, -1);
		
		originalTask = whatToDoItem.getTask();
		originalDetail = whatToDoItem.getDetail();
		taskText.setText( originalTask, BufferType.SPANNABLE );
		detailText.setText( originalDetail, BufferType.SPANNABLE );
		Linkify.addLinks(taskText, Linkify.ALL);
		Linkify.addLinks(detailText, Linkify.ALL);
		
		created.setText(TIME_FORMAT.format(whatToDoItem.getCreatedTime()));
		modified.setText( TIME_FORMAT.format(whatToDoItem.getModifiedTime()));
	}

	private boolean shouldEditable() {
		return getIntent().getBooleanExtra( KEY_DETAIL_EDITABLE, false );
	}

	private boolean doOK() {
		if( shouldEditable() ) {
			doEdit();
		} else {
			Intent intent = new Intent( Intent.ACTION_PICK );
			intent.putExtra( KEY_ROW_POSITION, position );
			setResult(RESULT_OK, intent);
		}
		
		return true;
	}

	private  boolean doEdit() {
		updateTaskAndDetail();
		if( newTask.length() == 0 ) {
			taskText.setText("");
			Toast.makeText(this, R.string.detail_title_hint, Toast.LENGTH_LONG )
				 .show();
			
			return false;
		}
		if( !isTextModified(newTask, newDetail) ) {
			Toast.makeText(this, R.string.detailNothingChanged, Toast.LENGTH_SHORT )
				 .show();
			setResult( RESULT_CANCELED );
			return true;
		}

		if( getApp().getItemListActor().doEdit(position, newTask, newDetail) ) {
			originalTask = newTask;
			originalDetail = newDetail;
			Toast.makeText(this, R.string.deatilSaved, Toast.LENGTH_SHORT )
			 .show();
		}
		setResult(RESULT_OK);
		return true;
	}

	private EncryptItApplication getApp() {
		return (EncryptItApplication)getApplicationContext();
	}

	private boolean isTextModified(String newTask, String newDetail) {
		return !newTask.equals( originalTask )
				|| !newDetail.equals(originalDetail);
	}
	
	@Override
	public void onBackPressed() {
		doCancel();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Intent intent = getIntent();
		
		positiveMenuItem = menu.add( intent.getIntExtra( KEY_DETAIL_OK, android.R.string.ok ) );
		positiveMenuItem
			.setShowAsActionFlags( MenuItem.SHOW_AS_ACTION_ALWAYS )
			.setOnMenuItemClickListener( new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if( doOK() ) {
						finish();
					}
					return true;
				}
			} );
		if( shouldEditable() ) {
			MenuItem saveItem = menu.add( R.string.detailSave );
			saveItem.setShowAsActionFlags( MenuItem.SHOW_AS_ACTION_ALWAYS )
					.setOnMenuItemClickListener( new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							doEdit();
							return true;
						}
					} );
		}
		MenuItem negitiveItem = menu.add( android.R.string.cancel );
		negitiveItem
			.setShowAsActionFlags( MenuItem.SHOW_AS_ACTION_ALWAYS )
			.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					doCancel();
					return true;
				}
			});
		
		return true;
	}

	private void doCancel() {
		if( cancelNeedPrompt() ) {
			initCancelPromptDialog().show();
		} else {
			super.onBackPressed();
		}
	}

	private Dialog initCancelPromptDialog() {
		if( cancelPromptDialogBuilder == null ) {
			cancelPromptDialogBuilder = new AlertDialogBuilder(this);
			cancelPromptDialogBuilder
				.setMessage(R.string.detailPromptCancel)
				.setTitle(R.string.app_name)
				.setPositiveButton( android.R.string.yes, 
					   			   	new DialogInterface.OnClickListener() {
					@Override
					public void onClick( DialogInterface dialog,
										 int which) {
						DetailActivity.super.onBackPressed();
					}
				} )
				.setNegativeButton( android.R.string.no,
					   			   	new DialogInterface.OnClickListener() {
					@Override
					public void onClick( DialogInterface dialog,
										 int which) {
					}
				} )
				.setCancelable( false );
		}	

		return cancelPromptDialogBuilder.create();
	}

	private boolean cancelNeedPrompt() {
		updateTaskAndDetail();
		return newTask.length() != 0 && isTextModified(newTask, newDetail);
	}

	private void updateTaskAndDetail() {
		newTask = taskText.getText().toString().trim();
		newDetail = detailText.getText().toString();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	protected boolean needPromptPassword() {
		return true;
	}

}
