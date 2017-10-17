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
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TableRow;
import android.widget.TextView;


public class PageReaderRegion extends Activity {
	private LogList mLogList;
	
	private LinearLayout mRegionDefaultLayout, mRegionCustomLayout;
	
	private TextView mGet, mSet;
	
	private RadioGroup mGroupRegion;
	private RadioGroup mGroupRegionDefaultType;
	private TextView mFreqStartText, mFreqEndText;
	private TableRow mDropDownRow1, mDropDownRow2;
	private List<String> mFreqStartList = new ArrayList<String>();
	private List<String> mFreqEndList = new ArrayList<String>();
	
	private EditText mFreqStartEditText, mFreqIntervalEditText, mFreqNumsEditText;
	
	private SpinerPopWindow mSpinerPopWindow1;
	private SpinerPopWindow mSpinerPopWindow2;
	
	private ReaderHelper mReaderHelper;
	private ReaderBase mReader;
	
	private int mPos1 = -1, mPos2 = -1;
	
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
		setContentView(R.layout.page_reader_region);
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
		mGroupRegion = (RadioGroup) findViewById(R.id.group_region);
		mGroupRegionDefaultType = (RadioGroup) findViewById(R.id.group_region_default_type);
		mFreqStartText = (TextView) findViewById(R.id.freq_start_text); 
		mFreqEndText = (TextView) findViewById(R.id.freq_end_text);
		
		mFreqStartEditText = (EditText) findViewById(R.id.set_freq_start_text); 
		mFreqIntervalEditText = (EditText) findViewById(R.id.set_freq_interval_text); 
		mFreqNumsEditText = (EditText) findViewById(R.id.set_freq_nums_text); 
		
		mDropDownRow1 = (TableRow) findViewById(R.id.table_row_spiner_freq_start);
		mDropDownRow2 = (TableRow) findViewById(R.id.table_row_spiner_freq_end);
		
		mRegionDefaultLayout = (LinearLayout) findViewById(R.id.layout_region_default);
		mRegionCustomLayout = (LinearLayout) findViewById(R.id.layout_region_custom);
		
		mGet = (TextView) findViewById(R.id.get);
		mSet = (TextView) findViewById(R.id.set);
		
		mGet.setOnClickListener(setRegionOnClickListener);
		mSet.setOnClickListener(setRegionOnClickListener);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
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

		mSpinerPopWindow1 = new SpinerPopWindow(this);
		mSpinerPopWindow1.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setFreqStartText(pos);
			}
		});
		
		mSpinerPopWindow2 = new SpinerPopWindow(this);
		mSpinerPopWindow2.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setFreqEndText(pos);
			}
		});
		
		updateView();
		changeData();
		
		if (mGroupRegion.getCheckedRadioButtonId() == R.id.set_region_default) {
			mRegionDefaultLayout.setVisibility(View.VISIBLE);
			mRegionCustomLayout.setVisibility(View.GONE);
		} else if (mGroupRegion.getCheckedRadioButtonId() == R.id.set_region_custom) {
			mRegionDefaultLayout.setVisibility(View.GONE);
			mRegionCustomLayout.setVisibility(View.VISIBLE);
		}
		
		mGroupRegion.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (mGroupRegion.getCheckedRadioButtonId() == R.id.set_region_default) {
					mRegionDefaultLayout.setVisibility(View.VISIBLE);
					mRegionCustomLayout.setVisibility(View.GONE);
				} else if (mGroupRegion.getCheckedRadioButtonId() == R.id.set_region_custom) {
					mRegionDefaultLayout.setVisibility(View.GONE);
					mRegionCustomLayout.setVisibility(View.VISIBLE);
				}
			}
		});
		
		mGroupRegionDefaultType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				mFreqStartText.setText("");
		    	mFreqEndText.setText("");
				changeData();
			}
		});
	}
	
	private void changeData() {
		float nStart = 0x0;
		int nloop = 0;
		
		mFreqStartList.clear();
		mFreqEndList.clear();
		if (mGroupRegionDefaultType.getCheckedRadioButtonId() == R.id.set_region_fcc) {
	        nStart = 902.00f;
	        for (nloop = 0; nloop < 53; nloop++) {
	            String strTemp = String.format("%.2f", nStart);
	            mFreqStartList.add(strTemp);
	            mFreqEndList.add(strTemp);
	            nStart += 0.5f;
	        }
        } else if (mGroupRegionDefaultType.getCheckedRadioButtonId() == R.id.set_region_etsi) {
        	nStart = 865.00f;
            for (nloop = 0; nloop < 7; nloop++) {
            	String strTemp = String.format("%.2f", nStart);
	            mFreqStartList.add(strTemp);
	            mFreqEndList.add(strTemp);
	            nStart += 0.5f;
            }
        } else if (mGroupRegionDefaultType.getCheckedRadioButtonId() == R.id.set_region_chn) {
            nStart = 920.00f;
            for (nloop = 0; nloop < 11; nloop++) {
            	String strTemp = String.format("%.2f", nStart);
	            mFreqStartList.add(strTemp);
	            mFreqEndList.add(strTemp);
	            nStart += 0.5f;
            }
        }
		
		mSpinerPopWindow1.refreshData(mFreqStartList, 0);
		mSpinerPopWindow2.refreshData(mFreqEndList, 0);
	}
	
	private void setFreqStartText(int pos){
		if (pos >= 0 && pos < mFreqStartList.size()){
			String value = mFreqStartList.get(pos);
			mFreqStartText.setText(value);
			mPos1 = pos;
		}
	}
	
	private void setFreqEndText(int pos){
		if (pos >= 0 && pos < mFreqStartList.size()){
			String value = mFreqStartList.get(pos);
			mFreqEndText.setText(value);
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

		switch(m_curReaderSetting.btRegion & 0xFF) {
            case 0x01:
                	mGroupRegion.check(R.id.set_region_default);
                	mFreqStartEditText.setText("");
                	mFreqIntervalEditText.setText("");
                	mFreqNumsEditText.setText("");
                	mGroupRegionDefaultType.check(R.id.set_region_fcc);
                	changeData();
                	
                	mPos1 = (m_curReaderSetting.btFrequencyStart & 0xFF) - 7;
                	if (mPos1 >= 0 && mPos1 < mFreqStartList.size())
                		mFreqStartText.setText(mFreqStartList.get(mPos1));
                	
                	mPos2 = (m_curReaderSetting.btFrequencyEnd & 0xFF) - 7;
                	if (mPos2 >= 0 && mPos2 < mFreqEndList.size())
                		mFreqEndText.setText(mFreqEndList.get(mPos2));
                break;
            case 0x02:
            	mGroupRegion.check(R.id.set_region_default);
            	mFreqStartEditText.setText("");
            	mFreqIntervalEditText.setText("");
            	mFreqNumsEditText.setText("");
            	mGroupRegionDefaultType.check(R.id.set_region_etsi);
            	changeData();
            	
            	mPos1 = m_curReaderSetting.btFrequencyStart & 0xFF;
            	if (mPos1 >= 0 && mPos1 < mFreqStartList.size())
            		mFreqStartText.setText(mFreqStartList.get(mPos1));
            	
            	mPos2 = m_curReaderSetting.btFrequencyEnd & 0xFF;
            	if (mPos2 >= 0 && mPos2 < mFreqEndList.size())
            		mFreqEndText.setText(mFreqEndList.get(mPos2));
                break;
            case 0x03:
            	mGroupRegion.check(R.id.set_region_default);
            	mFreqStartEditText.setText("");
            	mFreqIntervalEditText.setText("");
            	mFreqNumsEditText.setText("");
            	mGroupRegionDefaultType.check(R.id.set_region_chn);
            	changeData();
            	
            	mPos1 = (m_curReaderSetting.btFrequencyStart & 0xFF) - 43;
            	if (mPos1 >= 0 && mPos1 < mFreqStartList.size())
            		mFreqStartText.setText(mFreqStartList.get(mPos1));
            	
            	mPos2 = (m_curReaderSetting.btFrequencyEnd & 0xFF) - 43;
            	if (mPos2 >= 0 && mPos2 < mFreqEndList.size())
            		mFreqEndText.setText(mFreqEndList.get(mPos2));
                break;
            case 0x04:
            	mGroupRegion.check(R.id.set_region_custom);
            	
            	mFreqStartEditText.setText(String.valueOf(m_curReaderSetting.nUserDefineStartFrequency));
            	mFreqIntervalEditText.setText(String.valueOf((m_curReaderSetting.btUserDefineFrequencyInterval & 0xFF) * 10));
            	mFreqNumsEditText.setText(String.valueOf((m_curReaderSetting.btUserDefineChannelQuantity & 0xFF)));
                break;
            default:
                break;
        }
	}
	
	private OnClickListener setRegionOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			int id = arg0.getId();
			if (id == R.id.get){
				mReader.getFrequencyRegion(m_curReaderSetting.btReadId);
			}else if (id == R.id.set){
				byte btRegion = 0x00, btStartFreq = 0x00, btEndFreq = 0x00;

				if (mGroupRegion.getCheckedRadioButtonId() == R.id.set_region_default) {
					if (mGroupRegionDefaultType.getCheckedRadioButtonId() == R.id.set_region_fcc) {
						btRegion = 0x01;
						btStartFreq = (byte) (mPos1 + 7);
						btEndFreq = (byte) (mPos2 + 7);
					} else if (mGroupRegionDefaultType.getCheckedRadioButtonId() == R.id.set_region_etsi) {
						btRegion = 0x02;
						btStartFreq = (byte) (mPos1);
						btEndFreq = (byte) (mPos2);
					} else if (mGroupRegionDefaultType.getCheckedRadioButtonId() == R.id.set_region_chn) {
						btRegion = 0x03;
						btStartFreq = (byte) (mPos1 + 43);
						btEndFreq = (byte) (mPos2 + 43);
					} else return;

					mReader.setFrequencyRegion(m_curReaderSetting.btReadId, btRegion, btStartFreq, btEndFreq);

					m_curReaderSetting.btRegion = btRegion;
					m_curReaderSetting.btFrequencyStart = btStartFreq;
					m_curReaderSetting.btFrequencyEnd = btEndFreq;

				} else if (mGroupRegion.getCheckedRadioButtonId() == R.id.set_region_custom) {
					int nStartFrequency = 0;
					int nFrequencyInterval = 0;
					byte btChannelQuantity = 0;
					try {
						nStartFrequency = Integer.parseInt(mFreqStartEditText.getText().toString());
						nFrequencyInterval = Integer.parseInt(mFreqIntervalEditText.getText().toString());
						nFrequencyInterval = nFrequencyInterval / 10;
						btChannelQuantity = (byte) Integer.parseInt(mFreqNumsEditText.getText().toString());
					} catch (Exception e) {
						return ;
					}

					mReader.setUserDefineFrequency(m_curReaderSetting.btReadId, (byte)nFrequencyInterval, btChannelQuantity, nStartFrequency);

					m_curReaderSetting.btRegion = 4;
					m_curReaderSetting.nUserDefineStartFrequency = nStartFrequency;
					m_curReaderSetting.btUserDefineFrequencyInterval = (byte)nFrequencyInterval;
					m_curReaderSetting.btUserDefineChannelQuantity = btChannelQuantity;
				}
			}
		}
	};
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				if (btCmd == CMD.SET_FREQUENCY_REGION || btCmd == CMD.GET_FREQUENCY_REGION) {
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

