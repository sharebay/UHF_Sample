package com.uhf.uhf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

import com.gy.vam.uhflib.R;

public class ConnectActivity extends Activity {

	private TextView mConectRs232;
	private TextView mConectTcpIp;
	private TextView mConectBluetooth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);

		((UHFApplication) getApplication()).addActivity(this);

		mConectRs232 = (TextView) findViewById(R.id.textview_connect_rs232);
		mConectTcpIp = (TextView) findViewById(R.id.textview_connect_tcp_ip);
		mConectBluetooth = (TextView) findViewById(R.id.textview_connect_bluetooth);

		mConectRs232.setOnClickListener(setConnectOnClickListener);
		mConectTcpIp.setOnClickListener(setConnectOnClickListener);
		mConectBluetooth.setOnClickListener(setConnectOnClickListener);
		
	}
	

	private OnClickListener setConnectOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			int id =v.getId();
			if (id == R.id.textview_connect_rs232){
				startActivity(new Intent().setClass(getApplicationContext(),
						ConnectRs232.class));
			} else if (id == R.id.textview_connect_tcp_ip){
				startActivity(new Intent().setClass(getApplicationContext(),
						ConnectTcpIp.class));
			} else if (id == R.id.textview_connect_bluetooth){
				if (BluetoothAdapter.getDefaultAdapter() == null) {
					Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.no_bluetooth),
							Toast.LENGTH_SHORT).show();
					return;
				}
				startActivity(new Intent().setClass(getApplicationContext(),
						ConnectBlueTooth.class));
			}
			finish();
		};
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}
}
