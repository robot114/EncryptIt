package com.zsm.encryptIt.action;

import com.zsm.encryptIt.WhatToDoItem;

/**
 * Interface of the list for the ItemListActor to communicate with the UI.
 * The methods of this interface will just affect the data of the UI, not the UI
 * itself.
 * 
 * @author zsm
 *
 */
public interface ItemList {

	WhatToDoItem getItem(int position);

	void clear();

	void removeItem(WhatToDoItem item);

	void addItem(WhatToDoItem item);

	void refilter();

	void notifyDataSetChanged();
}
