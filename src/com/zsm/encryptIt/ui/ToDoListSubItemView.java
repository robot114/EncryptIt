package com.zsm.encryptIt.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.log.Log;

class ToDoListSubItemView extends LinearLayout {

	private TextView detailView;
	private DetailTimeLayout timeView;
	private View separator;

	public ToDoListSubItemView(Context context) {
		super(context);
		init();
	}

	private void init() {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li
			= (LayoutInflater)getContext().getSystemService( infService );
		li.inflate( R.layout.todo_list_exapanable_subitem, this, true );
		
		detailView = (TextView)findViewById( R.id.expanableSubItemDetail );
		timeView = (DetailTimeLayout)findViewById(R.id.expandableSubItemDetailTime);
		separator = findViewById( R.id.expandableSubItemSeparator );
	}
	
	public void setDisplayValue( WhatToDoListViewItem item ) {
		WhatToDoItem data = item.getData();
		if( hasDetail( item ) ) {
			detailView.setVisibility( View.VISIBLE );
			separator.setVisibility(View.VISIBLE);
			detailView.setText( data.getDetail() );
		} else {
			separator.setVisibility( View.GONE );
			detailView.setVisibility( View.GONE );
		}
		
		timeView.setCreateTime( data.getCreatedTime() );
		timeView.setModifyTime( data.getModifiedTime() );
	}

	private boolean hasDetail(WhatToDoListViewItem item) {
		String detail = item.getData().getDetail();
		return ( detail != null && detail.length() > 0 );
	}

}
