package com.zsm.encryptIt.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class WhatToDoItemAdapter extends ArrayAdapter<WhatToDoListViewItem> {

	private ModeKeeper modeKeeper;

	public WhatToDoItemAdapter(Context context, int resource,
							   List<WhatToDoListViewItem> objects ) {
		
		super(context, resource, objects);
	}

	public void setModeKeeper(ModeKeeper mk) {
		modeKeeper = mk;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ToDoListItemView view;
		if( convertView == null ) {
			view = new ToDoListItemView( getContext(), modeKeeper );
		} else {
			view = (ToDoListItemView)convertView;
		}

		view.setDisplayValue( getItem( position ), position );
		return view;
	}

}
