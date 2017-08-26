package com.zsm.encryptIt.ui;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.backup.PasswordBackupOperator;
import com.zsm.encryptIt.backup.BackupTask;
import com.zsm.encryptIt.ui.preferences.Preferences;
import com.zsm.log.Log;
import com.zsm.security.KeyManager;
import com.zsm.security.PasswordPolicy;

public class SecurityBackupFragment extends Fragment {

	private static final String MIME_TYPE = "application/octet-stream";
	protected static final int REQUEST_CODE_BACKUP_PATH = 201;
	private View mView;
	private VisiblePassword mPasswordView;
	private VisiblePassword mPasswordConfirmView;
	private TextView mHintView;
	private Button mBackupButton;
	private TextView mPathView;
	private EditText mNameView;
	private Uri mPathUri;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		
		if( mView == null ) {
			mView = inflater.inflate( R.layout.security_backup_fragment,
									  container, false );
			
			mPathUri = Preferences.getInstance().getBackupSecurityUri();
			mPathView
				= (TextView)mView.findViewById( R.id.textViewPath );
			updatePathView();
			
			Button buttonPath = (Button)mView.findViewById(R.id.buttonBackupPath);
			buttonPath.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent( Intent.ACTION_OPEN_DOCUMENT_TREE );
					startActivityForResult(intent, REQUEST_CODE_BACKUP_PATH );
				}
			} );
			
			mNameView = (EditText)mView.findViewById( R.id.editTextFileName );
			mPasswordView
				= (VisiblePassword)mView.findViewById( R.id.backupPassword );
			mPasswordConfirmView
				= (VisiblePassword)mView.findViewById( R.id.backupPasswordConfirm );
			mHintView
				= (TextView)mView.findViewById( R.id.textViewBackupCheckResult );
			mBackupButton = (Button)mView.findViewById( R.id.buttonBackup );
			mBackupButton.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					doBackup();
				}
			} );
			
			TextWatcher tw = new PasswordTransformationMethod(){
				@Override
				public void afterTextChanged(Editable s) {
					mBackupButton.setEnabled( checkForBackup() );
				}
			};
			mPasswordView.addTextChangedListener( tw );
			mPasswordConfirmView.addTextChangedListener(tw);
			mPathView.addTextChangedListener(tw);
			mNameView.addTextChangedListener(tw);
			
			mBackupButton.setEnabled( false );
		}
		
		return mView;
	}
	
	private void doBackup() {
		StringBuilder strBuilder = new StringBuilder();
		final Vector<DocumentFile> files = checkFileExist(strBuilder);
		if( files.size() > 0 ) {
			AlertDialog.Builder dlgBuilder
				= new AlertDialog.Builder( getActivity() );
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
		} else {
			executeBackup();
		}
	}

	private Vector<DocumentFile> checkFileExist(StringBuilder strBuilder) {
		final Vector<DocumentFile> files = new Vector<DocumentFile>( 5 );
		
		String nameStr = mNameView.getText().toString();
		DocumentFile dir = DocumentFile.fromTreeUri(getApp(), mPathUri);
		final String keyName = nameStr + ".key";
		final DocumentFile keyFile = dir.findFile( keyName );
		if( keyFile != null ) {
			strBuilder.append( keyName );
			files.add(keyFile);
		}
		final String dbName = nameStr + ".db";
		final DocumentFile dbFile = dir.findFile( dbName );
		if( dbFile != null ) {
			strBuilder.append( strBuilder.length() > 0 ? ", " : "" );
			strBuilder.append( dbName );
			files.add(dbFile);
		}
		return files;
	}

	private void executeBackup() {
		String nameStr = mNameView.getText().toString();
		final ContentResolver contentResolver = getApp().getContentResolver();
		final char[] password = mPasswordView.getPassword().toCharArray();
		
		Uri directoryUri
			= DocumentsContract.buildDocumentUriUsingTree(mPathUri,
					DocumentsContract.getTreeDocumentId(mPathUri));

		final String keyName = nameStr + ".key";
		final Uri keyUri
			= DocumentsContract.createDocument(contentResolver,
					   directoryUri, MIME_TYPE, keyName);
		
		final PasswordBackupOperator kmParam
			= new PasswordBackupOperator( contentResolver,
										  KeyManager.getInstance(), keyUri,
										  password);

		final String dbName = nameStr + ".db";
		final Uri dbUri
			= DocumentsContract.createDocument(contentResolver,
					   directoryUri, MIME_TYPE, dbName);
		final PasswordBackupOperator dbParam
			= new PasswordBackupOperator( 
					contentResolver,
					getApp().getItemListController().getBackupInputAgent(),
					dbUri, password);

		new BackupTask( getActivity() ).execute( kmParam, dbParam );
	}
	
	private int checkPasswordReslut() {
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

	private int checkBackupResult() {
		if( mPathView.getText().length() == 0 ) {
			return R.string.promptBackupPathEmpty;
		}
		
		if( mNameView.getText().toString().trim().length() == 0 ) {
			return R.string.promptBackupNameEmpty;
		}
		
		return checkPasswordReslut();
	}
	
	private boolean checkForBackup() {
		int id = checkBackupResult();
		if( id > 0 ) {
			mHintView.setText( id );
			return false;
		}
		
		mHintView.setText( "" );
		return true;
	}
	
	private EncryptItApplication getApp() {
		return (EncryptItApplication)getActivity().getApplication();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch( requestCode ) {
			case REQUEST_CODE_BACKUP_PATH:
				if( resultCode == Activity.RESULT_OK ) {
					doBackupPath(data);
				}
				break;
			default:
				Log.w( "Invalid request code: ", requestCode );
				break;
		}
	}

	private void doBackupPath(Intent data) {
		if( data != null && data.getData() != null ) {
			mPathUri = data.getData();
			int takeFlags
				= data.getFlags() & Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
	
			getApp().getContentResolver()
				.takePersistableUriPermission(mPathUri, takeFlags );
			Preferences.getInstance().setSecurityBackupUri( mPathUri );
			
			updatePathView();
			mBackupButton.setEnabled( checkForBackup() );
		}
	}

	private void updatePathView() {
		mPathView.setText( mPathUri == null ? "" : mPathUri.getLastPathSegment() );
	}

}
