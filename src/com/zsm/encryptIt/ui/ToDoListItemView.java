package com.zsm.encryptIt.ui;

import java.text.DateFormat;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zsm.encryptIt.R;
import com.zsm.encryptIt.WhatToDoItem;
import com.zsm.log.Log;

public class ToDoListItemView extends LinearLayout {

	private Paint marginPaint;
	private Paint linePaint;
	private int paperColor;
	private float margin;
	
	private CheckBox selectedView;
	private TextView textView;
	private TextView dateView;
	private ImageView deleteView;
	
	private WhatToDoListViewItem item;
	private int position;
	private ModeKeeper modeKeeper;
	private ExpandOperator expandOperator;
	
	private static int dateViewWidth = 0;
	private static int textViewWidth = 0;
	
	final static private DateFormat DATE_FORMAT
		= DateFormat.getDateInstance( DateFormat.MEDIUM );
	
	public ToDoListItemView(Context context, int resource, ModeKeeper mk ) {
		super(context);
		modeKeeper = mk;
		
		init( resource );
	}

	private void init( int resource ) {
		setWillNotDraw( false );
		
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li
			= (LayoutInflater)getContext().getSystemService( infService );
		li.inflate( resource, this, true );
		
		selectedView = (CheckBox)findViewById( R.id.rowCheck );
		textView = (TextView)findViewById(R.id.row);
		dateView = (TextView)findViewById(R.id.rowDate);
		
		deleteView = (ImageView)findViewById( R.id.rowDelete );
		
		selectedView.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				item.setSelected( ((CheckBox)v).isChecked() );
			}
		} );
		
		final ImageView expandView
			= (ImageView)findViewById( R.id.imageViewExpand );
		
		if( expandView != null ) {
			expandView.setOnClickListener( new OnExpandClickListener(expandView) );
		}
		
		DetailClickListener editListener
			= new DetailClickListener( R.string.detail_edit, true,
									   MainActivity.SHOW_FOR_EDIT,
									   R.string.detailSaveAndBack );
		textView.setOnClickListener( editListener );
		if( dateView != null ) {
			dateView.setOnClickListener( editListener );
		}
		
		deleteView.setOnClickListener(
			new DetailClickListener( R.string.detail_delete, false,
									 MainActivity.SHOW_FOR_DELETE,
									 R.string.detail_delete ) );

		setOnLongClickListener( new LongClickListener() );
		
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

	public void setExpandOperator( ExpandOperator eo ) {
		this.expandOperator = eo;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		initViewWidth();
	}

	private void initViewWidth() {
		if( textViewWidth <= 0 ) {
			String t
				= DATE_FORMAT.format( 
						new GregorianCalendar( 1988, 12, 28, 23, 58, 58 ).getTime() )
				  + "  ";
			
			dateViewWidth
				= dateView != null 
				  ? ( (int) dateView.getPaint().measureText(t)
						+ dateView.getPaddingLeft()+dateView.getPaddingRight() )
				  : 0;
			
			textViewWidth
				= getMeasuredWidth() - dateViewWidth - textView.getPaddingRight()
					- getChildrenWidthOtherThanText() - (int)margin;
		}
	}
	
	private int getChildrenWidthOtherThanText() {
		int width = 0;
		
		ViewGroup thisView = (ViewGroup) getChildAt( 0 );
		
		int count = thisView.getChildCount();
		for( int i = 0; i < count; i++ ) {
			View child = thisView.getChildAt( i );
            if (child.getVisibility() != GONE 
            	&& ( child.getId() != R.id.row && child.getId() != R.id.rowDate ) ) {
            	
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                width
                	+= child.getMeasuredWidth() + child.getPaddingLeft()
                		+ params.leftMargin;
            }
		}
		return width;
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

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		dateViewWidth = 0;
		textViewWidth = 0;
	}

	public void setDisplayValue( WhatToDoListViewItem item, int position ) {
		initViewWidth();
		WhatToDoItem data = item.getData();
		
		String time = DATE_FORMAT.format(data.getModifiedTime());
		
		selectedView.setChecked( item.isSelected() );

		textView.setText( data.getTask() );
		textView.setWidth(textViewWidth);
		if( dateView != null ) {
			dateView.setText( time );
			dateView.setWidth(dateViewWidth);
		}
		this.item = item;
		this.position = position;
		
		makeComponentVisibleByMode();
	}
	
	@Override
	public void setOnLongClickListener( OnLongClickListener lcl ) {
		// No super's setOnLongClickListener called to break the calling list.
		textView.setOnLongClickListener(lcl);
		if( dateView != null ) {
			dateView.setOnLongClickListener(lcl);
		}
	}
	
	private void makeComponentVisibleByMode() {
		boolean isBrowseMode = modeKeeper.getMode() == ModeKeeper.MODE.BROWSE;
//		selectedView.setVisibility( isBrowseMode ? View.GONE : View.VISIBLE );
		deleteView.setVisibility( isBrowseMode ? View.VISIBLE : View.GONE );
		textView.setLongClickable(isBrowseMode);
		if( dateView != null) {
			dateView.setLongClickable(isBrowseMode);
		}
	}

	private final class OnExpandClickListener implements OnClickListener {
		private final ImageView expandView;
		private boolean expanded = false;

		private OnExpandClickListener(ImageView expandView) {
			this.expandView = expandView;
		}

		@Override
		public void onClick(View v) {
			if( expandOperator != null ) {
				expandView.setImageResource( expanded 
											 ? R.drawable.expand 
											 : R.drawable.collapse );
				Resources r = getResources();
				String cd
					= r.getString( expanded 
								   ? R.string.expandDescription 
								   : R.string.collapseDescription );
				
				expandView.setContentDescription( cd );
				expanded = !expanded;
				expandOperator.expand( expanded, position );
			}
		}
	}

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
			intent.putExtra( DetailActivity.KEY_ROW_ITEM, 
							 item.getData().toByteArray() );
			intent.putExtra( DetailActivity.KEY_ROW_POSITION, position );
			intent.putExtra( DetailActivity.KEY_DETAIL_EDITABLE, editable );
			intent.putExtra( DetailActivity.KEY_DETAIL_TITLE, titleId );
			intent.putExtra( DetailActivity.KEY_DETAIL_OK, positiveTextId );
			((Activity)context).startActivityForResult( intent, requestCode );
			Log.d( "Start detail activity.", "requestCode", requestCode,
				   "intent", intent, "position", position );
		}
	}
	
	private final class LongClickListener implements OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			modeKeeper.switchTo( ModeKeeper.MODE.EDIT );
			return true;
		}
	}

}
