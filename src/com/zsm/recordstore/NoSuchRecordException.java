package com.zsm.recordstore;

public class NoSuchRecordException extends RecordStoreException {

	private static final long serialVersionUID = 6428446706947129101L;

	public NoSuchRecordException(String message) {
		super(message);
	}

}
