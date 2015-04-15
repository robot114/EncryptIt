package com.zsm.encryptIt.ui;


public interface ModeKeeper {
	public enum MODE{ BROWSE, EDIT };

	MODE getMode();

	void switchTo(MODE edit);

}
