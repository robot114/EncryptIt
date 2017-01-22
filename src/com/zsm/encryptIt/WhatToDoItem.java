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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;

import com.zsm.encryptIt.backup.ProcessIndicator;
import com.zsm.util.ByteArray;

public class WhatToDoItem implements ByteArray, Serializable {

	private static final String ELEMENT_NAME_OUT_OF_ELEMENT = "_OUT_OF_ELEMENT";
	public static final String ELEMENT_NAME_ITEM = "item";
	private static final String ELEMENT_NAME_DETAIL = "detail";
	private static final String ELEMENT_NAME_MODIFY_TIME = "modify_time";
	private static final String ELEMENT_NAME_CREATE_TIME = "create_time";
	private static final String ELEMENT_NAME_TASK = "task";

	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat DATE_FORMAT
				= new SimpleDateFormat( "MMM dd yyyy HH:mm:ss zzz" );

	private static final long serialVersionUID = 293178926451020105L;
	
	private String task;
	private String detail = "";
	private Date createdTime;
	private Date modifiedTime;
	
	private transient Object context;
	
	public WhatToDoItem( String task ) {
		this( task, new Date( System.currentTimeMillis() ) );
	}
	
	public WhatToDoItem(String task, Date created) {
		this.task = task;
		this.detail = "";
		this.createdTime = created;
		modifiedTime = (Date)created.clone();
	}
	
	public WhatToDoItem(byte[] a, int offset) {
		fromByteArray(a, offset);
	}
	
	private WhatToDoItem() {
	}

	public String getTask() {
		return task;
	}
	
	public String getDetail() {
		return detail;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public Date getModifiedTime() {
		return modifiedTime;
	}

	public void setTask(String task) {
		this.task = task;
	}
	
	public void setDetail(String detail) {
		this.detail = detail;
	}

	public void changeModifiedTimeToCurrent() {
		modifiedTime = new Date( System.currentTimeMillis() );
	}
	
	public void setModifiedTime(Date modify) {
		modifiedTime = modify;
	}
	
	public Object getContext() {
		return context;
	}
	public void setContext(Object context) {
		this.context = context;
	}

	@Override
	public String toString() {
		String createdStr = DATE_FORMAT.format(createdTime);
		return "(" + createdStr + ") " + task;
	}
	
	@Override
	public byte[] toByteArray() {
		ByteArrayOutputStream aos
			= new ByteArrayOutputStream( (task.length()+Long.SIZE)*2+64);
		
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
			os.writeUTF( task == null ? "" : task );
			os.writeUTF( detail == null ? "" : detail );
			os.writeLong( createdTime.getTime() );
			os.writeLong( modifiedTime.getTime() );
			size = os.size();
			os.close();
		} catch (IOException e) {
			// This should not happen
			e.printStackTrace();
		}
		return size;
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
			item.task = ( task == null ? "" : task );
			String detail = dis.readUTF();
			item.detail= ( detail == null ? "" : detail );
			item.createdTime = new Date( dis.readLong() );
			item.modifiedTime = new Date( dis.readLong() );
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
		item.createdTime = DATE_FORMAT.parse( ctString );
		
		String mtString = reader.readLine();
		if( mtString == null ) {
			throw new ParseException( "No modify time", 0 );
		}
		item.modifiedTime = DATE_FORMAT.parse( mtString );
		
		StringBuffer buff = new StringBuffer();
		for( int ch = reader.read(); ch > 0; ch = reader.read() ) {
			buff.append((char)ch);
		}
		item.detail = buff.toString();
		
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
	
	public static WhatToDoItem fromXmlElement( XmlPullParser xpp,
											   ProcessIndicator indicator )
					throws XmlPullParserException, ParseException, IOException {
		
		int type = xpp.getEventType();
		if( type == XmlPullParser.END_DOCUMENT ) {
			return null;
		}
		
		WhatToDoItem item = new WhatToDoItem();
		String elementName = xpp.getName();
		do {
			xpp.next();
			type = xpp.getEventType();
			indicator.update( xpp.getLineNumber() );
			
			if( type == XmlPullParser.START_TAG ) {
				elementName = xpp.getName();
			} else if( type == XmlPullParser.END_TAG ) {
				elementName = ELEMENT_NAME_OUT_OF_ELEMENT;
			} else if( type == XmlPullParser.TEXT ) {
				String text = xpp.getText();
				switch( elementName ) {
					case ELEMENT_NAME_ITEM:
						break;		// Skip the text of the item element
					case ELEMENT_NAME_TASK:
						item.task = text;
						break;
					case ELEMENT_NAME_CREATE_TIME:
						item.createdTime = DATE_FORMAT.parse( text );
						break;
					case ELEMENT_NAME_MODIFY_TIME:
						item.modifiedTime = DATE_FORMAT.parse( text );
						break;
					case ELEMENT_NAME_DETAIL:
						item.detail = text;
						break;
					case ELEMENT_NAME_OUT_OF_ELEMENT:
						// Out of the range of the element, do nothing
						break;
					default:
						throw new XmlPullParserException( 
								"Parse error at line: " + xpp.getLineNumber() );
				}
			} else {
				
			}
		} while( ( type != XmlPullParser.START_TAG 
				   || !elementName.equals( ELEMENT_NAME_ITEM ) )
				 && type != XmlPullParser.END_DOCUMENT );
		
		return item;
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
		
		if( !task.equals(item.task) ) {
			return false;
		}
		
		if( !( detail == null ? item.detail == null : detail.equals(item.detail) ) ) {
			return false;
		}
		
		// Do not care modify time, because it will change
		return createdTime.equals( item.createdTime );
	}

	@Override
	public int hashCode() {
		return task.hashCode() * 37 * 37 + detail.hashCode() * 37 + createdTime.hashCode();
	}

}
