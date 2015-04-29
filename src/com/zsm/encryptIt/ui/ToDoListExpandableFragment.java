package com.zsm.encryptIt.ui;

import com.zsm.encryptIt.AndroidItemListOperator;
import com.zsm.encryptIt.R;
import com.zsm.util.FilterableList;

import android.app.Fragment;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class ToDoListExpandableFragment extends Fragment implements FragmentAdapter {

	private WhatToDoItemExpanableAdapter adapter;
	
	private View view = null;

	private AndroidItemListOperator listOperator;

	private FilterableList<WhatToDoListViewItem, String> list;


	@Override
	public void setListOperator(AndroidItemListOperator operator) {
		this.listOperator = operator;
		listOperator.setDataList( list );
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if( view == null ) {
			view
				= inflater.inflate( R.layout.todo_list_expanable_fragment, 
									container, false );
		}
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		ExpandableListView lv;
	    lv = (ExpandableListView) view.findViewById(R.id.expandableTodoList);
	    
		list = new FilterableList<WhatToDoListViewItem, String>();
		adapter = new WhatToDoItemExpanableAdapter( getActivity(), lv, list );
	    lv.setAdapter(adapter);
	    lv.setGroupIndicator(null);
	}

	@Override
	public void setModeKeeper( ModeKeeper mk ) {
		adapter.setModeKeeper( mk );
	}

	@Override
	public void registerListDataSetObserver( DataSetObserver observer) {
		adapter.registerDataSetObserver(observer);
	}

	@Override
	public void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}

}
