package com.zsm.encryptIt.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.recordstore.LongRowId;

class WhatToDoItemExpanableAdapter extends BaseExpandableListAdapter {

	private List<WhatToDoListViewItem> list;
	private ModeKeeper modeKeeper;
	private Context context;
	private ExpandableListView listView;

	WhatToDoItemExpanableAdapter( Context context, ExpandableListView lv ) {
		this.context = context;
		listView = lv;
	}
	
	public void setDataList(List<WhatToDoListViewItem> list) {
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
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, final boolean isExpanded,
							 View convertView, ViewGroup parent) {
		
		ToDoListItemView view;
		if( convertView == null ) {
			view
				= new ToDoListItemView( context,
										R.layout.expandable_item,
										modeKeeper );
			
			view.setExpandOperator( new ExpandOperator() {

				@Override
				public void expand(boolean expanded, int groupPosition) {
					if( expanded ) {
						listView.expandGroup(groupPosition);
					} else {
						listView.collapseGroup(groupPosition);
					}
				}
				
			} );
		} else {
			view = (ToDoListItemView)convertView;
		}

		if( groupPosition % 2 == 0 ) {
			view.setBackgroundResource( R.drawable.item_view_even );
		} else {
			view.setBackgroundResource( R.drawable.item_view_odd );
		}
		view.setDisplayValue( list.get( groupPosition ), groupPosition );
		view.setExpanded( isExpanded );
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

		if( groupPosition % 2 == 0 ) {
			view.setBackgroundResource( R.drawable.item_view_even );
		} else {
			view.setBackgroundResource( R.drawable.item_view_odd );
		}
		view.setDisplayValue( list.get( groupPosition ) );
		
		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

}
