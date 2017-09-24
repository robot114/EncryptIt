package com.zsm.encryptIt.ui;

import com.zsm.encryptIt.R;

import android.content.Intent;
import android.os.Bundle;

public class SecurityBackupActivity extends ProtectedActivity {

	static final String DATA_LAYOUT = "LAYOUT";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent data = getIntent();
		int layout
			= data.getIntExtra( DATA_LAYOUT, R.layout.security_backup_activity );
		setContentView( layout );
	}

	@Override
	protected boolean needPromptPassword() {
		return true;
	}

}
