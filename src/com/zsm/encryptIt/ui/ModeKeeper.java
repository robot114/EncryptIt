package com.zsm.encryptIt.ui;


public interface ModeKeeper {
	public enum MODE{ BROWSE, EDIT, MULTI_DETAIL };

	MODE getMode();

	void switchTo(MODE mode);

}
