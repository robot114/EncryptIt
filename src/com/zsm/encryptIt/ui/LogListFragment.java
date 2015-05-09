package com.zsm.encryptIt.ui;

import java.util.Locale;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zsm.encryptIt.R;

public class LogListFragment extends ListFragment {

	private ArrayAdapter<String> adapter;
	private int currentPosition;
	private int searchOffset;
	private AlertDialog forwardFromBeginDlg;
	private AlertDialog searchNoFoundDlg;
	private AlertDialog backwardFromBeginDlg;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    adapter
	    	= new ArrayAdapter<String>(getActivity(),
	    							   android.R.layout.simple_list_item_1);
	    setListAdapter(adapter);
		ListView listView = getListView();
		listView.setSelected(true);
		listView.setBackgroundResource( R.color.logBackground );
		listView.setOnScrollListener( new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
								int visibleItemCount, int totalItemCount) {
				changeItemStyleAt(currentPosition, firstVisibleItem, visibleItemCount);
			}
		} );
		
		listView.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				
				currentPosition = position;
				final int first = getListView().getFirstVisiblePosition();
				final int count = getListView().getLastVisiblePosition() - first;
				changeItemStyleAt(position, first, count);
				// searchOffset MUST be cleared after the style changed, because
				// in changeItemStyleAt, the style will not take effect when 
				// searchOffset is 0
				searchOffset = 0;
			}
		} );
	    currentPosition = 0;
	    searchOffset = 0;
	}
	
	void add( String log ) {
		adapter.add(log);
	}

	public void clear() {
		adapter.clear();
	}
	
	public void searchForward( final String str ) {
		int count = adapter.getCount();
		if( count == 0 ) {
			return;
		}
		String lowerCase = str.toLowerCase(Locale.getDefault());
		int start = currentPosition + searchOffset;
		searchForward( lowerCase, start , count );
	}

	public void searchBackward( final String str ) {
		if( adapter.getCount() == 0 ) {
			return;
		}
		String lowerCase = str.toLowerCase(Locale.getDefault());
		int start = currentPosition - searchOffset;
		searchBackward( lowerCase, start , -1 );
	}

	private void searchFound(int position) {
		currentPosition = position;
		searchOffset = 1;
		final ListView listView = getListView();
		listView.setSelection( position );
	}

	private AlertDialog createSearchAroundDlg( final String str, int messageId,
											   final boolean forward ) {
		return new AlertDialog.Builder( getActivity() )
			.setMessage(messageId  )
			.setPositiveButton(android.R.string.yes, 
							   new OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog,
									 int which) {
					if( forward ) {
						searchForward( str, 0, currentPosition );
					} else {
						searchBackward( str,
										adapter.getCount() - 1,
										currentPosition );
					}
				}} )
			.setNegativeButton( android.R.string.no, null )
			.setCancelable( false )
			.create();
	}

	private AlertDialog searchNoFoundDlg() {
		if( searchNoFoundDlg == null ) {
			searchNoFoundDlg = new AlertDialog.Builder( getActivity() )
			.setMessage( R.string.searchLogNoFound )
			.setPositiveButton( android.R.string.ok, null )
			.setCancelable( false )
			.create();
		}
		return searchNoFoundDlg;
	}
	
	private void searchForward( String str, int start, int end ) {
		int i;
		for( i = start; i < end && !match(str, i); i++ ) {
		}
		if( i == end ) {
			if( start > 0 ) {
				if( forwardFromBeginDlg == null ) {
					forwardFromBeginDlg
						= createSearchAroundDlg(str, 
												R.string.searchLogToEnd,
												true);
				}
				forwardFromBeginDlg.show();
			} else {
				searchNoFoundDlg().show();
			}
		} else {
			searchFound(i);
		}
	}
	
	private boolean match(String ls, int position) {
		return adapter.getItem(position)
				.toLowerCase(Locale.getDefault()).contains(ls);
	}

	private void searchBackward( String str, int start, int end ) {
		int i;
		for( i = start; i > end && !match(str, i); i-- ) {
		}
		if( i == end ) {
			if( start < adapter.getCount() - 1 ) {
				if( backwardFromBeginDlg == null ) {
					backwardFromBeginDlg
						= createSearchAroundDlg(str, 
												R.string.searchLogToBegin,
												false);
				}
				backwardFromBeginDlg.show();
			} else {
				searchNoFoundDlg().show();
			}
		} else {
			searchFound(i);
		}
	}

	public void clearSearchOffset() {
		searchOffset = 0;
	}

	private void changeItemStyleAt(int position, int first, int count) {
		
		ListView listView = getListView();
		for( int i = 0; i < count; i++ ) {
			TextView tv = (TextView) listView.getChildAt( i );
			if( tv == null ) {
				return;
			}
			if( (i + first) == position && searchOffset > 0 ) {
				tv.setBackgroundResource( R.color.logFoundBackground );
			} else {
				tv.setBackgroundResource( R.color.logBackground );
			}
		}
	}
}
