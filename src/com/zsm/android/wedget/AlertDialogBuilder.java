package com.zsm.android.wedget;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

/**
 * Exchange the positions of the positive button and the negative button.
 * That means the positive will be on the left side.
 * 
 * @author zsm
 *
 */
public class AlertDialogBuilder extends Builder {

	public AlertDialogBuilder(Context context, int theme) {
		super(context, theme);
	}

	public AlertDialogBuilder(Context context) {
		super(context);
	}

	@Override
    public Builder setPositiveButton(int textId, final OnClickListener listener) {
        return super.setNegativeButton(textId, listener);
    }
    
	@Override
	public Builder setPositiveButton(CharSequence text, final OnClickListener listener) {
       return super.setNegativeButton(text, listener);
    }
    
	@Override
    public Builder setNegativeButton(int textId, final OnClickListener listener) {
        return super.setPositiveButton(textId, listener);
    }
    
	@Override
    public Builder setNegativeButton(CharSequence text, final OnClickListener listener) {
		return super.setPositiveButton(text, listener);
	}
}
