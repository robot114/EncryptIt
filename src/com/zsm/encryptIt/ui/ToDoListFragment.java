package com.zsm.encryptIt.ui;

import android.app.ListFragment;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zsm.encryptIt.AndroidItemListOperator;
import com.zsm.encryptIt.R;
import com.zsm.util.FilterableList;

public class ToDoListFragment extends ListFragment implements FragmentAdapter {

	private WhatToDoItemAdapter adapter;
	
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
				= inflater.inflate( R.layout.todo_list_fragment, 
									container, false );
			
			list = new FilterableList<WhatToDoListViewItem, String>();

			adapter
				= new WhatToDoItemAdapter( getActivity(),
										   R.layout.todo_list_item,
										   list );
			setListAdapter( adapter );
			
//			view
//				= inflater.inflate( R.layout.todo_list_expanable_fragment, 
//									container, false );
//			adapter = new WhatToDoItemExpanableAdapter( getActivity() );
			
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
	public void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}

}
