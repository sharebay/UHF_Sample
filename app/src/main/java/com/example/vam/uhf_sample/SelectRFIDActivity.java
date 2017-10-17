package com.example.vam.uhf_sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.uhf.reader.base.CMD;
import com.uhf.reader.helper.ReaderHelper;


public class SelectRFIDActivity extends Activity implements View.OnClickListener{
    ImageButton imgbtn_rfid_scan_settings;
    Button btn_scan;
    private LocalBroadcastManager lbm;//应用内部广播
    private Handler mLoopHandler = new Handler();
    private Runnable mLoopRunnable = new Runnable() {
        public void run () {
            mLoopHandler.postDelayed(this, 2000);
        }
    };

    private Handler mHandler = new Handler();
    private Runnable mRefreshRunnable = new Runnable() {
        public void run () {
            //
            mHandler.postDelayed(this, 2000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //lbm  = LocalBroadcastManager.getInstance(this);
        IntentFilter itent = new IntentFilter();
        //gy.帮助文档中注释为：读写日志操作标 读写操作执行成功发送action为BROADCAST_WRITE_LOG 的广播
        //itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
        //gy.读写器实时盘存标识，盘存任何一个6C标签成功发送action为BROADCAST_REFRESH_INVENTORY_REAL 的广播
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL);
        //lbm.registerReceiver(mRecv, itent);
    }

    private void initView() {
        btn_scan = (Button) findViewById(R.id.btn_scan);
        imgbtn_rfid_scan_settings = (ImageButton) findViewById(R.id.imgbtn_rfid_scan_settings);
        btn_scan.setOnClickListener(this);
        imgbtn_rfid_scan_settings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_scan:
                //1.检查是否存在RFID扫描模块
                //2.检查RFID是否连接成功

                Intent intent0 = new Intent();
                intent0.setClass(SelectRFIDActivity.this, com.gy.vam.uhflib.RFIDCaptureActivity.class);
                startActivity(intent0);
//
                /*mLoopHandler.removeCallbacks(mLoopRunnable);
                mLoopHandler.postDelayed(mLoopRunnable,2000);
                mHandler.removeCallbacks(mRefreshRunnable);
                mHandler.postDelayed(mRefreshRunnable,2000);*/

                break;
            case R.id.imgbtn_rfid_scan_settings:
                Intent intent = new Intent();
                intent.setClass(SelectRFIDActivity.this,ScanRFID_SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }


    private final BroadcastReceiver mRecv = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL)) {
                byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
                Toast.makeText(context,"gy.byCmd = "+btCmd,Toast.LENGTH_SHORT).show();
                switch (btCmd) {
                    case CMD.REAL_TIME_INVENTORY:
                    case CMD.CUSTOMIZED_SESSION_TARGET_INVENTORY:

                        mLoopHandler.removeCallbacks(mLoopRunnable);
                        mLoopHandler.postDelayed(mLoopRunnable,2000);
                        //接受到广播就弹出吐司
                        Toast.makeText(context,"gy."+"拿到实时数据！",Toast.LENGTH_SHORT).show();
                        break;
                    case ReaderHelper.INVENTORY_ERR:
                    case ReaderHelper.INVENTORY_ERR_END:
                    case ReaderHelper.INVENTORY_END:

                        break;
                }


            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_LOG)) {
                Toast.makeText(context,"gy."+"其他广播",Toast.LENGTH_SHORT).show();
                //mLogList.writeLog((String)intent.getStringExtra("log"), intent.getIntExtra("type", ERROR.SUCCESS));
            }
        }
    };

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (lbm != null)
            lbm.unregisterReceiver(mRecv);

        mLoopHandler.removeCallbacks(mLoopRunnable);
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //接收到扫描数据后弹出Toast
    }
}
