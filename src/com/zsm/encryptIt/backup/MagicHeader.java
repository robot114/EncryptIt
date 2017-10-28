package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.zsm.log.Log;

abstract class MagicHeader {

	final static byte[] MAGIC_CODE = "Backuped by EncryptIt. Ver 1.0.0".getBytes();
	
	public boolean checkHeader( InputStream in ) {
		
		final byte[] mMagicBuffer = new byte[MAGIC_CODE.length];
		try {
			if( in.read(mMagicBuffer) < mMagicBuffer.length ) {
				Log.w( "Length of source is less than the Magic code!" );
				return false;
			}
		} catch (IOException e) {
			Log.w( e, "Read the magic code failed!");
			return false;
		}
		
		boolean res = Arrays.equals(mMagicBuffer, MAGIC_CODE);
		Log.d( "Check header's magic result: ", res );
		
		return res;
	}
	
	public void outputHeader( OutputStream out ) throws IOException {
		out.write(MAGIC_CODE);
	}

}
