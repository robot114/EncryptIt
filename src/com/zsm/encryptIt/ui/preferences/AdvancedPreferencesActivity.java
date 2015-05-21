package com.zsm.encryptIt.ui.preferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.encryptIt.ui.ActivityOperator;

public class AdvancedPreferencesActivity extends PreferenceActivity
				implements ActivityOperator {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.advanced_preferences);
	}

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
