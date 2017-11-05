package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.OutputStream;

abstract public class BackupOutputStream {

	protected enum CLOSE_TYPE { NORMAL_ONE, NORMAL_ALL, EXCEPTION };
	
	private OutputStream mOutStream;
	
	protected BackupOutputStream( OutputStream out ) {
		mOutStream = out;
	}
	
	protected OutputStream getOutputStream() {
		return mOutStream;
	}
	
    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * position {@code offset} to this stream.
     *
     * @param buffer
     *            the buffer to be written.
     * @param offset
     *            the start position in {@code buffer} from where to get bytes.
     * @param count
     *            the number of bytes from {@code buffer} to write to this
     *            stream.
     * @throws IOException
     *             if an error occurs while writing to this stream.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if
     *             {@code offset + count} is bigger than the length of
     *             {@code buffer}.
     */
    public void write(byte[] buffer, int offset, int count) throws IOException {
    	mOutStream.write(buffer, offset, count);
    }
    
    abstract void close( CLOSE_TYPE closeType ) throws IOException;
}
