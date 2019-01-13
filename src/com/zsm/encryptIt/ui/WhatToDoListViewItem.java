package com.zsm.encryptIt.ui;

import java.util.Observable;

import com.zsm.encryptIt.WhatToDoItemV2;

public class WhatToDoListViewItem extends Observable {
	
	private WhatToDoItemV2 data;
	private boolean selected;
	
	public WhatToDoListViewItem( WhatToDoItemV2 data ) {
		this.data = data;
	}
	
	public WhatToDoItemV2 getData() {
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

	public void setSelected(boolean selected, boolean notifyChanged) {
		if( notifyChanged ) {
			setSelected( selected );
		} else {
			this.selected = selected;
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