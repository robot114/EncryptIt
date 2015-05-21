package com.zsm.encryptIt.ui;

import java.util.List;

import android.database.DataSetObserver;

public interface ListFragmentAdapter {

	public abstract void notifyDataSetChanged();

	public abstract void registerListDataSetObserver(DataSetObserver observer);

	public abstract void setModeKeeper(ModeKeeper mk);

	public void setDataListToAdapter(List<WhatToDoListViewItem> list);

	public abstract void unregisterListDataSetObserver( DataSetObserver observer );

}
