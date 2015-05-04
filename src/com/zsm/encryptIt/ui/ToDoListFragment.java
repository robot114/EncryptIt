package com.zsm.encryptIt.ui;

import java.util.List;

import android.app.ListFragment;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsm.encryptIt.R;

public class ToDoListFragment extends ListFragment implements FragmentAdapter {

	private WhatToDoItemAdapter adapter;
	
	private View view = null;

	@Override
	public void setDataListToAdapter(List<WhatToDoListViewItem> list) {
		adapter.setDataList(list);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		if( view == null ) {
			view
				= inflater.inflate( R.layout.todo_list_fragment, 
									container, false );
			
			adapter
				= new WhatToDoItemAdapter( getActivity(),
										   R.layout.todo_list_item );
			setListAdapter( adapter );
		}
		return view;
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
	public void unregisterListDataSetObserver( DataSetObserver observer) {
		adapter.unregisterDataSetObserver(observer);
	}

	@Override
	public void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}

}
