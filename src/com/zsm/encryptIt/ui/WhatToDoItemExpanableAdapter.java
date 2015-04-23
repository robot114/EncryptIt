package com.zsm.encryptIt.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.recordstore.LongRowId;

class WhatToDoItemExpanableAdapter extends BaseExpandableListAdapter {

	private List<WhatToDoListViewItem> list;
	private ModeKeeper modeKeeper;
	private Context context;

	WhatToDoItemExpanableAdapter( Context context, List<WhatToDoListViewItem> list ) {
		this.context = context;
		this.list = list;
	}
	
	public void setModeKeeper(ModeKeeper mk) {
		modeKeeper = mk;
	}

	@Override
	public int getGroupCount() {
		return list.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	private WhatToDoItem getData(int groupPosition) {
		return list.get(groupPosition).getData();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return list.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return getData(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return ((LongRowId)(getData(groupPosition).getContext())).getLongId();
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {
		
		ToDoListItemView view;
		if( convertView == null ) {
			view = new ToDoListItemView( context, modeKeeper, false );
		} else {
			view = (ToDoListItemView)convertView;
		}

		view.setDisplayValue( list.get( groupPosition ), groupPosition );
		return view;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
							 boolean isLastChild, View convertView,
							 ViewGroup parent) {
		
		ToDoListSubItemView view;
		if( convertView == null ) {
			view = new ToDoListSubItemView( context );
		} else {
			view = (ToDoListSubItemView)convertView;
		}

		view.setDisplayValue( list.get( groupPosition ) );
		
		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

}
