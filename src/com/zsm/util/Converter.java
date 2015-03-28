package com.zsm.util;

public interface Converter {

	/**
	 * Convert data to another type.
	 * 
	 * @param source source data
	 * @return target data
	 */
	Object convert( Object data );
}
