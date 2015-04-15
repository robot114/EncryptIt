package com.zsm.encryptIt.ui;

import java.util.Observable;
import java.util.Observer;

import com.zsm.encryptIt.WhatToDoItem;

class WhatToDoListViewItem extends Observable {
	
	private WhatToDoItem data;
	private boolean selected;
	
	public WhatToDoListViewItem( WhatToDoItem data, Observer selectionObserver ) {
		this.data = data;
		addObserver(selectionObserver);
	}
	
	public WhatToDoItem getData() {
		return data;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		if( this.selected != selected ) {
			setChanged();
			this.selected = selected;
			notifyObservers( selected );
		}
	}
	
}