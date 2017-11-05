package com.zsm.encryptIt.backup.ui;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsm.android.ui.VisiblePassword;
import com.zsm.android.ui.documentSelector.DocumentHandler;
import com.zsm.android.ui.documentSelector.DocumentOperation;
import com.zsm.android.ui.documentSelector.DocumentSelector;
import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.backup.BackupParameter;
import com.zsm.encryptIt.backup.BackupTargetFiles;
import com.zsm.encryptIt.backup.BackupTargetFilesConsts;
import com.zsm.encryptIt.ui.preferences.Preferences;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.FileUtilities;
import com.zsm.util.file.android.DocumentFileUtilities;

public abstract class BaseSecurityBackupFragment extends Fragment {

	protected static final String MIME_TYPE = "application/octet-stream";
	protected static final int REQUEST_CODE_FOR_SUBCLASS = 300;
	
	protected enum OPERATION { BACKUP, RESTORE };
							
	final private int mLayoutId;
	final private OPERATION mOperation;
	
	protected Uri mPathUri;
	
	protected View mView;
	protected VisiblePassword mPasswordView;
	protected ImageView mActionButton;
	private EditText mNameView;
	private TextView mHintView;
	private TextView mPathView;
	private DocumentSelector mDocumentSelector;
	
	abstract protected void afterInitViews( TextWatcher tw );
	abstract protected int checkPasswordReslut();

	public BaseSecurityBackupFragment( int layoutId, OPERATION o ) {
		mLayoutId = layoutId;
		mOperation = o;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if( mView == null ) {
			mView = inflater.inflate( mLayoutId, container, false );
			
			initViews();

			DocumentHandler handler = new DocumentHandler() {
				@Override
				public void handleDocument(DocumentOperation o,
										   DocumentFile document,
										   String fileName) {
					
					Uri uri = document.getUri();
					mPathUri
						= DocumentFileUtilities.getEncodePathUri( 
								getActivity(), uri );
					
					Preferences.getInstance().setSecurityBackupUri( mPathUri );
					
					String nfn
						= BackupTargetFilesConsts.getBackupTargetFilesInstance()
								.normalizeFileName(fileName);
					mNameView.setText(nfn);
					updatePathView();
				}
			};
			
			DocumentOperation operation;
			if( isBackupToMultiFiles() ) {
				operation = DocumentOperation.FOLDER;
			} else {
				operation
					= mOperation == OPERATION.BACKUP 
						? DocumentOperation.SAVE : DocumentOperation.LOAD;
			}
			
			final Uri backupSecurityUri
						= Preferences.getInstance().getBackupSecurityUri();
			
			final FileExtensionFilter[] extensionFilter
				= BackupTargetFilesConsts.getBackupTargetFilesInstance()
						.getExtensionFilter();
			
			mDocumentSelector
				= new DocumentSelector( getActivity(), operation,
										backupSecurityUri, handler,
										extensionFilter );
		}
		
		return mView;
	}

	protected void initViews( ) {
		mHintView
			= (TextView)mView.findViewById( R.id.textViewCheckResult );
		
		TextWatcher tw = new PasswordTransformationMethod(){
			@Override
			public void afterTextChanged(Editable s) {
				checkForAction();
			}
		};
		
		mPasswordView
			= (VisiblePassword)mView.findViewById( R.id.backupPassword );
		mPasswordView.addTextChangedListener( tw );
		
		initPathAndFile( tw );
		
		mActionButton = (ImageView)mView.findViewById( R.id.buttonAction );
		mActionButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = checkBackupResult();
				if( id > 0 ) {
					mHintView.setText(id);
				} else {
					doAction( v );
				}
			}
		});
		
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
	
	protected abstract String getOpenSingleDocumentAction();
	
	private void initPathAndFile(TextWatcher tw) {
		mPathUri = Preferences.getInstance().getBackupSecurityUri();
		TextView pathFileLabel = (TextView)mView.findViewById( R.id.textViewPathLabel );
		mPathView = (TextView)mView.findViewById( R.id.textViewPath );
		updatePathView();
		
		mPathView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDocumentSelector.show();
			}
		} );
		mPathView.addTextChangedListener(tw);
		
		mNameView = (EditText)mView.findViewById( R.id.editTextFileName );
		mNameView.addTextChangedListener(tw);
		if( isBackupToMultiFiles() ) {
			mPathView.setHint( R.string.hintBackupPath );
		} else {
			mPathView.setHint( R.string.hintBackupFile );
		}
	}

	protected abstract void alignLabels();
	
	protected abstract void doAction( View view );
	
	private int checkBackupResult() {
		if( mPathView.getText().length() == 0 ) {
			return isBackupToMultiFiles()
					? R.string.promptBackupPathEmpty
					: R.string.promptBackupFileEmpty;
		}
		
		if( isBackupToMultiFiles()
			&& mNameView.getText().toString().trim().length() == 0 ) {
			
			return R.string.promptBackupNameEmpty;
		}
		
		return checkPasswordReslut();
	}
	
	private boolean isBackupToMultiFiles() {
		return BackupTargetFilesConsts.getBackupTargetFilesInstance().isToMultiFiles();
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

	private void updatePathView() {
		mPathView.setText( 
				mPathUri == null ? "" : mPathUri.getLastPathSegment() );
	}
	
	protected BackupParameter newBackupParameter() {
		final char[] password = mPasswordView.getPassword().toCharArray();
		
		BackupTargetFiles btf = BackupTargetFilesConsts.getBackupTargetFilesInstance();
		String prefix = getPrefixFromView();
		BackupParameter param
			= new BackupParameter( mPathUri, prefix, password, btf);
		
		return param;
	}
	
	protected String getPrefixFromView() {
		BackupTargetFiles btf = BackupTargetFilesConsts.getBackupTargetFilesInstance();
		String prefix = mNameView.getText().toString();
		if( btf.acceptedByExtensionFilter(prefix) ) {
			prefix = FileUtilities.removeExtension(prefix);
		}
		
		return prefix;
	}
	
}
