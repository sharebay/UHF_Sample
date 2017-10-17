package com.uhf.uhf.tagpage;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.uhf.uhf.TagRealList;
import com.uhf.uhf.UHFApplication;
import com.uhf.uhf.spiner.SpinerPopWindow;
import com.uhf.uhf.spiner.AbstractSpinerAdapter.IOnItemSelectListener;

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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class PageInventoryReal extends Activity {
	private LogList mLogList;
	
	private TextView mStart;
	private TextView mStop;
	
	private TextView mRefreshButton;
	
	private LinearLayout mLayoutRealSession;
	
	private TextView mSessionIdTextView, mInventoriedFlagTextView;
	private TableRow mDropDownRow1, mDropDownRow2;
	
	private CheckBox mCbRealSession;
	private CheckBox mCbAntenna1, mCbAntenna2, mCbAntenna3, mCbAntenna4;
	
	private List<String> mSessionIdList;
	private List<String> mInventoriedFlagList;
	
	private SpinerPopWindow mSpinerPopWindow1, mSpinerPopWindow2;
	
	private EditText mRealRoundEditText;
	
	private TagRealList mTagRealList;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;

	private int mPos1 = -1, mPos2 = -1;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
    
    private LocalBroadcastManager lbm;
    
    private long mRefreshTime;
    
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
		setContentView(R.layout.page_inventory_real);
		//gy.每个activity都被接管了，当发生crash的时候会保存log信息到文件
		((UHFApplication) getApplication()).addActivity(this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mSessionIdList = new ArrayList<String>();
		mInventoriedFlagList = new ArrayList<String>();

		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(R.id.log_list);
		mStart = (TextView) findViewById(R.id.start);
		mStop = (TextView) findViewById(R.id.stop);
		
		mCbRealSession = (CheckBox) findViewById(R.id.check_real_session);
		mLayoutRealSession = (LinearLayout) findViewById(R.id.layout_real_session);
		
		//gy.设置天线1234
		mCbAntenna1 = (CheckBox) findViewById(R.id.check_antenna_1);
		mCbAntenna2 = (CheckBox) findViewById(R.id.check_antenna_2);
		mCbAntenna3 = (CheckBox) findViewById(R.id.check_antenna_3);
		mCbAntenna4 = (CheckBox) findViewById(R.id.check_antenna_4);
		
		mSessionIdTextView =  (TextView) findViewById(R.id.session_id_text);
		mInventoriedFlagTextView =  (TextView) findViewById(R.id.inventoried_flag_text);
		mDropDownRow1 = (TableRow) findViewById(R.id.table_row_session_id);
		mDropDownRow2 = (TableRow) findViewById(R.id.table_row_inventoried_flag);
		
		mTagRealList = (TagRealList) findViewById(R.id.tag_real_list);
		
		mRealRoundEditText = (EditText) findViewById(R.id.real_round_text);

		mStart.setOnClickListener(setInventoryRealOnClickListener);
		
		mStop.setOnClickListener(setInventoryRealOnClickListener);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		
		//gy.帮助文档中注释为：读写日志操作标 读写操作执行成功发送action为BROADCAST_WRITE_LOG 的广播
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		//gy.读写器实时盘存标识，盘存任何一个6C标签成功发送action为BROADCAST_REFRESH_INVENTORY_REAL 的广播
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL);
		lbm.registerReceiver(mRecv, itent);
		
		mDropDownRow1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindow1();
			}
		});
		
		mDropDownRow2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindow2();
			}
		});
		
		mSessionIdList.clear(); mInventoriedFlagList.clear();
		String[] lists = getResources().getStringArray(R.array.session_id_list);
		for(int i = 0; i < lists.length; i++){
			mSessionIdList.add(lists[i]);
		}
		lists = getResources().getStringArray(R.array.inventoried_flag_list);
		for(int i = 0; i < lists.length; i++){
			mInventoriedFlagList.add(lists[i]);
		}
		
		mSpinerPopWindow1 = new SpinerPopWindow(this);
		mSpinerPopWindow1.refreshData(mSessionIdList, 0);
		mSpinerPopWindow1.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setSessionIdText(pos);
			}
		});
		
		mSpinerPopWindow2 = new SpinerPopWindow(this);
		mSpinerPopWindow2.refreshData(mInventoriedFlagList, 0);
		mSpinerPopWindow2.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setInventoriedFlagText(pos);
			}
		});
		
		updateView();
		
		mCbRealSession.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (mCbRealSession.isChecked()) {
					mLayoutRealSession.setVisibility(View.VISIBLE);
				} else {
					mLayoutRealSession.setVisibility(View.GONE);
				}
			}
		});
		
		if (mReaderHelper.getInventoryFlag()) {
			mHandler.removeCallbacks(mRefreshRunnable);
			mHandler.postDelayed(mRefreshRunnable,2000);
		}
		mStop.setEnabled(mReaderHelper.getInventoryFlag());
		mStart.setEnabled(!mReaderHelper.getInventoryFlag());
		
		mRefreshButton = (TextView) findViewById(R.id.refresh);
		mRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				m_curInventoryBuffer.clearInventoryRealResult();
				refreshList();
				refreshText();
				clearText();

				mRealRoundEditText.setText("1");
	            mCbAntenna1.setChecked(true);
	            mCbAntenna2.setChecked(false);
	            mCbAntenna3.setChecked(false);
	            mCbAntenna4.setChecked(false);
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
 			byte btWorkAntenna = m_curInventoryBuffer.lAntenna.get(m_curInventoryBuffer.nIndexAntenna);
 			if (btWorkAntenna < 0) btWorkAntenna = 0;
 			
 			//gy.设置工作天线
 			mReader.setWorkAntenna(m_curReaderSetting.btReadId, btWorkAntenna);
 			mLoopHandler.postDelayed(this, 2000); 
         }
    };
	
	private void setSessionIdText(int pos){
		if (pos >= 0 && pos < mSessionIdList.size()){
			String value = mSessionIdList.get(pos);
			mSessionIdTextView.setText(value);
			mPos1 = pos;
		}
	}
	
	private void setInventoriedFlagText(int pos){
		if (pos >= 0 && pos < mInventoriedFlagList.size()){
			String value = mInventoriedFlagList.get(pos);
			mInventoriedFlagTextView.setText(value);
			mPos2 = pos;
		}
	}
	
	private void showSpinWindow1() {
		mSpinerPopWindow1.setWidth(mDropDownRow1.getWidth());
		mSpinerPopWindow1.showAsDropDown(mDropDownRow1);
	}
	
	private void showSpinWindow2() {
		mSpinerPopWindow2.setWidth(mDropDownRow2.getWidth());
		mSpinerPopWindow2.showAsDropDown(mDropDownRow2);
	}
	
	private void updateView() {

		if (m_curInventoryBuffer.bLoopCustomizedSession) {
			mCbRealSession.setChecked(true);
			mLayoutRealSession.setVisibility(View.VISIBLE);
		} else {
			mCbRealSession.setChecked(false);
			mLayoutRealSession.setVisibility(View.GONE);
		}
		
		
		mPos1 = m_curInventoryBuffer.btSession;
		mPos2 = m_curInventoryBuffer.btTarget;
		
		mCbAntenna1.setChecked(false);
		mCbAntenna2.setChecked(false);
		mCbAntenna3.setChecked(false);
		mCbAntenna4.setChecked(false);

		//gy 默认添加编号为0x00的天线。
		if (m_curInventoryBuffer.lAntenna.size() <= 0) m_curInventoryBuffer.lAntenna.add((byte) 0x00);
		
		for (Byte num : m_curInventoryBuffer.lAntenna) {
			if (num == 0x00) {
				mCbAntenna1.setChecked(true);
			} else if (num == 0x01) {
				mCbAntenna2.setChecked(true);
			} else if (num == 0x02) {
				mCbAntenna3.setChecked(true);
			} else if (num == 0x03) {
				mCbAntenna4.setChecked(true);
			}
		}
		
		int nRepeat = m_curInventoryBuffer.btRepeat & 0xFF;
		mRealRoundEditText.setText(String.valueOf(nRepeat <=0 ? 1 : nRepeat));
		
		setSessionIdText(mPos1);
		setInventoriedFlagText(mPos2);
	}
	
	private OnClickListener setInventoryRealOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			
			m_curInventoryBuffer.clearInventoryPar();
			
			if (mCbAntenna1.isChecked()) m_curInventoryBuffer.lAntenna.add((byte)0x00);
			if (mCbAntenna2.isChecked()) m_curInventoryBuffer.lAntenna.add((byte)0x01);
			if (mCbAntenna3.isChecked()) m_curInventoryBuffer.lAntenna.add((byte)0x02);
			if (mCbAntenna4.isChecked()) m_curInventoryBuffer.lAntenna.add((byte)0x03);
			
			if (m_curInventoryBuffer.lAntenna.size() <= 0) {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.antenna_empty),
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			m_curInventoryBuffer.bLoopInventoryReal = true;
			m_curInventoryBuffer.btRepeat = 0;
			
			String strRepeat = mRealRoundEditText.getText().toString();				
			if (strRepeat == null || strRepeat.length() <= 0) {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.repeat_empty),
						Toast.LENGTH_SHORT).show();
				return ;
			}
			
			m_curInventoryBuffer.btRepeat = (byte) Integer.parseInt(strRepeat);
			
			if ((m_curInventoryBuffer.btRepeat & 0xFF) <= 0) {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.repeat_min),
						Toast.LENGTH_SHORT).show();
				return ;
			}
			
			//gy.自定义选区
            if (mCbRealSession.isChecked()) {
                m_curInventoryBuffer.bLoopCustomizedSession = true;
                m_curInventoryBuffer.btSession = (byte)(mPos1 & 0xFF);
                m_curInventoryBuffer.btTarget = (byte)(mPos2 & 0xFF);
            } else {
                m_curInventoryBuffer.bLoopCustomizedSession = false;
            }

            //gy.点击“停止存盘”
			if(arg0.getId() == R.id.stop) {
				refreshText();
				mReaderHelper.setInventoryFlag(false);
				m_curInventoryBuffer.bLoopInventoryReal = false;
				
				//mReaderHelper.clearInventoryTotal();
				
				mStop.setEnabled(false);
				mStart.setEnabled(true);
				
				mLoopHandler.removeCallbacks(mLoopRunnable);
				mHandler.removeCallbacks(mRefreshRunnable);
				refreshList();
				return;
			}
			
			m_curInventoryBuffer.clearInventoryRealResult();
			mReaderHelper.setInventoryFlag(true);
			
			mReaderHelper.clearInventoryTotal();
			
			refreshText();
			
			byte btWorkAntenna = m_curInventoryBuffer.lAntenna.get(m_curInventoryBuffer.nIndexAntenna);
			if (btWorkAntenna < 0) btWorkAntenna = 0;
			
			mReader.setWorkAntenna(m_curReaderSetting.btReadId, btWorkAntenna);
			
			m_curReaderSetting.btWorkAntenna = btWorkAntenna;
			
			mRefreshTime = new Date().getTime();
			
			mStop.setEnabled(true);
			mStart.setEnabled(false);
			
			mLoopHandler.removeCallbacks(mLoopRunnable);
			mLoopHandler.postDelayed(mLoopRunnable,2000);
			mHandler.removeCallbacks(mRefreshRunnable);
			mHandler.postDelayed(mRefreshRunnable,2000);
		}
	};
	
	private void refreshList() {
		mTagRealList.refreshList();
	}
	
	private void refreshText() {
		mTagRealList.refreshText();
	}
	
	private void clearText() {
		mTagRealList.clearText();
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				switch (btCmd) {
				case CMD.REAL_TIME_INVENTORY:
				case CMD.CUSTOMIZED_SESSION_TARGET_INVENTORY:
//					if (new Date().getTime() - mRefreshTime > 2000) {
//						refreshList();
//						mRefreshTime = new Date().getTime();
//					}
//					
					mLoopHandler.removeCallbacks(mLoopRunnable);
					mLoopHandler.postDelayed(mLoopRunnable,2000);
					refreshText();
					break;
				case ReaderHelper.INVENTORY_ERR:
				case ReaderHelper.INVENTORY_ERR_END:
				case ReaderHelper.INVENTORY_END:
//					refreshList();
					if (mReaderHelper.getInventoryFlag()) {
						mLoopHandler.removeCallbacks(mLoopRunnable);
						mLoopHandler.postDelayed(mLoopRunnable,2000);
					} else {
						mLoopHandler.removeCallbacks(mLoopRunnable);
					}
					refreshText();
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

