package com.zsm.encryptIt.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView.OnEditorActionListener;

import com.zsm.encryptIt.R;

public class ClearableEditor extends RelativeLayout {

	private EditText editor;
	private ImageView clearButton;

	public ClearableEditor(Context context) {
		super( context );
		init( true );
	}

	public ClearableEditor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init( getButtonOnRight( context, attrs ) );
	}

	public ClearableEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
		init( getButtonOnRight( context, attrs ) );
	}

	private boolean getButtonOnRight( Context context, AttributeSet attrs ) {
		TypedArray a
			= context.obtainStyledAttributes(attrs, R.styleable.ClearableEditor );
		final int n = a.getIndexCount();
		for (int i = 0; i < n; ++i) {
			int attr = a.getIndex(i);
		    switch (attr) {
		        case R.styleable.ClearableEditor_clearButtonOnRight:
		        	return a.getBoolean(attr, true);
		    }
		}
		a.recycle();
		return true;
	}
	
	public Editable getText() {
		return editor.getText();
	}

	public void clearText() {
		editor.setText( "" );
	}

	public void addTextChangedListener(TextWatcher textWatcher) {
		editor.addTextChangedListener(textWatcher);
	}
	
	public void setOnEditorActionListener(OnEditorActionListener l) {
		editor.setOnEditorActionListener( l );
	}
	
	private void init( boolean buttonOnRight ) {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li
			= (LayoutInflater)getContext().getSystemService( infService );
		if( buttonOnRight ) {
			li.inflate( R.layout.clearable_editor, this, true );
		} else {
			li.inflate( R.layout.clearable_editor_left, this, true );
		}
		
		clearButton = (ImageView)findViewById( R.id.clearButton );
		editor = (EditText)findViewById( R.id.editText );
		
		hookupButton();
	}

	private void hookupButton() {
		clearButton.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				clearText();
			}
		} );
	}

	public void setIMEOption(int options) {
		editor.setImeOptions( options );
	}
}
