package com.zsm.encryptIt.backup;

import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import android.content.ContentResolver;
import android.net.Uri;

import com.zsm.encryptIt.SystemParameter;

public class PasswordBackupOperator extends BackupOperator {
	
	protected final char[] mPassword;

	public PasswordBackupOperator( ContentResolver cr, Backupable source,
								   Uri target, char[] password ) {
		
		super( source, new UriBackupTarget(cr, target) );
		mPassword = password;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Backup task ---- Source: " )
				.append( mSource )
				.append( ", Target: " )
				.append( mTarget.displayName() );
		if( hasPassword() ) {
			builder.append( ", With Password" );
		} else {
			builder.append( ", Without Password" );
		}
		
		return builder.toString();
	}

	@Override
	public OutputStream openOutputStream() 
				throws IOException, GeneralSecurityException {
		
		if( hasPassword() ) {
			return SystemParameter.getPasswordBasedInOutDecorator(
					mPassword ).wrapOutputStream(mTarget.openOutputStream());
		} else {
			return mTarget.openOutputStream();
		}
	}
	

	private boolean hasPassword() {
		return mPassword != null && mPassword.length > 0;
	}

}