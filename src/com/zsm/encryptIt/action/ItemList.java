package com.zsm.encryptIt.action;

import com.zsm.encryptIt.WhatToDoItemV2;

/**
 * Interface of the list for the ItemListActor to communicate with the UI.
 * The methods of this interface will just affect the data of the UI, not the UI
 * itself.
 * 
 * @author zsm
 *
 */
public interface ItemList {

	WhatToDoItemV2 getItem(int position);

	void clear();

	boolean removeItem(WhatToDoItemV2 item);

	void addItemToView(WhatToDoItemV2 item);

	void refilter();

	void notifyDataSetChanged();
}
