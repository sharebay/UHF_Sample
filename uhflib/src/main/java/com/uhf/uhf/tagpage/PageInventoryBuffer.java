package com.uhf.uhf.tagpage;


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
import com.uhf.uhf.TagBufferList;
import com.uhf.uhf.TagRealList;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class PageInventoryBuffer extends Activity {
	private LogList mLogList;
	
	private TextView mStart;
	private TextView mStop;
	
	private TextView mGetBuffer;
	private TextView mGetClearBuffer;
	private TextView mClearBuffer;
	private TextView mQueryBuffer;
	
	private TextView mRefreshButton;

	private CheckBox mCbAntenna1, mCbAntenna2, mCbAntenna3, mCbAntenna4;
	
	private EditText mBufferRoundEditText;
	
	private TagBufferList mTagBufferList;
	
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
		setContentView(R.layout.page_inventory_buffer);
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
		mStart = (TextView) findViewById(R.id.start);
		mStop = (TextView) findViewById(R.id.stop);
		
		mGetBuffer = (TextView) findViewById(R.id.tag_get_buffer);
		mGetClearBuffer = (TextView) findViewById(R.id.tag_get_clear_buffer);
		mClearBuffer = (TextView) findViewById(R.id.tag_clear_buffer);
		mQueryBuffer = (TextView) findViewById(R.id.tag_query_buffer);
		
		mStart.setOnClickListener(setInventoryOnClickListener);
		mStop.setOnClickListener(setInventoryOnClickListener);
		mGetBuffer.setOnClickListener(setInventoryOnClickListener);
		mGetClearBuffer.setOnClickListener(setInventoryOnClickListener);
		mClearBuffer.setOnClickListener(setInventoryOnClickListener);
		mQueryBuffer.setOnClickListener(setInventoryOnClickListener);
		
		mCbAntenna1 = (CheckBox) findViewById(R.id.check_antenna_1);
		mCbAntenna2 = (CheckBox) findViewById(R.id.check_antenna_2);
		mCbAntenna3 = (CheckBox) findViewById(R.id.check_antenna_3);
		mCbAntenna4 = (CheckBox) findViewById(R.id.check_antenna_4);
		
		mBufferRoundEditText = (EditText) findViewById(R.id.buffer_round_text);
		
		mTagBufferList = (TagBufferList) findViewById(R.id.tag_buffer_list);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY);
		lbm.registerReceiver(mRecv, itent);
		
		updateView();

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
	            
	            mBufferRoundEditText.setText("1");
	            mCbAntenna1.setChecked(true);
	            mCbAntenna2.setChecked(false);
	            mCbAntenna3.setChecked(false);
	            mCbAntenna4.setChecked(false);
			}
		});
	}

	private void updateView() {

		mCbAntenna1.setChecked(false);
		mCbAntenna2.setChecked(false);
		mCbAntenna3.setChecked(false);
		mCbAntenna4.setChecked(false);
		
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
		mBufferRoundEditText.setText(String.valueOf(nRepeat <=0 ? 1 : nRepeat));
	}
	
	private OnClickListener setInventoryOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			
			if(arg0.getId() == R.id.start || arg0.getId() == R.id.stop) {
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
				
				m_curInventoryBuffer.bLoopInventory = true;
				m_curInventoryBuffer.btRepeat = 0;
				
				String strRepeat = mBufferRoundEditText.getText().toString();				
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

				if(arg0.getId() == R.id.stop) {
					refreshText();
					mReaderHelper.setInventoryFlag(false);
					m_curInventoryBuffer.bLoopInventory = false;
					
					mReaderHelper.clearInventoryTotal();
					
					mStop.setEnabled(false);
					mStart.setEnabled(true);
					
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
				
				mStop.setEnabled(true);
				mStart.setEnabled(false);
			} else if (arg0.getId() == R.id.tag_get_buffer) {
				m_curInventoryBuffer.clearTagMap();
				refreshList();
				mReader.getInventoryBuffer(m_curReaderSetting.btReadId);
			} else if (arg0.getId() == R.id.tag_get_clear_buffer) {
				m_curInventoryBuffer.clearTagMap();
				refreshList();
				mReader.getAndResetInventoryBuffer(m_curReaderSetting.btReadId);
			} else if (arg0.getId() == R.id.tag_clear_buffer) {
	            m_curInventoryBuffer.clearInventoryRealResult();
	            refreshList();
	            refreshText();
	            mReader.resetInventoryBuffer(m_curReaderSetting.btReadId);
			} else if (arg0.getId() == R.id.tag_query_buffer) {
				mReader.getInventoryBufferTagCount(m_curReaderSetting.btReadId);
			}
		}
	};
	
	private void refreshList() {
		mTagBufferList.refreshList();
	}

	private void refreshText() {
		mTagBufferList.refreshText();
	}
	
	private void clearText() {
		mTagBufferList.clearText();
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_INVENTORY)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				switch (btCmd) {
				case CMD.INVENTORY:
					refreshText();
					break;
				case CMD.GET_INVENTORY_BUFFER:
				case CMD.GET_AND_RESET_INVENTORY_BUFFER:
					refreshList();
					break;
				case CMD.RESET_INVENTORY_BUFFER:
					refreshList();
					break;
				case ReaderHelper.INVENTORY_ERR:
				case ReaderHelper.INVENTORY_ERR_END:
				case ReaderHelper.INVENTORY_END:
					refreshList();
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
	}
}

