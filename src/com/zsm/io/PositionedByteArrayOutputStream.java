package com.zsm.io;

import java.io.ByteArrayOutputStream;

import com.zsm.util.ArrayUtility;

public class PositionedByteArrayOutputStream extends ByteArrayOutputStream {

	private boolean expandable = true;
	private int offset = 0;
	
	/**
	 * Make a existing buffer as the buffer of the ByteArrayOutputStream.
	 * <p>If the ByteArrayOutputStream create by this constructor, it 
	 * cannot be expanded. Which means that when the invoker wants to put
	 * more bytes than the buffer can take, IndexOutOfBoundsException will
	 * be thrown.
	 * <p>When the output stream is reset, it is reset to the offset position
	 * where passed when it create. But when the method {@link toByteArray()}, 
	 * {@link toString} and {@link writeTo} invoked, the whole buffer, including
	 * the contents before the offset, will be exported.
	 * 
	 * @param buff buffer to hold the contents
	 * @param offset position from where this output stream used
	 */
    public PositionedByteArrayOutputStream(byte[] buff, int offset) {
    	expandable = false;
    	this.buf = buff;
    	count = offset;		// bytes before is not sure, and not care
    	this.offset = offset;
	}
    
	@Override
	public synchronized void reset() {
		count = offset;
	}

	/**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * offset {@code index} to this stream.
     *
     * @param buffer
     *            the buffer to be written.
     * @param offset
     *            the initial position in {@code buffer} to retrieve bytes.
     * @param len
     *            the number of bytes of {@code buffer} to write.
     * @throws NullPointerException
     *             if {@code buffer} is {@code null}.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code len < 0}, or if
     *             {@code offset + len} is greater than the length of
     *             {@code buffer}.
     */
    @Override
    public synchronized void write(byte[] buffer, int offset, int len) {
    	ArrayUtility.checkOffsetAndCount(buffer.length, offset, len);
        if (len == 0) {
            return;
        }
        expand(len);
        System.arraycopy(buffer, offset, buf, this.count, len);
        this.count += len;
    }

    /**
     * Writes the specified byte {@code oneByte} to the OutputStream. Only the
     * low order byte of {@code oneByte} is written.
     *
     * @param oneByte
     *            the byte to be written.
     */
    @Override
    public synchronized void write(int oneByte) {
        if (count == buf.length) {
            expand(1);
        }
        buf[count++] = (byte) oneByte;
    }

    private void expand(int i) {
        /* Can the buffer handle @i more bytes, if not expand it */
        if (count + i <= buf.length) {
            return;
        }

    	if( !expandable ) {
    		throw new ArrayIndexOutOfBoundsException(
        			"Extended out of the unexpandable buffer. "
        				+ "length=" + buf.length + "; regionStart=" + count
        				+ "; regionLength=" + i);
    	}
    	
        byte[] newbuf = new byte[(count + i) * 2];
        System.arraycopy(buf, 0, newbuf, 0, count);
        buf = newbuf;
    }
}
