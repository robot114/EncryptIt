package com.zsm.recordstore;

import java.sql.RowId;
import java.util.Date;

public abstract class AbstractRawCursor extends AbstractCursor {

	public AbstractRawCursor( ) {
		super();
	}

	@Override
	public String[] getColumnNames() {
		return RawRecordStore.RAW_COLUMNS;
	}

	/**
	 * Get the Id of the row this cursor pointed to.
	 * 
	 * @return Id of this row
	 * @throws RecordStoreNotOpenException if the record store is closed or the 
	 * 									   cursor is closed.
	 */
	public abstract RowId currentId();

	/**
	 * Returns the value of the data column as a byte array.
	 *
	 * @return the value of that column as a byte array.
	 */
	public abstract byte[] getData();

	/**
	 * Returns the date and time when this row created.
	 *
	 * @return the created date and time.
	 */
	public abstract Date getCreated();

	/**
	 * Returns the date and time when this row modified recently.
	 *
	 * @return the date and time of last modification.
	 */
	public abstract Date getLastModified();

}