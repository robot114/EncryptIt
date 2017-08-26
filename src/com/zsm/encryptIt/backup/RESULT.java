package com.zsm.encryptIt.backup;

import android.content.Context;

import com.zsm.encryptIt.R;

enum RESULT { OK, FAILED, CANCELLED;

	String getString( Context context ) {
		int resId;
		switch( this ) {
			case FAILED:
				resId = R.string.failed;
				break;
			case CANCELLED:
				resId = R.string.cancelled;
				break;
			case OK:
			default:
				resId = R.string.succeed;
				break;
		}
		return context.getString(resId);
	}
}