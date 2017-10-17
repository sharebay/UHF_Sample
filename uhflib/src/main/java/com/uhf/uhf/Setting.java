package com.uhf.uhf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import com.gy.vam.uhflib.R;
import com.uhf.reader.base.CMD;
import com.uhf.reader.base.ERROR;
import com.uhf.reader.base.ReaderBase;
import com.uhf.reader.helper.ISO180006BOperateTagBuffer;
import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.OperateTagBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.reader.helper.ReaderSetting;
import com.uhf.uhf.setpage.PageReaderAddress;
import com.uhf.uhf.setpage.PageReaderAntDetector;
import com.uhf.uhf.setpage.PageReaderAntenna;
import com.uhf.uhf.setpage.PageReaderBeeper;
import com.uhf.uhf.setpage.PageReaderFirmwareVersion;
import com.uhf.uhf.setpage.PageReaderGpio;
import com.uhf.uhf.setpage.PageReaderIdentifier;
import com.uhf.uhf.setpage.PageReaderMonza;
import com.uhf.uhf.setpage.PageReaderOutPower;
import com.uhf.uhf.setpage.PageReaderProfile;
import com.uhf.uhf.setpage.PageReaderRegion;
import com.uhf.uhf.setpage.PageReaderReturnLoss;
import com.uhf.uhf.setpage.PageReaderTemperature;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

public class Setting extends ScrollView {
	private Context mContext;

	private TableRow mSettingResetRow;
	private TableRow mSettingReaderAddressRow;
	private TextView mSettingReaderAddressText;
	private TableRow mSettingIdentifierRow;
	private TextView mSettingIdentifierText;
	private TableRow mSettingFirmwareVersionRow;
	private TextView mSettingFirmwareVersionText;
	private TableRow mSettingTemperatureRow;
	private TextView mSettingTemperatureText;
	private TableRow mSettingGpioRow;
	private TextView mSettingGpioText;
	private TableRow mSettingBeeperRow;
	private TextView mSettingBeeperText;
	private TableRow mSettingOutPowerRow;
	private TextView mSettingOutPowerText;
	private TableRow mSettingAntennaRow;
	private TextView mSettingAntennaText;
	private TableRow mSettingReturnLossRow;
	private TextView mSettingReturnLossText;
	private TableRow mSettingAntDetectorRow;
	private TextView mSettingAntDetectorText;
	private TableRow mSettingMonzaRow;
	private TextView mSettingMonzaText;
	private TableRow mSettingRegionRow;
	private TextView mSettingRegionText;
	private TableRow mSettingProfileRow;
	private TextView mSettingProfileText;

	private ReaderBase mReader;
	private ReaderHelper mReaderHelper;

	private LogList mLogList;

	private static ReaderSetting m_curReaderSetting;
	private static InventoryBuffer m_curInventoryBuffer;
	private static OperateTagBuffer m_curOperateTagBuffer;
	private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;

	public Setting(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.setting, this);

		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			return;
		}

		initSettingView();

		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper
				.getCurOperateTagISO18000Buffer();

	}

	public void setLogList(LogList logList) {
		mLogList = logList;
	}

	private void initSettingView() {

		mSettingResetRow = (TableRow) findViewById(R.id.table_row_setting_reset);
		mSettingResetRow.setOnClickListener(setSettingOnClickListener);
		mSettingReaderAddressRow = (TableRow) findViewById(R.id.table_row_setting_reader_address);
		mSettingReaderAddressRow.setOnClickListener(setSettingOnClickListener);
		mSettingReaderAddressText = (TextView) findViewById(R.id.text_setting_reader_address);

		mSettingIdentifierRow = (TableRow) findViewById(R.id.table_row_setting_identifier);
		mSettingIdentifierRow.setOnClickListener(setSettingOnClickListener);
		mSettingIdentifierText = (TextView) findViewById(R.id.text_setting_identifier);

		mSettingFirmwareVersionRow = (TableRow) findViewById(R.id.table_row_setting_firmware_version);
		mSettingFirmwareVersionRow
				.setOnClickListener(setSettingOnClickListener);
		mSettingFirmwareVersionText = (TextView) findViewById(R.id.text_setting_firmware_version);

		mSettingTemperatureRow = (TableRow) findViewById(R.id.table_row_setting_temperature);
		mSettingTemperatureRow.setOnClickListener(setSettingOnClickListener);
		mSettingTemperatureText = (TextView) findViewById(R.id.text_setting_temperature);

		mSettingGpioRow = (TableRow) findViewById(R.id.table_row_setting_gpio);
		mSettingGpioRow.setOnClickListener(setSettingOnClickListener);
		mSettingGpioText = (TextView) findViewById(R.id.text_setting_gpio);

		mSettingBeeperRow = (TableRow) findViewById(R.id.table_row_setting_beeper);
		mSettingBeeperRow.setOnClickListener(setSettingOnClickListener);
		mSettingBeeperText = (TextView) findViewById(R.id.text_setting_beeper);

		mSettingOutPowerRow = (TableRow) findViewById(R.id.table_row_setting_out_power);
		mSettingOutPowerRow.setOnClickListener(setSettingOnClickListener);
		mSettingOutPowerText = (TextView) findViewById(R.id.text_setting_out_power);

		mSettingAntennaRow = (TableRow) findViewById(R.id.table_row_setting_antenna);
		mSettingAntennaRow.setOnClickListener(setSettingOnClickListener);
		mSettingAntennaText = (TextView) findViewById(R.id.text_setting_antenna);

		mSettingReturnLossRow = (TableRow) findViewById(R.id.table_row_setting_return_loss);
		mSettingReturnLossRow.setOnClickListener(setSettingOnClickListener);
		mSettingReturnLossText = (TextView) findViewById(R.id.text_setting_return_loss);

		mSettingAntDetectorRow = (TableRow) findViewById(R.id.table_row_setting_ant_detector);
		mSettingAntDetectorRow.setOnClickListener(setSettingOnClickListener);
		mSettingAntDetectorText = (TextView) findViewById(R.id.text_setting_ant_detector);

		mSettingMonzaRow = (TableRow) findViewById(R.id.table_row_setting_monza);
		mSettingMonzaRow.setOnClickListener(setSettingOnClickListener);
		mSettingMonzaText = (TextView) findViewById(R.id.text_setting_monza);

		mSettingRegionRow = (TableRow) findViewById(R.id.table_row_setting_region);
		mSettingRegionRow.setOnClickListener(setSettingOnClickListener);
		mSettingRegionText = (TextView) findViewById(R.id.text_setting_region);

		mSettingProfileRow = (TableRow) findViewById(R.id.table_row_setting_profile);
		mSettingProfileRow.setOnClickListener(setSettingOnClickListener);
		mSettingProfileText = (TextView) findViewById(R.id.text_setting_profile);
	}

	private void writeLog(String strLog, byte type) {
		if (mLogList != null)
			mLogList.writeLog(strLog, type);
	}

	public void refreshReaderSetting(byte btCmd) {
		switch (btCmd) {

		}
	}

	private OnClickListener setSettingOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = null;
			int id = v.getId();
			if (id == R.id.table_row_setting_reset) {
				mReader.reset(m_curReaderSetting.btReadId);
				writeLog(CMD.format(CMD.RESET), ERROR.SUCCESS);
			} else if (R.id.table_row_setting_reader_address == id) {
				intent = new Intent().setClass(mContext,
						PageReaderAddress.class);
			} else if (id == R.id.table_row_setting_identifier){
				intent = new Intent().setClass(mContext,
						PageReaderIdentifier.class);
			}else if(id == R.id.table_row_setting_firmware_version){
				intent = new Intent().setClass(mContext,
						PageReaderFirmwareVersion.class);
			} else if(id == R.id.table_row_setting_temperature){
				intent = new Intent().setClass(mContext,
						PageReaderTemperature.class);
			} else if(id ==R.id.table_row_setting_gpio){
				intent = new Intent().setClass(mContext, PageReaderGpio.class);
			} else if(id ==R.id.table_row_setting_beeper){
				intent = new Intent()
						.setClass(mContext, PageReaderBeeper.class);
			} else if(id == R.id.table_row_setting_out_power){
				intent = new Intent().setClass(mContext,
						PageReaderOutPower.class);
			} else if(id == R.id.table_row_setting_antenna){
				intent = new Intent().setClass(mContext,
						PageReaderAntenna.class);
			} else if (id == R.id.table_row_setting_return_loss){
				intent = new Intent().setClass(mContext,
						PageReaderReturnLoss.class);
			} else if(id == R.id.table_row_setting_ant_detector){
				intent = new Intent().setClass(mContext,
						PageReaderAntDetector.class);
			} else if(id == R.id.table_row_setting_monza){
				intent = new Intent().setClass(mContext, PageReaderMonza.class);
			} else if(id == R.id.table_row_setting_region){
				intent = new Intent()
						.setClass(mContext, PageReaderRegion.class);
			} else if (id == R.id.table_row_setting_profile){
				intent = new Intent().setClass(mContext,
						PageReaderProfile.class);
			} else{
				intent = null;
			}

			if (intent != null)
				mContext.startActivity(intent);
		}
	};
}
