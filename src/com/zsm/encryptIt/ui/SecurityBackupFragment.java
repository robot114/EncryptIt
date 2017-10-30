package com.zsm.encryptIt.ui;

import java.util.Hashtable;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.zsm.android.ui.Utility;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.backup.BackupOperator;
import com.zsm.encryptIt.backup.BackupTask;
import com.zsm.encryptIt.backup.PasswordBackupOperator;
import com.zsm.security.PasswordPolicy;

public class SecurityBackupFragment extends BaseSecurityBackupFragment {

	private VisiblePassword mPasswordConfirmView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		
		if( mView == null ) {
			mView = inflater.inflate( R.layout.security_backup_fragment,
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
				doBackup();
			}
		} );
		
		mPasswordConfirmView
			= (VisiblePassword)mView.findViewById( R.id.backupPasswordConfirm );
		mPasswordConfirmView.addTextChangedListener(tw);
	}

	@Override
	protected void alignLabels() {
		Utility.makeTextViewsSameWidth( mPasswordConfirmView.getLabel(),
										mPasswordView.getLabel() );
	}

	private void doBackup() {
		final Vector<DocumentFile> files = checkBackupFileExist();
		if( files.size() > 0 ) {
			doWithExistTarget(files);
		} else {
			executeBackup();
		}
	}

	private void doWithExistTarget(final Vector<DocumentFile> files) {
		StringBuilder strBuilder = new StringBuilder();
		for( DocumentFile df : files ) {
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
						for( DocumentFile f : files ) {
							f.delete();
						}
						executeBackup();
					}
			})
			.setNegativeButton( android.R.string.cancel, null )
			.show();
	}

	private void executeBackup() {
		final ContentResolver contentResolver = getApp().getContentResolver();
		final char[] password = mPasswordView.getPassword().toCharArray();
		
		Uri directoryUri
			= DocumentsContract.buildDocumentUriUsingTree(mPathUri,
					DocumentsContract.getTreeDocumentId(mPathUri));

		Hashtable<String, String> backupNames = getBackupFileNames();
		BackupOperator param[] = new BackupOperator[backupNames.size()];
		int i = 0;
		for( String ext : backupNames.keySet() ) {
			Uri uri
				= DocumentsContract.createDocument(contentResolver,
							directoryUri, MIME_TYPE, backupNames.get(ext));
			
			param[i++]
				= new PasswordBackupOperator( contentResolver,
											  getBackupables().get(ext),
											  uri, password);
		}
		new BackupTask( getActivity() ).execute( param );
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

}
