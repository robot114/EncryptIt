package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

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

public class BackupTask extends AsyncTask<BackupOperator, Object, RESULT> {
	
	private static final int BUFFER_SIZE = 4096;
	private Activity mActivity;
	private ProgressDialog mProgressDlg;
	
	private BackupOperator[] mTasks;
	private long mFinishedSize;
	private BackupOperator mCurrentTask;
	private byte[] mBuffer;

	public BackupTask(Activity activity ) {
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
	protected RESULT doInBackground(BackupOperator... tasks) {
		Log.d( "Backup tasks: ", (Object[])tasks );
		mProgressDlg.setMax((int) getBackupSize( tasks ));
		
		mTasks = tasks;
		
		for( BackupOperator t : mTasks ) {
			mCurrentTask = t;
			try {
				RESULT r = backupOne(t);
				if( r != RESULT.OK ) {
					return r;
				}
				Log.d( "Backup successfully: ", t );
			} catch ( GeneralSecurityException | IOException e) {
				Log.e( e, "Backup failed: ", t );
				return RESULT.FAILED;
			}
		}
		
		mCurrentTask = null;
		return RESULT.OK;
	}

	private RESULT backupOne(BackupOperator t)
			throws IOException, GeneralSecurityException {
		
		String message
			= mActivity.getString( R.string.promptBackup, t.displayName() );
		publishProgress( message, 0 );
		
		try( OutputStream out = t.openOutputStream();
			 InputStream in = t.openInputStream() ) {
			
			t.outputHeader(out);
			int count = 0;
			while( ( count = in.read( mBuffer ) ) > 0 ) {
				if( isCancelled() ) {
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
				return mActivity.getString( R.string.promptBackupOk,
										    builder.toString() );
			case FAILED:
				return mActivity.getString(R.string.promptBackupFailed,
							mCurrentTask.displayName() );
			case CANCELLED:
				return mActivity.getString( R.string.promptBackupCancelled);
			default:
				return "";
		}
	}

	private long getBackupSize(BackupOperator[] tasks) {
		long size = 0;
		for( BackupOperator t :  tasks ) {
			size += t.size();
		}
		return size;
	}
}
