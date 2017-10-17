package com.uhf.uhf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.regex.Pattern;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import com.gy.vam.uhflib.R;
import com.uhf.reader.helper.ReaderHelper;

@SuppressLint("HandlerLeak")
public class ConnectTcpIp extends Activity {
	
	private static final String HOSTNAME_REGEXP =  "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
	private EditText mConectTcpIpAddress;
	private EditText mConectTcpIpPort;
	private TextView mConectButton;
	
	private static final int CONNECTING = 0x10;
	private static final int CONNECT_TIMEOUT = 0x100;
	private static final int CONNECT_FAIL = 0x101;
	private static final int CONNECT_SUCCESS = 0x102;
	
	private ReaderHelper mReaderHelper;
	
	private Socket mSocket;
	InetSocketAddress mRemoteAddr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_tcp_ip);
		
		((UHFApplication) getApplication()).addActivity(this);
		

		mConectTcpIpAddress = (EditText) findViewById(R.id.connect_tcp_ip_address_text);
		mConectTcpIpPort = (EditText) findViewById(R.id.connect_tcp_ip_port_text);
		mConectButton = (TextView) findViewById(R.id.textview_connect);
		
		mConectButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String host = mConectTcpIpAddress.getText().toString();
				String strPort = mConectTcpIpPort.getText().toString();
				
				if (host == null || host.length() <= 0) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.ip_null),
							Toast.LENGTH_SHORT).show();
					return ;
				}
				
				if (strPort == null || strPort.length() <= 0) {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.port_null),
							Toast.LENGTH_SHORT).show();
					return ;
				}
				
				
				int port = Integer.parseInt(strPort);
				
				if(host.matches(HOSTNAME_REGEXP))
					;
				else {
					Toast.makeText(
							getApplicationContext(),
							getResources().getString(R.string.ip_error),
							Toast.LENGTH_SHORT).show();
					return ;
				}
				
				mRemoteAddr = new InetSocketAddress(host, port);
				mSocket = new Socket();
				try {
					mSocket.setReceiveBufferSize(128 * 1024);
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				new Thread() {
					public void run() {
						try {
							mHandler.obtainMessage(CONNECTING).sendToTarget();
							mSocket.connect(mRemoteAddr, 2000);
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
							mHandler.obtainMessage(CONNECT_TIMEOUT).sendToTarget();
							return ;
						} catch (IOException e) {
							e.printStackTrace();
							mHandler.obtainMessage(CONNECT_FAIL).sendToTarget();
							return ;
						}
						
						mHandler.obtainMessage(CONNECT_SUCCESS).sendToTarget();
						
						((UHFApplication)getApplication()).setTcpSocket(mSocket);
					};
				}.start();
			}
		});
	}

	private final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			Intent intent = null;
			switch (msg.what) {
			case CONNECTING:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.ip_connecting),
						Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_TIMEOUT:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.ip_connect_timeout),
						Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_FAIL:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.ip_connect_fail),
						Toast.LENGTH_SHORT).show();
				break;
			case CONNECT_SUCCESS:
				Toast.makeText(
						getApplicationContext(),
						getResources().getString(R.string.ip_connect_success),
						Toast.LENGTH_SHORT).show();
				try {
					mReaderHelper = ReaderHelper.getDefaultHelper();
					mReaderHelper.setReader(mSocket.getInputStream(), mSocket.getOutputStream());
				} catch (Exception e) {
					e.printStackTrace();
					
					return ;
				}
				
				intent = new Intent().setClass(ConnectTcpIp.this, MainActivity.class);
				startActivity(intent);
				
				finish();
				break;
			}
		}
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			startActivity(new Intent().setClass(ConnectTcpIp.this, ConnectActivity.class));
			
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
