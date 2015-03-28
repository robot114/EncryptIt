package com.zsm.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ArrayUtility {

    /**
     * Checks that the range described by {@code offset} and {@code count} doesn't exceed
     * {@code arrayLength}.
     *
     */
    public static void checkOffsetAndCountInRange(int arrayLength,
    											  int offset, int count) {
    	
        if ((offset | count) < 0 || offset > arrayLength || arrayLength - offset < count) {
            throw new ArrayIndexOutOfBoundsException(
            			"length=" + arrayLength + "; regionStart=" + offset
            				+ "; regionLength=" + count);
        }
    }

    /**
     * Checks that the range described by {@code offset} doesn't exceed
     * {@code arrayLength}. And make sure {@code offset} and {@code count}
     * not less to zero
     *
     */
    public static void checkOffsetAndCount(int arrayLength, int offset, int count) {
        if ((offset | count) < 0 || offset > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(
            			"length=" + arrayLength + "; regionStart=" + offset
            				+ "; regionLength=" + count);
        }
    }

    /**
     * Checks that the range described by {@code start} and {@code end} doesn't exceed
     * {@code len}.
     *
     */
    public static void checkStartAndEnd(int len, int start, int end) {
        if (start < 0 || end > len) {
            throw new ArrayIndexOutOfBoundsException("start < 0 || end > len."
                    + " start=" + start + ", end=" + end + ", len=" + len);
        }
        if (start > end) {
            throw new IllegalArgumentException("start > end: " + start + " > " + end);
        }
    }
    
    public static ByteArray fromByteArray( Class<? extends ByteArray> c,
    									   byte[] a, int offset )
    				throws NoSuchMethodException, IllegalArgumentException,
    						InvocationTargetException, IllegalAccessException {
    	
    	Method m = c.getDeclaredMethod( "fromByteArray", byte[].class, Integer.class );
    	return (ByteArray)m.invoke( null,  a, offset );
    }
}
