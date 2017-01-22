package com.zsm.encryptIt.backup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

class ReadableByteArrayOutputStream extends ByteArrayOutputStream {

	public ReadableByteArrayOutputStream(int size) {
		super( size );
	}

	ByteArrayInputStream getInputStream() {
		return new ByteArrayInputStream(buf, 0, size());
	}
}
