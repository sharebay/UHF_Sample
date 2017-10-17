package com.uhf.uhf.tagpage;


import com.gy.vam.uhflib.R;
import com.uhf.reader.base.CMD;
import com.uhf.reader.base.ERROR;
import com.uhf.reader.base.ReaderBase;
import com.uhf.reader.helper.ISO180006BOperateTagBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.reader.helper.ReaderSetting;
import com.uhf.uhf.LogList;
import com.uhf.uhf.TagReal6BList;
import com.uhf.uhf.TagReal6BList.OnItemSelectedListener;
import com.uhf.uhf.UHFApplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class PageInventoryReal6B extends Activity {
	private LogList mLogList;
	
	private TextView mStart;
	private TextView mStop;
	
	private TextView mRefreshButton;
	
	private TagReal6BList mTagReal6BList;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;

	private static ReaderSetting m_curReaderSetting;
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
		setContentView(R.layout.page_inventory_real_6b);
		((UHFApplication) getApplication()).addActivity(this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(R.id.log_list);
		mStart = (TextView) findViewById(R.id.start);
		mStop = (TextView) findViewById(R.id.stop);
		

		mTagReal6BList = (TagReal6BList) findViewById(R.id.tag_real_6b_list);

		mStart.setOnClickListener(setInventoryReal6BOnClickListener);
		
		mStop.setOnClickListener(setInventoryReal6BOnClickListener);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B);
		lbm.registerReceiver(mRecv, itent);
		
		if (mReaderHelper.getInventoryFlag()) {
			mHandler.removeCallbacks(mRefreshRunnable);
			mHandler.postDelayed(mRefreshRunnable,2000);
		}
		mStop.setEnabled(mReaderHelper.getISO6BContinue());
		mStart.setEnabled(!mReaderHelper.getISO6BContinue());
		
		mRefreshButton = (TextView) findViewById(R.id.refresh);
		mRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				m_curOperateTagISO18000Buffer.clearBuffer();
				
				refreshList();
				refreshText();
				clearText();
			}
		});
	}
	
	private Handler mHandler = new Handler();
    private Runnable mRefreshRunnable = new Runnable() {
         public void run () {
        	 refreshList();
        	 mHandler.postDelayed(this, 2000); 
      }
    };
    
	private Handler mLoopHandler = new Handler();
    private Runnable mLoopRunnable = new Runnable() {
         public void run () {
        	 mReader.iso180006BInventory(m_curReaderSetting.btReadId);
        	 mLoopHandler.postDelayed(this, 2000); 
         }
    };
	
	private OnClickListener setInventoryReal6BOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			
			if(arg0.getId() == R.id.stop) {
				mReaderHelper.setISO6BContinue(false);
				
				mLoopHandler.removeCallbacks(mLoopRunnable);
				mHandler.removeCallbacks(mRefreshRunnable);
				mStop.setEnabled(false);
				mStart.setEnabled(true);
			} else if (arg0.getId() == R.id.start) {
				mReaderHelper.setISO6BContinue(true);
				m_curOperateTagISO18000Buffer.clearBuffer();
				mReader.iso180006BInventory(m_curReaderSetting.btReadId);
				
				mLoopHandler.removeCallbacks(mLoopRunnable);
				mLoopHandler.postDelayed(mLoopRunnable,2000);
				mHandler.removeCallbacks(mRefreshRunnable);
				mHandler.postDelayed(mRefreshRunnable,2000);
				mStop.setEnabled(true);
				mStart.setEnabled(false);
			}
			refreshList();
		}
	};
	
	private void refreshList() {
		mTagReal6BList.refreshList();
	}
	
	private void refreshText() {
		mTagReal6BList.refreshText();
	}
	
	private void clearText() {
		mTagReal6BList.clearText();
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				switch (btCmd) {
				case CMD.ISO18000_6B_INVENTORY:
					refreshText();
					mLoopHandler.removeCallbacks(mLoopRunnable);
					mLoopHandler.postDelayed(mLoopRunnable,2000);
					break;
				case ReaderHelper.INVENTORY_ERR:
				case ReaderHelper.INVENTORY_ERR_END:
				case ReaderHelper.INVENTORY_END:
//					refreshList();
					refreshText();
					if (mReaderHelper.getISO6BContinue()) {
						mLoopHandler.removeCallbacks(mLoopRunnable);
						mLoopHandler.postDelayed(mLoopRunnable,2000);
					} else {
						mLoopHandler.removeCallbacks(mLoopRunnable);
					}
					break;
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
		
		mLoopHandler.removeCallbacks(mLoopRunnable);
		mHandler.removeCallbacks(mRefreshRunnable);
	}
}

