package com.zsm.encryptIt.ui;

import java.security.Key;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.provider.DocumentFile;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.action.KeyAction;
import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.encryptIt.backup.PasswordRestoreOperator;
import com.zsm.encryptIt.backup.RESULT;
import com.zsm.encryptIt.backup.RestoreOperator;
import com.zsm.encryptIt.backup.RestoreTask;
import com.zsm.log.Log;
import com.zsm.security.PasswordHandler;

public class SecurityRestoreFragment extends BaseSecurityBackupFragment
				implements ActivityOperator {

	private static final int REQUEST_CODE_RELOGIN = REQUEST_CODE_SUBCLASS + 1;
	private RestoreOperator mRestoreParameters[];
	
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
		mActionButton.setOnClickListener( new OnClickListener() {
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
	
	@Override
	protected void alignLabels() {
	}

	private void doRestore() {
		final Vector<String> missedFiles = getMissedBackupFilesInBackupDir();
		if( missedFiles.size() > 0 ) {
			Log.d( "Backup file missed: ", missedFiles );
			doWithMissSource(missedFiles);
		} else {
			executeRestore();
		}
	}

	private void doWithMissSource(Vector<String> missedFiles) {
		StringBuilder builder = new StringBuilder( );
		
		for( String fileName : missedFiles ) {
			builder.append( fileName ).append( "\r\n" );
		}
		Activity context = getActivity();
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
		String message
			= context.getString( 
				R.string.promptRestoreMissFile, builder.toString() );
		
		dlgBuilder
			.setTitle( R.string.app_name )
			.setIcon( android.R.drawable.ic_dialog_alert )
			.setMessage( message )
			.setNegativeButton( R.string.close, null )
			.show();
	}

	private void executeRestore() {
		final ContentResolver contentResolver = getApp().getContentResolver();
		final char[] password = mPasswordView.getPassword().toCharArray();
		
		final Vector<DocumentFile> backupFiles = checkBackupFileExist();
		Log.d( "Restored from the following backup files: ", backupFiles );
		int size = backupFiles.size();
		mRestoreParameters = new RestoreOperator[size];
		for( int i = 0; i < size; i++ ) {
			DocumentFile bf = backupFiles.get(i);
			final Uri uri = bf.getUri();
			final String name = uri.getLastPathSegment();
			final int lastIndex = name.lastIndexOf( "." );
			final String ext = name.substring(lastIndex);
			mRestoreParameters[i]
				= new PasswordRestoreOperator( contentResolver,
											   getBackupables().get(ext),
											   uri, password);
		}
		new RestoreTask( getActivity(), new RestoreTask.ResultCallback(){
			@Override
			public void onFinished(RESULT res) {
				switch( res ) {
					case OK:
						onRestoreSucceed( );
						break;
					default:
						getActivity().finish();
						break;
				}
			}
		} ).execute( mRestoreParameters );
	}

	protected void onRestoreSucceed() {
		
		try {
			KeyAction.getInstance().reinitialize();
		} catch (Exception e) {
			Log.e( e, "Reinitialize the key actor failed!" );
			doReinitializeFailed( R.string.promptInitKeyFailed,
								  mRestoreParameters );
			return;
		}
		getApp().promptPassword( this, REQUEST_CODE_RELOGIN );
	}

	@Override
	protected int checkPasswordReslut() {
		return 0;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch( requestCode ) {
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
			case REQUEST_CODE_RELOGIN:
				doRelogin( resultCode, data );
				break;
		}
	}

	private void doRelogin(int resultCode, Intent data) {
		switch( resultCode ) {
			case Activity.RESULT_OK:
				doReloginOK(data);
				break;
			case PasswordPromptParameter.LOGIN_FAILED:
			default:
				doReinitializeFailed( R.string.promptReloginFailed,
									  mRestoreParameters );
				break;
		}
	}

	private void doReloginOK(Intent data) {
		try {
			getApp().getItemListController().getItemStorageAdapter().reopen();
		} catch (Exception e) {
			Log.e( e, "Reopen the storage provider failed!" );
			doReinitializeFailed( R.string.promptReopenDBFailed,
		  			  			  mRestoreParameters );
			return;
		}
		Key key = (Key) data.getSerializableExtra( PasswordHandler.KEY_KEY );
		boolean res
			= getApp().getUIListOperator().initList(key, new Handler( ) );
		if( !res ) {
			doReinitializeFailed( R.string.promptReloginFailed,
					  			  mRestoreParameters );
		} else {
			getActivity().finish();
		}
	}

	private void doReinitializeFailed(int promptId, final RestoreOperator[] param) {
		new AlertDialog.Builder( getActivity() )
			.setIcon( android.R.drawable.ic_dialog_alert )
			.setMessage( promptId )
			.setPositiveButton( R.string.titleRestoreUndo,
								new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int resId
						= undoRestore( param ) 
							? R.string.promptRestoreUndoOK
							: R.string.promptRestoreFailed;
					
					Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT ).show();
					getActivity().finish();
				}
			} )
			.setNegativeButton( R.string.titleRestoreExitApp,
								new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getApp().getItemListController().getItemStorageAdapter().close();
					finishAffinity();
				}
			})
			.create().show();
			
	}

	protected boolean undoRestore(RestoreOperator[] param) {
		boolean undo = true;
		for( RestoreOperator t : param ) {
			try {
				if( t.restoreFromRename() ) {
					t.reopenTarget();
				} else {
					undo = false;
				}
			} catch (Exception e) {
				Log.w(e, "Restore from the renamed locally failed: ", t);
				undo = false;
			}
		}
		
		return undo;
	}

	@Override
	public void finishAffinity() {
		getActivity().finishAffinity();
	}
}
