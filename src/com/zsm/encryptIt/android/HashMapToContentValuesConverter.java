package com.zsm.encryptIt.android;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import com.zsm.util.ByteArray;
import com.zsm.util.Converter;

import android.content.ContentValues;


public class HashMapToContentValuesConverter implements Converter {

	@Override
	public ContentValues convert(Object source) {
		ContentValues values = new ContentValues();
		@SuppressWarnings("unchecked")
		HashMap<String, Object> data = (HashMap<String, Object>)source;
		Set<String> keys = data.keySet();
		for( String key : keys ) {
			Object o = data.get(key);
			if( o == null ) {
				values.putNull(key);
			} else if( o instanceof ByteArray ) {
				values.put(key, ((ByteArray)o).toByteArray());
			} else if( o instanceof Date ) {
				values.put(key, ((Date)o).getTime());
			} else if( o instanceof String ) {
				values.put(key, (String)o);
			} else if( o instanceof Integer ) {
				values.put(key, (Integer)o);
			} else if( o instanceof Long ) {
				values.put(key, (Long)o);
			} else if( o instanceof Float ) {
				values.put(key, (Float)o);
			} else if( o instanceof Double ) {
				values.put(key, (Double)o);
			} else if( o instanceof Short ) {
				values.put(key, (Short)o);
			} else if( o instanceof Character ) {
				values.put(key, (Short)o);
			} else if( o instanceof Byte ) {
				values.put(key, (Byte)o);
			} else if( o instanceof Boolean ) {
				values.put(key, (Boolean)o);
			} else {
				throw new IllegalArgumentException( "Unsupport type: (" + key
													+ ", " + o.getClass() + " )" );
			}
		}
		
		return values;
	}

}
