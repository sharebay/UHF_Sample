package com.uhf.uhf;


import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gy.vam.uhflib.R;

public class TitleGoToMain extends RelativeLayout {
	private TextView mGotoBackTextView;
	public TitleGoToMain(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.title_goto_main, this);
		
		mGotoBackTextView = (TextView) findViewById(R.id.textview_goto_main);
		
		mGotoBackTextView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((Activity) getContext()).finish();
			}
		});
	}
}
