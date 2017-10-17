package com.uhf.uhf;

import java.io.IOException;
import java.util.UUID;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import com.gy.vam.uhflib.R;
import com.uhf.reader.helper.ReaderHelper;

@SuppressLint("HandlerLeak")
public class ConnectBlueTooth extends Activity {

	private TextView mConectButton;
	
	private static final int CONNECTING = 0x10;
	private static final int CONNECT_TIMEOUT = 0x100;
	private static final int CONNECT_FAIL = 0x101;
	private static final int CONNECT_SUCCESS = 0x102;
	
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 0;
	
	private ReaderHelper mReaderHelper;
	
	private static BluetoothAdapter mBluetoothAdapter;
	private static ConnectThread mConnectThread;
	private static BluetoothSocket mSocket;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_bluetooth);
		
		((UHFApplication) getApplication()).addActivity(this);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if (mBluetoothAdapter == null) {
			Toast.makeText(
					getApplicationContext(),
					getResources().getString(R.string.no_bluetooth),
					Toast.LENGTH_SHORT).show();
			return ;
		}
		
		mConectButton = (TextView) findViewById(R.id.textview_connect);

		if (!mBluetoothAdapter.isEnabled())
			mBluetoothAdapter.enable();
				
		
		mConectButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent serverIntent = new Intent().setClass(ConnectBlueTooth.this, DeviceListActivity.class);
				mBluetoothAdapter.cancelDiscovery();
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			}
		});
	}
	
	class ConnectThread extends Thread {
		private final UUID MY_UUID = UUID
				.fromString("00001101-0000-1000-8000-00805F9B34FB");

		public ConnectThread(BluetoothDevice device) {
			BluetoothSocket tmp = null;
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
			}
			mSocket = tmp;
		}

		public synchronized void run() {
			mBluetoothAdapter.cancelDiscovery();

			try {
				mHandler.obtainMessage(CONNECTING).sendToTarget();
				mSocket.connect();
				
				mHandler.obtainMessage(CONNECT_SUCCESS).sendToTarget();
				
				((UHFApplication)getApplication()).setBtSocket(mSocket);
				
			} catch (IOException connectException) {
				try {
					mHandler.obtainMessage(CONNECT_FAIL).sendToTarget();
					mSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}
		}
	}

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Intent intent = null;
			switch (msg.what) {
			case CONNECTING:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.bluetooth_connecting),
						Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_TIMEOUT:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.bluetooth_connect_timeout),
						Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_FAIL:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.bluetooth_connect_fail),
						Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_SUCCESS:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.bluetooth_connect_success),
						Toast.LENGTH_SHORT).show();
				try {
					mReaderHelper = ReaderHelper.getDefaultHelper();
					mReaderHelper.setReader(mSocket.getInputStream(), mSocket.getOutputStream());
				} catch (Exception e) {
					e.printStackTrace();
					
					return ;
				}
				
				intent = new Intent().setClass(ConnectBlueTooth.this, MainActivity.class);
				startActivity(intent);
				
				finish();
				break;
			}
		}
	};
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);

				BluetoothDevice device = BluetoothAdapter
						.getDefaultAdapter().getRemoteDevice(address);

				if (device == null) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.bluetooth_connect_fail),
							Toast.LENGTH_SHORT).show();
					return ;
				}
				
				mBluetoothAdapter.cancelDiscovery();
				mConnectThread = new ConnectThread(device);
				mConnectThread.start();
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.bluetooth_connecting),
						Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			startActivity(new Intent().setClass(ConnectBlueTooth.this, ConnectActivity.class));
			
			finish();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
