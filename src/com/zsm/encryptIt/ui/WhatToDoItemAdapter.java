package com.zsm.encryptIt.ui;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zsm.encryptIt.R;
import com.zsm.recordstore.LongRowId;

public class WhatToDoItemAdapter extends BaseAdapter {

	private ModeKeeper modeKeeper;
	private Context context;
	private int resource;
	private List<WhatToDoListViewItem> list;

	public WhatToDoItemAdapter(Context context, int resource ) {
		
		this.context = context;
		this.resource = resource;
	}
	
	public void setDataList( List<WhatToDoListViewItem> list ) {
		this.list = list;
	}

	public void setModeKeeper(ModeKeeper mk) {
		modeKeeper = mk;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ToDoListItemView view;
		if( convertView == null ) {
			view = new ToDoListItemView( context, resource, modeKeeper );
		} else {
			view = (ToDoListItemView)convertView;
		}

		if( position % 2 == 0 ) {
			view.setBackgroundResource( R.drawable.item_view_even );
		} else {
			view.setBackgroundResource( R.drawable.item_view_odd );
		}
		view.setDisplayValue( getItem( position ), position );
		return view;
	}

	@Override
	public int getCount() {
		if( list == null ) {
			return 0;
		}
		return list.size();
	}

	@Override
	public WhatToDoListViewItem getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return ((LongRowId)(getItem(position).getData().getContext())).getLongId();
	}

}
