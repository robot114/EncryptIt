package com.zsm.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileLog extends Log {

	private File file;
	private FileWriter writer;

	/**
	 * Constructor of the FileLog.
	 * 
	 * @param fileName name of the file to store the logs
	 * @param maxLength max length in byte of the file. Every time the file 
	 * 			log instance created, the file length will be checked. If
	 * 			the file's length is larger than maxLength, the log file will
	 * 			be deleted.
	 * @throws IOException when create or open the file failed.
	 */
	public FileLog( String fileName, long maxLength ) throws IOException {
		file = new File( fileName );
		if( file.exists() ) {
			if( maxLength > 0 && file.length() > maxLength ) {
				clearContent();
			}
		} else {
			createFile( file );
		}
		writer = new FileWriter( file, true );
	}
	
	static private boolean createFile( File f ) throws IOException {
		File parentFile = f.getParentFile();
		if( parentFile.exists() || parentFile.mkdirs() ) {
			return f.createNewFile();
		}
		
		return false;
	}
	
	@Override
	public BufferedReader createReader() throws IOException {
		return new BufferedReader( new FileReader( file ) );
	}

	@Override
	protected void print(Throwable t, Object message, int level)
			throws IOException {
		
		writer.append( message.toString() ).append( "\r\n" );
		
		if( t != null ) {
			StackTraceElement[] ste = t.getStackTrace();
			for( StackTraceElement e : ste ) {
				writer.append( e.toString() ).append( "\r\n" );
			}
		}
		
		writer.flush();
	}

	@Override
	public void clearContent() throws IOException {
		writer.close();
		file.delete();
		createFile( file );
		writer = new FileWriter( file, true );
	}

	@Override
	protected void uninstall() throws IOException {
		writer.close();
	}
}
