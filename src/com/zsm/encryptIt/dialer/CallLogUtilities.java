package com.zsm.encryptIt.dialer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

public class CallLogUtilities {

	private CallLogUtilities() {
	}
	
	public static void deleteLastOutgoingCall( Context context, String phoneNumber ) {
        Uri contacts = CallLog.Calls.CONTENT_URI;
        StringBuffer selection = new StringBuffer();
        selection.append( CallLog.Calls.TYPE ).append( "=" ).append(  CallLog.Calls.OUTGOING_TYPE );
        if( phoneNumber != null ) {
	        selection.append( " AND ");
	        selection.append( CallLog.Calls.NUMBER ).append( "=\"" ).append( phoneNumber ).append( "\"" );
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
			}
        } finally {
        	cursor.close();
        }
        cr.delete( contacts, "_id = " + id, null );
	}
	
}
