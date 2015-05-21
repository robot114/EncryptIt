package com.zsm.encryptIt.ui.preferences;

import com.zsm.encryptIt.R;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class AboutDialog extends DialogPreference {

	public AboutDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
		setDialogLayoutResource( R.layout.about );
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		
		builder
		.setPositiveButton( null, null )
		.setNegativeButton( android.R.string.ok, null );
	}

}
