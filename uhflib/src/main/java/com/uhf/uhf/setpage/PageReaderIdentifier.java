package com.uhf.uhf.setpage;


import com.gy.vam.uhflib.R;
import com.uhf.reader.base.CMD;
import com.uhf.reader.base.ERROR;
import com.uhf.reader.base.ReaderBase;
import com.uhf.reader.base.StringTool;
import com.uhf.reader.helper.ISO180006BOperateTagBuffer;
import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.OperateTagBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.reader.helper.ReaderSetting;
import com.uhf.uhf.LogList;
import com.uhf.uhf.UHFApplication;

import android.R.integer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class PageReaderIdentifier extends Activity {
	private LogList mLogList;
	
	private TextView mSet;
	private TextView mGet;
	
	private EditText mSetIdentifierText;
	private EditText mGetIdentifierText;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
    
    private LocalBroadcastManager lbm;
    
    @Override
    protected void onResume() {
    	if (mReader != null) {
    		if (!mReader.IsAlive())
    			mReader.StartWait();
    	}
    	super.onResume();
    };
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.page_reader_identifier);
		((UHFApplication) getApplication()).addActivity(this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(R.id.log_list);
		mSet = (TextView) findViewById(R.id.set);
		mGet = (TextView) findViewById(R.id.get);
		mSetIdentifierText = (EditText) findViewById(R.id.set_identifier_text);
		mGetIdentifierText = (EditText) findViewById(R.id.get_identifier_text);
		
		mSet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String strIdentifier = mSetIdentifierText.getText().toString();
				
				if (strIdentifier != null) strIdentifier = strIdentifier.trim();
				
				if (strIdentifier == null || strIdentifier.equals(""))	return ;
				
				String []strAryIdentifier = StringTool.stringToStringArray(strIdentifier, 2);
				byte[] btAryIdentifier = StringTool.stringArrayToByteArray(strAryIdentifier, 12);
				if (btAryIdentifier == null) return;
				
				m_curReaderSetting.btAryReaderIdentifier = btAryIdentifier;
				mReader.setReaderIdentifier(m_curReaderSetting.btReadId, btAryIdentifier);
			}
		});
		
		mGet.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mReader.getReaderIdentifier(m_curReaderSetting.btReadId);
			}
		});
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
		lbm.registerReceiver(mRecv, itent);
		
		if (m_curReaderSetting.btAryReaderIdentifier != null) {
			String strIdentifier = String.format("%02X", m_curReaderSetting.btAryReaderIdentifier[0]);
	        for (int i = 1; i < 12; i ++) {
	        	strIdentifier = strIdentifier + " " + String.format("%02X", m_curReaderSetting.btAryReaderIdentifier[i]);
	        }
			mGetIdentifierText.setText(strIdentifier.toUpperCase());
		}
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				if (btCmd == CMD.GET_READER_IDENTIFIER || btCmd == CMD.SET_READER_IDENTIFIER) {
					if (m_curReaderSetting.btAryReaderIdentifier != null) {
						String strIdentifier = String.format("%02X", m_curReaderSetting.btAryReaderIdentifier[0]);
				        for (int i = 1; i < 12; i ++) {
				        	strIdentifier = strIdentifier + " " + String.format("%02X", m_curReaderSetting.btAryReaderIdentifier[i]);
				        }
						mGetIdentifierText.setText(strIdentifier.toUpperCase());
					}
				}
            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_LOG)) {
            	mLogList.writeLog((String)intent.getStringExtra("log"), intent.getIntExtra("type", ERROR.SUCCESS));
            }
		}
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mLogList.tryClose()) return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (lbm != null)
			lbm.unregisterReceiver(mRecv);
	}
}

