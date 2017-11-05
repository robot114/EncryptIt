package com.zsm.encryptIt.telephony;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.NoSuchPaddingException;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import com.zsm.encryptIt.SystemParameter;
import com.zsm.log.Log;
import com.zsm.persistence.InOutDecorator;

public class SecurityMessager {

	private static final String CHARSET = "UTF-8";

	private static final String ACTION_SENT_SMS
		= "com.zsm.security.message.ACTION_SENT_SMS";
	private static final String ACTION_DELIVER_SMS
		= "com.zsm.security.message.ACTION_DELIVER_SMS";
	
	private static SecurityMessager instance;
	private static SmsManager mSmsManager;
	private static Context mContext;
	private BroadcastReceiver mSendReceiver;
	private BroadcastReceiver mDeliverReceiver;
	private SmsResultReceiver mSendResultReceiver;
	private SmsResultReceiver mDeliverResultReceiver;

	public interface SmsResultReceiver {
		void onReceive( int result, Intent intent );
	};
	
	private SecurityMessager( Context context ) {
		mContext = context;
		mSmsManager = SmsManager.getDefault();
		
		mSendReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if( mSendResultReceiver != null ) {
					mSendResultReceiver.onReceive(getResultCode(), intent);
				}
			}
		};
		
		mDeliverReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if( mDeliverResultReceiver != null ) {
					mDeliverResultReceiver.onReceive(getResultCode(), intent);
				}
			}
		};
	}

	static public void initMessager( Context context ) {
		instance = new SecurityMessager( context );
		Log.d( "Security messager initialized" );
	}
	
	static public SecurityMessager getInstance() {
		if( instance == null ) {
			throw new IllegalStateException( "Instance has not been initialized, "+
											  "call initMessager first" );
		}
		
		return instance;
	}

	/**
	 * Send SMS in plain text
	 * 
	 * @param target
	 * @param message
	 */
	public void sendSms( String target, String message ) {
		sendSms( target, message, null, null );
	}
	
	/**
	 * Send SMS in plain text
	 * 
	 * @param target
	 * @param message
	 * @param sendReceiver
	 * @param deliverReceiver
	 */
	public void sendSms( String target, String message, SmsResultReceiver sendReceiver,
						 SmsResultReceiver deliverReceiver ) {
		
		mSendResultReceiver = sendReceiver;
		mDeliverResultReceiver = deliverReceiver;
		
		ArrayList<String> messageArray = mSmsManager.divideMessage(message);
		int size = messageArray.size();
		ArrayList<PendingIntent> spia = new ArrayList<PendingIntent>( size );
		ArrayList<PendingIntent> dpia = new ArrayList<PendingIntent>( size );
		for( int i = 0; i < size; i++ ) {
			spia.add( eventIntent(mContext, ACTION_SENT_SMS, size) );
			dpia.add( eventIntent(mContext, ACTION_DELIVER_SMS, size) );
		}
		mSmsManager.sendMultipartTextMessage(target, null, messageArray, spia, dpia);
	}
	
	/**
	 * Send SMS encoded by password based key
	 * 
	 * @param target
	 * @param message
	 * @param password
	 * 
	 * @return true, encode the message successfully, and send it successfully;
	 * 			false, otherwise.
	 * 			When it return it is not guaranteed to send successfully.
	 */
	public boolean sendSms( String target, String message, char[] password ) {
		return sendSms( target, message, password, null, null ); 
	}
	
	/**
	 * Send SMS encoded by password based key
	 * 
	 * @param target
	 * @param message
	 * @param password
	 * @param sendReceiver
	 * @param deliverReceiver
	 * 
	 * @return true, encode the message successfully, and send it successfully;
	 * 			false, otherwise.
	 * 			When it return it is not guaranteed to send successfully.
	 */
	public boolean sendSms( String target, String message, char[] password,
							SmsResultReceiver sendReceiver,
							SmsResultReceiver deliverReceiver ) {
		
		try {
			InOutDecorator io
				= SystemParameter.getPasswordBasedInOutDecorator( password );
			byte[] encodeData = io.encode(message.getBytes( CHARSET ));
			byte[] rb = toReadableBytes(encodeData);
			String sendMsg = new String( rb, "US-ASCII" );
			sendSms( target, sendMsg, sendReceiver, deliverReceiver );
			return true;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException | IOException e) {
			Log.e( e, "Encode the message failed!" );
			return false;
		}
	}
	
	/**
	 * Build a pending intent to catch message event information
	 * 
	 * @param context
	 * @param action what the event
	 * @param number how many parts of the message
	 * @param nth the nth of this part, begin from 1
	 * @return
	 */
	private PendingIntent eventIntent(Context context, String action,
									  int number) {
		
		Intent sentIntent = new Intent( action );
		sentIntent.putExtra( SecurityMessageActivity.KEY_MESSAGE_TOTAL_NUMBER, number );
		return PendingIntent.getBroadcast( context, 0, sentIntent,
									  	   PendingIntent.FLAG_UPDATE_CURRENT );
	}
	
	public void registerReceiver() {
		mContext.registerReceiver(mSendReceiver, new IntentFilter( ACTION_SENT_SMS ) );
		mContext.registerReceiver(mDeliverReceiver, new IntentFilter( ACTION_DELIVER_SMS ) );
	}
	
	public void unregisterReceiver() {
		mContext.unregisterReceiver(mSendReceiver);
		mContext.unregisterReceiver(mDeliverReceiver);
	}

	/**
	 * Decode the received message. If decode failed, the original message returned.
	 * 
	 * @param originalMessage
	 * @param password
	 * @return
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	public String decodeMessage(String originalMessage, String password)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
				   InvalidKeySpecException, IOException {
		
		InOutDecorator io
			= SystemParameter.getPasswordBasedInOutDecorator( 
					password.toCharArray() );
		
		byte[] encodeData = originalMessage.getBytes("US-ASCII");
		byte[] ob = fromReadable(encodeData);
		byte[] plainData = io.decode( ob );
		return new String( plainData, CHARSET );
	}
	
	// Convert one byte value from [-128, 127] to two bytes (PREFIX, [' ', '~'])
	final static private byte PREFIX[][]
		= { {'L', '$', '{', 'P', 'Z', '&', 'Q', '^', '\\', '*', '%', '>', 'R', 'j', 'J', '|',
			 '9', ')', '1', '_', 't', 'r', ':', '2'},
			{'z', 'V', 'B', '4', '`', '[', 'M', 'F', 'q', 'k', '@', '-', 'X', 'y', 'w', 'o',
			 'Y', 'G', 'e', 'D', '#', 'H', '0', 's'},
			{'C', 'a', 'i', 'b', '5', 'E', ' ', 'n', '=', 'c', 'f', '}', '6', 'T', 'I', '\'',
			 'g', ']', 'l', 'W', '.', 'v', '7', ',' },
			{ 'h', '+', '<', 'x', '"', 'U', 'N', '3', '8', 'O', ';', '!', 'p', 'd', '/',
			  '~', '(', 'm', 'u', 'S', 'A', 'K' } };
	
	final static private int LEVEL_INDEX[] = {
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
		2, 3, 3, 1, 0, 0, 0, 2, 3, 0, 0, 3, 2, 1, 2, 3, 1, 0, 0, 3, 1, 2, 2, 2, 3,
		0, 0, 3, 3, 2, 0, -1, 1, 3, 1, 2, 1, 2, 1, 1, 1, 2, 0, 3, 0, 1, 3, 3, 0, 0,
		0, 3, 2, 3, 1, 2, 1, 1, 0, 1, 0, 2, 0, 0, 1, 2, 2, 2, 3, 1, 2, 2, 3, 2, 0,
		1, 2, 3, 2, 1, 3, 1, 0, 1, 0, 3, 2, 1, 3, 1, 1, 0, 0, 2, 3,
		-1 	
	};
	
	private byte[] toReadableBytes( byte[] data ) {
		byte[] rb = new byte[data.length*2];
		
		for( int i = 0; i < data.length; i++ ) {
			int level = 0;
			byte b = data[i];
			if( b < 0 ) {
				level = 2;
				b += 128;
			}
			if( b >= 0 && b < ' ' ) {
				level++;
				b += ' ';
			} else if( b == 127 ) {
				level++;
				b = '~';
			}
			
			byte[] pp = PREFIX[level];
			byte prefix = pp[(int) (Math.random()*pp.length)];
			
			rb[2*i] = prefix;
			rb[2*i+1] = b;
		}
		
		return rb;
	}
	
	private byte[] fromReadable( byte[] data ) {
		int length = data.length;
		if( length/2*2 != length ) {
			throw new IllegalArgumentException( "Invalid length of data" );
		}
		
		byte[] ob = new byte[length/2];
		for( int i = 0; i < length; i+=2 ) {
			int level = LEVEL_INDEX[data[i]];
			byte value = data[i+1];
			if( level == 1 || level == 3 ) {
				if( value == '~' ) {
					value = 127;
				} else {
					value -= ' ';
				}
			}
			if( level >= 2 ) {
				value -= 128;
			}
			ob[i/2] = value;
		}
		
		return ob;
	}
}
