package com.zsm.recordstore;

public class RecordStoreNotOpenException extends RecordStoreException {

	private static final long serialVersionUID = -7255674774021860277L;

	public RecordStoreNotOpenException(String message) {
		super(message);
	}
}
