package com.zsm.recordstore.driver.android.sqlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.zsm.recordstore.RawRecordStore;
import com.zsm.recordstore.RecordStore;
import com.zsm.util.Converter;

public class SQLiteRawRecordStore extends RawRecordStore {

	private static final Converter OUT_DATA_CONVERTER
		= new Converter() {
		
			@Override
			public Object convert(Object s) {
				ContentValues values = new ContentValues();
				values.put(COLUMN_DATA, (byte[])s);
				values.put(COLUMN_CREATE, System.currentTimeMillis());
				values.put(COLUMN_MODIFY, System.currentTimeMillis());
				
				return values;
			}
	};

	SQLiteRawRecordStore(SQLiteDatabase db, boolean readOnly) {
		// Explicitly requested for a read only database or the concrete database
		// being read only, all these make the record store is read only.
		super( db, readOnly || db.isReadOnly() ); 
	}

	@Override
	protected RecordStore generateRecordStore(Object db, boolean readOnly) {
		return new SQLiteRecordStore((SQLiteDatabase) db, readOnly);
	}

	@Override
	protected Converter getConverter() {
		return OUT_DATA_CONVERTER;
	}
}
