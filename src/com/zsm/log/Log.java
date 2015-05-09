package com.zsm.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

abstract public class Log {

	private static final String CLASS_NAME = Log.class.getName();

	/**
     * Constant indicating the logging level Debug is the default and the lowest level
     * followed by info, warning and error
     */
    public static final int DEBUG = 1;

    /**
     * Constant indicating the logging level information is the default and the lowest level
     * followed by info, warning and error
     */
    public static final int INFO = 2;

    /**
     * Constant indicating the logging level warning is the default and the lowest level
     * followed by info, warning and error
     */
    public static final int WARNING = 3;

    /**
     * Constant indicating the logging level error is the default and the lowest level
     * followed by info, warning and error
     */
    public static final int ERROR = 4;
    

    /**
     * Constant indicating no log will be output
     */
    public static final int NO_LOG = 5;
    
    final static private long zeroTime = System.currentTimeMillis();

    private static HashMap <String, Log> instances;
    
    private int level = ERROR;
    
    static private int globalLevel = ERROR;
    
    /**
     * Do anything needed when the log instance is uninstalled.
     * @throws IOException 
     */
    abstract protected void uninstall() throws IOException;

	/**
     * Create a reader to get all the logs. The reader <b>MUST NOT</b> append
     * the new line nor the return char for each line automatically.
     * 
     * @return Reader reader to get all the logs.
     * @throws IOException when creating the reader failed.
     */
    abstract public BufferedReader createReader() throws IOException;
    
	/**
     * Print the message to the log. After this method called,
     * the data passed in, <b>MUST</b> be flushed.
	 * @param level Level of this message
	 * @param message Message this time to log
	 * @param t Throwable instance to record the stack. It can be null.
     * 
     * @throws IOException when print failed
     */
    protected abstract void print(Throwable t, Object message, int level) throws IOException;
    
	/**
     * Clear all the logs.
     * 
     * @throws IOException when clearing fail
     */
    abstract public void clearContent() throws IOException;

    /**
     * Installs an instance.
     * 
     * @param id identity of the instance
     * @param newInstance the new instance for the Log object
     * @throws LogException when there is already an instance installed.
     */
    public static void install(String id, Log newInstance) throws LogException {
    	if( instances == null ) {
    		instances = new HashMap<String, Log>();
    	}
        instances.put(id, newInstance);
    }
    
    /**
     * To check if the special instance installed
     * 
     * @param id of the instance to be checked
     * @return true, the instance installed; false, otherwise
     */
    public static boolean isIinstalled( String id ) {
   		return instances.get(id) != null;
    }
    
   /**
     * Uninstall the log instance if it has been installed. If no instance
     * installed, nothing will happen. 
     * 
     * @param id id of the instance to be uninstalled
     * @throws IOException 
     */
    public static void uninstall( String id ) throws IOException {
   		instances.remove(id).uninstall();
    }
    
    /**
     * Uninstall all the instances
     * @throws IOException 
     */
    public void uninstallAll() throws IOException {
    	Set<String> keySet = instances.keySet();
    	for( String key : keySet ) {
    		uninstall( key );
    	}
    }
    
	/**
     * Get an instance by its id
     * 
     * @param id of the instance
     * @return the instance
     */
	public static Log getInstance(String id) {
		return instances.get(id);
	}
    
    /**
     * Log the event with DEBUG level
     * 
     * @param t make the log traceable
     * @param message the message to print
     */
    public static void d(Throwable t, String message, Object... objects) {
        p(t, DEBUG, message, objects);
    }
    
    /**
     * Log the event with DEBUG level
     * 
     * @param message the message to print
     */
    public static void d(String message, Object... objects) {
        p(null, DEBUG, message, objects);
    }
    
    /**
     * Log the calling position with DEBUG level
     * 
     */
    public static void d(Object... objects) {
        p(null, DEBUG, "", objects);
    }
    
    /**
     * Log the event with INFO level
     * 
     * @param t make the log traceable
     * @param message the message to print
     */
    public static void i(Throwable t, String message, Object... objects) {
        p(t, INFO, message, objects);
    }
    
    /**
     * Log the event with INFO level
     * 
     * @param message the message to print
     */
    public static void i(String message, Object... objects) {
        p(null, INFO, message, objects);
    }
    
    /**
     * Log the event with WARNING level
     * 
     * @param t make the log traceable
     * @param message the message to print
     */
    public static void w(Throwable t, String message, Object... objects) {
        p(t, WARNING, message, objects);
    }
    
    /**
     * Log the event with WARNING level
     * 
     * @param message the message to print
     */
    public static void w(String message, Object... objects) {
        p(null, WARNING, message, objects);
    }
    
    /**
     * Log the event with ERROR level
     * 
     * @param t make the log traceable
     */
	public static void e(Throwable t) {
		p(t, ERROR, t.toString() );
	}

    /**
     * Log the event with ERROR level
     * 
     * @param t make the log traceable
     * @param message the message to print
     */
    public static void e(Throwable t, String message, Object... objects) {
        p(t, ERROR, message, objects);
    }
    
    /**
     * Log the event with ERROR level
     * 
     * @param message the message to print
     */
    public static void e(String message, Object... objects) {
        p(null, ERROR, message, objects);
    }
    
    /**
     * Log the event with given level
     * @param level specify the log level, one of DEBUG, INFO, WARNING, ERROR
     * @param message the message to print
     */
    public static void p(Throwable t, int level, String message, Object... objects) {
    	if( globalLevel > level ) {
    		return;
    	}
    	
    	try {
    		Set<Entry<String, Log>> set = instances.entrySet();
    		StringBuffer buffer = null;
            synchronized( instances ) {
	    		for( Entry<String, Log> e : set) {
	    			if( e.getValue().level <= level ) {
	    				if( buffer == null ) {
	    					buffer = message( message, objects );
    	                }
	                	e.getValue().print(t, buffer, level);
    				}
    			}
    		}
    	} catch ( Exception e ) {
    		// When failed to record a log, the system MUST NOT be affected!
    		System.out.println( message );
    		e.printStackTrace();
    	}
   }

	private static StringBuffer message(String message, Object... objects) {
		
		StringBuffer buffer = new StringBuffer();
		
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		int count = 0;
		int i;
		for( i = 0; i < e.length; i++ ) {
			if( e[i].getClassName().equals( CLASS_NAME ) ) {
				count++;
			} else if( count > 0 ) {
				break;
			}
		}
		
		buffer.append( getThreadAndTimeStamp() );
		buffer.append( "-" );
		if( i < e.length ) {
			buffer.append( e[i] );
			buffer.append( ". Message: " );
		}
		buffer.append(message);
		if( objects.length > 0 ) {
			buffer.append( " With objects: " );
			for( Object obj : objects ) {
				buffer.append(obj);
				buffer.append( ", " );
			}
		}
		return buffer;
	}
    
    /**
     * Sets the global logging level for printing log details, the lower the value 
     * the more verbose information would be out. If a log's level is lower the
     * global level, it will not be out at all. If a log's level is higher than
     * or equal to the global level, but lower than a special instance's level,
     * it will not be out for this special instance. But it may be out for other
     * instance.
     * 
     * @param level one of DEBUG, INFO, WARNING, ERROR
     */
    public static void setGlobalLevel(int level) {
    	globalLevel = level;
    }

    /**
     * Returns the global logging level for printing log details.
     * 
     * @see {@link setGlobalLevel}
     * 
     * @id id of the instance to get the level
     * @return one of DEBUG, INFO, WARNING, ERROR
     */
    public static int getGlobalLevel( ) {
        return globalLevel;
    }
    
    /**
     * Sets the logging level for a special instance.
     * 
     * @see {@link setGlobalLevel}
     * 
     * @param id of the instance to change the level
     * @param level one of DEBUG, INFO, WARNING, ERROR
     */
    public static void setLevel(String id, int level) {
    	Log log = getInstance(id);
    	if( log != null ) {
    		getInstance(id).level = level;
    	} else {
    		Log.e( "Instance not installed", "instance", id );
    	}
    }

    /**
     * Returns the logging level for special instance
     * 
     * @see {@link setGlobalLevel}
     * 
     * @id id of the instance to get the level
     * @return one of DEBUG, INFO, WARNING, ERROR
     */
    public static int getLevel( String id ) {
        return getInstance(id).level;
    }
    
    /**
     * Returns the logging level for <b>THIS</b> instance
     * 
     * @see {@link setGlobalLevel}
     * 
     * @id id of the instance to get the level
     * @return one of DEBUG, INFO, WARNING, ERROR
     */
    protected int getLevel( ) {
        return level;
    }
    
    /**
     * Returns the contents of the log as a single long string to be displayed by
     * the application any way it sees fit
     * 
     * @return string containing the whole log
     */
    public String getLogContent() {
    	BufferedReader r = null;
        try {
            StringBuffer text = new StringBuffer();
            r = createReader();
            String str;
            while( ( str = r.readLine() ) != null ) {
            	text.append( str  );
            	text.append( "\r\n" );
            }
            return text.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        } finally {
        	if( r != null ) {
        		try {
					r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
        }
    }

    /**
     * Returns a simple string containing a timestamp and thread name.
     * 
     * @return timestamp string for use in the log
     */
    private static String getThreadAndTimeStamp() {
        long time = System.currentTimeMillis() - zeroTime;
        long milli = time % 1000;
        time /= 1000;
        long sec = time % 60;
        time /= 60;
        long min = time % 60; 
        time /= 60;
        long hour = time % 60; 
        
        return "[" + Thread.currentThread().getName() + "] "
        		+ hour  + ":" + min + ":" + sec + "," + milli;
    }
}
