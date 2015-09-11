package com.zsm.encryptIt.ui.preferences;

import com.zsm.driver.android.log.LogPreferencesActivity;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.ui.ActivityOperator;

public class SecurityAdvancedPreferencesActivity extends LogPreferencesActivity
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
