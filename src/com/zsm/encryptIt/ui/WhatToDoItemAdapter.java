package com.zsm.encryptIt.ui;

import java.util.List;

import com.zsm.encryptIt.WhatToDoItem;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class WhatToDoItemAdapter extends ArrayAdapter<WhatToDoItem> {

	public WhatToDoItemAdapter(Context context, int resource,
							   List<WhatToDoItem> objects) {
		
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ToDoListItemView view;
		if( convertView == null ) {
			view = new ToDoListItemView( getContext() );
		} else {
			view = (ToDoListItemView)convertView;
		}

		view.setDisplayValue( getItem( position ), position );
		return view;
	}

}
