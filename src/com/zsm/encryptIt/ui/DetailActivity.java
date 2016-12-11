package com.zsm.encryptIt.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.telephony.SecurityDialerActivity;
import com.zsm.encryptIt.telephony.SecurityMessageActivity;
import com.zsm.log.Log;

public class DetailActivity extends ProtectedActivity {
	
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
		replacePhoneNumber(taskText.getEditableText());
		replacePhoneNumber(detailText.getEditableText());
		
		taskText.addTextChangedListener(new LinksWatcher( taskText ));
		detailText.addTextChangedListener(new LinksWatcher( detailText ));
		
		created.setText(TIME_FORMAT.format(whatToDoItem.getCreatedTime()));
		modified.setText( TIME_FORMAT.format(whatToDoItem.getModifiedTime()));
	}

	private boolean shouldEditable() {
		return getIntent().getBooleanExtra( KEY_DETAIL_EDITABLE, false );
	}

	/**
	 * The positive button clicked. The item is saved or deleted.
	 * 
	 * @return true, the detail activity should be closed; false, nothing need to
	 * 			be done to the activity.
	 */
	private boolean doOK() {
		if( shouldEditable() ) {
			return doEdit();
		} else {
			final Intent intent = new Intent( Intent.ACTION_PICK );
			intent.putExtra( KEY_ROW_POSITION, position );
			getPomptDialogBuilder( R.string.confirmDeleteSelected, intent ).show();
			// The activity has been closed by the prompt dialog, if necessary.
			return false;
		}
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
		
		MenuItem negitiveItem = menu.add( android.R.string.cancel );
		negitiveItem
			.setShowAsActionFlags( MenuItem.SHOW_AS_ACTION_ALWAYS )
			.setIcon( R.drawable.back )
			.setOnMenuItemClickListener(new OnMenuItemClickListener(){
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					doCancel();
					return true;
				}
			});
		
		if( shouldEditable() ) {
			if( getPackageManager().hasSystemFeature( PackageManager.FEATURE_TELEPHONY ) ) {
				menu.add( R.string.detailSendMessage )
					.setIcon( R.drawable.message )
					.setShowAsActionFlags( MenuItem.SHOW_AS_ACTION_ALWAYS )
					.setOnMenuItemClickListener(new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							// Must has telephony feature
							doSecurityMessage();
							return true;
						}
					});
			}
			MenuItem saveItem = menu.add( R.string.detailSave );
			saveItem.setShowAsActionFlags( MenuItem.SHOW_AS_ACTION_ALWAYS )
					.setIcon( R.drawable.save )
					.setOnMenuItemClickListener( new OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {
							doEdit();
							return true;
						}
					} );
		}
		positiveMenuItem
			= menu.add( intent.getIntExtra( KEY_DETAIL_OK,
											android.R.string.ok ) );
		positiveMenuItem
			.setShowAsActionFlags( MenuItem.SHOW_AS_ACTION_ALWAYS )
			.setIcon( shouldEditable() ? R.drawable.save_and_back : R.drawable.delete_white )
			.setOnMenuItemClickListener( new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					if( doOK() ) {
						finish();
					}
					return true;
				}
			} );
		return true;
	}

	private void doCancel() {
		if( cancelNeedPrompt() ) {
			getPomptDialogBuilder(R.string.detailPromptCancel, null).show();
		} else {
			super.onBackPressed();
		}
	}

	private boolean doSecurityMessage() {
		Intent intent = new Intent( this, SecurityMessageActivity.class );
		intent.setAction( SecurityMessageActivity.ACTION_SEND_SMS );

		updateTaskAndDetail();
		String message
			= ( newDetail == null || newDetail.length() == 0 ) ? newTask : newDetail;
		intent.putExtra( SecurityMessageActivity.KEY_MESSAGE, message );
		
		startActivity( intent );
		return true;
	}
	
	private Builder getPomptDialogBuilder(int messageId, final Intent resultData ) {
		Builder promptDialogBuilder = new Builder(this);
		promptDialogBuilder
			.setMessage(messageId)
			.setTitle(R.string.app_name)
			.setPositiveButton( android.R.string.yes, 
				   			   	new DialogInterface.OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog, int which) {
					setResult( RESULT_OK, resultData );
					DetailActivity.super.onBackPressed();
				}
			} )
			.setNegativeButton( android.R.string.no,
				   			   	new DialogInterface.OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog, int which) {
				}
			} )
			.setCancelable( false );
		
		return promptDialogBuilder;
	}

	private boolean cancelNeedPrompt() {
		updateTaskAndDetail();
		return newTask.length() != 0 && isTextModified(newTask, newDetail);
	}

	private void updateTaskAndDetail() {
		newTask = taskText.getText().toString().trim();
		newDetail = detailText.getText().toString();
	}

	protected boolean needPromptPassword() {
		return true;
	}

	private void replacePhoneNumber(Editable current) {
		URLSpan[] spans
			= current.getSpans(0, current.length(), URLSpan.class);

		for (URLSpan span : spans) {
			Uri uri = Uri.parse( span.getURL() );
			if( SecurityDialerActivity.TEL_SCHEME.equals( uri.getScheme() ) ) {
				int start = current.getSpanStart(span);
				int end = current.getSpanEnd(span);

				current.removeSpan(span);
				current.setSpan(new PhoneNumberSpan(uri), start, end, 0);
			}
		}
	}
	
	private static class PhoneNumberSpan extends ClickableSpan {
		private Uri mUri;

		public PhoneNumberSpan(Uri uri) {
			mUri = uri;
		}

		@Override
		public void onClick(View widget) {
			try {
				Log.d( "Phone number is clicked" );
				Context context = widget.getContext();
				Intent intent
					= new Intent( context, SecurityDialerActivity.class );
				intent.setData(mUri);
				intent.setAction( SecurityDialerActivity.ACTION_CALL );
			
				context.startActivity( intent );
			} catch (ActivityNotFoundException e) {
				Log.e( e, "SecurityDialerActivity not found!");
			}
		}
	}
	
	final private class LinksWatcher implements TextWatcher {
		private TextView view;
		
		private LinksWatcher( TextView tv ) {
			view = tv;
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
			Linkify.addLinks(view, Linkify.ALL);
			replacePhoneNumber(s);
		}
		
	}

}
