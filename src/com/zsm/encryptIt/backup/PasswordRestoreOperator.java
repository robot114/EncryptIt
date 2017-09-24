package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import android.content.ContentResolver;
import android.net.Uri;

import com.zsm.encryptIt.SystemParameter;

public class PasswordRestoreOperator extends RestoreOperator {

	private final char[] mPassword;
	
	public PasswordRestoreOperator( ContentResolver cr, Backupable target,
								    Uri source, char[] password ) {
		
		super( new UriRestoreSource(cr, source), target );
		mPassword = password;
	}
	
	@Override
	public InputStream openInputStream()
				throws GeneralSecurityException, IOException {
		
		if( hasPassword() ) {
			return SystemParameter.getPasswordBasedInOutDecorator(
					mPassword ).wrapInputStream(mSource.openInputStream());
		} else {
			return mSource.openInputStream();
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Restore task ---- Target: " )
				.append( mTarget )
				.append( ", Source: " )
				.append( mSource );
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
}
