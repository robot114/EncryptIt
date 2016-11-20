package com.zsm.encryptIt.telephony;

import com.zsm.log.Log;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.Telephony;
import android.provider.Telephony.TextBasedSmsColumns;

public class TelephonyLogUtilities {

	private TelephonyLogUtilities() {
	}
	
	public static void deleteLastOutgoingCall( Context context, String phoneNumber ) {
        Uri contacts = CallLog.Calls.CONTENT_URI;
        StringBuffer selection = new StringBuffer();
        selection.append( CallLog.Calls.TYPE ).append( "=" )
        		 .append(  CallLog.Calls.OUTGOING_TYPE );
        if( phoneNumber != null ) {
	        selection.append( " AND ");
	        selection.append( CallLog.Calls.NUMBER ).append( "=\"" )
	        		 .append( phoneNumber ).append( "\"" );
        }
        
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        int id = -1;
        try {
			cursor
	        	= cr.query(contacts, new String[]{"_id"}, selection.toString(), null,
	        			   CallLog.Calls.DATE + " DESC limit 1");
			if( cursor != null && cursor.getCount() >= 1 ) {
				cursor.moveToFirst();
				id = cursor.getInt( cursor.getColumnIndex( "_id" ) );
		        cr.delete( contacts, "_id = " + id, null );
			}
        } catch( Exception e ) {
        	Log.e( e, "Clean last call failed!" );
        }finally {
        	if( cursor != null ) {
        		cursor.close();
        	}
        }
	}

	public static void deleteLastOutgoingSms(Context context, String phoneNumber) {
		Uri contacts = Telephony.Sms.CONTENT_URI;
		
        StringBuffer selection = new StringBuffer();
        selection.append( "(" )
        		 .append( TextBasedSmsColumns.TYPE ).append( "=" )
        		 .append( TextBasedSmsColumns.MESSAGE_TYPE_SENT )
        		 .append( " OR " )
        		 .append( TextBasedSmsColumns.TYPE ).append( "=" )
        		 .append( TextBasedSmsColumns.MESSAGE_TYPE_FAILED )
        		 .append( " OR " )
        		 .append( TextBasedSmsColumns.TYPE ).append( "=" )
        		 .append( TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX )
        		 .append( ")" );
        if( phoneNumber != null ) {
	        selection.append( " AND ");
	        selection.append( TextBasedSmsColumns.ADDRESS ).append( "=\"" )
	        		 .append( phoneNumber ).append( "\"" );
        }
        
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        int id = -1;
        try {
			cursor
	        	= cr.query(contacts, new String[]{"_id"}, selection.toString(), null,
	        			TextBasedSmsColumns.DATE_SENT + " DESC limit 1");
			if( cursor != null && cursor.getCount() >= 1 ) {
				cursor.moveToFirst();
				id = cursor.getInt( cursor.getColumnIndex( "_id" ) );
		        int rowNum = cr.delete( contacts, "_id = " + id, null );
		        Log.d( "Deleted row number of sms log is ", rowNum );
			}
        } catch( Exception e ) {
        	Log.e( e, "Clean last message failed!" );
        } finally {
        	if( cursor != null ) {
        		cursor.close();
        	}
        }
	}
	
}
