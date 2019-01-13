package com.zsm.encryptIt.action;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.RowId;

import com.zsm.encryptIt.ItemCompator;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.WhatToDoItemV2;
import com.zsm.log.Log;
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
	public byte[] getMetaData(String key) {
		return persistence.getMetaData(key);
	}

	@Override
	public WhatToDoItemV2 read(AbstractRawCursor cursor)
			throws ClassNotFoundException, IOException,
					BadPersistenceFormatException {
		
		Object obj = persistence.read(cursor);
		
		return ItemCompator.toLastVersionItem(obj);
	}

	@Override
	public void remove(RowId rowId) {
		try {
			persistence.remove(rowId);
		} catch (IOException e) {
			Log.e( e, "Remove a row failed! rowId: ", rowId );
		}
	}

	@Override
	public RowId add(WhatToDoItemV2 item) throws IOException {
		return persistence.add(item);
	}

	@Override
	public void update(RowId rowId, WhatToDoItemV2 item) throws IOException {
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

	@Override
	public void reopen() throws Exception {
		persistence.close();
		persistence.open();
	}

}
