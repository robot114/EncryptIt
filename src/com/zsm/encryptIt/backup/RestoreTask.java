package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Vector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.zsm.encryptIt.R;
import com.zsm.log.Log;

public class RestoreTask extends AsyncTask<RestoreOperator, Object, RESULT> {
	
	private static final int BUFFER_SIZE = 4096;
	private Activity mActivity;
	private ProgressDialog mProgressDlg;
	
	private RestoreOperator[] mTasks;
	private long mFinishedSize;
	private RestoreOperator mCurrentTask;
	private byte[] mBuffer;

	public RestoreTask(Activity activity ) {
		super();
		mActivity = activity;
		mBuffer = new byte[BUFFER_SIZE];
	}
	
	protected Context getContext() {
		return mActivity;
	}
	
	@Override
	protected void onPreExecute() {
		mFinishedSize = 0;
		mProgressDlg = buildProgressDlg();
		mProgressDlg.show();
	}

	@Override
	protected RESULT doInBackground(RestoreOperator... tasks) {
		Log.d( "Backup tasks: ", (Object[])tasks );
		mProgressDlg.setMax((int) getTotalSize( tasks ));
		
		mTasks = tasks;
		
		Vector<RestoreOperator> v = protectCurrent(tasks);
		if( v.size() < tasks.length ) {
			Log.w( "Rename to backup current used failed. "+
					"The followings are renamed successfully:", v );
			restoreCurrent( v );
			return RESULT.FAILED;
		}
		
		RESULT res = doRestore(tasks, v);
		if( res != RESULT.OK  ) {
			restoreCurrent(v);
			return res;
		}
		mCurrentTask = null;
		return RESULT.OK;
	}
	
	private RESULT doRestore( RestoreOperator[] tasks, Vector<RestoreOperator> v ) {
		for( RestoreOperator t : mTasks ) {
			mCurrentTask = t;
			try {
				RESULT r = restoreOne(t);
				if( r != RESULT.OK ) {
					return r;
				}
				Log.d( "Backup successfully: ", t );
			} catch ( GeneralSecurityException | IOException e) {
				Log.e( e, "Restore failed: ", t );
				return RESULT.FAILED;
			}
		}
		
		mCurrentTask = null;
		v.clear();
		
		return RESULT.OK;
	}
	
	private Vector<RestoreOperator> protectCurrent( RestoreOperator[] tasks ) {
		Vector<RestoreOperator> v = new Vector<RestoreOperator>(tasks.length);
		for( RestoreOperator t : tasks ) {
			boolean res = false;
			try {
				res = t.renameForRestore();
			} catch (IOException e) {
				Log.w( e, "Rename for restore to local failed!", t );
			}
			if( res ) {
				v.add(t);
			} else {
				Log.w( "Rename for restore to local return false!", t );
				break;
			}
		}
		
		return v;
	}
	
	private void restoreCurrent( Iterable<RestoreOperator> tasks ) {
		for( RestoreOperator t : tasks ) {
			try {
				t.restoreFromRename();
			} catch (IOException e) {
				Log.w( e, "Restore from local failed!", t );
			}
		}
	}

	private RESULT restoreOne(RestoreOperator t)
			throws FileNotFoundException, IOException, GeneralSecurityException {
		
		String message
			= mActivity.getString( R.string.promptRestore, t.displayName() );
		publishProgress( message, 0 );
		
		try( 
			OutputStream out = t.openOutputStream();
			InputStream in = t.openInputStream() ) {
			
			int count = 0;
			while( ( count = in.read( mBuffer ) ) > 0 ) {
				if( isCancelled() ) {
					restoreCurrent(Arrays.asList(mTasks));
					return RESULT.CANCELLED;
				}
				out.write(mBuffer, 0, count);
				publishProgress( message, count );
			}
		}
		
		return RESULT.OK;
	}

	@Override
	protected void onProgressUpdate(Object... values) {
		mFinishedSize += (int)values[1];
		mProgressDlg.setMessage( (String)values[0] );
		mProgressDlg.setProgress( (int)mFinishedSize );
	}

	@Override
	protected void onPostExecute(RESULT result) {
		showResult( result );
	}

	@Override
	protected void onCancelled() {
		mProgressDlg.dismiss();
		mActivity.finish();
		Toast
			.makeText(mActivity, getResultMessage( RESULT.CANCELLED ),
					  Toast.LENGTH_SHORT)
			.show();
	}

	protected ProgressDialog buildProgressDlg( ) {

		ProgressDialog dlg = new ProgressDialog(mActivity);
		dlg.setTitle(R.string.app_name );
		dlg.setMessage( "" );
		dlg.setCancelable(false);
		dlg.setIndeterminate(false);
		dlg.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
		dlg.setButton(DialogInterface.BUTTON_NEGATIVE,
				mActivity.getText(android.R.string.cancel),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						cancel(false);
					}

				});

		return dlg;
	};

	protected void showResult(RESULT result) {
		mProgressDlg.setMessage( getResultMessage(result) );
		Button button = mProgressDlg.getButton( DialogInterface.BUTTON_NEGATIVE );
		button.setText( R.string.close );
		button.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgressDlg.dismiss();
				mActivity.finish();
			}
		} );
	}

	private String getResultMessage(RESULT result) {
		switch( result ) {
			case OK:
				StringBuilder builder = new StringBuilder();
				builder.append( mTasks[0].displayName() );
				for( int i = 1; i < mTasks.length; i++ ) {
					builder.append( "\r\n " );
					builder.append( mTasks[i].displayName() );
				}
				return mActivity.getString( R.string.promptRestoreOk,
										    builder.toString() );
			case FAILED:
			final String displayName
				= mCurrentTask == null ? "" : mCurrentTask.displayName();
			return mActivity.getString(R.string.promptRestoreFailed,
							displayName );
			case CANCELLED:
				return mActivity.getString( R.string.promptRestoreCancelled);
			default:
				return "";
		}
	}

	private long getTotalSize(RestoreOperator[] tasks) {
		long size = 0;
		for( RestoreOperator t :  tasks ) {
			size += t.size();
		}
		return size;
	}
}
