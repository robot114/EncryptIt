package com.zsm.encryptIt.ui;

import java.util.List;

import android.app.Fragment;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.zsm.encryptIt.R;

public class ToDoListExpandableFragment extends Fragment implements ListFragmentAdapter {

	private WhatToDoItemExpanableAdapter adapter;
	
	private View view = null;

	private ExpandableListView listView;

	@Override
	public void setDataListToAdapter(List<WhatToDoListViewItem> list) {
		adapter.setDataList( list );
	    listView.setAdapter(adapter);
	    listView.setGroupIndicator(null);
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
		listView = (ExpandableListView) view.findViewById(R.id.expandableTodoList);
	    
		adapter = new WhatToDoItemExpanableAdapter( getActivity(), listView );
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
