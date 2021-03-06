package com.zsm.persistence;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.sql.RowId;
import java.util.Arrays;

import com.zsm.log.Log;
import com.zsm.recordstore.AbstractMetaDataCursor;
import com.zsm.recordstore.AbstractRawCursor;
import com.zsm.recordstore.RawRecordStore;
import com.zsm.recordstore.RecordStoreManager;

import android.os.ParcelFileDescriptor;

public class Persistence implements Closeable {

	static final private InOutDecorator DEFAULT_DECORATOR
		= new PlainInOutDecorator();
	
	// This is the first line of the database
	private byte[] verifyData = null;
	
	RawRecordStore recordStore;

	private String name;

	private boolean opened;

	private InOutDecorator inOutDecorator;

	/**
	 * Construct a persistence instance.
	 * 
	 * @param name persistence name. If a database used for persistence, it is the name
	 * 			   of the database. If a file used, it is the full path name of the file.
	 * @param magic to make sure the persistence is the one needed
	 * @param version version of the persistence. The explanation of the version is 
	 * 				  implementation-defined
	 * @param options implementation-defined
	 * @param decorator stream decorator to change the datas read and written
	 */
	public Persistence( String name, byte[] magic, long version, byte[] options,
						InOutDecorator decorator) {
		
		int len = magic.length + Long.SIZE + options.length;
		verifyData = new byte[len];
		
		System.arraycopy(magic, 0, verifyData, 0, magic.length);
		ByteBuffer bb = ByteBuffer.wrap(verifyData, magic.length, Long.SIZE );
		bb.putLong(version);
		System.arraycopy(options, 0, verifyData, len - options.length,
						 options.length);
		
		this.name = name;
		inOutDecorator = decorator;
	}
	
	/**
	 * Construct a persistence instance.
	 * 
	 * @param name persistence name. If a database used for persistence, it is the name
	 * 			   of the database. If a file used, it is the full path name of the file.
	 * @param magic to make sure the persistence is the one needed
	 * @param version version of the persistence. The explanation of the version is 
	 * 				  implementation-defined
	 * @param options implementation-defined
	 */
	public Persistence( String name, byte[] magic, long version, byte[] options) {
		this( name, magic, version, options, DEFAULT_DECORATOR );
	}
	
	/**
	 * Set the input and output decorator. The decorator can be set only once
	 * after the persistence object create.
	 * 
	 * @param decorator input and output decorator
	 */
	public void setInOutDecorator( InOutDecorator decorator ) {
		if( inOutDecorator != null && inOutDecorator != DEFAULT_DECORATOR ) {
			throw new IllegalStateException( "Stream Decorator can only be set once!" );
		}
		inOutDecorator = decorator;
	}
	
	/**
	 * Get the input and output decorator. The method getData of cursor of the 
	 * persistence should call the method {@link #code decode.InOutDecorator}
	 * to transfer the data from the database, before return it.
	 * 
	 * return decorator input and output decorator
	 */
	private InOutDecorator getInOutDecorator( ) {
		return inOutDecorator;
	}
	
	/**
	 * Open or create a persistence instance on the storage. After the persistence 
	 * opened, a cursor of the the under lay database is opened.
	 * <p>When the existed persistence instance is opened, this method will check
	 * the validation.  
	 * 
	 * If the persistence is opened already, no thing will be done.
	 * 
	 * @throws BadPersistenceFormatException Check validation fails.
	 * @throws IOException Read or write the persistence fails.
	 */
	public void open() throws BadPersistenceFormatException, IOException {
		if( opened ) {
			Log.d( "The persistence has been already opened, nothing will be done." );
			return;
		}
		boolean rsExist = RecordStoreManager.getInstance().recordStoreExist(name);
		recordStore
			= RecordStoreManager.getInstance().openRawRecordStore(name);
		
		if( rsExist ) {
			checkMagic( );
		} else {
			setMagic();
			Log.d( "DB does not exist. Magic data set." );
			query();
		}
		
		opened = true;
	}
	
	private void checkMagic( )
			throws BadPersistenceFormatException, IOException {
		
		// Create the cursor without cursor mover, to avoid skipping the magic data
		AbstractRawCursor cursor
			= recordStore.newCursor( null, null, null, null,
									 RawRecordStore.COLUMN_ID + " ASC LIMIT 1",
									 null);
		if( !cursor.moveToFirst() ) {
			throw new BadPersistenceFormatException( "No needed data to check!" );
		}
		// With the method recordStore.getInputStream, to get the original data
		// in the database, instead of 'wrapped' data.
		InputStream in = recordStore.getInputStream(cursor);
		byte[] buffer = new byte[verifyData.length];
		try {
			in.read(buffer);
		} finally {
			in.close();
		}
		
		if( !Arrays.equals(buffer, verifyData) ) {
			throw new BadPersistenceFormatException( "Verification datas do not match!" );
		}
		cursor.moveToFirst();
	}

	private void setMagic() throws IOException {
		AbstractRawCursor c = null;
		try {
			c = recordStore.add( verifyData );
		} finally {
			if( c != null ) {
				c.close();
			}
		}
	}

	/**
	 * Query the persistence and generate a cursor as the result.
	 * 
	 * @param id which row with this id will be return. For null, all rows are returned.
	 * @return cursor points to the query result
	 */
	public AbstractRawCursor query( RowId id ) {
		Log.d( "Start Query.", "recordStore", recordStore,
				"id", id );
		
		AbstractRawCursor c = recordStore.newCursor( id );
		
		Log.d( "Query OK.", "cursor", c );
		return new PersistenceCursor( c );
	}

	public AbstractRawCursor query( ) {
		return query( null );
	}
	
	/**
	 * Get the integer meta data
	 * 
	 * @param key KEY of the meta data
	 * @param defaultValue default value of the data, if failed to get get it
	 * 			from the record store
	 */
	public int getIntMetaData( String key, int defaultValue ) {
		return recordStore.getIntMetaData(key, defaultValue);
	}

	public AbstractMetaDataCursor queryMetaData( String key ) {
		return recordStore.queryMetaData(key);
	}
	
	public byte[] getMetaData( String key ) {
		return recordStore.getMetaData(key);
	}
	
	/**
	 * Close the persistence
	 * 
	 */
	public void close() {
		if( !opened ) {
			return;
		}
		opened = false;
		
		RecordStoreManager.getInstance().closeRawRecordStore( recordStore );
	}
	
	/**
	 * Clear the persistence. The data base will be deleted. But it will not be
	 * rebuilt. The database will be rebuilt next time it is opened.
	 * 
	 */
	public void clear() {
		close();
		RecordStoreManager.getInstance().deleteRecordStore( name );
	}
	

	public String getFullPathName() {
		return RecordStoreManager.getInstance().getFullPathName( name );
	}

	public boolean renameTo(String newFullPathName) throws FileNotFoundException {
		close();
		return RecordStoreManager.getInstance().rename( getFullPathName(), newFullPathName);
	}

	public boolean backupToLocal() throws FileNotFoundException {
		return renameTo( getFullBackupName() );
	}

	private String getFullBackupName() {
		return getFullPathName() + ".bak";
	}

	/**
	 * Add a new record with a Serializable object.
	 * 
	 * @param data to be added
	 * @return id of the new record
	 * @throws IOException if IO exception happens
	 */
	public RowId add( Serializable data ) throws IOException {
		Log.d( "The following is the data to be added: ", data );
		try( ByteArrayOutputStream aos = new ByteArrayOutputStream();
			 ObjectOutputStream serOut
			 	= new ObjectOutputStream( wrapOutputStream( aos ) )
		) {
			serOut.writeObject( data );
			serOut.close();		// flush
			
			return add( aos.toByteArray() );
		}
	}

	/**
	 * Add a new record with a byte buffer.
	 * 
	 * @param data to be added
	 * @return id of the new record
	 * @throws IOException if IO exception happens
	 */
	public RowId add( byte[] data ) throws IOException {
		checkOpened();
		Log.d( "The following byte buffer is to be added: ", data );
		RowId id = null;
		AbstractRawCursor c = recordStore.add( getInOutDecorator().encode(data) );
		id = c.currentId();
		Log.d( "Data added to the db as a byte array!" );
		c.close();
		
		return id;
	}

	private void checkOpened() throws IOException {
		if( !opened ) {
			throw new IOException( "Persistence is not opened!" );
		}
	}

	/**
	 * Read the current data as a serializable object. This object <b>MUST</b>
	 * be written by the method{@link add( Serializable data )}.
	 * 
	 * @param cursor from where the object read
	 * @return object represented by the cursor record
     * @throws ClassNotFoundException
     *             if the class of one of the objects in the object graph cannot
     *             be found.
     * @throws IOException
     *             if an error occurs while reading from the source stream.
     * @throws BadPersistenceFormatException
     *             if primitive data types were found instead of an object.
     * @throws StreamCorruptedException
     * 				if the source stream does not contain serialized objects
     * 				that can be read.
	 */
	public Object read(AbstractRawCursor cursor)
			throws BadPersistenceFormatException, ClassNotFoundException, IOException {
		
		checkOpened();
		Object obj = null;
		
		try(
			InputStream in = getInputStream(cursor);
			ObjectInputStream serIn = new ObjectInputStream(in);
		) {
			obj = serIn.readObject();
		} catch( OptionalDataException | StreamCorruptedException e ) {
			Log.e( e, "Bad item format in persistence." );
			throw new BadPersistenceFormatException( e );
		}
		
		return obj;
	}
	
	/**
	 * Update the current record with a Serializable object.
	 * 
	 * @param cursor where the data will be updated
	 * @param data by which to update the current record
	 * 
	 * @throws IOException if IO exception happens
	 */
	public void update( AbstractRawCursor cursor, Serializable data )
							throws IOException {
		update( cursor.currentId(), data );
	}

	/**
	 * Update the record, who has the id, with a Serializable object.
	 * 
	 * @param id of the record
	 * @param data by which to update the current record
	 * @throws IOException if IO exception happens
	 */
	public void update( RowId id, Serializable data ) throws IOException {
		checkOpened();

		OutputStream out = getOutputStream( id );
		ObjectOutputStream serOut = null;
		try {
			serOut = new ObjectOutputStream( out );
			serOut.writeObject( data );
		} finally {
			if( serOut != null ) {
				serOut.close();
			}
			out.close();
		}
	}

	/**
	 * Update the current record with a byte buffer.
	 * 
	 * @param cursor where the data will be updated
	 * @param data by which to update the current record
	 * 
	 * @throws IOException if IO exception happens
	 */
	public void update( AbstractRawCursor cursor, byte[] data ) throws IOException {
		update( cursor.currentId(), data );
	}

	/**
	 * Update the record, who has the id, with a byte buffer.
	 * 
	 * @param id of the record
	 * @param data by which to update the current record
	 * @throws IOException if IO exception happens
	 */
	public void update( RowId id, byte[] data ) throws IOException {
		checkOpened();
		recordStore.update(id, getInOutDecorator().encode(data));
	}

	/**
	 * Remove the record with special id
	 * 
	 * @param id remove a record by id
	 * @return count of records removed. It should be 0 or 1 here.
	 */
	public int remove( RowId id ) throws IOException {
		checkOpened();
		int num = recordStore.remove(id);
		return num;
	}
	
	/**
	 * Remove the current record
	 * 
	 * @return count of records removed. It should be 0 or 1 here.
	 * @param cursor record at where to be removed
	 * @throws IOException 
	 */
	public int remove(AbstractRawCursor cursor) throws IOException {
		checkOpened();
		int num = recordStore.remove(cursor);
		return num;
	}
	
	/**
	 * Get a input stream for the cursor points to
	 * @param cursor at where the input stream get
	 * 
	 * @return the input stream
	 * @throws IOException IO error occurred
	 */
	public InputStream getInputStream(AbstractRawCursor cursor) throws IOException {
		checkOpened();
		return wrapInputStream( recordStore.getInputStream(cursor) );
	}
	
	/**
	 * Get a input stream of the record with the id
	 * 
	 * @param id of the record
	 * @return the input stream
	 * @throws IOException IO error occurred
	 */
	public InputStream getInputStream( RowId id ) throws IOException {
		checkOpened();
		return wrapInputStream( recordStore.getInputStream(id) );
	}
	
	/**
	 * Get a output stream for the cursor points to. 
	 * @param cursor at where the output stream get
	 * 
	 * @return the output stream
	 * @throws IOException IO error occurred
	 */
	public DataOutputStream getOutputStream(AbstractRawCursor cursor)
				throws IOException {
		
		checkOpened();
		return wrapOutputStream( recordStore.getOutputStream(cursor) );
	}

	/**
	 * Get a output stream for the record with the id
	 * 
	 * @param id of the record
	 * @return the output stream
	 * @throws IOException IO error occurred
	 */
	public DataOutputStream getOutputStream( RowId id ) throws IOException {
		checkOpened();
		return wrapOutputStream( recordStore.getOutputStream(id) );
	}

	/**
	 * Wrap the input stream. By this method, the subclass can add some
	 * additional operations for the input stream.
	 * 
	 * @param in the input stream to be wrapped
	 * @return wrapped input stream
	 * @throws IOException error occurred
	 */
	private InputStream wrapInputStream( InputStream in )
				throws IOException {
		if( inOutDecorator != null ) {
			return inOutDecorator.wrapInputStream(in);
		}
		return in;
	}
	
	/**
	 * Wrap the output stream. By this method, the subclass can add some
	 * additional operations for the output stream.
	 * 
	 * @param out the output stream to be wrapped
	 * @return wrapped output stream
	 * @throws IOException error occurred
	 */
	private DataOutputStream wrapOutputStream( OutputStream out )
				throws IOException {
		if( inOutDecorator != null ) {
			return inOutDecorator.wrapOutputStream(out);
		}
		return new DataOutputStream( out );
	}

	private boolean restoreFromLocal(String backupName)
						throws FileNotFoundException {
		
		File bakFile = new File( backupName ).getAbsoluteFile();
		if( !bakFile.exists() ) {
			throw new FileNotFoundException( 
					"The backup file(" + bakFile.getAbsolutePath()
					+ ") is not found!" );
		}
		
		String backupName2 = backupName + ".bak2";
		if( !renameTo( backupName2 ) ) {
			Log.w( "Rename the current db to backup2 failed, to file:",
				   backupName2);
			
			return false;
		}
		
		String fullpathName = getFullPathName();
		
		// The the file with "name" is the local backup file
		try {
			if( !RecordStoreManager.getInstance().rename( backupName, fullpathName ) ) {
				boolean res = restoreForRestore(backupName2);
				Log.w( "Rename from the local backuped failed.",  "from file",
					   backupName, "result of restore from backup2", res );
				return false;
			}
		} catch ( FileNotFoundException e ) {
			boolean res = restoreForRestore(backupName2);
			Log.w( "The local backuped does not found.",  "from file",
				   backupName, "result of restore from backup2", res );
			return false;
		}
		
		new File( backupName2).delete();
		Log.d( "Restore from the local backuped file successfully!" );
		return true;
	}

	private boolean restoreForRestore(String backupName)
			throws FileNotFoundException {
		// Delete the local backup file
		String fullPathName = getFullPathName();
		new File( fullPathName ).delete();
		return RecordStoreManager.getInstance().rename( backupName, fullPathName );
	}
	
	public boolean restoreFromLocal() throws FileNotFoundException {
		return restoreFromLocal( getFullBackupName() );
	}

	
	public ParcelFileDescriptor openFileDescriptorForBackup(String mode)
					throws FileNotFoundException {
		
		if( !"r".equals( mode ) && !"w".equals(mode) ) {
			throw new InvalidParameterException(
						"Invalid mode, only 'r' or 'w' supported" );
		}
		
        int modeBits = ParcelFileDescriptor.parseMode(mode);
		return ParcelFileDescriptor.open( new File( getFullPathName() ), modeBits );
	}

	public long getBackupSize() {
		return new File( getFullPathName() ).length();
	}
}
