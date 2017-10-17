package com.uhf.uhf.tagpage;


import java.util.ArrayList;
import java.util.List;

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
import com.uhf.uhf.HexEditTextBox;
import com.uhf.uhf.LogList;
import com.uhf.uhf.TagAccessList;
import com.uhf.uhf.TagReal6BList;
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
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


public class PageTag6BAccess extends Activity {
	private LogList mLogList;
	
	private TextView mRead, mWrite, mSetWP, mGetWP;
	
	private TextView mRefreshButton;

	private TextView mTag6BUIDListText;
	private TableRow mDropDownRow;
	
	private List<String> mUIDList;
	
	private SpinerPopWindow mSpinerPopWindow;
	
	private HexEditTextBox mReadStartAddrEditText;
	private HexEditTextBox mReadDataLenEditText;
	private HexEditTextBox mReadDataEditText;
	
	private HexEditTextBox mWriteStartAddrEditText;
	private HexEditTextBox mWriteDataLenEditText;
	//private EditText mWriteDataRoundText;
	//private EditText mWriteDataTimesText;
	private HexEditTextBox mWriteDataEditText;
	
	private HexEditTextBox mSetWPAddrEditText;
	private HexEditTextBox mGetWPAddrEditText;
	private EditText mWPStatusText;

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
		setContentView(R.layout.page_tag_6b_access);
		((UHFApplication) getApplication()).addActivity(this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mUIDList = new ArrayList<String>();
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(R.id.log_list);
		mRead = (TextView) findViewById(R.id.read);
		mWrite = (TextView) findViewById(R.id.write);
		mSetWP = (TextView) findViewById(R.id.set_wp_ever);
		mGetWP = (TextView) findViewById(R.id.get_wp_ever);
		mRead.setOnClickListener(setAccessOnClickListener);
		mWrite.setOnClickListener(setAccessOnClickListener);
		mWrite.setOnClickListener(setAccessOnClickListener);
		mSetWP.setOnClickListener(setAccessOnClickListener);
		mGetWP.setOnClickListener(setAccessOnClickListener);
		
		mReadStartAddrEditText = (HexEditTextBox) findViewById(R.id.read_start_addr_text);
		mReadDataLenEditText = (HexEditTextBox) findViewById(R.id.read_data_len_text);
		mReadDataEditText = (HexEditTextBox) findViewById(R.id.read_data_show_text);
		mWriteStartAddrEditText = (HexEditTextBox) findViewById(R.id.write_start_addr_text);
		mWriteDataLenEditText = (HexEditTextBox) findViewById(R.id.write_data_len_text);
		//mWriteDataRoundText = (EditText) findViewById(R.id.write_round_times_text);
		//mWriteDataTimesText = (EditText) findViewById(R.id.write_round_ok_times_text);
		mWriteDataEditText = (HexEditTextBox) findViewById(R.id.write_data_show_text);
		
		mSetWPAddrEditText = (HexEditTextBox) findViewById(R.id.tag_6b_wp_ever_addr_text);
		mGetWPAddrEditText = (HexEditTextBox) findViewById(R.id.tag_6b_get_wp_ever_addr_text);
		mWPStatusText = (EditText) findViewById(R.id.tag_6b_wp_ever_status_text);
		
		mTag6BUIDListText =  (TextView) findViewById(R.id.tag_6b_uid_list_text);
		mDropDownRow = (TableRow) findViewById(R.id.table_row_tag_6b_uid_list);

		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B);
		lbm.registerReceiver(mRecv, itent);
		
		mDropDownRow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindow();
			}
		});
		
		mUIDList.clear();
		for(int i = 0; i < m_curOperateTagISO18000Buffer.lsTagList.size(); i++){
			mUIDList.add(m_curOperateTagISO18000Buffer.lsTagList.get(i).strUID);
		}
		
		mSpinerPopWindow = new SpinerPopWindow(this);
		mSpinerPopWindow.refreshData(mUIDList, 0);
		mSpinerPopWindow.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setAccessSelectText(pos);
			}
		});
		
		updateView();
		
		mRefreshButton = (TextView) findViewById(R.id.refresh);
		mRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mReadStartAddrEditText.setText("");
				mReadDataLenEditText.setText("");
				mReadDataEditText.setText("");
				mWriteStartAddrEditText.setText("");
				mWriteDataLenEditText.setText("");
				mWriteDataEditText.setText("");
				mSetWPAddrEditText.setText("");
				mGetWPAddrEditText.setText("");
				mWPStatusText.setText("");
			}
		});
	}
	
	private void setAccessSelectText(int pos){
		if (pos >= 0 && pos < mUIDList.size()){
			String value = mUIDList.get(pos);
			mTag6BUIDListText.setText(value);
			mPos = pos;
		}
	}
	
	private void showSpinWindow() {
		mSpinerPopWindow.setWidth(mDropDownRow.getWidth());
		mSpinerPopWindow.showAsDropDown(mDropDownRow);
	}
	
	private void updateView() {
		if (mPos < 0) mPos = 0;
		setAccessSelectText(mPos);
	}
	
	private OnClickListener setAccessOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			String strText = mTag6BUIDListText.getText().toString();
			String[] result = null;
			byte[] btAryUID = null;
			
			if (strText == null || strText.length() <= 0) {
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.uid_select),
						Toast.LENGTH_SHORT).show();
				return ;
			}
			
			
			try {
				result = StringTool.stringToStringArray(strText.toUpperCase(), 2);
				btAryUID = StringTool.stringArrayToByteArray(result, 8);
			} catch (Exception e) {
            	Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.uid_error),
						Toast.LENGTH_SHORT).show();
				return;
            }

            int id = arg0.getId();
			if (id == R.id.read){
				byte btWordAdd = 0x00, btWordCnt = 0x00;

				try {
					btWordAdd = (byte) (Integer.parseInt(mReadStartAddrEditText.getText().toString(), 16) & 0xFF);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_read_start_addr_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				try {
					btWordCnt = (byte) (Integer.parseInt(mReadDataLenEditText.getText().toString(), 16) & 0xFF);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_read_data_len_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				mReader.iso180006BReadTag(m_curReaderSetting.btReadId, btAryUID, btWordAdd, btWordCnt);
			} else if (id == R.id.write) {
				byte btWordAdd = 0x00, btWordCnt = 0x00;
				byte btAryBuffer[] = null;

				try {
					result = StringTool.stringToStringArray(mWriteDataEditText.getText().toString().toUpperCase(), 2);
					btAryBuffer = StringTool.stringArrayToByteArray(result, result.length);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_write_data_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				try {
					btWordAdd = (byte) (Integer.parseInt(mWriteStartAddrEditText.getText().toString(), 16) & 0xFF);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_write_start_addr_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				btWordCnt = (byte) (result.length & 0xFF);

				mWriteDataLenEditText.setText(String.format("%02X", btWordCnt));

				mReader.iso180006BWriteTag(m_curReaderSetting.btReadId, btAryUID, btWordAdd, btWordCnt, btAryBuffer);
			} else if (id == R.id.set_wp_ever) {
				byte btWordAdd = 0x00;

				try {
					btWordAdd = (byte) (Integer.parseInt(mSetWPAddrEditText.getText().toString(), 16) & 0xFF);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_wp_ever_addr_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				mReader.iso180006BLockTag(m_curReaderSetting.btReadId, btAryUID, btWordAdd);
			} else if (id == R.id.get_wp_ever) {
				byte btWordAdd = 0x00;

				try {
					btWordAdd = (byte) (Integer.parseInt(mGetWPAddrEditText.getText().toString(), 16) & 0xFF);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_get_wp_ever_addr_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				mReader.iso180006BQueryLockTag(m_curReaderSetting.btReadId, btAryUID, btWordAdd);
			}
		}
	};

	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				switch (btCmd) {
				case CMD.ISO18000_6B_READ_TAG:
					mReadDataEditText.setText(m_curOperateTagISO18000Buffer.strReadData);
					break;
				case CMD.ISO18000_6B_QUERY_LOCK_TAG:
					switch (m_curOperateTagISO18000Buffer.btStatus & 0xFF) {
					case 0x00:
						mWPStatusText.setText(getResources().getString(R.string.unlocked));
						break;
					case 0xFE:
						mWPStatusText.setText(getResources().getString(R.string.locked));
						break;
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
	}
}

