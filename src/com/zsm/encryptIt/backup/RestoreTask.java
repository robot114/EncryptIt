package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Hashtable;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.provider.DocumentFile;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.backup.BackupTargetFilesConsts.BACKUP_TARGET_KEY;
import com.zsm.encryptIt.backup.BackupableConst.BACKUPABLES_KEY;
import com.zsm.log.Log;

public class RestoreTask extends AsyncTask<BackupParameter, Object, RESULT> {
	
	public interface ResultCallback {
		void onFinished( RESULT res );
	}
	
	private static final int BUFFER_SIZE = 4096;
	private Context mContext;
	private ProgressDialog mProgressDlg;
	
	private long mFinishedSize;
	private byte[] mBuffer;
	private ResultCallback mResultCallback;
	private BACKUPABLES_KEY mCurrentRestore;

	public RestoreTask(Context activity, ResultCallback resultCallback ) {
		super();
		mContext = activity;
		mBuffer = new byte[BUFFER_SIZE];
		mResultCallback = resultCallback;
	}
	
	protected Context getContext() {
		return mContext;
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
		Log.d( "Restore parameter: ", parameter );
		
		Hashtable<BACKUP_TARGET_KEY, DocumentFile> valids
			= new Hashtable<BACKUP_TARGET_KEY, DocumentFile>();
		Hashtable<BACKUP_TARGET_KEY, String> invalids
			= new Hashtable<BACKUP_TARGET_KEY, String>();
		BackupTargetFilesConsts.getBackupTargetFilesInstance()
			.checkTargetFileValid(mContext, parameter.mTargetUri, parameter.mPrefix,
								  parameter.mPassword, valids, invalids);
		
		mProgressDlg.setMax((int) getTotalSize( valids ));
		if( invalids.size() > 0 ) {
			return RESULT.FAILED;
		}
		
		Hashtable<BACKUPABLES_KEY, Backupable> toLocalOk = protectCurrent();
		if( toLocalOk.size() < valids.size() ) {
			Log.w( "Rename to backup current used failed. "+
					"The followings are renamed successfully:",
					toLocalOk.values().toArray() );
			restoreCurrent( toLocalOk );
			return RESULT.FAILED;
		}
		
		RESULT res = doRestore( valids, parameter );
		if( res != RESULT.OK  ) {
			restoreAllCurrent();
			return res;
		}
		return RESULT.OK;
	}

	private RESULT doRestore(Hashtable<BACKUP_TARGET_KEY, DocumentFile> valids,
							 BackupParameter parameter) {
		
		Hashtable<BACKUPABLES_KEY, Backupable> backupables
				= BackupableConst.getBackupables( getApp() );
		
		for( BACKUPABLES_KEY key : BACKUPABLES_KEY.values() ) {
			Backupable backupable = backupables.get(key);
			try {
				BACKUP_TARGET_KEY targetKey
					= parameter.mTargetFiles.getTargetKeyFromBackupableKey(key);
				RESULT r
					= restoreOne( key, backupable, valids.get(targetKey),
								  parameter.mPassword, parameter.mTargetFiles );
				
				if( r != RESULT.OK ) {
					return r;
				}
				Log.d( "Restore successfully: ",
					   backupables.get(key).displayName() );
			} catch ( GeneralSecurityException | IOException e) {
				Log.e( e, "Restore failed: ",
					   backupables.get(key).displayName() );
				return RESULT.FAILED;
			}
		}
		
		return RESULT.OK;
	}
	
	private Hashtable<BACKUPABLES_KEY, Backupable> protectCurrent( ) {
		
		Hashtable<BACKUPABLES_KEY, Backupable> backupables
				= BackupableConst.getBackupables( getApp() );
		Hashtable<BACKUPABLES_KEY, Backupable> toLocalOk
				= new Hashtable<BACKUPABLES_KEY, Backupable>( );
		
		for( BACKUPABLES_KEY key : BACKUPABLES_KEY.values() ) {
			Backupable backupable = backupables.get(key);
			boolean res;
			try {
				res = backupable.backupToLocal();
			} catch (IOException e) {
				Log.w( e, "Backup to local failed!", backupable.displayName() );
				res = false;
			}
			
			if( res ) {
				toLocalOk.put(key, backupable);
			} else {
				Log.w( "Backup to local return false!", backupable.displayName() );
				break;
			}
		}
		
		return toLocalOk;
	}

	private EncryptItApplication getApp() {
		return (EncryptItApplication)mContext.getApplicationContext();
	}
	
	private void restoreAllCurrent() {
		restoreCurrent( BackupableConst.getBackupables( getApp() ) );
	}

	private void restoreCurrent( Hashtable<BACKUPABLES_KEY,Backupable> toBeRestored ) {
		for( Backupable b : toBeRestored.values() ) {
			try {
				b.restoreFromLocalBackup();
			} catch (IOException e) {
				Log.w( e, "Restore from local failed!", b.displayName() );
			}
		}
	}

	private RESULT restoreOne(BACKUPABLES_KEY key, Backupable backupable,
							  DocumentFile documentFile, char[] password,
							  BackupTargetFiles targetFiles)
	
			throws FileNotFoundException, IOException, GeneralSecurityException {
		
		mCurrentRestore = key;
		String message
			= mContext.getString( R.string.promptRestore,
								  backupable.displayName() );
		publishProgress( message, 0 );
		
		try( 
			OutputStream out = backupable.openRestoreTargetOutputStream();
			InputStream in
				= targetFiles.openInputStream( getApp(), key, documentFile,
											   password ) ) {
			
			int count = 0;
			while( ( count = in.read( mBuffer ) ) > 0 ) {
				if( isCancelled() ) {
					Log.d( "Restore cancelled: ", key );
					restoreAllCurrent();
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
		Toast
			.makeText(mContext, getResultMessage( RESULT.CANCELLED ),
					  Toast.LENGTH_SHORT)
			.show();
		
		mResultCallback.onFinished( RESULT.CANCELLED );
	}

	protected ProgressDialog buildProgressDlg( ) {

		ProgressDialog dlg = new ProgressDialog(mContext);
		dlg.setTitle(R.string.app_name );
		dlg.setMessage( "" );
		dlg.setCancelable(false);
		dlg.setIndeterminate(false);
		dlg.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
		dlg.setButton(DialogInterface.BUTTON_NEGATIVE,
			mContext.getText(android.R.string.cancel),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancel(false);
				}

			});

		return dlg;
	};

	protected void showResult(final RESULT result) {
		mProgressDlg.setMessage( getResultMessage(result) );
		Button button = mProgressDlg.getButton( DialogInterface.BUTTON_NEGATIVE );
		button.setText( R.string.close );
		button.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mProgressDlg.dismiss();
				mResultCallback.onFinished(result);
			}
		} );
	}

	private String getResultMessage(RESULT result) {
		Hashtable<BACKUPABLES_KEY, Backupable> backupables = getBackupables();
		switch( result ) {
			case OK:
				return mContext.getString( R.string.promptRestoreOk,
						BackupableConst.toString( backupables, "\r\n " ) );
			case FAILED:
				String displayName
					= mCurrentRestore == null 
						? "" : backupables.get(mCurrentRestore).displayName();
				
				return mContext.getString(R.string.promptRestoreFailed, displayName );
			case CANCELLED:
				return mContext.getString( R.string.promptRestoreCancelled);
			default:
				return "";
		}
	}

	private long getTotalSize(Hashtable<BACKUP_TARGET_KEY,DocumentFile> valids) {
		return BackupTargetFilesConsts.getBackupTargetFilesInstance()
					.getTotalSize(valids);
	}

	private Hashtable<BACKUPABLES_KEY, Backupable> getBackupables() {
		return BackupableConst.getBackupables(
				(EncryptItApplication) mContext.getApplicationContext() );
	}
}
