package com.zsm.encryptIt.action;

import java.io.Closeable;
import java.io.IOException;
import java.sql.RowId;

import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.backup.Backupable;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.recordstore.AbstractRawCursor;

public interface ItemStorageAdapter extends Closeable, Backupable {

	void clear();

	AbstractRawCursor query();

	WhatToDoItem read(AbstractRawCursor cursor)
			throws ClassNotFoundException, IOException, BadPersistenceFormatException;
	
	void remove(RowId rowId);

	RowId add(WhatToDoItem item) throws IOException;
	
	void update( RowId rowId, WhatToDoItem item ) throws IOException;

	void close();
}
