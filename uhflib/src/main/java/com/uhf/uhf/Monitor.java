package com.uhf.uhf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.gy.vam.uhflib.R;
import com.uhf.reader.base.ERROR;
import com.uhf.reader.base.MessageTran;
import com.uhf.reader.base.ReaderBase;
import com.uhf.reader.base.StringTool;
import com.uhf.reader.helper.ISO180006BOperateTagBuffer;
import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.OperateTagBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.reader.helper.ReaderSetting;
import com.uhf.uhf.setpage.PageReaderAddress;
import com.uhf.uhf.setpage.PageReaderFirmwareVersion;
import com.uhf.uhf.setpage.PageReaderIdentifier;
import com.uhf.uhf.setpage.PageReaderTemperature;
import com.uhf.uhf.tagpage.PageInventoryBuffer;
import com.uhf.uhf.tagpage.PageInventoryFast;
import com.uhf.uhf.tagpage.PageInventoryReal;
import com.uhf.uhf.tagpage.PageInventoryReal6B;
import com.uhf.uhf.tagpage.PageTag6BAccess;
import com.uhf.uhf.tagpage.PageTagAccess;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

public class Monitor extends LinearLayout {
	private Context mContext;
	
	private TextView mDataSendButton;
	private HexEditTextBox mDataText;
	private HexEditTextBox mDataCheck;
	
	private CheckBox mCheckOpenMonitor;
	private TextView mRefreshButton;
	private ListView mMonitorList;
	private ArrayAdapter<CharSequence> mMonitorListAdapter;
	private ArrayList<CharSequence> mMonitorListItem;
	
	private ReaderBase mReader;
	private ReaderHelper mReaderHelper;
	
	private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;
    private static OperateTagBuffer m_curOperateTagBuffer;
    private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
	
	public Monitor(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.monitor, this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			return ;
		}
		
		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();
		
		mCheckOpenMonitor = (CheckBox) findViewById(R.id.check_open_monitor);
		

		mMonitorList = (ListView)findViewById(R.id.monitor_list_view);
		
		mMonitorListItem = new ArrayList<CharSequence>();
		
		mMonitorListAdapter = new ArrayAdapter<CharSequence>(mContext, R.layout.monitor_list_item, mMonitorListItem);
		
		mMonitorList.setAdapter(mMonitorListAdapter);
		
		mRefreshButton = (TextView) findViewById(R.id.refresh);
		mRefreshButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mDataText.setText("");
				mMonitorListItem.clear();
				mMonitorListAdapter.notifyDataSetChanged();
			}
		});
		
		mDataText = (HexEditTextBox) findViewById(R.id.data_send_text);
		
		mDataText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				String[] result = StringTool.stringToStringArray(mDataText.getText().toString().toUpperCase(), 2);
				try {
					byte[] buf = StringTool.stringArrayToByteArray(result, result.length);
					MessageTran msgTran = new MessageTran();
					byte check = msgTran.checkSum(buf, 0, buf.length);
					mDataCheck.setText("" + (check & 0xFF));
				} catch (Exception e) {
					};
				}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mDataSendButton = (TextView) findViewById(R.id.send);
		mDataSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String[] result = StringTool.stringToStringArray(mDataText.getText().toString().toUpperCase(), 2);
				
				if (result.length > 0) {
					mReader.sendBuffer(StringTool.stringArrayToByteArray(result, result.length));
				}
			}
		});
		
		mDataCheck = (HexEditTextBox) findViewById(R.id.data_send_check);
		
	}
	
	public final void writeMonitor(String strLog, int type) {
		
		if (!mCheckOpenMonitor.isChecked()) return;
		
		Date now=new Date();
		SimpleDateFormat temp=new SimpleDateFormat("kk:mm:ss");
		SpannableString tSS = new SpannableString(temp.format(now) + ":\n" + strLog);
		tSS.setSpan(new ForegroundColorSpan(type == ERROR.SUCCESS ? Color.BLACK : Color.RED), 0, tSS.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  //��ɫ����
		mMonitorListItem.add(tSS);
		
		if (mMonitorListItem.size() > 1000) mMonitorListItem.remove(0);

		mMonitorListAdapter.notifyDataSetChanged();
	}
	
}
