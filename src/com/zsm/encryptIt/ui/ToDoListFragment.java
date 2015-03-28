package com.zsm.encryptIt.ui;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Locale;

import javax.crypto.NoSuchPaddingException;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.action.ItemList;
import com.zsm.encryptIt.action.ItemListActor;
import com.zsm.encryptIt.action.ItemStorageAdapter;
import com.zsm.encryptIt.action.PersistenceStorageAdapter;
import com.zsm.encryptIt.android.AndroidPersistence;
import com.zsm.encryptIt.android.AndroidWrappedRawCursor;
import com.zsm.encryptIt.android.EncryptItContentProvider;
import com.zsm.encryptIt.android.ProviderStorageAdapter;
import com.zsm.encryptIt.android.RawWrappedAndroidCursor;
import com.zsm.encryptIt.app.EncryptItApplication;
import com.zsm.log.Log;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.persistence.Persistence;
import com.zsm.recordstore.AbstractRawCursor;

public class ToDoListFragment extends ListFragment
				implements ItemList, LoaderCallbacks<Cursor> {

	private ItemStorageAdapter storageAdapter;
	private ArrayList<WhatToDoItem> todoList;
	
	private ArrayList<WhatToDoItem> showList;
	private ArrayAdapter<WhatToDoItem> adapter;
	private String filtString = "";
	
	private View view = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		Log.d( "onCreateView", getActivity(), view );
		
		if( view == null ) {
			view = inflater.inflate( R.layout.todo_list_fragment, container, false );
			
			todoList = new ArrayList<WhatToDoItem>();
			showList = new ArrayList<WhatToDoItem>();
			
			setListAdapter( showList );
		}
		return view;
	}
	
	private void setListAdapter( ArrayList<WhatToDoItem> list) {
		adapter
			= new WhatToDoItemAdapter(
						getActivity(),
						R.layout.todo_list_item,
						list );

		setListAdapter( adapter );
	}

	public void addItem(final WhatToDoItem newItem) {
		todoList.add( newItem );
		if( shouldInShowList(newItem) ) {
			showList.add( newItem );
		}
	}

	public WhatToDoItem getItem(int position) {
		return adapter.getItem(position);
	}

	public void clear() {
		todoList.clear();
		showList.clear();
	}

	public void removeItem(final WhatToDoItem item) {
		todoList.remove( item );
		showList.remove( item );
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader( getActivity().getApplicationContext(),
								 EncryptItContentProvider.getContentUri(),
								 null, null, null, null );
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Cursor c = cursor;
		if( cursor instanceof CursorWrapper ) {
			c = ((CursorWrapper)cursor).getWrappedCursor();
		}
		
		AbstractRawCursor rrsc;
		if( c instanceof AndroidWrappedRawCursor ) {
			rrsc = ((AndroidWrappedRawCursor)c).getInnerCursor();
		} else {
			rrsc = new RawWrappedAndroidCursor( c );
		}
		getApp().getItemListActor().initList( rrsc );
		refilter();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if( storageAdapter != null ) {
			storageAdapter.close();
			storageAdapter = null;
		}
	}

	public boolean initList( Key key, Handler handler ) {
		getApp().setItemListActor( new ItemListActor() );
//		return forContentProvider( key, handler );
		return forAndroidPersistence(key, handler);
	}
	
	private boolean forContentProvider( Key key, Handler handler ) {
		EncryptItApplication context = getApp();
	
		try {
			context.initEncryptSetting(context, key);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException e) {

			Log.d( e, "Initialize encrypt setting failed!" );
		}
		
		storageAdapter = new ProviderStorageAdapter( context );
		if( !getApp().getItemListActor().initialize(this, storageAdapter) ) {
			return false;
		}
		handler.post( new Runnable() {
			@Override
			public void run() {
				getLoaderManager().initLoader(MainActivity.ENCRYPT_IT_ID, null,
											  ToDoListFragment.this);
			}
		});
		// For the content provider, it does not need to initialize the list
		// here. Because the list will be filled when the cursor loader finishes
		// loading, the list will be initialized.
		
		return true;
	}

	private EncryptItApplication getApp() {
		return (EncryptItApplication)getActivity().getApplicationContext();
	}

	private boolean forAndroidPersistence(Key key, Handler handler) {
		EncryptItApplication context
			= getApp();

		AndroidPersistence persistence = null;
		try {
			persistence = new AndroidPersistence( context, key );
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException e) {
			
			Log.e( e, "Cannot initialize the persistence!" );
			return false;
		}
		if( !openPersistence( persistence ) ) {
			return false;
		}
		
		storageAdapter = new PersistenceStorageAdapter( persistence );
		if( !getApp().getItemListActor().initialize( this, storageAdapter ) ) {
			// Initialize failed, quit.
			return false;
		}
		
		handler.post( new Runnable() {
			@Override
			public void run() {
				getApp().getItemListActor().initList();
				notifyDataSetChanged();
			}
		});
		
		return true;
	}

	private boolean openPersistence(Persistence persistence ) {
		try {
			persistence.open();
		} catch (BadPersistenceFormatException e) {
			Log.d( e, "This should not happen!" );
		} catch (IOException e) {
			Log.e( e, "Cannot read from or write to the persistence!" );
			return false;
		}
		
		return true;

	}

	public boolean doAdd(String string) {
		final boolean res = getApp().getItemListActor().doAdd(string);
		if( res ) {
			filter( "" );
		}
		return res;
	}

	public boolean doEdit(int position, String task, String detail ) {
		boolean res = getApp().getItemListActor().doEdit(position, task, detail);
		if( res ) {
			refilter();
		}
		return res;
	}

	public boolean doDelete(int position) {
		boolean res = getApp().getItemListActor().doDelete(position);
		if( res ) {
			refilter();
		}
		return res;
	}

	private void refilter() {
		if( filtString != null && filtString.length() > 0 ) {
			filter( filtString );
		}
		notifyDataSetChanged();
	}

	public void filter(@NonNull CharSequence s) {
		Log.d( "Filt string when typing: ", s, "total size",
					todoList.size() );
		showList.clear();
		if( s == null || s.length() == 0 ) {
			showList.addAll(todoList);
			filtString = "";
		} else {
	        filtString = s.toString().toLowerCase( Locale.getDefault() );
	        int count = todoList.size();
	        
			for (int i = 0; i < count ; i++) {
	            final WhatToDoItem item = todoList.get(i);
	            if (shouldInShowList(item)) {
	                showList.add(item);
	            }
			}
		}
		notifyDataSetChanged();
	}
	
	private void notifyDataSetChanged() {
		adapter.notifyDataSetChanged();
	}

	private boolean shouldInShowList( WhatToDoItem item ) {
        final String text
	    	= item.getTask().toString().toLowerCase(Locale.getDefault());
	
	    return text.contains(filtString);
	}
}
