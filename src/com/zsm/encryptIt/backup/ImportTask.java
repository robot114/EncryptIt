package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.action.ItemOperator;
import com.zsm.log.Log;

public class ImportTask extends ExportImportTask {

	private static final int IMPORT_FROM_TEXT = 0;
	private static final int IMPORT_FROM_XML = 1;
	private ItemOperator mOperator;
	private Uri mSourceUri;
	private long mSourceSize;
	
	/**
	 * Import the items from the sourceUri into the list in an AsyncTask.
	 * So the exporting progress is to be done in a thread.
	 * 
	 * @param context MUST be an instance of an Activity, otherwise the
	 * 			ProgressDialog will no be able to shown
	 * @param operator The import list. The items will APPEND to the end of the list
	 * @param sourceUri From which the items will be imported.
	 */
	public static void doImport( Context context, ItemOperator operator,
							     Uri sourceUri, long sourceSize ) {
		
		new ImportTask(context, operator, sourceUri, sourceSize).execute();
	}

	private ImportTask(Context context, ItemOperator operator, Uri sourceUri,
					   long sourceSize) {

		super(context, sourceUri.getLastPathSegment());
		
		mContext = context;
		mOperator = operator;
		mSourceUri = sourceUri;
		mSourceSize = sourceSize;
	}

	@Override
	protected void onPreExecute() {
		mProgressDlg
			= buildProgressDlg(
					mContext, R.string.titleImportDlg, (int)mSourceSize);
		if( mSourceSize >= 0 && !asXml( mSourceUri.getLastPathSegment() ) ) {
			mProgressDlg.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
		} else {
			mProgressDlg.setProgressStyle( ProgressDialog.STYLE_SPINNER );
		}
		
		mProgressDlg.show();
	}

	@Override
	protected RESULT doInBackground(Void... params) {
		try ( InputStream in
				= mContext.getContentResolver().openInputStream(mSourceUri); ) {
			
			if( asXml( mSourceUri.getLastPathSegment() ) ) {
				return importFromXml( mOperator, in );
			} else {
				return importFromText( mOperator, in );
			}
		} catch (IOException | ParseException | XmlPullParserException e) {
			Log.w( e, "Import failed!" );
			return RESULT.FAILED;
		}
	}

	private RESULT importFromXml(ItemOperator operator, InputStream in)
				throws XmlPullParserException, ParseException, IOException {
		
		XmlPullParserFactory factory = null;
		XmlPullParser parser = null;
		
		factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		parser = factory.newPullParser();
		parser.setInput(in, null);
		
		ProcessIndicator indicator = new ProcessIndicator() {
			@Override
			public void update(int process) {
				publishProgress( process, IMPORT_FROM_XML );
			}
		};
		
		int type;
		String elementName;
		boolean firtElement = true;
		int count = 0;
		while( ( type = parser.getEventType()) != XmlPullParser.END_DOCUMENT ) {
			if( type == XmlPullParser.START_TAG ) {
				elementName = parser.getName();
				if( elementName.equals( ELEMENT_NAME_ROOT ) ) {
					if( !firtElement ) {
						throw new XmlPullParserException( 
							ELEMENT_NAME_ROOT
							+ " should be the first element. Line : "
							+ parser.getLineNumber());
					}
					
					firtElement = false;
				} else if( elementName.equals( WhatToDoItem.ELEMENT_NAME_ITEM )) {
					if( firtElement ) {
						throw new XmlPullParserException( 
								"The first element is not " + ELEMENT_NAME_ROOT
								+ ". Line : " + parser.getLineNumber());
					}
					
					WhatToDoItem item = WhatToDoItem.fromXmlElement(parser, indicator);
					while( item != null ) {
						operator.doAdd(item);
						count++;
						if( isCancelled() ) {
							Log.d( "Import cancelled at item ", count );
							return RESULT.CANCELLED;
						}
						item = WhatToDoItem.fromXmlElement(parser, indicator);
					}
					
					break;
				} else {
					throw new XmlPullParserException( 
							"Invalid element name " + elementName
							+ ". Line: " + parser.getLineNumber() );
				}
			}
			
			parser.next();
		}
		
		Log.d( "Import successfully.", "uri", mSourceUri, "number", count );
		return RESULT.OK;
	}

	private RESULT importFromText(ItemOperator operator, InputStream in)
						throws IOException, ParseException {
		
		int count = 0;
		try ( ReadableByteArrayOutputStream baos
				= new ReadableByteArrayOutputStream( 4096 ) ) {
			
			int ch = -1;
			int bytes = 0;
			do {
				baos.reset();
				for( ch = in.read(); ch >= 0 && ch != ExportImportTask.PAGE_BREAK;
					 ch = in.read() ) {
					
					baos.write(ch);
				}
				
				WhatToDoItem item
					= WhatToDoItem.fromReadableText( baos.getInputStream() );
				
				if( item != null ) {
					operator.doAdd(item);
					count++;
					if( isCancelled() ) {
						Log.d( "Import cancelled at item ", count );
						return RESULT.CANCELLED;
					}
					
					bytes += baos.size() + ( ch == ExportImportTask.PAGE_BREAK ? 1 : 0 );
					publishProgress( bytes, IMPORT_FROM_TEXT );
				} else {
					break;
				}
			} while( ch >= 0 );
		}
		
		Log.d( "Import successfully.", "uri", mSourceUri, "number", count );
		return RESULT.OK;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if( (int)values[1] == IMPORT_FROM_XML ) {
			String message
				= mContext.getString( R.string.promptParsingXml, (int)values[0] );
			mProgressDlg.setMessage( message );
		} else {
			super.onProgressUpdate(values);
		}
	}
	
	@Override
	protected void onPostExecute(RESULT result) {
		showResult(R.string.promptImportResult, result);
	}
}
