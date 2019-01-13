package com.zsm.recordstore.driver.android.sqlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.security.InvalidParameterException;
import java.util.HashMap;

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

	private static final Converter META_DATA_CONVERTER
		= new Converter() {
		
			@Override
			public Object convert(Object s) {
				Object[] data = (Object[])s;
				ContentValues values = new ContentValues();
				values.put( COLUMN_KEY, (String)data[0]);
				values.put(COLUMN_KEY, (byte[])data[1]);
				
				return values;
			}
	};

	private static final HashMap<String, Converter> CONVERTER_MAP
							= new HashMap<String, Converter>();
	static {
		CONVERTER_MAP.put( RawRecordStore.RAW_DATA_TABLE_NAME, OUT_DATA_CONVERTER );
		CONVERTER_MAP.put( RawRecordStore.META_DATA_TABLE_NAME, META_DATA_CONVERTER );
	}

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
	protected Converter getConverter( String tableName ) {
		Converter c = CONVERTER_MAP.get(tableName);
		if( c == null ) {
			throw new InvalidParameterException(
						"No converter found for table: " + tableName );
		}
		
		return c;
	}
}
