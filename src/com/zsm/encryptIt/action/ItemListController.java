package com.zsm.encryptIt.action;

import java.io.IOException;
import java.sql.RowId;
import java.util.Date;

import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.encryptIt.backup.BackupInputAgent;
import com.zsm.log.Log;
import com.zsm.persistence.BadPersistenceFormatException;
import com.zsm.recordstore.AbstractRawCursor;

/**
 * Combine the View( {@link ItemList} ) and the Module( {@link ItemStorageAdapter} ).
 * 
 * @author zsm
 *
 */
public class ItemListController {

	private ItemList list;
	private ItemStorageAdapter adapter;

	/**
	 * Construct the Action, NOT including open the persistence in which
	 * the data stored and filling the list.
	 * 
	 * @param list in which the items are stored in the memory. It may be the model
	 * 				for the view to display the items in MVC architecture.
	 * @param adapter by which the items are stored to and read from a involatile
	 * 			storage. It may be adapter of a persistence or a content provider.
	 * @return true, initialized successfully; false, otherwise
	 */
	public ItemListController(ItemList list, ItemStorageAdapter adapter) {
		this.list = list;
		this.adapter = adapter;
	}

	/**
	 * Clear the current contents in the list view and fill it with all the items
	 * from the storage adapter.
	 */
	public void initList() {
		AbstractRawCursor cursor = adapter.query();
		initList( cursor );
	}

	/**
	 * Clear the current contents in the list view and fill it with all the items
	 * from the storage adapter with the cursor.
	 * 
	 * @param cursor by which to iterate all the items
	 */
	public void initList( AbstractRawCursor cursor ) {
		list.clear();
		
		boolean hasItem = false;
		
		try {
			hasItem = cursor.moveToFirst();
		} catch( Exception e ) {
			Log.e( e, "Initialize the list failed!" );
		}
		
		while( hasItem ) {
			try {
				WhatToDoItem item = adapter.read( cursor );
				item.setContext( cursor.currentId() );
				list.addItem( item );
			} catch (ClassNotFoundException | IOException
					 | BadPersistenceFormatException e) {
				
				Log.e( e, "Failed to read the item!" );
			}
			hasItem = cursor.moveToNext();
		}
	}

	public void clearList() {
		list.clear();
	}

	/**
	 * Add a new item to the list. The list could be the list of the UI
	 * in which all the items displayed. It is the invoker's response to notify
	 * the UI to update after this method.
	 * 
	 * @param task new task string. If the task string is empty or it equals to
	 * 					the original item, no adding happen
	 * @return true, added successfully; false, not added
	 */
	public boolean doAdd(String task) {
		if( !task.trim().equals( "" ) ) {
			WhatToDoItem item = new WhatToDoItem( task );
			doAdd( item );
			return true;
		}
		return false;
	}

	/**
	 * Add a new item to the list. The list could be the list of the UI
	 * in which all the items displayed. It is the invoker's response to notify
	 * the UI to update after this method.
	 * 
	 * @param item new WhatToDoItem item
	 * @return true, added successfully; false, not added
	 */
	public boolean doAdd(WhatToDoItem item) {
		try {
			RowId id = adapter.add(item);
			item.setContext(id);
			Log.d( "New item has just be added.", item );
		} catch (IOException e) {
			// Just log this event, and do not change the list
			Log.e(e, "Add new task failed!" );
			return false;
		}
		list.addItem( item );
		return true;
	}

	/**
	 * Edit a specified item in the list. The list could be the list of the UI
	 * in which all the items displayed. It is the invoker's response to notify
	 * the UI to update after this method.
	 * 
	 * @param position position in the list. In this method, the position will
	 * 					be checked.
	 * @param task new task string. If the task string is empty or it equals to
	 * 					the original item, no editing happen
	 * @param detail detail of the new task
	 * @return true, modified successfully; false, not modified
	 */
	public boolean doEdit( int position, String task, String detail ) {
		if( position < 0 ) {
			Log.e( "Do not know which item to be edited" );
			return false;
		}
		
		WhatToDoItem item = list.getItem( position );
		String oldTask = item.getTask();
		String oldDetail = item.getDetail();
		Date oldModify = item.getModifiedTime();
		
		if( task.equals( "" )			// Cannot be empty string
			|| sameTask(item, task, detail ) ) {
			
			Log.d( "Nothing changed, considered as Cancel." );
			return false;
		}
		
		item.setTask(task);
		item.setDetail(detail);
		item.changeModifiedTimeToCurrent();
		try {
			adapter.update( (RowId)item.getContext(), item );
		} catch (IOException e) {
			// Just log, and leave the item unchanged.
			Log.e(e, "Unable to change the task" );
			// Change the data in the list back
			item.setTask(oldTask);
			item.setDetail(oldDetail);
			item.setModifiedTime( oldModify );
			return false;
		}
		
		return true;
	}

	private boolean sameTask(WhatToDoItem item, String task, String detail) {
		return item.getTask().equals( task ) 
				&& ( item.getDetail() == detail
				|| ( detail != null && detail.equals( item.getDetail() ) ) ) ;
	}

	/**
	 * Delete the item in the list. The list could be the list of the UI to
	 * display all the items. It is the invoker's response to notify
	 * the UI to update after this method.
	 * 
	 * @param position position in the list to delete. In this method,
	 * 					the position will be checked.
	 * @return true, delete successfully, false, not deleted.
	 */
	public boolean doDelete( int position ) {
		Log.d( "Position to be deleted. ", "position", position );
		if( position == -1 ) {
			Log.e( "Do not know which item to be delete" );
			return false;
		}
		WhatToDoItem item = list.getItem( position );
		
		return doDelete(item);
	}

	/**
	 * Delete the item in the list. The list could be the list of the UI to
	 * display all the items. It is the invoker's response to notify
	 * the UI to update after this method.
	 * 
	 * @param item the item to be deleted.
	 * @return true, delete successfully, false, no such item or delete failed.
	 */
	public boolean doDelete(WhatToDoItem item) {
		try {
			adapter.remove( (RowId)item.getContext() );
		} catch (Exception e) {
			// Just log, and leave the item unchanged.
			Log.e(e, "Unable to delete the item.", item );
			return false;
		}
		return list.removeItem( item );
	}

	public void closeStorageAdapter() {
		if( adapter != null ) {
			adapter.close();
			adapter = null;
		}
	}
	
	public BackupInputAgent getBackupInputAgent() {
		return adapter.getBackupInputAgent();
	}
}
