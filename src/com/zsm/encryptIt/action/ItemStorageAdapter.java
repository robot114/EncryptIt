package com.zsm.encryptIt.action;

import java.io.Closeable;
import java.io.IOException;
import java.sql.RowId;

import com.zsm.encryptIt.WhatToDoItemV2;
import com.zsm.encryptIt.backup.Backupable;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.recordstore.AbstractRawCursor;

public interface ItemStorageAdapter extends Closeable, Backupable {

	void clear();

	AbstractRawCursor query();

	WhatToDoItemV2 read(AbstractRawCursor cursor)
			throws ClassNotFoundException, IOException, BadPersistenceFormatException;
	
	void remove(RowId rowId);

	RowId add(WhatToDoItemV2 item) throws IOException;
	
	void update( RowId rowId, WhatToDoItemV2 item ) throws IOException;
	
	byte[] getMetaData( String key );

	void close();
}
