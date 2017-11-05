package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Hashtable;

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
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.backup.BackupableConst.BACKUPABLES_KEY;
import com.zsm.log.Log;

public class BackupTask extends AsyncTask<BackupParameter, Object, RESULT> {
	
	private static final int BUFFER_SIZE = 4096;
	private Activity mActivity;
	private ProgressDialog mProgressDlg;
	
	private long mFinishedSize;
	private BACKUPABLES_KEY mCurrentBackupable;
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
	protected RESULT doInBackground(BackupParameter... parameters) {
		BackupParameter parameter = parameters[0];
		Log.d( "Backup tasks: ", parameter );
		mProgressDlg.setMax((int) getBackupSize( ));
		
		boolean first = true;
		Hashtable<BACKUPABLES_KEY, Backupable> backupables = getBackupables();

		BACKUPABLES_KEY[] keySet = BACKUPABLES_KEY.values();
		for( int i = 0; i < keySet.length; i++ ) {
			BACKUPABLES_KEY key = keySet[i];
			mCurrentBackupable = key;
			try {
				RESULT r
					= backupOne(backupables, key, parameter, first,
								i == keySet.length-1 );
				if( r != RESULT.OK ) {
					return r;
				}
				Log.d( "Backup successfully: ", mCurrentBackupable );
			} catch ( GeneralSecurityException | IOException e) {
				Log.e( e, "Backup failed: ", mCurrentBackupable );
				return RESULT.FAILED;
			}
			first = false;
		}
		
		mCurrentBackupable = null;
		return RESULT.OK;
	}

	private Hashtable<BACKUPABLES_KEY, Backupable> getBackupables() {
		return BackupableConst.getBackupables(
				(EncryptItApplication) mActivity.getApplicationContext() );
	}

	private RESULT backupOne(Hashtable<BACKUPABLES_KEY, Backupable> backupables,
							 BACKUPABLES_KEY key, BackupParameter parameter,
							 boolean first, boolean last)
			throws IOException, GeneralSecurityException {
		
		Backupable source = backupables.get(key);
		String message
			= mActivity.getString( R.string.promptBackup, source.displayName() );
		publishProgress( message, 0 );
		
		BackupOutputStream out = null;
		try( InputStream in = source.openBackupSrcInputStream() ) {
			
			out = parameter.mTargetFiles.openOutputStream(mActivity, key,
						parameter.mTargetUri, parameter.mPrefix,
						parameter.mPassword, first);
			
			int count = 0;
			while( ( count = in.read( mBuffer ) ) > 0 ) {
				if( isCancelled() ) {
					return RESULT.CANCELLED;
				}
				out.write(mBuffer, 0, count);
				publishProgress( message, count );
			}
			
			BackupOutputStream.CLOSE_TYPE ct
				= last ? BackupOutputStream.CLOSE_TYPE.NORMAL_ALL 
					: BackupOutputStream.CLOSE_TYPE.NORMAL_ONE;
			out.close( ct );
			out = null;
		} catch ( Exception e ) {
			if( out != null ) {
				out.close( BackupOutputStream.CLOSE_TYPE.EXCEPTION );
				out = null;
			}
			throw e;
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
		Hashtable<BACKUPABLES_KEY, Backupable> backupables = getBackupables();
		switch( result ) {
			case OK:
				return mActivity.getString( R.string.promptBackupOk,
										    BackupableConst.toString(backupables,
										    						 "\r\n ") );
			case FAILED:
				return mActivity.getString(R.string.promptBackupFailed,
							backupables.get(mCurrentBackupable).displayName() );
			case CANCELLED:
				return mActivity.getString( R.string.promptBackupCancelled);
			default:
				return "";
		}
	}

	private long getBackupSize() {
		long size = 0;
		Hashtable<BACKUPABLES_KEY, Backupable> backupables = getBackupables();
		
		for( BACKUPABLES_KEY key : BACKUPABLES_KEY.values() ) {
			size += backupables.get(key).size();
		}
		return size;
	}
}
