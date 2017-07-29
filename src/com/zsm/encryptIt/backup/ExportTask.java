package com.zsm.encryptIt.backup;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.ui.WhatToDoListViewItem;
import com.zsm.log.Log;

public class ExportTask extends BackupTask {

	private Uri mTargetUri;
	private List<WhatToDoListViewItem> mList;
	/**
	 * Export the items in the list in an AsyncTask. So the exporting progress
	 * is to be done in a thread.
	 * 
	 * @param context MUST be an instance of an Activity, otherwise the
	 * 			ProgressDialog will no be able to shown
	 * @param list The export list
	 * @param targetUri To which the items will be exported
	 */
	public static void doExport( Context context, List<WhatToDoListViewItem> list,
							     Uri targetUri ) {
		
		new ExportTask(context, list, targetUri).execute();
	}
	
	private ExportTask(Context context,
			List<WhatToDoListViewItem> list, Uri targetUri) {

		super( context, targetUri.getLastPathSegment() );
		
		mList = list;
		mTargetUri = targetUri;
	}

	@Override
	protected void onPreExecute() {
		mProgressDlg
			= buildProgressDlg(
					mContext, R.string.titleExportDlg, mList.size(), this);
		mProgressDlg.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
		
		mProgressDlg.show();
	}

	@Override
	protected RESULT doInBackground(Void... params) {
		
		try ( OutputStream out
				= mContext.getContentResolver().openOutputStream(mTargetUri); ) {
			
			if( asXml( mTargetUri.getLastPathSegment() ) ) {
				return exportToXml( mList, out );
			} else {
				return exportToText( mList, out );
			}
		} catch (IOException | ParserConfigurationException | TransformerException e) {
			Log.w( e, "Export failed!" );
			return RESULT.FAILED;
		}
	}
	
	private RESULT exportToXml(List<WhatToDoListViewItem> list, OutputStream out)
				throws ParserConfigurationException, TransformerException {
		
		DocumentBuilderFactory documentBuilderFactory
			= DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder
			= documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		Element rootElement = document.createElement( ELEMENT_NAME_ROOT );
		document.appendChild(rootElement);
		
		int current = 0;
		for (WhatToDoListViewItem viewItem : list) {
			current++;
			publishProgress( current );
			
			if( isCancelled() ) {
				Log.d( "Export cancelled at item ", current );
				return RESULT.CANCELLED;
			}
			
			WhatToDoItem data = viewItem.getData();
			rootElement.appendChild( data.toXmlElement(document) );
		}
		
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		Properties outFormat = new Properties();
		outFormat.setProperty(OutputKeys.INDENT, "yes");
		outFormat.setProperty(OutputKeys.METHOD, "xml");
		outFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		outFormat.setProperty(OutputKeys.VERSION, "1.0");
		outFormat.setProperty(OutputKeys.ENCODING, ENCODING);
		transformer.setOutputProperties(outFormat);
		DOMSource domSource = new DOMSource(document.getDocumentElement());
		StreamResult result = new StreamResult(out);
		transformer.transform(domSource, result);
		
		Log.d( "Export successfully.", "uri", mTargetUri, "number", current );
		return RESULT.OK;
	}

	private RESULT exportToText(List<WhatToDoListViewItem> list,
							  OutputStream out) throws IOException {

		OutputStreamWriter ow = new OutputStreamWriter( out );
		BufferedWriter writer = new BufferedWriter( ow );
		
		int current = 0;
		try {
			for (WhatToDoListViewItem viewItem : list) {
				current++;
				publishProgress( current );
				
				if( isCancelled() ) {
					Log.d( "Export cancelled at item ", current );
					return RESULT.CANCELLED;
				}
				
				WhatToDoItem data = viewItem.getData();
				data.toReadableText(writer);
				writer.append(BackupTask.PAGE_BREAK);
			}
		} finally {
			writer.close();
			ow.close();
		}
		
		Log.d( "Export successfully.", "uri", mTargetUri, "number", current );
		return RESULT.OK;
	}

	@Override
	protected void onPostExecute(RESULT result) {
		showResult(R.string.promptExportResult, result);
	}
}
