package com.zsm.persistence;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface InOutDecorator {

	InputStream wrapInputStream(InputStream in) throws IOException;

	DataOutputStream wrapOutputStream(OutputStream out) throws IOException;
	
	byte[] encode( byte[] data ) throws IOException;
	
	byte[] decode( byte[] data ) throws IOException;
}
