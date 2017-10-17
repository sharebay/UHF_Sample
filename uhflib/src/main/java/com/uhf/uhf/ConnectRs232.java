package com.uhf.uhf;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.gy.vam.uhflib.R;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.uhf.serialport.SerialPort;
import com.uhf.uhf.serialport.SerialPortFinder;
import com.uhf.uhf.spiner.SpinerPopWindow;
import com.uhf.uhf.spiner.AbstractSpinerAdapter.IOnItemSelectListener;

import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

@SuppressLint("HandlerLeak")
public class ConnectRs232 extends Activity {

	private TextView mConectButton;

	private static final int CONNECTING = 0x10;
	private static final int CONNECT_TIMEOUT = 0x100;
	private static final int CONNECT_FAIL = 0x101;
	private static final int CONNECT_SUCCESS = 0x102;

	private static final int REQUEST_CONNECT_DEVICE_SECURE = 0;

	private ReaderHelper mReaderHelper;

	private List<String> mPortList = new ArrayList<String>();
	private List<String> mBaudList = new ArrayList<String>();

	private TextView mPortTextView, mBaudTextView;
	private TableRow mDropPort, mDropBaud;
	private SpinerPopWindow mSpinerPort, mSpinerBaud;

	private int mPosPort = -1, mPosBaud = -1;

	private SerialPortFinder mSerialPortFinder;

	String[] entries = null;
	String[] entryValues = null;

	private SerialPort mSerialPort = null;

	private AlertDialog mStepDialog;

	private Product mProduct;

    private PowerManager pm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_rs232);
        ((UHFApplication) getApplication()).addActivity(this);

        mSerialPortFinder = new SerialPortFinder();

        //gy.获取到所有的串口号+波特率
        entries = mSerialPortFinder.getAllDevices();
        entryValues = mSerialPortFinder.getAllDevicesPath();


		mConectButton = (TextView) findViewById(R.id.textview_connect);

		mPortTextView = (TextView) findViewById(R.id.comport_text);
		mBaudTextView = (TextView) findViewById(R.id.baudrate_text);
		mDropPort = (TableRow) findViewById(R.id.table_row_spiner_comport);
		mDropBaud = (TableRow) findViewById(R.id.table_row_spiner_baudrate);

		//TODO 串口连接通讯启动。
		mConectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mPosPort < 0 || mPosBaud < 0) {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.rs232_error),
							Toast.LENGTH_SHORT).show();
					return;
				}

				try {
					mSerialPort = new SerialPort(
							new File(entryValues[mPosPort]), Integer
									.parseInt(mBaudList.get(mPosBaud)), 0);

					try {
						mReaderHelper = ReaderHelper.getDefaultHelper();
						mReaderHelper.setReader(mSerialPort.getInputStream(),
								mSerialPort.getOutputStream());
					} catch (Exception e) {
						e.printStackTrace();

						return;
					}

					Intent intent;
					intent = new Intent().setClass(ConnectRs232.this,
							MainActivity.class);
					startActivity(intent);

					finish();
				} catch (SecurityException e) {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.error_security),
							Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.error_unknown),
							Toast.LENGTH_SHORT).show();
				} catch (InvalidParameterException e) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(
									R.string.error_configuration),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		
		//端口号所在的TableRow的点击事件：使用popup_window 模拟spinner
		mDropPort.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showPortSpinWindow();
			}
		});
		
		//波特
		mDropBaud.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showBaudSpinWindow();
			}
		});

		String[] lists = entries;
		for (int i = 0; i < lists.length; i++) {
			mPortList.add(lists[i]);
		}

		mSpinerPort = new SpinerPopWindow(this);
		mSpinerPort.refreshData(mPortList, 0);
		mSpinerPort.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setPortText(pos);
			}
		});

		lists = getResources().getStringArray(R.array.baud_rate);
		for (int i = 0; i < lists.length; i++) {
			mBaudList.add(lists[i]);
		}

		mSpinerBaud = new SpinerPopWindow(this);
		mSpinerBaud.refreshData(mBaudList, 0);
		mSpinerBaud.setItemListener(new IOnItemSelectListener() {
			public void onItemClick(int pos) {
				setBaudText(pos);
			}
		});

		/*<--begin 20171017 gy delete */
		/*//gy.add 20170901 增加电源管理
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManagerUtils.open(pm,2);
		PowerManagerUtils.close(pm,0x0C);*/
		/*--20171017 gy delete end-->*/
	}

	/*自定义的spinner弹出*/
	private void showPortSpinWindow() {
		mSpinerPort.setWidth(mDropPort.getWidth());
		mSpinerPort.showAsDropDown(mDropPort);
	}

	private void showBaudSpinWindow() {
		mSpinerBaud.setWidth(mDropBaud.getWidth());
		mSpinerBaud.showAsDropDown(mDropBaud);
	}

	//给端口号的文本显示赋值
	private void setPortText(int pos) {
		if (pos >= 0 && pos < mPortList.size()) {
			String value = mPortList.get(pos);
			mPortTextView.setText(value);
			mPosPort = pos;
		}
	}

	//设置波特率
	private void setBaudText(int pos) {
		if (pos >= 0 && pos < mBaudList.size()) {
			String value = mBaudList.get(pos);
			mBaudTextView.setText(value);
			mPosBaud = pos;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			startActivity(new Intent().setClass(ConnectRs232.this,
					ConnectActivity.class));

			finish();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mProduct = getProduct(this);
		mProduct.fullPortAndBaud();
		if (null == mStepDialog) {
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("匹配的机型");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mPosPort = mProduct.Port;
							mPosBaud = mProduct.Baud;
							mConectButton.performClick();
						}
					});
			mStepDialog = builder.create();
		}
		StringBuilder message = new StringBuilder();
		if (null != mProduct) {
			message.append("平台：");
			message.append(mProduct.Playform);
			message.append("\n");
			message.append("机型：");
			message.append(mProduct.Type);
			message.append("\n");
			message.append("版本：");
			message.append(mProduct.Version);
		}

		mStepDialog.setMessage(message.toString());
		// mStepDialog.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

        //gy.delete 20171017
        /*//gy.add pm 20170901
        PowerManagerUtils.close(pm,2);*/

	}

	private Product getProduct(Context context) {
		Product product = null;
		String value = SystemPropertiesUtils.get("ro.build.flavor", "");//从装备上获取到的: full_setting6735_T82S-user
		System.out.println(value);
		if (null != value && !"".equals(value)) {

			String[] split = value.split("[_,-]");
			if (split.length == 4) {
				product = new Product();
				product.Playform = split[1];
				product.Type = split[2];
				product.Version = split[3];
			}
		}

		return product;
	}

	public static class Product {
		public String Playform;
		public String Type;
		public String Version;
		public int Port = -1;
		public int Baud = -1;
		public boolean isInit;

		public void fullPortAndBaud() {

            //gy.delete 20171017
			/*if ("s50".equals(Type)) {
				Port = 3;
				Baud = 0;
				isInit = true;
			} else if ("t71".equals(Type)) {
				Port = 2;
				Baud = 0;
				isInit = true;
			}*/

            Port = 2;
            Baud = 0;
            isInit = true;
		}

	}
}
