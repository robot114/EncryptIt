package com.zsm.encryptIt.ui;

import com.zsm.encryptIt.R;

import android.os.Bundle;

public class SecurityBackupActivity extends ProtectedActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.security_backup_activity );
	}

	@Override
	protected boolean needPromptPassword() {
		return true;
	}

}
