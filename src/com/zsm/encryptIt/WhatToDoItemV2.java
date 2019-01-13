package com.zsm.encryptIt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.zsm.log.Log;
import com.zsm.util.ByteArray;

import android.annotation.SuppressLint;

public class WhatToDoItemV2 extends WhatToDoItem implements ByteArray, Serializable {

	public static final long serialVersionUID = 6644720856467892158L;
	
	private static final String SAPERATOR_ATTACHMENT = "|";
	private static final String ELEMENT_NAME_ATTACHMENTS = "attachments";

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat DATE_FORMAT
				= new SimpleDateFormat( "MMM dd yyyy HH:mm:ss zzz" );

	private static final char[] INVALID_PATH_CHAR = { '|', '?', '*', '<', '>' };
	
	private List<String> mAttachments;	// Full pathname of each attachment
	
	public WhatToDoItemV2( WhatToDoItem item ) {
		mTask = item.mTask;
		mDetail = item.mDetail;
		mCreatedTime = item.mCreatedTime;
		mModifiedTime = item.mModifiedTime;
		newAttachments();
	}
	
	public WhatToDoItemV2( String task ) {
		this( task, new Date( System.currentTimeMillis() ) );
	}
	
	public WhatToDoItemV2(String task, Date created) {
		super( task, created );
		newAttachments();
	}

	public WhatToDoItemV2(byte[] a, int offset) {
		fromByteArray(a, offset);
	}
	
	private WhatToDoItemV2() {
		newAttachments();
	}

	private void newAttachments() {
		mAttachments = new ArrayList<String>();
	}
	
	public List<String> getAttachments() {
		return mAttachments;
	}

	public void addAttachment(String fullPathname) {
		if( mAttachments == null ) {
			newAttachments();
		}
		
		checkAttachment( fullPathname );
		mAttachments.add( fullPathname );
	}

	private void checkAttachment(String fullPathname) {
		for( char ch : INVALID_PATH_CHAR ) {
			if( fullPathname.indexOf( ch ) >= 0 ) {
				throw new InvalidParameterException(
						"Invalid character(" + ch + ") in the path : " + fullPathname );
			}
		}
	}

	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream aos
			= new ByteArrayOutputStream( (mTask.length()+Long.SIZE)*2+64);
		
		writeTo(aos);
		
		try {
			aos.close();
		} catch (IOException e) {
			// This should not happen
			e.printStackTrace();
		}
		
		return aos.toByteArray();
	}

	private int writeTo(OutputStream aos) {
		DataOutputStream os = new DataOutputStream( aos );
		
		int size = 0;
		try {
			writeToDataOutputStream( os );
			os.writeUTF( buildAttachmentsString( ) );
			size = os.size();
			os.close();
		} catch (IOException e) {
			// This should not happen
			e.printStackTrace();
		}
		return size;
	}

	private String buildAttachmentsString( ) {
		StringBuilder builder = new StringBuilder( ELEMENT_NAME_ATTACHMENTS );
		
		for( String att : mAttachments ) {
			builder.append( SAPERATOR_ATTACHMENT ).append( att );
		}
		
		return builder.toString();
	}
	
	/**
	 * Generate an instance of WhatToDoItem from a byte array. The context will
	 * not be generated in this method. The context must be set by {@link setContext}
	 * later.
	 * 
	 * @param a byte array, from which the item generated
	 * @param offset start position in the array
	 * @return generated instance of WhatToDoItem
	 */
	public static WhatToDoItemV2 fromByteArray(byte[] a, int offset) {
		int len = a.length - offset;
		ByteArrayInputStream in
			= new ByteArrayInputStream( a, offset, len );

		WhatToDoItemV2 item = new WhatToDoItemV2();
		
		readFrom(in, item);
		
		try {
			in.close();
		} catch (IOException e) {
			// Should not happen
			Log.w( e, "Failed to close ByteArrayInputStream!" );
		}
		
		return item;
	}

	private static int readFrom(InputStream in, WhatToDoItemV2 item) {
		DataInputStream dis = new DataInputStream( in );

		int sizeRead = 0;
		
		try {
			int available = in.available();
			String task = dis.readUTF();
			item.mTask = ( task == null ? "" : task );
			String detail = dis.readUTF();
			item.mDetail= ( detail == null ? "" : detail );
			item.mCreatedTime = new Date( dis.readLong() );
			item.mModifiedTime = new Date( dis.readLong() );
			String attStr = dis.readUTF();
			parseAttachmentsString( attStr, item.mAttachments );
			sizeRead = available - in.available();
			dis.close();
		} catch (IOException e) {
			// This should not happen
			Log.w( e, "Read item from inputstream failed!");
		}
		
		return sizeRead;
	}
	
	/**
	 * Parse the attachment list from a string
	 * 
	 * @param attStr the String from which to parse the list
	 * @param list the output list
	 * @return true, parse successfully; false, not an attachment list string, or parse failed
	 */
	private static boolean parseAttachmentsString( String attStr, List<String> list ) {
		list.clear();
		
		StringTokenizer st = new StringTokenizer( attStr, SAPERATOR_ATTACHMENT );
		
		if( !st.hasMoreTokens() || !ELEMENT_NAME_ATTACHMENTS.equals( st.nextToken() ) ) {
			Log.w( "Invalid attachments: ", attStr );
			return false;
		}
		
		while( st.hasMoreTokens() ) {
			String oneAtt = st.nextToken();
			if( !list.contains(oneAtt) ) {
				list.add( oneAtt );
			}
		}
		
		return true;
	}
	
	public void toReadableText( BufferedWriter writer ) throws IOException {
		writer.append(getTask());
		writer.newLine();
		writer.append(DATE_FORMAT.format( getCreatedTime() ) );
		writer.newLine();
		writer.append(DATE_FORMAT.format( getModifiedTime() ) );
		writer.newLine();
		writer.append( buildAttachmentsString() );
		writer.newLine();
		writer.append(getDetail());
	}
	
	public static WhatToDoItemV2 fromReadableText( InputStream in )
					throws IOException, ParseException {
		
		BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
		
		String t = reader.readLine();
		if( t == null ) {
			return null;
		}

		WhatToDoItemV2 item = new WhatToDoItemV2( t );
		String ctString = reader.readLine();
		if( ctString == null ) {
			throw new ParseException( "No created time", 0 );
		}
		item.mCreatedTime = DATE_FORMAT.parse( ctString );
		
		String mtString = reader.readLine();
		if( mtString == null ) {
			throw new ParseException( "No modify time", 0 );
		}
		item.mModifiedTime = DATE_FORMAT.parse( mtString );
		
		StringBuffer buff = new StringBuffer();
		String attStr = reader.readLine();
		if( !parseAttachmentsString(attStr, item.mAttachments )) {
			buff.append( attStr );
		}
		
		for( int ch = reader.read(); ch > 0; ch = reader.read() ) {
			buff.append((char)ch);
		}
		item.mDetail = buff.toString();
		
		return item;
	}
	
	public Element toXmlElement( Document document ) {
		Element element = super.toXmlElement(document);
		
		Element attachmentsEle
			= createListElement(document,ELEMENT_NAME_ATTACHMENTS, getAttachments() );
		if( attachmentsEle != null ) {
			element.appendChild( attachmentsEle );
		}
		return element;
	}
	
	private Element createListElement(Document doc, String elementName, List<?> list ) {
		Element element = null;
		if( list != null && !list.isEmpty() ) {
			element = doc.createElement(elementName);
			for( Object o : list ) {
				element.appendChild(doc.createElement( ELEMENT_NAME_ITEM ))
						.appendChild( doc.createTextNode( o.toString() ) );
			}
		}
		
		return element;
	}
	
	public static WhatToDoItemV2 fromXmlElement( Element element )
					throws SAXException, ParseException {
		
		WhatToDoItemV2 item = new WhatToDoItemV2();
		fillItemFromXml(element, item);
		
		item.mAttachments = fromTextListElements(element);
		
		return item;
	}

	private static List<String> fromTextListElements(Element element ) throws SAXException {

		List<String> list = new ArrayList<String>();
		NodeList attachments = element.getElementsByTagName(ELEMENT_NAME_ATTACHMENTS);
		if( attachments == null || attachments.getLength() == 0 ) {
			return list;
		}
		NodeList attNodes = ((Element) attachments.item(0)).getChildNodes();
		for( int i = 0; i < attNodes.getLength(); i++ ) {
			if( attNodes.item(i) instanceof Element ) {
				Element att = (Element) attNodes.item(i);
				if( ELEMENT_NAME_ITEM.equals( att.getTagName() ) ) {
					list.add( att.getTextContent() );
				} else {
					throw new SAXException( "Invalid item: " + att );
				}
			}
		}
		
		return list;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if( !super.equals(o) ) {
			return false;
		}
		
		WhatToDoItemV2 item = (WhatToDoItemV2)o;
		return ( mAttachments == null && item .mAttachments == null ) 
				|| ( mAttachments != null && mAttachments.equals( item.mAttachments ) );
	}

	@Override
	public int hashCode() {
		return mTask.hashCode() * 37 * 37 + mDetail.hashCode() * 37 + mCreatedTime.hashCode();
	}

	public long getSerialVersionUID() {
		return serialVersionUID;
	}

}
