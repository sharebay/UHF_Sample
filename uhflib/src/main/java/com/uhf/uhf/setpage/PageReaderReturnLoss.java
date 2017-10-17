package com.uhf.uhf.setpage;


import java.util.ArrayList;
import java.util.List;

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
import com.uhf.uhf.spiner.SpinerPopWindow;
import com.uhf.uhf.spiner.AbstractSpinerAdapter.IOnItemSelectListener;

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
import android.widget.TableRow;
import android.widget.TextView;


public class PageReaderReturnLoss extends Activity {
	private LogList mLogList;
	
	private TextView mMeasure;
	
	private EditText mReturnLossText;
	private TextView mMeasureText;
	private TableRow mDropDownRow;
	private List<String> mMeasureList = new ArrayList<String>();
	
	private SpinerPopWindow mSpinerPopWindow;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;
	
	private int mPos = -1;
	
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
		setContentView(R.layout.page_reader_return_loss);
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
		mMeasure = (TextView) findViewById(R.id.measure);
		mReturnLossText =  (EditText) findViewById(R.id.return_loss_text);
		mMeasureText =  (TextView) findViewById(R.id.measure_text);
		mDropDownRow = (TableRow) findViewById(R.id.table_row_spiner_return_loss);

		mMeasure.setOnClickListener(setReturnLossOnClickListener);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
		lbm.registerReceiver(mRecv, itent);
		
		mDropDownRow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindow();
			}
		});
		
		
		String[] lists = getResources().getStringArray(R.array.return_loss_list);
		for(int i = 0; i < lists.length; i++){
			mMeasureList.add(lists[i]);
		}
		
		mSpinerPopWindow = new SpinerPopWindow(this);
		mSpinerPopWindow.refreshData(mMeasureList, 0);
		mSpinerPopWindow.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setMeasureText(pos);
			}
		});
		
		updateView();
	}
	
	private void setMeasureText(int pos){
		if (pos >= 0 && pos < mMeasureList.size()){
			String value = mMeasureList.get(pos);
			mMeasureText.setText(value);
			mPos = pos;
		}
	}
	
	private void showSpinWindow() {
		mSpinerPopWindow.setWidth(mDropDownRow.getWidth());
		mSpinerPopWindow.showAsDropDown(mDropDownRow);
	}
	
	private void updateView() {
		
		mPos = m_curReaderSetting.btReturnLoss;
		
		mReturnLossText.setText(String.valueOf(m_curReaderSetting.btReturnLoss & 0xFF));
	}
	
	private OnClickListener setReturnLossOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (arg0.getId() ==R.id.measure){
				byte btFrequency = (byte)mPos;
				if (btFrequency < 0 || btFrequency > mMeasureList.size()) return;

				mReader.getRfPortReturnLoss(m_curReaderSetting.btReadId, btFrequency);
				m_curReaderSetting.btReturnLoss = btFrequency;
			}
		}
	};
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				if (btCmd == CMD.GET_RF_PORT_RETURN_LOSS) {
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

