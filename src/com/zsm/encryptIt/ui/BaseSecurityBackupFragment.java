package com.zsm.encryptIt.ui;

import java.util.Hashtable;
import java.util.Vector;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.backup.Backupable;
import com.zsm.encryptIt.ui.preferences.Preferences;
import com.zsm.log.Log;
import com.zsm.security.KeyManager;

public abstract class BaseSecurityBackupFragment extends Fragment {

	protected static final String MIME_TYPE = "application/octet-stream";
	protected static final int REQUEST_CODE_BACKUP_PATH = 201;
	protected static final int REQUEST_CODE_SUBCLASS = 300;
	
	private static final String[] BACKUP_EXT = { ".key", ".db" };
	private static Hashtable<String, Backupable> BACKUPABLES;
							
	protected View mView;
	protected VisiblePassword mPasswordView;
	protected Button mActionButton;
	protected EditText mNameView;
	protected Uri mPathUri;
	private TextView mHintView;
	private TextView mPathView;
	
	private Hashtable<String, String> mBackupFileNames;

	abstract protected void afterInitViews( TextWatcher tw );
	abstract protected int checkPasswordReslut();

	protected void initViews( ) {
		mPathUri = Preferences.getInstance().getBackupSecurityUri();
		mPathView
			= (TextView)mView.findViewById( R.id.textViewPath );
		updatePathView();
		
		Button buttonPath = (Button)mView.findViewById(R.id.buttonBackupPath);
		buttonPath.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = getIntent( Intent.ACTION_OPEN_DOCUMENT_TREE );
				startActivityForResult(intent, REQUEST_CODE_BACKUP_PATH );
			}
		} );
		
		mNameView = (EditText)mView.findViewById( R.id.editTextFileName );
		mPasswordView
			= (VisiblePassword)mView.findViewById( R.id.backupPassword );
		mHintView
			= (TextView)mView.findViewById( R.id.textViewCheckResult );
		mActionButton = (Button)mView.findViewById( R.id.buttonAction );
		TextWatcher tw = new PasswordTransformationMethod(){
			@Override
			public void afterTextChanged(Editable s) {
				mActionButton.setEnabled( checkForAction() );
			}
		};
		mPasswordView.addTextChangedListener( tw );
		mPathView.addTextChangedListener(tw);
		mNameView.addTextChangedListener(tw);
		
		mActionButton.setEnabled( false );
		
		final ViewTreeObserver.OnGlobalLayoutListener listener
					= new ViewTreeObserver.OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				mView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				
				alignLabels();
			}
		};
		
		mView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
		
		afterInitViews( tw );
	}

	protected abstract void alignLabels();
	
	private Intent getIntent( String action ) {
		Intent intent = new Intent(action);
//		intent.addCategory(Intent.CATEGORY_OPENABLE);
//		intent.setType("*/*");
		String[] mimetypes = {"application/key", "application/db"};
		intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
		return intent;
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

	private boolean checkForAction() {
		int id = checkBackupResult();
		if( id > 0 ) {
			mHintView.setText( id );
			return false;
		}
		
		mHintView.setText( "" );
		return true;
	}

	protected EncryptItApplication getApp() {
		return (EncryptItApplication)getActivity().getApplication();
	}

	protected Hashtable<String, Backupable> getBackupables() {
		if( BACKUPABLES == null ) {
			BACKUPABLES = new Hashtable<String, Backupable>( BACKUP_EXT.length );
			BACKUPABLES.put( ".key", KeyManager.getInstance() );
			BACKUPABLES.put(
				".db", getApp().getItemListController().getItemStorageAdapter() );
		}

		return BACKUPABLES;
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
				= data.getFlags() 
				  & ( Intent.FLAG_GRANT_WRITE_URI_PERMISSION
					  | Intent.FLAG_GRANT_READ_URI_PERMISSION );
	
			getApp().getContentResolver()
				.takePersistableUriPermission(mPathUri, takeFlags );
			Preferences.getInstance().setSecurityBackupUri( mPathUri );
			
			updatePathView();
			mActionButton.setEnabled( checkForAction() );
		}
	}

	private void updatePathView() {
		mPathView.setText( mPathUri == null ? "" : mPathUri.getLastPathSegment() );
	}
	
	protected Hashtable<String, String> getBackupFileNames() {
		if( mBackupFileNames == null ) {
			mBackupFileNames = new Hashtable<String, String>(BACKUP_EXT.length);
		}
		
		String nameStr = mNameView.getText().toString();
		for( String ext : BACKUP_EXT ) {
			mBackupFileNames.put(ext, nameStr + ext );
		}
		
		return mBackupFileNames;
	}
	
	protected Vector<DocumentFile> checkBackupFileExist() {
		final Vector<DocumentFile> files
			= new Vector<DocumentFile>( BACKUP_EXT.length );
		
		String nameStr = mNameView.getText().toString();
		DocumentFile dir = DocumentFile.fromTreeUri(getApp(), mPathUri);
		for( String ext : BACKUP_EXT ) {
			final String fn = nameStr + ext;
			final DocumentFile file = dir.findFile( fn );
			if( file != null && !file.isDirectory() ) {
				files.add(file);
			}
		}
		return files;
	}

	protected Vector<String> getMissedBackupFilesInBackupDir() {
		final Vector<String> files = new Vector<String>( BACKUP_EXT.length );
		
		String nameStr = mNameView.getText().toString();
		DocumentFile dir = DocumentFile.fromTreeUri(getApp(), mPathUri);
		for( String ext : BACKUP_EXT ) {
			final String fn = nameStr + ext;
			final DocumentFile file = dir.findFile( fn );
			if( file == null || file.isDirectory() ) {
				files.add(fn);
			}
		}
		return files;
	}

}
