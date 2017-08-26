package com.zsm.encryptIt.backup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.NoSuchPaddingException;

import com.zsm.encryptIt.SystemParameter;

import android.content.ContentResolver;
import android.net.Uri;

public class PasswordBackupOperator extends BackupOperator {
	
	final private BackupInputAgent mInputAgent;
	final private char[] mPassword;
	private UriOutputAgent mOutputAgent;
	
	public PasswordBackupOperator( ContentResolver cr, BackupInputAgent agent,
								   Uri targetUri, char[] password ) {
		
		mInputAgent = agent;
		mOutputAgent = new UriOutputAgent(cr, targetUri);
		mPassword = password;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Backup task ---- Agent: " )
				.append( mInputAgent )
				.append( ", Target: " )
				.append( mOutputAgent.displayName() );
		if( hasPassword() ) {
			builder.append( ", Without Password" );
		} else {
			builder.append( ", With Password" );
		}
		
		return builder.toString();
	}

	private boolean hasPassword() {
		return mPassword == null || mPassword.length == 0;
	}

	@Override
	public InputStream openBackupInputStream() throws FileNotFoundException {
		return mInputAgent.openBackupInputStream();
	}

	@Override
	public long size() {
		return mInputAgent.size();
	}

	@Override
	public OutputStream openOutputStream() 
				throws NoSuchAlgorithmException, NoSuchPaddingException,
					   InvalidKeySpecException, IOException {
		
		if( hasPassword() ) {
			return SystemParameter.getPasswordBasedInOutDecorator(
					mPassword ).wrapOutputStream(mOutputAgent.openOutputStream());
		} else {
			return mOutputAgent.openOutputStream();
		}
	}

	@Override
	public String displayName() {
		return mOutputAgent.displayName();
	}
	
	
}