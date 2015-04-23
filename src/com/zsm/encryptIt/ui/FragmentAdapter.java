package com.zsm.encryptIt.ui;

import com.zsm.encryptIt.AndroidItemListOperator;

import android.database.DataSetObserver;

public interface FragmentAdapter {

	public abstract void notifyDataSetChanged();

	public abstract void registerListDataSetObserver(DataSetObserver observer);

	public abstract void setModeKeeper(ModeKeeper mk);

	public abstract void setListOperator(AndroidItemListOperator operator);

}
