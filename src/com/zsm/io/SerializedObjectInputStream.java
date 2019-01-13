package com.zsm.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;

public class SerializedObjectInputStream extends ObjectInputStream {

	private long mLastSerialVersionUID;

	public SerializedObjectInputStream(InputStream input)
					throws StreamCorruptedException, IOException {
		
		super(input);
	}
	
	  @Override
	  protected ObjectStreamClass readClassDescriptor() throws IOException,
	      ClassNotFoundException {
	    ObjectStreamClass descriptor = super.readClassDescriptor();
	    mLastSerialVersionUID = descriptor.getSerialVersionUID();
	    return descriptor;
	  }
	  
	  public long getLastSerialVersionUID() {
		  return mLastSerialVersionUID;
	  }
}
