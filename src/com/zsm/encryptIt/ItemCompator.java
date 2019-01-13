package com.zsm.encryptIt;

import com.zsm.persistence.BadPersistenceFormatException;

public class ItemCompator {

	private ItemCompator() {
	}
	
	public static WhatToDoItemV2 toLastVersionItem( Object obj )
									throws BadPersistenceFormatException {
		
		if( !( obj instanceof WhatToDoItem ) ) {
			throw new BadPersistenceFormatException( "Invalid data class: " + obj.getClass() );
		}
		WhatToDoItem tempItem = (WhatToDoItem)obj;
		long serialVersionUID = tempItem.getSerialVersionUID();
		
		WhatToDoItemV2 item = null;

		if( serialVersionUID == WhatToDoItem.serialVersionUID ) {
			item = new WhatToDoItemV2( tempItem );
		} else if( serialVersionUID == WhatToDoItemV2.serialVersionUID ) {
			item = (WhatToDoItemV2)obj;
		} else {
//			Log.w( "Bad persistence data serialVersionUID. ", 
//					"serialVersionUID", serialVersionUID );
//			WhatToDoItem tempItem = (WhatToDoItem)obj;
//			item = new WhatToDoItemV2( tempItem );
			throw new BadPersistenceFormatException(
					"Invalid serialVersionUID: " + serialVersionUID );
		}
		
		if( !item.isValid() ) {
			throw new BadPersistenceFormatException( "Invalid item data: " + item );
		}
		return item;
		
	}
}
