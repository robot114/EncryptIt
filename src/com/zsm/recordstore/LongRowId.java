package com.zsm.recordstore;

import java.nio.ByteBuffer;
import java.sql.RowId;

public class LongRowId implements RowId {
	private ByteBuffer buffer = ByteBuffer.allocate(Long.SIZE/8);
	
	private Long id;
	
	public LongRowId( long id ) {
		this.id = id;
	}
	
	public LongRowId( byte[] bytes ) {
		fromByteArrayInter(bytes, 0);
	}
	
	@Override
	public byte[] getBytes() {
		buffer.putLong(0, id);
        return buffer.array();
	}

	@Override
	public boolean equals(Object o) {
		if( this == o ) {
			return true;
		}
		
		if( o.getClass().equals( LongRowId.class ) && id == ((LongRowId)o).id ) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "" + id;
	}

	private void fromByteArrayInter(byte[] a, int offset) {
        buffer.put(a, offset, Long.SIZE/8);
        buffer.flip();//need flip 
        id = buffer.getLong();
	}

	public long getLongId() {
		return id;
	}
}
