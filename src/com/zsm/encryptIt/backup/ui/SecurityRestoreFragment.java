package com.zsm.encryptIt.backup.ui;

import java.security.Key;
import java.util.Arrays;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.provider.DocumentFile;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.action.KeyAction;
import com.zsm.encryptIt.android.action.PasswordPromptParameter;
import com.zsm.encryptIt.backup.BackupTargetFilesConsts;
import com.zsm.encryptIt.backup.BackupTargetFilesConsts.BACKUP_TARGET_KEY;
import com.zsm.encryptIt.backup.BackupableConst;
import com.zsm.encryptIt.backup.RESULT;
import com.zsm.encryptIt.backup.RestoreTask;
import com.zsm.encryptIt.ui.ActivityOperator;
import com.zsm.log.Log;
import com.zsm.security.PasswordHandler;

public class SecurityRestoreFragment extends BaseSecurityBackupFragment
				implements ActivityOperator {

	private static final int REQUEST_CODE_RELOGIN = REQUEST_CODE_FOR_SUBCLASS + 1;
	
	public SecurityRestoreFragment() {
		super(R.layout.security_restore_fragment, OPERATION.RESTORE);
	}

	@Override
	protected void afterInitViews(TextWatcher tw) {
	}

	@Override
	public void doAction( View v ) {
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
	
	@Override
	protected void alignLabels() {
	}

	private void doRestore() {
		String fileNamePrefix = getPrefixFromView();
		final char[] password = mPasswordView.getPassword().toCharArray();
		Hashtable<BACKUP_TARGET_KEY, DocumentFile> valids
			= new Hashtable<BACKUP_TARGET_KEY, DocumentFile>();
		Hashtable<BACKUP_TARGET_KEY, String> invalids
			= new Hashtable<BACKUP_TARGET_KEY, String>();
		
		BackupTargetFilesConsts.getBackupTargetFilesInstance()
			.checkTargetFileValid(getApp(), mPathUri, fileNamePrefix,
									 password, valids, invalids);
		
		if( invalids.size() > 0 ) {
			Log.d( "Backup file invalid: ",
				   Arrays.toString( invalids.values().toArray() ) );
			doWithInvalids(invalids);
		} else {
			executeRestore( valids );
		}
	}

	private void doWithInvalids(Hashtable<BACKUP_TARGET_KEY, String> missed) {
		StringBuilder builder = new StringBuilder( );
		
		for( String fileName : missed.values() ) {
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

	private void executeRestore(Hashtable<BACKUP_TARGET_KEY,DocumentFile> valids) {
		Log.d( "Restored from the following backup files: ",
				Arrays.toString( valids.values().toArray() ) );
		
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
		} ).execute( newBackupParameter() );
	}

	protected void onRestoreSucceed() {
		
		try {
			KeyAction.getInstance().reinitialize();
		} catch (Exception e) {
			Log.e( e, "Reinitialize the key actor failed!" );
			doReinitializeFailed( R.string.promptInitKeyFailed );
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
				doReinitializeFailed( R.string.promptReloginFailed );
				break;
		}
	}

	private void doReloginOK(Intent data) {
		try {
			getApp().getItemListController().getItemStorageAdapter().reopen();
		} catch (Exception e) {
			Log.e( e, "Reopen the storage provider failed!" );
			doReinitializeFailed( R.string.promptReopenDBFailed );
			return;
		}
		Key key = (Key) data.getSerializableExtra( PasswordHandler.KEY_KEY );
		boolean res
			= getApp().getUIListOperator().initList(key, new Handler( ) );
		if( !res ) {
			doReinitializeFailed( R.string.promptReloginFailed );
		} else {
			getActivity().finish();
		}
	}

	private void doReinitializeFailed(int promptId) {
		new AlertDialog.Builder( getActivity() )
			.setIcon( android.R.drawable.ic_dialog_alert )
			.setMessage( promptId )
			.setPositiveButton( R.string.titleRestoreUndo,
								new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					int resId
						= BackupableConst.undoRestore( getApp() ) 
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

	@Override
	public void finishAffinity() {
		getActivity().finishAffinity();
	}

	@Override
	protected String getOpenSingleDocumentAction() {
		return Intent.ACTION_OPEN_DOCUMENT;
	}
}
