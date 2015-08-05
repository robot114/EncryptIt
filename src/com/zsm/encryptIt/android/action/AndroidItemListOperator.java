package com.zsm.encryptIt.android.action;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javax.crypto.NoSuchPaddingException;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

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
import com.zsm.encryptIt.ui.ListFragmentAdapter;
import com.zsm.encryptIt.ui.MainActivity;
import com.zsm.encryptIt.ui.WhatToDoListViewItem;
import com.zsm.log.Log;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.persistence.Persistence;
import com.zsm.recordstore.AbstractRawCursor;
import com.zsm.util.FilterableList;
import com.zsm.util.Matcher;

public class AndroidItemListOperator
				implements ItemList, LoaderCallbacks<Cursor> {

	final private FilterableList<WhatToDoListViewItem, String> list;
	private String filtString = "";
	
	private Context context;
	private LoaderManager loaderManager;

	private ItemStorageAdapter storageAdapter;
	
	private int selectedCount;
	private SelectObserver selectionObserver;
	private ListFragmentAdapter fragmentAdapter;
	
	public AndroidItemListOperator( Context context, LoaderManager lm,
									ListFragmentAdapter adapter ) {
		
		list
			= new FilterableList<WhatToDoListViewItem, String>(
					new StringMatcher() );
		
		this.context = context;
		loaderManager = lm;
		selectionObserver = new SelectObserver();
		fragmentAdapter = adapter;
		fragmentAdapter.setDataListToAdapter(list);
	}
	
	public void closeStorage() {
		if( storageAdapter != null ) {
			storageAdapter.close();
			storageAdapter = null;
		}
	}
	
	public boolean initList( Key key, Handler handler ) {
		getApp().setItemListActor( new ItemListActor() );
		return forContentProvider( key, handler );
//		return forAndroidPersistence(key, handler);
	}
	
	private boolean forContentProvider( Key key, Handler handler ) {
		EncryptItApplication app = getApp();
	
		try {
			app.initEncryptSetting(app, key);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidKeySpecException e) {

			Log.d( e, "Initialize encrypt setting failed!" );
		}
		
		storageAdapter = new ProviderStorageAdapter( app );
		if( !getApp().getItemListActor().initialize(this, storageAdapter) ) {
			return false;
		}
		handler.post( new Runnable() {
			@Override
			public void run() {
				loaderManager.initLoader(MainActivity.ENCRYPT_IT_ID, null,
										 AndroidItemListOperator.this);
			}
		});
		// For the content provider, it does not need to initialize the list
		// here. Because the list will be filled when the cursor loader finishes
		// loading, the list will be initialized.
		
		return true;
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader( context,
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
		selectedCount = 0;
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

	private EncryptItApplication getApp() {
		return (EncryptItApplication)context;
	}

	@Override
	public void addItem(final WhatToDoItem newItem) {
		WhatToDoListViewItem newListViewItem
			= new WhatToDoListViewItem( newItem );
		newListViewItem.addObserver(selectionObserver);
		
		list.add( newListViewItem );
	}

	@Override
	public WhatToDoItem getItem(int position) {
		return list.get(position).getData();
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean removeItem(final WhatToDoItem item) {
		return list.remove( new WhatToDoListViewItem( item ) );
	}

	@Override
	public void refilter() {
		if( filtString != null && filtString.length() > 0 ) {
			filter( filtString );
		}
		notifyDataSetChanged();
	}

	public void filter(@NonNull CharSequence s) {
		Log.d( "Filt string when typing: ", s, "total size",
					list.totalSize() );
		list.filter( s.toString() );
		notifyDataSetChanged();
	}
	
	@Override
	public void notifyDataSetChanged() {
		fragmentAdapter.notifyDataSetChanged();
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
	
	public boolean doDeleteSelected() {
		List<WhatToDoListViewItem> sl = getSelectedDataList();
		ItemListActor actor = getApp().getItemListActor();
		boolean res = false;
		for( WhatToDoListViewItem item : sl ) {
			res |= actor.doDelete(item.getData());
		}
		
		if( res ) {
			refilter();
		}
		return res;
	}

	public void selectAll( boolean select ) {
		for( WhatToDoListViewItem item : list ) {
			item.setSelected( select );
		}
		selectedCount = select ? list.size() : 0;
		notifyDataSetChanged();
	}

	public int getSelectedCount() {
		return selectedCount;
	}

	public int getShownCount() {
		return list.size();
	}
	
	public void selectReverse() {
		for( WhatToDoListViewItem item : list ) {
			item.setSelected( !item.isSelected() );
		}
		notifyDataSetChanged();
	}

	public List<WhatToDoListViewItem> getDataList() {
		return list;
	}
	
	public List<WhatToDoListViewItem> getSelectedDataList() {
		List<WhatToDoListViewItem> sl = new ArrayList<WhatToDoListViewItem>();
		
		for( WhatToDoListViewItem item : list ) {
			if( item.isSelected() ) {
				sl.add(item);
			}
		}
		return sl;
	}

	private final class SelectObserver implements Observer {
		public void update( Observable obs, Object obj ) {
			if( (Boolean)obj ) {
				selectedCount++;
			} else {
				selectedCount--;
			}
			notifyDataSetChanged();
		}
	}

	private final class StringMatcher 
		implements Matcher<WhatToDoListViewItem, String> {
	
		@Override
		public boolean match(WhatToDoListViewItem e, String transferedCondition) {
	        final String text
		    	= e.getData().getTask().toString()
		    		.toLowerCase(Locale.getDefault());
		
		    return text.contains(transferedCondition);
		}
	
		@Override
		public boolean matchAll(String transferedCondition) {
			return (transferedCondition == null
					|| transferedCondition.length() == 0);
		}
	
		@Override
		public String transferCondition(String originalCondition) {
			if( originalCondition == null || originalCondition.length() == 0 ) {
				return "";
			}
			return originalCondition.toLowerCase( Locale.getDefault() );
		}

		@Override
		public void afterMatched(WhatToDoListViewItem e, boolean match) {
			if( !match ) {
				e.setSelected(false);
				e.notifyObservers(false);
			}
		}
		
	}
}
