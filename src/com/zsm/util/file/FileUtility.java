package com.zsm.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtility {

	public static void copyFile(String src, String target)
						throws FileNotFoundException, IOException {

	    InputStream in = null;
	    OutputStream out = null;
	    try {

	        //create output directory if it doesn't exist
	        File targetPath = new File(target).getAbsoluteFile().getParentFile(); 
	        if (!targetPath.exists())
	        {
	            targetPath.mkdirs();
	        }

	        in = new FileInputStream(src);        
	        out = new FileOutputStream(target);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;

            // write the output file (You have now copied the file)
            out.flush();
	        out.close();
	        out = null;        

	    } finally {
	    	if( in != null ) {
	    		in.close();
	    	}
	    	if( out != null ) {
	    		out.close();
	    	}
	    }

	}
}
