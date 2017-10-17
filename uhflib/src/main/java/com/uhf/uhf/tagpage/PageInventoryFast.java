package com.uhf.uhf.tagpage;


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
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
import com.uhf.uhf.TagFastList;
import com.uhf.uhf.UHFApplication;
import com.uhf.uhf.spiner.AbstractSpinerAdapter.IOnItemSelectListener;
import com.uhf.uhf.spiner.SpinerPopWindow;

import java.util.ArrayList;
import java.util.List;

public class PageInventoryFast extends Activity {
	private LogList mLogList;
	
	private TextView mStart;
	private TextView mStop;
	
	private TextView mRefreshButton;
	
	private TableRow mDropDownRowA, mDropDownRowB, mDropDownRowC, mDropDownRowD;
	
	private TextView mStayATextView, mStayBTextView, mStayCTextView, mStayDTextView;
	private EditText mStayARoundText, mStayBRoundText, mStayCRoundText, mStayDRoundText;
	
	private EditText mFastDelayText, mFastRepeatText;
	
	private List<String> mStayList;
	
	private SpinerPopWindow mSpinerPopWindowA, mSpinerPopWindowB, mSpinerPopWindowC, mSpinerPopWindowD;
	
	private TagFastList mTagFastList;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;

	private int mPosA = -1, mPosB = -1, mPosC = -1, mPosD = -1;
	
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
		setContentView(R.layout.page_inventory_fast);
		((UHFApplication) getApplication()).addActivity(this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mStayList = new ArrayList<String>();
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(R.id.log_list);
		mStart = (TextView) findViewById(R.id.start);
		mStop = (TextView) findViewById(R.id.stop);
		
		mDropDownRowA = (TableRow) findViewById(R.id.table_row_fast_stay_a);
		mDropDownRowB = (TableRow) findViewById(R.id.table_row_fast_stay_b);
		mDropDownRowC = (TableRow) findViewById(R.id.table_row_fast_stay_c);
		mDropDownRowD = (TableRow) findViewById(R.id.table_row_fast_stay_d);
		
		mStayATextView = (TextView) findViewById(R.id.fast_stay_a_text);
		mStayBTextView = (TextView) findViewById(R.id.fast_stay_b_text);
		mStayCTextView = (TextView) findViewById(R.id.fast_stay_c_text);
		mStayDTextView = (TextView) findViewById(R.id.fast_stay_d_text);
		
		mStayARoundText = (EditText) findViewById(R.id.fast_stay_a_round_text);
		mStayBRoundText = (EditText) findViewById(R.id.fast_stay_b_round_text);
		mStayCRoundText = (EditText) findViewById(R.id.fast_stay_c_round_text);
		mStayDRoundText = (EditText) findViewById(R.id.fast_stay_d_round_text);
		
		mFastDelayText = (EditText) findViewById(R.id.fast_delay_text);
		mFastRepeatText = (EditText) findViewById(R.id.fast_repeat_text);
		
		mTagFastList = (TagFastList) findViewById(R.id.tag_fast_list);

		mStart.setOnClickListener(setInventoryFastOnClickListener);
		
		mStop.setOnClickListener(setInventoryFastOnClickListener);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH);
		lbm.registerReceiver(mRecv, itent);
		
		mDropDownRowA.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindowA();
			}
		});
		
		mDropDownRowB.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindowB();
			}
		});
		
		mDropDownRowC.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindowC();
			}
		});
		
		mDropDownRowD.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindowD();
			}
		});
		
		
		mStayList.clear();
		String[] lists = getResources().getStringArray(R.array.fast_antenna_list);
		for(int i = 0; i < lists.length; i++){
			mStayList.add(lists[i]);
		}
		
		mSpinerPopWindowA = new SpinerPopWindow(this);
		mSpinerPopWindowA.refreshData(mStayList, 0);
		mSpinerPopWindowA.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setStayAText(pos);
			}
		});
		
		mSpinerPopWindowB = new SpinerPopWindow(this);
		mSpinerPopWindowB.refreshData(mStayList, 0);
		mSpinerPopWindowB.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setStayBText(pos);
			}
		});
		
		mSpinerPopWindowC = new SpinerPopWindow(this);
		mSpinerPopWindowC.refreshData(mStayList, 0);
		mSpinerPopWindowC.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setStayCText(pos);
			}
		});
		
		mSpinerPopWindowD = new SpinerPopWindow(this);
		mSpinerPopWindowD.refreshData(mStayList, 0);
		mSpinerPopWindowD.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setStayDText(pos);
			}
		});
		
		updateView();
		
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
	            
				setStayAText(0);
				setStayBText(4);
				setStayCText(4);
				setStayDText(4);

				mStayARoundText.setText("1");
				mStayBRoundText.setText("1");
				mStayCRoundText.setText("1");
				mStayDRoundText.setText("1");

				mFastDelayText.setText("0");
				mFastRepeatText.setText("10");
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
        	 mReader.fastSwitchAntInventory(m_curReaderSetting.btReadId, 
						m_curInventoryBuffer.btA, m_curInventoryBuffer.btStayA, m_curInventoryBuffer.btB, m_curInventoryBuffer.btStayB, 
						m_curInventoryBuffer.btC, m_curInventoryBuffer.btStayC, m_curInventoryBuffer.btD, m_curInventoryBuffer.btStayD, 
						m_curInventoryBuffer.btInterval, m_curInventoryBuffer.btFastRepeat);
 			mLoopHandler.postDelayed(this, 2000); 
         }
    };
	
    private void showSpinWindowA() {
		mSpinerPopWindowA.setWidth(mDropDownRowA.getWidth());
		mSpinerPopWindowA.showAsDropDown(mDropDownRowA);
	}
    private void showSpinWindowB() {
		mSpinerPopWindowB.setWidth(mDropDownRowB.getWidth());
		mSpinerPopWindowB.showAsDropDown(mDropDownRowB);
	}
    private void showSpinWindowC() {
		mSpinerPopWindowC.setWidth(mDropDownRowC.getWidth());
		mSpinerPopWindowC.showAsDropDown(mDropDownRowC);
	}
    private void showSpinWindowD() {
		mSpinerPopWindowD.setWidth(mDropDownRowD.getWidth());
		mSpinerPopWindowD.showAsDropDown(mDropDownRowD);
	}
    
    private void setStayAText(int pos){
		if (pos >= 0 && pos < mStayList.size()){
			String value = mStayList.get(pos);
			mStayATextView.setText(value);
			mPosA = pos;
		}
	}
    private void setStayBText(int pos){
    	if (pos >= 0 && pos < mStayList.size()){
			String value = mStayList.get(pos);
			mStayBTextView.setText(value);
			mPosB = pos;
		}
	}
    private void setStayCText(int pos){
    	if (pos >= 0 && pos < mStayList.size()){
			String value = mStayList.get(pos);
			mStayCTextView.setText(value);
			mPosC = pos;
		}
	}
    private void setStayDText(int pos){
    	if (pos >= 0 && pos < mStayList.size()){
			String value = mStayList.get(pos);
			mStayDTextView.setText(value);
			mPosD = pos;
		}
	}

	private void updateView() {
		
		mPosA = m_curInventoryBuffer.btA;
		mPosB = m_curInventoryBuffer.btB;
		mPosC = m_curInventoryBuffer.btC;
		mPosD = m_curInventoryBuffer.btD;
		
		if (mPosA > 4 || mPosA < 0) mPosA = 4;
		if (mPosB > 4 || mPosB < 0) mPosB = 4;
		if (mPosC > 4 || mPosC < 0) mPosC = 4;
		if (mPosD > 4 || mPosD < 0) mPosD = 4;
		
		
		setStayAText(mPosA);
		setStayBText(mPosB);
		setStayCText(mPosC);
		setStayDText(mPosD);
		
		mStayARoundText.setText(String.valueOf(m_curInventoryBuffer.btStayA));
		mStayBRoundText.setText(String.valueOf(m_curInventoryBuffer.btStayB));
		mStayCRoundText.setText(String.valueOf(m_curInventoryBuffer.btStayC));
		mStayDRoundText.setText(String.valueOf(m_curInventoryBuffer.btStayD));

		mFastDelayText.setText(String.valueOf(m_curInventoryBuffer.btInterval));
		
		int nFastRepeat = m_curInventoryBuffer.btFastRepeat & 0xFF;
		mFastRepeatText.setText(String.valueOf(nFastRepeat <= 0 ? 10 : nFastRepeat));
	}
	
	private OnClickListener setInventoryFastOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if(arg0.getId() == R.id.stop) {
				refreshText();
				mReaderHelper.setInventoryFlag(false);
				m_curInventoryBuffer.bLoopInventory = false;
				
				mStop.setEnabled(false);
				mStart.setEnabled(true);
				mLoopHandler.removeCallbacks(mLoopRunnable);
				mHandler.removeCallbacks(mRefreshRunnable);
				refreshList();
				return;
			} else if (arg0.getId() == R.id.start) {
				m_curInventoryBuffer.clearInventoryRealResult();
				mReaderHelper.setInventoryTotal(0);
				refreshList();
				
				m_curInventoryBuffer.bLoopInventory = true;
				
				byte btStayA = 0, btStayB = 0, btStayC = 0, btStayD = 0;
				byte btInterval = 0, btRepeat = 0;
				
				try {
					btStayA = (byte) Integer.parseInt(mStayARoundText.getText().toString());
					btStayB = (byte) Integer.parseInt(mStayBRoundText.getText().toString());
					btStayC = (byte) Integer.parseInt(mStayCRoundText.getText().toString());
					btStayD = (byte) Integer.parseInt(mStayDRoundText.getText().toString());
					btInterval = (byte) Integer.parseInt(mFastDelayText.getText().toString());
					btRepeat = (byte) Integer.parseInt(mFastRepeatText.getText().toString());
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.fast4ant_empty),
							Toast.LENGTH_SHORT).show();
					return ;
				}
				
				if ((mPosA == 4 && mPosB == 4 && mPosC == 4 && mPosD == 4) ||
						(mPosA != 4 && btStayA < 0) ||
						(mPosB != 4 && btStayB < 0) ||
						(mPosC != 4 && btStayC < 0) ||
						(mPosD != 4 && btStayD < 0) ||
						btRepeat <= 0) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.fast4ant_error),
							Toast.LENGTH_SHORT).show();
					return ;
				}
				
				mReader.fastSwitchAntInventory(m_curReaderSetting.btReadId, 
						(byte)mPosA, btStayA, (byte)mPosB, btStayB, 
						(byte)mPosC, btStayC, (byte)mPosD, btStayD, btInterval, btRepeat);
				
				m_curInventoryBuffer.btA = (byte)mPosA;
				m_curInventoryBuffer.btB = (byte)mPosB;
				m_curInventoryBuffer.btC = (byte)mPosC;
				m_curInventoryBuffer.btD = (byte)mPosD;
				
				m_curInventoryBuffer.btStayA = btStayA;
				m_curInventoryBuffer.btStayB = btStayB;
				m_curInventoryBuffer.btStayC = btStayC;
				m_curInventoryBuffer.btStayD = btStayD;
				
				m_curInventoryBuffer.btInterval = btInterval;
				m_curInventoryBuffer.btFastRepeat = btRepeat;
				
				mStop.setEnabled(true);
				mStart.setEnabled(false);
				
				mLoopHandler.removeCallbacks(mLoopRunnable);
				mLoopHandler.postDelayed(mLoopRunnable,2000);
				mHandler.removeCallbacks(mRefreshRunnable);
				mHandler.postDelayed(mRefreshRunnable,2000);
			}
		}
	};
	
	private void refreshList() {
		mTagFastList.refreshList();
	}
	
	private void refreshText() {
		mTagFastList.refreshText();
	}
	
	private void clearText() {
		mTagFastList.clearText();
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				switch (btCmd) {
				case CMD.FAST_SWITCH_ANT_INVENTORY:
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
					refreshText();
					if (mReaderHelper.getInventoryFlag()) {
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

