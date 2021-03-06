package com.zsm.encryptIt.backup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.ui.preferences.Preferences;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.FilenameUtils;
import com.zsm.util.file.android.DocumentFileUtilities;


public abstract class ExportImportTask extends AsyncTask<Void, Integer, RESULT> {

	protected static final String EXTENSION_TEXT = "txt";
	protected static final String EXTENSION_XML = "xml";
	protected static final String EXTENSION_DIRECT = "edb";
	
	protected static final String MIME_TYPE_XML = "application/xml";
	protected static final String MIME_TYPE_TEXT = "text/plain";
	
	protected Context mContext;
	protected ProgressDialog mProgressDlg;
	protected String mFileName;
	public static final char PAGE_BREAK = (char) 12;
	protected static final String ENCODING = "UTF-8";
	protected static final String ELEMENT_NAME_ROOT = "tasks";
	
	private static final FileExtensionFilter[] EXPORT_FILTERS
		= new FileExtensionFilter[]{
			new FileExtensionFilter( EXTENSION_TEXT + "|" + EXTENSION_XML, "" ) };

	protected ExportImportTask(Context context, Uri uri ) {
		mContext = context;
		mFileName = DocumentFileUtilities.getName( uri );
	}

	protected void showResult(int messageId, RESULT result) {
		String message = getResultMessage(messageId, result);
		mProgressDlg.setMessage( message );
		mProgressDlg.getButton( DialogInterface.BUTTON_NEGATIVE )
						.setText( R.string.close );
	}

	private String getResultMessage(int messageId, RESULT result) {
		String message
			= mContext.getString( messageId, mFileName, result.getString(mContext) );
		return message;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		mProgressDlg.setProgress( (int) values[0] );
	}

	@Override
	protected void onCancelled() {
		mProgressDlg.dismiss();
		Toast
			.makeText(mContext, getResultMessage( R.string.promptExportResult,
												  RESULT.CANCELLED ),
					  Toast.LENGTH_SHORT)
			.show();
	}

	public static String getExportMimeType() {
		return Intent.normalizeMimeType(
					Preferences.getInstance().exportAsXml()
					? "text/xml" : "text/plain");
	}
	
	public static String getExportExtension() {
		return Preferences.getInstance().exportAsXml()
					? EXTENSION_XML : EXTENSION_TEXT;
	}

	public static FileExtensionFilter[] getExportFileFilter() {
		return EXPORT_FILTERS; 
	}
	
	public static String getDefaultImExPortExt() {
		return EXPORT_FILTERS[0].getDefaultExtension();
	}

	protected ProgressDialog buildProgressDlg( Context context, int titleId,
											   int max ) {
		
		ProgressDialog dlg = new ProgressDialog(context);
		dlg.setTitle(titleId);
		dlg.setMessage("");
		dlg.setMax( max );
		dlg.setCancelable(false);
		dlg.setIndeterminate(false);
		dlg.setButton( DialogInterface.BUTTON_NEGATIVE,
					   context.getText( android.R.string.cancel ),
					   new DialogInterface.OnClickListener() {
	
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ExportImportTask.this.cancel(false);
			}
			
		} );
		
		return dlg;
	};
	
	protected boolean asXml( String fileName ) {
		String ext = FilenameUtils.getExtension( fileName );
		return EXTENSION_XML.equalsIgnoreCase(ext);
	}
	
}
