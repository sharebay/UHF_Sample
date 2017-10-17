package com.uhf.uhf.setpage;


import com.gy.vam.uhflib.R;
import com.uhf.reader.base.CMD;
import com.uhf.reader.base.ERROR;
import com.uhf.reader.base.ReaderBase;
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
import android.widget.RadioGroup;
import android.widget.TextView;

public class PageReaderProfile extends Activity {
	private LogList mLogList;
	
	private TextView mSet, mGet;
	
	private RadioGroup mGroupProfile;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
    
    private LocalBroadcastManager lbm = null;
    
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
		setContentView(R.layout.page_reader_profile);
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
		mGroupProfile =  (RadioGroup) findViewById(R.id.group_profile);
		
		mSet.setOnClickListener(setProfileOnClickListener);
		mGet.setOnClickListener(setProfileOnClickListener);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
		lbm.registerReceiver(mRecv, itent);
		
		updateView();
	}
	
	private OnClickListener setProfileOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			int id = arg0.getId();
			if (id == R.id.get){
				mReader.getRfLinkProfile(m_curReaderSetting.btReadId);
			} else if(id == R.id.set){
				byte btProfile = 0;
				int buttonId = mGroupProfile.getCheckedRadioButtonId();
				if (buttonId == R.id.set_profile_option0){
					btProfile = (byte) 0xD0;
				} else if (buttonId == R.id.set_profile_option1){
					btProfile = (byte) 0xD1;
				} else if (buttonId == R.id.set_profile_option2){
					btProfile = (byte) 0xD2;
				} else if (buttonId == R.id.set_profile_option3){
					btProfile = (byte) 0xD3;
				} else {
					return;
				}
				mReader.setRfLinkProfile(m_curReaderSetting.btReadId, btProfile);
				m_curReaderSetting.btRfLinkProfile = btProfile;
			}
		}
	};
	
	private void updateView() {
		if ((m_curReaderSetting.btRfLinkProfile & 0xFF) == 0xD0) {
			mGroupProfile.check(R.id.set_profile_option0);
		} else if ((m_curReaderSetting.btRfLinkProfile & 0xFF) == 0xD1) {
			mGroupProfile.check(R.id.set_profile_option1);
		} else if ((m_curReaderSetting.btRfLinkProfile & 0xFF) == 0xD2) {
			mGroupProfile.check(R.id.set_profile_option2);
		} else if ((m_curReaderSetting.btRfLinkProfile & 0xFF) == 0xD3) {
			mGroupProfile.check(R.id.set_profile_option3);
		}
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				if (btCmd == CMD.GET_RF_LINK_PROFILE || btCmd == CMD.SET_RF_LINK_PROFILE) {
					updateView();
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

