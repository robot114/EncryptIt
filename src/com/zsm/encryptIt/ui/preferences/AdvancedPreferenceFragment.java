package com.zsm.encryptIt.ui.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.zsm.encryptIt.R;

public class AdvancedPreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.advanced_preferences);
	}
}
