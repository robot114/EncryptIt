package com.zsm.encryptIt.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zsm.encryptIt.R;

public class ClearableEditor extends RelativeLayout {

	private EditText editor;
	private ImageView clearButton;

	public ClearableEditor(Context context) {
		super(context);
		
		init();
	}

	public ClearableEditor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ClearableEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
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
	
	private void init() {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li
			= (LayoutInflater)getContext().getSystemService( infService );
		li.inflate( R.layout.clearable_editor, this, true );
		
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
}
