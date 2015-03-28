package com.zsm.encryptIt.ui;

import java.text.DateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.log.Log;

public class ToDoListItemView extends RelativeLayout {

	private final class DetailClickListener implements OnClickListener {
		
		private int titleId;
		private boolean editable;
		private int requestCode;
		private int positiveTextId;

		private DetailClickListener( int titleId, boolean editable,
									 int requestCode, int positiveTextId ) {
			
			this.titleId = titleId;
			this.editable = editable;
			this.requestCode = requestCode;
			this.positiveTextId = positiveTextId;
		}
		
		public void onClick(View v) {
			Context context = ToDoListItemView.this.getContext();
			
			Intent intent = new Intent( context, DetailActivity.class );
			intent.putExtra( DetailActivity.KEY_ROW_ITEM, data.toByteArray() );
			intent.putExtra( DetailActivity.KEY_ROW_POSITION, position );
			intent.putExtra( DetailActivity.KEY_DETAIL_EDITABLE, editable );
			intent.putExtra( DetailActivity.KEY_DETAIL_TITLE, titleId );
			intent.putExtra( DetailActivity.KEY_DETAIL_OK, positiveTextId );
			((Activity)context).startActivityForResult( intent, requestCode );
			Log.d( "Start detail activity.", "requestCode", requestCode,
				   "intent", intent, "position", position );
		}
	}

	private Paint marginPaint;
	private Paint linePaint;
	private int paperColor;
	private float margin;
	private TextView textView;
	private TextView dateView;
	private ImageView editView;
	private ImageView deleteView;
	
	private WhatToDoItem data;
	private int position;
	
	public ToDoListItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ToDoListItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ToDoListItemView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setWillNotDraw( false );
		
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li
			= (LayoutInflater)getContext().getSystemService( infService );
		li.inflate( R.layout.todo_list_item, this, true );
		
		textView = (TextView)findViewById(R.id.row);
		dateView = (TextView)findViewById(R.id.rowDate);
		editView = (ImageView)findViewById( R.id.rowEdit );
		deleteView = (ImageView)findViewById( R.id.rowDelete );
		
		editView.setOnClickListener(
			new DetailClickListener( R.string.detail_edit, true,
									 MainActivity.SHOW_FOR_EDIT,
									 R.string.detailSaveAndBack ) );
		
		deleteView.setOnClickListener(
			new DetailClickListener( R.string.detail_delete, false,
									 MainActivity.SHOW_FOR_DELETE,
									 R.string.detail_delete ) );

		initPaints();
	}

	private void initPaints() {
		Resources res = getResources();
		
		marginPaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		marginPaint.setColor( res.getColor( R.color.notepad_margin ) );
		
		linePaint = new Paint( Paint.ANTI_ALIAS_FLAG );
		linePaint.setColor( res.getColor( R.color.notepad_line ));
		
		paperColor = res.getColor( R.color.notepad_paper );
		margin = res.getDimension( R.dimen.notepad_margin );
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(paperColor);
		canvas.drawLine(0, 0, 0, getMeasuredHeight(), linePaint );
		canvas.drawLine( 0, getMeasuredHeight(), getMeasuredWidth(),
						 getMeasuredHeight(), linePaint );
		
		canvas.drawLine( margin, 0, margin, getMeasuredHeight(), marginPaint );
		
		canvas.save();
		canvas.translate( margin, 0 );
		
		super.onDraw(canvas);
		
		canvas.restore();
	}

	public void setDisplayValue( WhatToDoItem item, int position ) {
		Date createdTime = item.getCreatedTime();
		String createdStr
			= DateFormat.getDateInstance( DateFormat.MEDIUM ).format(createdTime);
		
		textView.setText( item.getTask() );
		dateView.setText( createdStr );
		editView.setTag( new Object[]{ item, position } );
		data = item;
		this.position = position;
	}

}
