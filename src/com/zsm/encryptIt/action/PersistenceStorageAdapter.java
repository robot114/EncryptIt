package com.zsm.encryptIt.action;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.RowId;

import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.backup.Source;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.persistence.Persistence;
import com.zsm.recordstore.AbstractRawCursor;

public class PersistenceStorageAdapter implements ItemStorageAdapter {

	private Persistence persistence;

	public PersistenceStorageAdapter( Persistence persistence ) {
		this.persistence = persistence;
	}
	
	@Override
	public void clear() {
		persistence.clear();
	}

	@Override
	public AbstractRawCursor query() {
		return persistence.query();
	}

	@Override
	public WhatToDoItem read(AbstractRawCursor cursor)
			throws ClassNotFoundException, IOException,
					BadPersistenceFormatException {
		
		return (WhatToDoItem) persistence.read(cursor);
	}

	@Override
	public void remove(RowId rowId) {
		persistence.remove(rowId);
	}

	@Override
	public RowId add(WhatToDoItem item) throws IOException {
		return persistence.add(item);
	}

	@Override
	public void update(RowId rowId, WhatToDoItem item) throws IOException {
		persistence.update(rowId, item);
	}

	@Override
	public void close() {
		persistence.close();
	}

	@Override
	public InputStream openBackupSrcInputStream() throws IOException {
		return new FileInputStream( persistence.getFullPathName() );
	}

	@Override
	public OutputStream openRestoreTargetOutputStream() throws IOException {
		return new FileOutputStream( persistence.getFullPathName() );
	}

	@Override
	public String displayName() {
		return "Database";
	}

	@Override
	public long size() {
		return persistence.getBackupSize();
	}

	@Override
	public boolean backupToLocal() throws IOException {
		return persistence.backupToLocal();
	}

	@Override
	public boolean restoreFromLocalBackup() throws FileNotFoundException {
		return persistence.restoreFromLocal( );
	}

}
