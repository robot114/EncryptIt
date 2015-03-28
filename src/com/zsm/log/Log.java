package com.zsm.log;

import java.io.BufferedReader;
import java.io.IOException;

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
    
    private static Log instance;
    
    private int level = ERROR;
    
    private long zeroTime = System.currentTimeMillis();

	/**
     * Create a reader to get all the logs. The reader <b>MUST NOT</b> append
     * the new line nor the return char for each line automatically.
     * 
     * @return Reader reader to get all the logs.
     * @throws IOException when creating the reader failed.
     */
    abstract protected BufferedReader createReader() throws IOException;
    
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
     * Installs an instance of
     * 
     * @param newInstance the new instance for the Log object
     * @throws LogException when there is already an instance installed.
     */
    public static void install(Log newInstance) throws LogException {
    	if( instance != null ) {
    		throw new LogException( "Uninstanll the installed instance first!" );
    	}
        instance = newInstance;
    }
    
    /**
     * Uninstall the log instance if it has been installed. If no instance
     * installed, nothing will happen. 
     * 
     */
    public static void uninstall() {
   		instance = null;
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
    	try {
        	if( level >= instance.level ) {
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
            	
        		buffer.append( instance.getThreadAndTimeStamp() );
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
                synchronized( instance ) {
                	instance.print(t, buffer, level);
                }
        	}
    	} catch ( Exception e ) {
    		// When failed to record a log, the system MUST NOT be affected!
    		System.out.println( message );
    		e.printStackTrace();
    	}
   }
    
    /**
     * Sets the logging level for printing log details, the lower the value 
     * the more verbose would the printouts be
     * 
     * @param level one of DEBUG, INFO, WARNING, ERROR
     */
    public static void setLevel(int level) {
        instance.level = level;
    }
    
    /**
     * Returns the logging level for printing log details, the lower the value 
     * the more verbose would the printouts be
     * 
     * @return one of DEBUG, INFO, WARNING, ERROR
     */
    public static int getLevel() {
        return instance.level;
    }
    
    /**
     * Returns the contents of the log as a single long string to be displayed by
     * the application any way it sees fit
     * 
     * @return string containing the whole log
     */
    public static String getLogContent() {
    	BufferedReader r = null;
        try {
            StringBuffer text = new StringBuffer();
            r = instance.createReader();
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
    private String getThreadAndTimeStamp() {
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
