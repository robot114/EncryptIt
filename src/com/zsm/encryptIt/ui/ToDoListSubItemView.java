package com.zsm.encryptIt.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;

class ToDoListSubItemView extends LinearLayout {

	private static final DateFormat TIME_FORMAT
		= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

	private TextView detailView;
	private TextView createdView;
	private TextView modifiedView;

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
		createdView = (TextView)findViewById(R.id.detailCreateTime);
		modifiedView = (TextView)findViewById( R.id.detailModifyTime );
	}
	
	public void setDisplayValue( WhatToDoListViewItem item ) {
		WhatToDoItem data = item.getData();
		if( hasDetail( item ) ) {
			detailView.setVisibility( View.VISIBLE );
			detailView.setText( data.getDetail() );
		} else {
			detailView.setVisibility( View.GONE );
		}
		
		createdView.setText(TIME_FORMAT.format(data.getCreatedTime()));
		modifiedView.setText( TIME_FORMAT.format(data.getModifiedTime()));
	}

	private boolean hasDetail(WhatToDoListViewItem item) {
		String detail = item.getData().getDetail();
		return ( detail == null || detail.length() == 0 );
	}

}
