package com.zsm.encryptIt.ui;

import java.util.Observable;

import com.zsm.encryptIt.WhatToDoItem;

public class WhatToDoListViewItem extends Observable {
	
	private WhatToDoItem data;
	private boolean selected;
	
	public WhatToDoListViewItem( WhatToDoItem data ) {
		this.data = data;
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

	@Override
	public boolean equals(Object o) {
		if( o == null || !( o instanceof WhatToDoListViewItem ) ) {
			return false;
		}
		if( this == o ) {
			return true;
		}
		return data.equals(((WhatToDoListViewItem)o).data );
	}

	@Override
	public int hashCode() {
		return data.hashCode();
	}
	
}