package com.zsm.encryptIt.backup.ui;

import java.util.Hashtable;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.provider.DocumentFile;
import android.text.TextWatcher;
import android.view.View;

import com.zsm.android.ui.Utility;
import com.zsm.android.ui.VisiblePassword;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.backup.BackupTargetFiles;
import com.zsm.encryptIt.backup.BackupTargetFilesConsts;
import com.zsm.encryptIt.backup.BackupTargetFilesConsts.BACKUP_TARGET_KEY;
import com.zsm.encryptIt.backup.BackupTask;
import com.zsm.security.PasswordPolicy;

public class SecurityBackupFragment extends BaseSecurityBackupFragment {

	private VisiblePassword mPasswordConfirmView;
	
	public SecurityBackupFragment() {
		super(R.layout.security_backup_fragment, OPERATION.BACKUP);
	}

	@Override
	protected void afterInitViews(TextWatcher tw) {
		mPasswordConfirmView
			= (VisiblePassword)mView.findViewById( R.id.backupPasswordConfirm );
		mPasswordConfirmView.addTextChangedListener(tw);
	}

	@Override
	protected void alignLabels() {
		Utility.makeTextViewsSameWidth( mPasswordConfirmView.getLabel(),
										mPasswordView.getLabel() );
	}

	@Override
	protected void doAction(View view) {
		BackupTargetFiles btf = BackupTargetFilesConsts.getBackupTargetFilesInstance();
		Hashtable<BACKUP_TARGET_KEY, DocumentFile> existings
						= new Hashtable<BACKUP_TARGET_KEY, DocumentFile>();
		Hashtable<BACKUP_TARGET_KEY, String> missed
						= new Hashtable<BACKUP_TARGET_KEY, String>();
		btf.checkExistingTargetFile(getActivity(), mPathUri,
									getPrefixFromView(),
									existings, missed);
		if( existings.size() > 0 ) {
			doWithExistTarget(existings);
		} else {
			executeBackup();
		}
	}

	private void doWithExistTarget(
					final Hashtable<BACKUP_TARGET_KEY, DocumentFile> existings) {
		
		StringBuilder strBuilder = new StringBuilder();
		for( DocumentFile df : existings.values() ) {
			strBuilder.append( df.getName() );
			strBuilder.append( "\r\n" );
		}
		AlertDialog.Builder dlgBuilder = new AlertDialog.Builder( getActivity() );
		String message
			= getApp().getString( R.string.promptBackupExist,
								  strBuilder.toString() );
		dlgBuilder
			.setMessage( message )
			.setIcon( android.R.drawable.ic_dialog_alert )
			.setTitle( R.string.titleBackup )
			.setPositiveButton( R.string.overwrite,
				new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for( DocumentFile f : existings.values() ) {
							f.delete();
						}
						executeBackup();
					}
			})
			.setNegativeButton( android.R.string.cancel, null )
			.show();
	}

	private void executeBackup() {
		new BackupTask( getActivity() ).execute( newBackupParameter() );
	}

	@Override
	protected int checkPasswordReslut() {
		String pwdStr = mPasswordView.getPassword();
		char[] password = pwdStr.toCharArray();
		PasswordPolicy passwordPolicy
			= EncryptItApplication.getPasswordPolicy();
		PasswordPolicy.Result res = passwordPolicy.check( password );
		if( res != PasswordPolicy.GoodResult.GOOD ) {
			return (int)passwordPolicy.getResult(res);
		}
		
		if( !pwdStr.equals( mPasswordConfirmView.getPassword() ) ) {
			return R.string.promptPasswordNotEqual;
		}
		return 0;
	}

	@Override
	protected String getOpenSingleDocumentAction() {
		return Intent.ACTION_CREATE_DOCUMENT;
	}

}
