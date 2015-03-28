package com.zsm.encryptIt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import com.zsm.util.ByteArray;

public class WhatToDoItem implements ByteArray, Serializable {

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
		String createdStr
			= DateFormat.getDateInstance( DateFormat.MEDIUM ).format(createdTime);
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
	
	@Override
	public int size() {
		return 0;
	}

}
