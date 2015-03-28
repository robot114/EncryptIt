package com.zsm.persistence;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PlainInOutDecorator implements InOutDecorator {

	@Override
	public InputStream wrapInputStream(InputStream in) throws IOException {
		return in;
	}

	@Override
	public DataOutputStream wrapOutputStream(OutputStream out)
			throws IOException {
		if( out instanceof DataOutputStream ) {
			return (DataOutputStream)out;
		} else {
			return new DataOutputStream( out );
		}
	}

	@Override
	public byte[] encode(byte[] data) throws IOException {
		return data;
	}

	@Override
	public byte[] decode(byte[] data) throws IOException {
		return data;
	}

}
