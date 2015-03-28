package com.zsm.persistence;

public class BadPersistenceFormatException extends Exception {

	private static final long serialVersionUID = 5880910342708233416L;

	public BadPersistenceFormatException(String message) {
		super(message);
	}

	public BadPersistenceFormatException(Exception e) {
		super( e );
	}

}
