package com.zsm.encryptIt.ui;

import com.zsm.encryptIt.R;
import com.zsm.log.Log;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

public class SecurityFragmentActivity extends ProtectedActivity {

	public static final String DATA_FRAGMENT_CLASS = "FRAGMENT_CLASS";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent data = getIntent();
		
		String className = data.getStringExtra( DATA_FRAGMENT_CLASS );
		Fragment fragment;
		try {
			Class<?> clz = Class.forName(className);
			fragment = (Fragment) clz.newInstance();
		} catch ( ClassNotFoundException | InstantiationException
				 | IllegalAccessException e) {
			
			Log.e( e, "Failed to init the fragment: ", className );
			return;
		}
		
		setContentView( R.layout.fragment_activity );
		
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager
				.beginTransaction()
				.replace(R.id.container, fragment )
				.commit();
	}

	@Override
	protected boolean needPromptPassword() {
		return true;
	}

}
