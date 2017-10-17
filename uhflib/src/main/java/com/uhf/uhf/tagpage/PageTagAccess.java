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


public class PageTagAccess extends Activity {
	private LogList mLogList;
	
	private TextView mGet, mRead, mSelect, mWrite, mLock, mKill;
	
	private TextView mRefreshButton;

	private TextView mTagAccessListText;
	private TableRow mDropDownRow;
	
	private List<String> mAccessList;
	
	private SpinerPopWindow mSpinerPopWindow;
	
	private HexEditTextBox mPasswordEditText;
	private EditText mStartAddrEditText;
	private EditText mDataLenEditText;
	private HexEditTextBox mDataEditText;
	private HexEditTextBox mLockPasswordEditText;
	private HexEditTextBox mKillPasswordEditText;
	
	private RadioGroup mGroupAccessAreaType;
	private RadioGroup mGroupLockAreaType;
	private RadioGroup mGroupLockType;
	
	private TagAccessList mTagAccessList;
	
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
		setContentView(R.layout.page_tag_access);
		((UHFApplication) getApplication()).addActivity(this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mAccessList = new ArrayList<String>();
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(R.id.log_list);
		mGet = (TextView) findViewById(R.id.get);
		mRead = (TextView) findViewById(R.id.read);
		mSelect = (TextView) findViewById(R.id.select);
		mWrite = (TextView) findViewById(R.id.write);
		mLock = (TextView) findViewById(R.id.lock);
		mKill = (TextView) findViewById(R.id.kill);
		mGet.setOnClickListener(setAccessOnClickListener);
		mRead.setOnClickListener(setAccessOnClickListener);
		mSelect.setOnClickListener(setAccessOnClickListener);
		mWrite.setOnClickListener(setAccessOnClickListener);
		mLock.setOnClickListener(setAccessOnClickListener);
		mKill.setOnClickListener(setAccessOnClickListener);
		
		mPasswordEditText = (HexEditTextBox) findViewById(R.id.password_text);
		mStartAddrEditText = (EditText) findViewById(R.id.start_addr_text);
		mDataLenEditText = (EditText) findViewById(R.id.data_length_text);
		mDataEditText = (HexEditTextBox) findViewById(R.id.data_write_text);
		mLockPasswordEditText = (HexEditTextBox) findViewById(R.id.lock_password_text);
		mKillPasswordEditText = (HexEditTextBox) findViewById(R.id.kill_password_text);
		
		mGroupAccessAreaType = (RadioGroup) findViewById(R.id.group_access_area_type);
		mGroupLockAreaType = (RadioGroup) findViewById(R.id.group_lock_area_type);
		mGroupLockType = (RadioGroup) findViewById(R.id.group_lock_type);
		
		mTagAccessListText =  (TextView) findViewById(R.id.tag_access_list_text);
		mDropDownRow = (TableRow) findViewById(R.id.table_row_tag_access_list);

		mTagAccessList = (TagAccessList) findViewById(R.id.tag_access_list);
		
		lbm  = LocalBroadcastManager.getInstance(this);
		
		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG);
		lbm.registerReceiver(mRecv, itent);
		
		mDropDownRow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showSpinWindow();
			}
		});
		
		mAccessList.clear();
		mAccessList.add("Cancel");
		for(int i = 0; i < m_curInventoryBuffer.lsTagList.size(); i++){
			mAccessList.add(m_curInventoryBuffer.lsTagList.get(i).strEPC);
		}
		
		mSpinerPopWindow = new SpinerPopWindow(this);
		mSpinerPopWindow.refreshData(mAccessList, 0);
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
				mTagAccessListText.setText("");
				mPos = -1;
				
				mStartAddrEditText.setText("");
				mDataLenEditText.setText("");
				mDataEditText.setText("");
				mPasswordEditText.setText("");
				mLockPasswordEditText.setText("");
				mKillPasswordEditText.setText("");
				
				mGroupAccessAreaType.check(R.id.set_access_area_password);
				mGroupLockAreaType.check(R.id.set_lock_area_access_password);
				mGroupLockType.check(R.id.set_lock_free);
				
				m_curOperateTagBuffer.clearBuffer();
				refreshList();
				refreshText();
				clearText();
			}
		});
	}
	
	private void setAccessSelectText(int pos){
		if (pos >= 0 && pos < mAccessList.size()){
			String value = mAccessList.get(pos);
			mTagAccessListText.setText(value);
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
			int id = arg0.getId();
			if (id == R.id.get) {
				mReader.getAccessEpcMatch(m_curReaderSetting.btReadId);
			} else if (id == R.id.select) {
				if (mPos <= 0) {
					mReader.cancelAccessEpcMatch(m_curReaderSetting.btReadId);
				} else {
					byte[] btAryEpc = null;

					try {
						String[] result = StringTool.stringToStringArray(mAccessList.get(mPos).toUpperCase(), 2);
						btAryEpc = StringTool.stringArrayToByteArray(result, result.length);
					} catch (Exception e) {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(R.string.param_unknown_error),
								Toast.LENGTH_SHORT).show();
						return;
					}

					if (btAryEpc == null) {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(R.string.param_unknown_error),
								Toast.LENGTH_SHORT).show();
						return;
					}

					mReader.setAccessEpcMatch(m_curReaderSetting.btReadId, (byte)(btAryEpc.length & 0xFF), btAryEpc);
				}
			} else if (id == R.id.read || id == R.id.write) {
				byte btMemBank = 0x00;
				byte btWordAdd = 0x00;
				byte btWordCnt = 0x00;
				byte []btAryPassWord = null;
				if (mGroupAccessAreaType.getCheckedRadioButtonId() == R.id.set_access_area_password) {
					btMemBank = 0x00;
				} else if (mGroupAccessAreaType.getCheckedRadioButtonId() == R.id.set_access_area_epc) {
					btMemBank = 0x01;
				} else if (mGroupAccessAreaType.getCheckedRadioButtonId() == R.id.set_access_area_tid) {
					btMemBank = 0x02;
				} else if (mGroupAccessAreaType.getCheckedRadioButtonId() == R.id.set_access_area_user) {
					btMemBank = 0x03;
				}

				try {
					btWordAdd = (byte) Integer.parseInt(mStartAddrEditText.getText().toString());
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_start_addr_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				try {
					String[] reslut = StringTool.stringToStringArray(mPasswordEditText.getText().toString().toUpperCase(), 2);
					btAryPassWord = StringTool.stringArrayToByteArray(reslut, 4);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_password_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (arg0.getId() == R.id.read) {
					try {
						btWordCnt = (byte) Integer.parseInt(mDataLenEditText.getText().toString());
					} catch (Exception e) {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(R.string.param_data_len_error),
								Toast.LENGTH_SHORT).show();
						return;
					}

					if ((btWordCnt & 0xFF) <= 0) {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(R.string.param_data_len_error),
								Toast.LENGTH_SHORT).show();
						return;
					}

					m_curOperateTagBuffer.clearBuffer();
					refreshList();
					mReader.readTag(m_curReaderSetting.btReadId, btMemBank, btWordAdd, btWordCnt, btAryPassWord);
				} else {
					byte[] btAryData = null;
					String[] result = null;
					try {
						result = StringTool.stringToStringArray(mDataEditText.getText().toString().toUpperCase(), 2);
						btAryData = StringTool.stringArrayToByteArray(result, result.length);
						btWordCnt = (byte)((result.length / 2 + result.length % 2) & 0xFF);
					} catch (Exception e) {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(R.string.param_data_error),
								Toast.LENGTH_SHORT).show();
						return;
					}

					if (btAryData == null || btAryData.length <= 0) {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(R.string.param_data_error),
								Toast.LENGTH_SHORT).show();
						return;
					}

					if (btAryPassWord == null || btAryPassWord.length < 4) {
						Toast.makeText(
								getApplicationContext(),
								getResources().getString(R.string.param_password_error),
								Toast.LENGTH_SHORT).show();
						return;
					}

					mDataLenEditText.setText(String.valueOf(btWordCnt & 0xFF));

					m_curOperateTagBuffer.clearBuffer();
					refreshList();
					mReader.writeTag(m_curReaderSetting.btReadId, btAryPassWord, btMemBank, btWordAdd, btWordCnt, btAryData);
				}
			} else if (id == R.id.lock) {
				byte btMemBank = 0x00;
				byte btLockType = 0x00;
				byte []btAryPassWord = null;
				if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_access_password) {
					btMemBank = 0x04;
				} else if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_kill_password) {
					btMemBank = 0x05;
				} else if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_epc) {
					btMemBank = 0x03;
				} else if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_tid) {
					btMemBank = 0x02;
				} else if (mGroupLockAreaType.getCheckedRadioButtonId() == R.id.set_lock_area_user) {
					btMemBank = 0x01;
				}

				if (mGroupLockType.getCheckedRadioButtonId() == R.id.set_lock_free) {
					btLockType = 0x00;
				} else if (mGroupLockType.getCheckedRadioButtonId() == R.id.set_lock_free_ever) {
					btLockType = 0x02;
				} else if (mGroupLockType.getCheckedRadioButtonId() == R.id.set_lock_lock) {
					btLockType = 0x01;
				} else if (mGroupLockType.getCheckedRadioButtonId() == R.id.set_lock_lock_ever) {
					btLockType = 0x03;
				}

				try {
					String[] reslut = StringTool.stringToStringArray(mLockPasswordEditText.getText().toString().toUpperCase(), 2);
					btAryPassWord = StringTool.stringArrayToByteArray(reslut, 4);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_lockpassword_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (btAryPassWord == null || btAryPassWord.length < 4) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_lockpassword_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				m_curOperateTagBuffer.clearBuffer();
				refreshList();
				mReader.lockTag(m_curReaderSetting.btReadId, btAryPassWord, btMemBank, btLockType);

			} else if (id == R.id.kill) {
				byte []btAryPassWord = null;
				try {
					String[] reslut = StringTool.stringToStringArray(mKillPasswordEditText.getText().toString().toUpperCase(), 2);
					btAryPassWord = StringTool.stringArrayToByteArray(reslut, 4);
				} catch (Exception e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_killpassword_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (btAryPassWord == null || btAryPassWord.length < 4) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.param_killpassword_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				m_curOperateTagBuffer.clearBuffer();
				refreshList();
				mReader.killTag(m_curReaderSetting.btReadId, btAryPassWord);
			}
		}
	};
	
	private void refreshList() {
		mTagAccessList.refreshList();
	}
	
	private void refreshText() {
		mTagAccessList.refreshText();
	}
	
	private void clearText() {
		mTagAccessList.clearText();
	}
	
	private final BroadcastReceiver mRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				
				switch (btCmd) {
				case CMD.GET_ACCESS_EPC_MATCH:
					mTagAccessListText.setText(m_curOperateTagBuffer.strAccessEpcMatch);
					break;
				case CMD.READ_TAG:
				case CMD.WRITE_TAG:
				case CMD.LOCK_TAG:
				case CMD.KILL_TAG:
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

