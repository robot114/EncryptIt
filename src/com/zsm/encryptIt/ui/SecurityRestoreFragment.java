package com.zsm.encryptIt.ui;

import java.util.Vector;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.backup.PasswordRestoreOperator;
import com.zsm.encryptIt.backup.RestoreOperator;
import com.zsm.encryptIt.backup.RestoreTask;
import com.zsm.log.Log;

public class SecurityRestoreFragment extends BaseSecurityBackupFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		
		if( mView == null ) {
			mView = inflater.inflate( R.layout.security_restore_fragment,
									  container, false );
			
			initViews();
		}
		
		return mView;
	}
	
	@Override
	protected void afterInitViews(TextWatcher tw) {
		mBackupButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder
					= new AlertDialog.Builder(getActivity());
				builder
					.setTitle( R.string.app_name )
					.setIcon( android.R.drawable.ic_dialog_alert )
					.setMessage( R.string.promptRestoreOverwrite )
					.setPositiveButton( R.string.overwrite,
							new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							doRestore();
						}
					})
					.setNegativeButton( android.R.string.cancel, null )
					.show();
			}
		} );
	}
	
	private void doRestore() {
		final Vector<DocumentFile> missedFiles = getMissedBackupFilesInBackupDir();
		if( missedFiles.size() > 0 ) {
			Log.d( "Backup file missed: ", missedFiles );
			doWithMissSource(missedFiles);
		} else {
			executeRestore();
		}
	}

	private void doWithMissSource(Vector<DocumentFile> missedFiles) {
		StringBuilder builder = new StringBuilder();
		for( DocumentFile file : missedFiles ) {
			builder.append( file.getName() ).append( "\r\n" );
		}
		Toast.makeText(getActivity(), builder.toString(), Toast.LENGTH_LONG )
			 .show();
	}

	private void executeRestore() {
		final ContentResolver contentResolver = getApp().getContentResolver();
		final char[] password = mPasswordView.getPassword().toCharArray();
		
		final Vector<DocumentFile> backupFiles = checkBackupFileExist();
		Log.d( "Restored from the following backup files: ", backupFiles );
		int size = backupFiles.size();
		final RestoreOperator param[] = new RestoreOperator[size];
		for( int i = 0; i < size; i++ ) {
			DocumentFile bf = backupFiles.get(i);
			final Uri uri = bf.getUri();
			final String name = uri.getLastPathSegment();
			final int lastIndex = name.lastIndexOf( "." );
			final String ext = name.substring(lastIndex);
			param[i]
				= new PasswordRestoreOperator( contentResolver,
											   getBackupables().get(ext),
											   uri, password);
		}
		new RestoreTask( getActivity() ).execute( param );
	}

	@Override
	protected int checkPasswordReslut() {
		return 0;
	}
}
