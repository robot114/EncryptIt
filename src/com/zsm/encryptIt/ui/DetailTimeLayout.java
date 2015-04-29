package com.zsm.encryptIt.ui;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zsm.encryptIt.R;

public class DetailTimeLayout extends LinearLayout {

	private static final int ORIENTATION_AUTO = 0;
	private static final int ORIENTATION_VERTICAL = 1;
	private static final int ORIENTATION_HORIZONTAL = 2;
	
	final static private DateFormat DATE_FORMAT
		= DateFormat.getDateInstance( DateFormat.MEDIUM );
	
	private int labelStyleId = R.style.styleDetailTimeTitle;
	private int timeStyleId = R.style.styleDetailTimeTime;
	private int orientation = ORIENTATION_AUTO;
	private int createLabelId = R.string.createdTimeTitle;
	private int modifyLabelId = R.string.modifyTimeTitle;
	
	private TextView createTitleView;
	private TextView modifyTitleView;
	private TextView createTimeView;
	private TextView modifyTimeView;
	private View view;

	public DetailTimeLayout(Context context) {
		super(context);
		
		init(context);
	}

	public DetailTimeLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
	
		super(context, attrs, defStyleAttr);
		initCustomAttrs( context, attrs );
		init(context);
	}
	
	public DetailTimeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCustomAttrs( context, attrs );
		init(context);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if( orientation == ORIENTATION_AUTO ) {
			orientation = getOrientationWhenAuto();
		}
		
		LinearLayout lv = (LinearLayout)view.findViewById( R.id.detailTimeLayout );
		lv.setOrientation( orientation == ORIENTATION_VERTICAL
							? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL );
	}

	private void init(Context context) {
		LayoutInflater li = LayoutInflater.from(context);
		view = li.inflate( R.layout.detail_time_layout, this, true );
		createTitleView = (TextView)view.findViewById( R.id.detailCreatedTitle );
		modifyTitleView = (TextView)view.findViewById( R.id.detailModifyTitle );
		createTimeView = (TextView)view.findViewById( R.id.detailCreateTime );
		modifyTimeView = (TextView)view.findViewById( R.id.detailModifyTime );
		
		createTitleView.setTextAppearance(context, labelStyleId);
		createTitleView.setText(createLabelId);
		createTimeView.setTextAppearance(context, timeStyleId);
		
		modifyTitleView.setTextAppearance(context, labelStyleId);
		modifyTitleView.setText(modifyLabelId);
		modifyTimeView.setTextAppearance(context, timeStyleId);
		
		this.setBackground( createTitleView.getBackground() );
		
		int createTitleWidth
			= measurePreferredTextViewWidth(createTitleView,
											createTitleView.getText().toString() );
		int modifyTitleWidth
			= measurePreferredTextViewWidth(modifyTitleView,
											modifyTitleView.getText().toString() );
		int titleWidth = Math.max(createTitleWidth, modifyTitleWidth);
		createTitleView.setWidth(titleWidth);
		modifyTitleView.setWidth(titleWidth);
	}

	private int getOrientationWhenAuto() {
		assert( orientation == ORIENTATION_AUTO );
		LayoutParams lp = (LayoutParams) getLayoutParams();
		
		if( lp.width == LayoutParams.WRAP_CONTENT ) {
			return ORIENTATION_VERTICAL;
		}
		
		Rect outRect = new Rect();
		getWindowVisibleDisplayFrame(outRect);
		
		int horizontalWidth = getHorizontalWidth();
		
		int maxWidth
			= lp.width == LayoutParams.MATCH_PARENT ? outRect.width() : lp.width;
		
		return maxWidth > horizontalWidth 
				? ORIENTATION_HORIZONTAL :  ORIENTATION_VERTICAL;
	}
	
	private int getHorizontalWidth() {
		String time
			= DATE_FORMAT.format( 
				new GregorianCalendar( 1988, 12, 28, 23, 58, 58 ).getTime() )
			  + "  ";
		int createTimeWidth
			= measurePreferredTextViewWidth(createTimeView, time );
		int modifyTimeWidth
			= measurePreferredTextViewWidth(modifyTimeView, time );
		return createTimeWidth + modifyTimeWidth
				+ createTitleView.getWidth() + modifyTitleView.getWidth();
	}

	private int measurePreferredTextViewWidth(TextView textView, String text) {
		return (int) textView.getPaint().measureText( text );
	}

	private void initCustomAttrs( Context context, AttributeSet attrs ) {
		TypedArray a
			= context.obtainStyledAttributes(attrs, R.styleable.DetailTimeLayout );
		final int n = a.getIndexCount();
		for (int i = 0; i < n; ++i) {
			int attr = a.getIndex(i);
		    switch (attr) {
		        case R.styleable.DetailTimeLayout_detailTimeLabelStyle:
		        	labelStyleId
		        		= a.getResourceId( attr, R.style.styleDetailTimeTitle);
		        	break;
		        case R.styleable.DetailTimeLayout_detailTimeStyle:
		        	timeStyleId
		        		= a.getResourceId(attr, R.style.styleDetailTimeTime);
		        	break;
		        case R.styleable.DetailTimeLayout_detailTimeOrientation:
		        	orientation = a.getInt(attr, ORIENTATION_AUTO);
		        	break;
		        case R.styleable.DetailTimeLayout_detailCreateTitle:
		        	createLabelId 
		        		= a.getResourceId(attr, R.string.createdTimeTitle);
		        	break;
		        case R.styleable.DetailTimeLayout_detailModifyTitle:
		        	modifyLabelId 
		        		= a.getResourceId(attr, R.string.modifyTimeTitle);
		        	break;
		    }
		}
		a.recycle();
	}
	
	public void setCreateTime( Date t ) {
		createTimeView.setText( DATE_FORMAT.format(t) );
	}

	public void setModifyTime( Date t ) {
		modifyTimeView.setText( DATE_FORMAT.format(t) );
	}
}
