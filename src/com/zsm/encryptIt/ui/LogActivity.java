package com.zsm.encryptIt.ui;

import java.io.BufferedReader;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.ui.preferences.AdvancedPreferencesActivity;
import com.zsm.log.Log;


public class LogActivity extends ProtectedActivity {
	
	private LogListFragment listFragment;
	private String logChannel = EncryptItApplication.DEFAULT_LOG;

	@Override
	protected boolean needPromptPassword() {
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		setContentView( R.layout.log_activity );
		
		FragmentManager fm = getFragmentManager();
		listFragment = (LogListFragment) fm.findFragmentById(R.id.fragmentLog);
		final ClearableEditor text
			= (ClearableEditor)findViewById( R.id.textViewSearchLogs );
		text.addTextChangedListener( new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				listFragment.clearSearchOffset();
			}
			
		} );
		
		text.setOnEditorActionListener( new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				doSearchForward(text);
				return true;
			}
		} );
		
		findViewById( R.id.imageViewSearchLogsForward )
			.setOnClickListener( new OnClickListener(){
				@Override
				public void onClick(View v) {
					doSearchForward(text);
				}
			} );
		
		findViewById( R.id.imageViewSearchLogsBackward )
		.setOnClickListener( new OnClickListener(){
			@Override
			public void onClick(View v) {
				doSearchBackward(text);
			}
		} );
	}

	@Override
	protected void onResume() {
		final Handler handler = new Handler();
		super.onResume();
		if( !((EncryptItApplication)getApplication()).wasInBackground ) {
			new Thread( new Runnable() {
				@Override
				public void run() {
					handler.post( new Runnable() {
						@Override
						public void run() {
							fillLogs(logChannel);
						}
					} );
				}
			} ).start();
		}
	}

	private void fillLogs(String logChannel) {
		listFragment.clear();
		Log log = Log.getInstance( logChannel );
		if( log == null ) {
			Log.i( "Log not installed!", logChannel );
			return;
		}
		
		BufferedReader r = null;
		try {
			r = log.createReader();
			String strLog;
			while( ( strLog = r.readLine() ) != null ) {
				listFragment.add(strLog);
			}
		} catch (IOException e) {
			Log.e( e, "Read from log failed!" );
		} finally {
			if( r != null ) {
				try {
					r.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater mi = getMenuInflater();
		mi.inflate( R.menu.log, menu);
		
        return true;
	}

	public void doClearLog(MenuItem item) {
		Log log = Log.getInstance(logChannel);
		if( log != null ) {
			try {
				log.clearContent();
				listFragment.clear();
				Log.i( "Logs are cleared!" );
			} catch (IOException e) {
				Log.e( "Clear the logs failed!" );
			}
		}
	}

	public void doLogPreferences(MenuItem item) {
		Intent intent = new Intent( this, AdvancedPreferencesActivity.class );
		startActivity( intent );
	}

	public void doSelectAndShowLog(MenuItem item) {
		AlertDialog.Builder builderSingle
			= new AlertDialog.Builder( LogActivity.this);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
        		LogActivity.this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Hardik");
        arrayAdapter.add("Archit");
        arrayAdapter.add("Jignesh");
        arrayAdapter.add("Umang");
        arrayAdapter.add("Gatti");
        builderSingle
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle("Select One Name:-")
        	.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(
                        		LogActivity.this);
                        builderInner.setMessage(strName);
                        builderInner.setTitle("Your Selected Item is");
                        builderInner.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builderInner.show();
                    }
                });
        builderSingle.show();
        fillLogs(logChannel);
	}

	private void doSearchForward(final ClearableEditor text) {
		String str = text.getText().toString();
		if( str.equals( "" ) ) {
			return;
		}
		listFragment.searchForward( str );
	}

	private void doSearchBackward(final ClearableEditor text) {
		String str = text.getText().toString();
		if( str.equals( "" ) ) {
			return;
		}
		listFragment.searchBackward( str );
	}
}