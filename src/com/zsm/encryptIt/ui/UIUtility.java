package com.zsm.encryptIt.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;

import com.zsm.encryptIt.R;

public class UIUtility {

	static public void promptResult(Activity a, int id) {
		Resources r = a.getResources();
		
		new AlertDialog.Builder(a)
			 .setTitle(r.getString( R.string.app_name )) 
			 .setMessage(r.getString( id ))
			 .setPositiveButton(android.R.string.ok, null)
			 .show();
	}

}
