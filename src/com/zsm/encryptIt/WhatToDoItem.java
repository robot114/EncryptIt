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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.zsm.log.Log;
import com.zsm.util.ByteArray;

import android.annotation.SuppressLint;

public class WhatToDoItem implements ByteArray, Serializable {

	public static final long serialVersionUID = 293178926451020105L;
	
	protected static final String ELEMENT_NAME_OUT_OF_ELEMENT = "_OUT_OF_ELEMENT";
	public static final String ELEMENT_NAME_ITEM = "item";
	protected static final String ELEMENT_NAME_DETAIL = "detail";
	protected static final String ELEMENT_NAME_MODIFY_TIME = "modify_time";
	protected static final String ELEMENT_NAME_CREATE_TIME = "create_time";
	protected static final String ELEMENT_NAME_TASK = "task";

	@SuppressLint("SimpleDateFormat")
	protected static final SimpleDateFormat DATE_FORMAT
				= new SimpleDateFormat( "MMM dd yyyy HH:mm:ss zzz" );

	protected String mTask;
	protected String mDetail = "";
	protected Date mCreatedTime;
	protected Date mModifiedTime;
	
	protected transient Object context;
	
	public WhatToDoItem( String task ) {
		this( task, new Date( System.currentTimeMillis() ) );
	}
	
	public WhatToDoItem(String task, Date created) {
		this.mTask = task;
		this.mDetail = "";
		this.mCreatedTime = created;
		mModifiedTime = (Date)created.clone();
	}
	
	public WhatToDoItem(byte[] a, int offset) {
		fromByteArray(a, offset);
	}
	
	protected WhatToDoItem() {
	}
	
	/**
	 * Check the validation of of the item
	 * 
	 * @return true, if the datas of the item are valid
	 */
	public boolean isValid() {
		if( mTask == null || mTask.trim().length() == 0 ) {
			Log.d( "Invalid item as no task" );
			return false;
		}
		
		if( mDetail == null || mCreatedTime == null || mModifiedTime == null ) {
			Log.d( "Invalid item with null data:", "mDetail", mDetail,
					"mCreateTime", mCreatedTime, "mModifiedTime", mModifiedTime );
			return false;
		}
		
		if( mCreatedTime.after(mModifiedTime) ) {
			Log.d( "Invalid item, reason create time after modified time", 
					"mCreateTime", mCreatedTime, "mModifiedTime", mModifiedTime );
			return false;
		}
		
		return true;
	}

	public String getTask() {
		return mTask;
	}
	
	public String getDetail() {
		return mDetail;
	}

	public Date getCreatedTime() {
		return mCreatedTime;
	}

	public Date getModifiedTime() {
		return mModifiedTime;
	}

	public void setTask(String task) {
		this.mTask = task;
	}
	
	public void setDetail(String detail) {
		this.mDetail = detail;
	}

	public void changeModifiedTimeToCurrent() {
		mModifiedTime = new Date( System.currentTimeMillis() );
	}
	
	public void setModifiedTime(Date modify) {
		mModifiedTime = modify;
	}
	
	public Object getContext() {
		return context;
	}
	public void setContext(Object context) {
		this.context = context;
	}

	@Override
	public String toString() {
		String createdStr
			= mCreatedTime == null ? "NullCreateTime" : DATE_FORMAT.format(mCreatedTime);
		
		return "(" + createdStr + ") " + mTask;
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
			writeToDataOutputStream(os);
			size = os.size();
			os.close();
		} catch (IOException e) {
			// This should not happen
			e.printStackTrace();
		}
		return size;
	}

	protected void writeToDataOutputStream(DataOutputStream os) throws IOException {
		os.writeUTF( mTask == null ? "" : mTask );
		os.writeUTF( mDetail == null ? "" : mDetail );
		os.writeLong( mCreatedTime.getTime() );
		os.writeLong( mModifiedTime.getTime() );
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
	public static WhatToDoItem fromByteArray(byte[] a, int offset) {
		int len = a.length - offset;
		ByteArrayInputStream in
			= new ByteArrayInputStream( a, offset, len );

		WhatToDoItem item = new WhatToDoItem();
		
		readFrom(in, item);
		
		try {
			in.close();
		} catch (IOException e) {
			// Should not happen
			e.printStackTrace();
		}
		
		return item;
	}

	private static int readFrom(InputStream in, WhatToDoItem item) {
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
			sizeRead = available - in.available();
			dis.close();
		} catch (IOException e) {
			// This should not happen
			e.printStackTrace();
		}
		
		return sizeRead;
	}
	
	public void toReadableText( BufferedWriter writer ) throws IOException {
		writer.append(getTask());
		writer.newLine();
		writer.append(DATE_FORMAT.format( getCreatedTime() ) );
		writer.newLine();
		writer.append(DATE_FORMAT.format( getModifiedTime() ) );
		writer.newLine();
		writer.append(getDetail());
	}
	
	public static WhatToDoItem fromReadableText( InputStream in )
					throws IOException, ParseException {
		
		BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
		
		String t = reader.readLine();
		if( t == null ) {
			return null;
		}

		WhatToDoItem item = new WhatToDoItem( t );
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
		for( int ch = reader.read(); ch > 0; ch = reader.read() ) {
			buff.append((char)ch);
		}
		item.mDetail = buff.toString();
		
		return item;
	}
	
	public Element toXmlElement( Document document ) {
		Element element = document.createElement( ELEMENT_NAME_ITEM );
		element.appendChild(document.createElement( ELEMENT_NAME_TASK ))
		 	   .appendChild( document.createTextNode( getTask() ) );
		element.appendChild(document.createElement( ELEMENT_NAME_CREATE_TIME ))
		 	   .appendChild( document.createTextNode( 
		 			   DATE_FORMAT.format( getCreatedTime() ) ) );
		element.appendChild(document.createElement( ELEMENT_NAME_MODIFY_TIME ))
		 	   .appendChild( document.createTextNode( 
		 			   DATE_FORMAT.format(getModifiedTime())  ) );
		element.appendChild(document.createElement( ELEMENT_NAME_DETAIL ))
		 	   .appendChild( document.createTextNode( getDetail() ) );
		
		return element;
	}
	
	public static WhatToDoItem fromXmlElement(Element element)
						throws SAXException, ParseException {

		WhatToDoItem item = new WhatToDoItem();
		fillItemFromXml(element, item);

		return item;
	}

	protected static void fillItemFromXml(Element element, WhatToDoItem item)
							throws SAXException, ParseException {
		
		item.mTask = getElementText(ELEMENT_NAME_TASK, element);
		if (item.mTask == null) {
			throw new SAXException("Task is null!");
		}
		item.mDetail = getElementText(ELEMENT_NAME_DETAIL, element);

		String createTime = getElementText(ELEMENT_NAME_CREATE_TIME, element);
		if (createTime == null) {
			item.mCreatedTime = new Date(System.currentTimeMillis());
		} else {
			item.mCreatedTime = DATE_FORMAT.parse(createTime);
		}

		String lastModify = getElementText(ELEMENT_NAME_MODIFY_TIME, element);
		if (lastModify == null) {
			item.mModifiedTime = item.mCreatedTime;
		} else {
			item.mModifiedTime = DATE_FORMAT.parse(lastModify);
		}
	}

	protected static String getElementText(String name, Element element) throws SAXException {

		String text = null;
		NodeList nodes = element.getElementsByTagName(name);
		if (nodes != null) {
			if (nodes.getLength() == 1) {
				text = nodes.item(0).getTextContent();
			} else {
				throw new SAXException("Invalid content number for " + element);
			}
		}
		return text;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if( o == null || !( o instanceof WhatToDoItem ) ) {
			return false;
		}
		if( this == o ) {
			return true;
		}
		WhatToDoItem item = (WhatToDoItem)o;
		
		if( !mTask.equals(item.mTask) ) {
			return false;
		}
		
		if( !( mDetail == null ? item.mDetail == null : mDetail.equals(item.mDetail) ) ) {
			return false;
		}
		
		// Do not care modify time, because it will change
		return mCreatedTime.equals( item.mCreatedTime );
	}

	@Override
	public int hashCode() {
		return mTask.hashCode() * 37 * 37 + mDetail.hashCode() * 37 + mCreatedTime.hashCode();
	}

	public long getSerialVersionUID() {
		return serialVersionUID;
	}

}
