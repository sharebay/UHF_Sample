package com.uhf.uhf;

import com.gy.vam.uhflib.R;
import com.uhf.reader.base.ReaderBase;
import com.uhf.reader.helper.ISO180006BOperateTagBuffer;
import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.OperateTagBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.reader.helper.ReaderSetting;
import com.uhf.uhf.setpage.PageReaderAddress;
import com.uhf.uhf.setpage.PageReaderFirmwareVersion;
import com.uhf.uhf.setpage.PageReaderIdentifier;
import com.uhf.uhf.setpage.PageReaderTemperature;
import com.uhf.uhf.tagpage.PageInventoryBuffer;
import com.uhf.uhf.tagpage.PageInventoryFast;
import com.uhf.uhf.tagpage.PageInventoryReal;
import com.uhf.uhf.tagpage.PageInventoryReal6B;
import com.uhf.uhf.tagpage.PageTag6BAccess;
import com.uhf.uhf.tagpage.PageTagAccess;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

public class Tag extends ScrollView {
	private Context mContext;
	
	private TableRow mInventoryReal6CRow;
	private TextView mInventoryReal6CText;
	private TableRow mInventoryReal6BRow;
	private TextView mInventoryReal6BText;
	private TableRow mInventoryBufferRow;
	private TextView mInventoryBufferText;
	private TableRow mInventoryFast4antRow;
	private TextView mInventoryFast4antText;
	private TableRow mAccessTag6CRow;
	private TextView mAccessTag6CText;
	private TableRow mAccessTag6BRow;
	private TextView mAccessTag6BText;
	
	private ReaderBase mReader;
	private ReaderHelper mReaderHelper;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
	
	public Tag(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.tag, this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			return ;
		}
		
		initSettingView();
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();
		
	}

	private void initSettingView() {
		
		mInventoryReal6CRow = (TableRow) findViewById(R.id.table_row_inventory_real);
		mInventoryReal6CRow.setOnClickListener(setTagOnClickListener);
		mInventoryReal6CText = (TextView) findViewById(R.id.text_inventory_real_mode);
		
		mInventoryBufferRow = (TableRow) findViewById(R.id.table_row_inventory_buffer);
		mInventoryBufferRow.setOnClickListener(setTagOnClickListener);
		mInventoryBufferText = (TextView) findViewById(R.id.text_inventory_buffer_mode);
		
		mInventoryFast4antRow = (TableRow) findViewById(R.id.table_row_inventory_fast4ant);
		mInventoryFast4antRow.setOnClickListener(setTagOnClickListener);
		mInventoryFast4antText = (TextView) findViewById(R.id.text_inventory_fast4ant_mode);
		
		mInventoryFast4antRow = (TableRow) findViewById(R.id.table_row_inventory_fast4ant);
		mInventoryFast4antRow.setOnClickListener(setTagOnClickListener);
		mInventoryFast4antText = (TextView) findViewById(R.id.text_inventory_fast4ant_mode);
		
		mInventoryReal6BRow = (TableRow) findViewById(R.id.table_row_inventory_real_6b);
		mInventoryReal6BRow.setOnClickListener(setTagOnClickListener);
		mInventoryReal6BText = (TextView) findViewById(R.id.text_inventory_real_mode_6b);
		
		mAccessTag6CRow = (TableRow) findViewById(R.id.table_row_access_tag_6c);
		mAccessTag6CRow.setOnClickListener(setTagOnClickListener);
		mAccessTag6BRow = (TableRow) findViewById(R.id.table_row_access_tag_6b);
		mAccessTag6BRow.setOnClickListener(setTagOnClickListener);
		mAccessTag6CText = (TextView) findViewById(R.id.text_access_tag_6c);
		mAccessTag6BText = (TextView) findViewById(R.id.text_access_tag_6b);
	}

	public void refreshReaderSetting(byte btCmd) {
		switch (btCmd) {
			
		}
	}
	
	private OnClickListener setTagOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = null;
			int id = arg0.getId();
			if (id == R.id.table_row_inventory_real){
				intent = new Intent().setClass(mContext, PageInventoryReal.class);
			} else if (id == R.id.table_row_inventory_buffer){
				intent = new Intent().setClass(mContext, PageInventoryBuffer.class);
			} else if (id == R.id.table_row_inventory_fast4ant){
				intent = new Intent().setClass(mContext, PageInventoryFast.class);
			} else if (id == R.id.table_row_access_tag_6c){
				intent = new Intent().setClass(mContext, PageTagAccess.class);
			} else if (id == R.id.table_row_access_tag_6b){
				intent = new Intent().setClass(mContext, PageTag6BAccess.class);
			} else if (id == R.id.table_row_inventory_real_6b){
				intent = new Intent().setClass(mContext, PageInventoryReal6B.class);
			} else {
				intent = null;
			}
			
			if (intent != null) mContext.startActivity(intent);
		}
	};
}
