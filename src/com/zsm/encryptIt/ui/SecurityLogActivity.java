package com.zsm.encryptIt.ui;

import com.zsm.driver.android.log.LogActivity;
import com.zsm.encryptIt.app.EncryptItApplication;


public class SecurityLogActivity extends LogActivity 
				implements ActivityOperator {
	
	@Override
	protected void onResume() {
		super.onResume();
		((EncryptItApplication)getApplication())
			.resumeProtectedActivity( this, true );
	}

	@Override
	protected void onPause() {
		super.onPause();
		((EncryptItApplication)getApplication()).startActivityTransitionTimer();
	}

}
