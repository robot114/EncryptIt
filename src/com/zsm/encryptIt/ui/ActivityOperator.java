package com.zsm.encryptIt.ui;

import android.app.Activity;
import android.content.Intent;

public interface ActivityOperator {

    /**
     * Call {@link Activity#startActivityForResult(Intent, int)} from the operator.
     * The operator may be an activity or a fragment.
     * 
     * @see {@link Activity#startActivityForResult(Intent, int)}
	 */
	void startActivityForResult(Intent intent, int requestCode);
	
    /**
     * Call {@link Activity#finishAffinity()} from the operator.
     * The operator may be an activity or a fragment. For a fragment,
     * the invocation will be from the activity it attached.
     * 
     * @see {@link Activity#finishAffinity()}
	 */
	void finishAffinity();

}
