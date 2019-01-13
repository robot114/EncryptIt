package com.zsm.recordstore;

abstract public class AbstractMetaDataCursor extends AbstractCursor {

	@Override
	public String[] getColumnNames() {
		return RawRecordStore.META_COLUMNS;
	}
	
	/**
	 * Get the key of the meta data
	 * 
	 * @return key of the meta data
	 */
	public abstract String getKey();

	/**
	 * Get the meta data
	 * 
	 * @return the meta data
	 */
	abstract public byte[] getData();
}
