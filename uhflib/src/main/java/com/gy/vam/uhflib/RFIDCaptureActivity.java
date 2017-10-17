package com.gy.vam.uhflib;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.uhf.reader.base.CMD;
import com.uhf.reader.base.ERROR;
import com.uhf.reader.base.ReaderBase;
import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.reader.helper.ReaderSetting;
import com.uhf.uhf.TagRealList;
import com.uhf.uhf.serialport.SerialPort;
import com.uhf.uhf.tagpage.RealListAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mylib.GyFunction;

public class RFIDCaptureActivity extends Activity {

    public static final String SCAN_RFID_RESULT = "rfid_result";//扫码返回结果（解析后的字符串）
    public static final String SCAN_RFID_HEXCODE = "rfid_hex_code";//扫码结果 十六进制码（未解析）
    public static final int PRE_LENGTH = 2;

    private Context mContext;

    private LocalBroadcastManager lbm;
    private ReaderHelper mReaderHelper;
    private ReaderBase mReader;

    private SerialPort mSerialPort = null;

    private static ReaderSetting m_curReaderSetting;
    private static InventoryBuffer m_curInventoryBuffer;

    //数据显示部分
    private List<InventoryBuffer.InventoryTagMap> data;
    private RealListAdapter mRealListAdapter;
    private ListView mTagRealList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfidcapture);
        //Toast.makeText(this, "test="+ new GyFunction().getString(), Toast.LENGTH_SHORT).show();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayShowHomeEnabled(true);

        this.mContext = this;
        initView();

        if (this.connToUHFDevice()){
            initReader();
            initBroadCast();
            initSettings();
            startScan();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        //设置bar的样式
        LinearLayout desc_bar = (LinearLayout) findViewById(R.id.show_bar_desc);
        LinearLayout tidy_bar = (LinearLayout) findViewById(R.id.show_bar_tidy);
        if (this.getResources().getBoolean(R.bool.use_tidy_bar_in_scan_face)){
            desc_bar.setVisibility(View.GONE);
            tidy_bar.setVisibility(View.VISIBLE);
        }else {
            desc_bar.setVisibility(View.VISIBLE);
            tidy_bar.setVisibility(View.GONE);
        }

        data = new ArrayList<InventoryBuffer.InventoryTagMap>();

        mTagRealList = (ListView) findViewById(R.id.tag_real_list_view);
        mRealListAdapter = new RealListAdapter(mContext, data);
        mTagRealList.setAdapter(mRealListAdapter);

        mTagRealList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                                    long arg3) {
                //gy.add
                ///解析前的十六进制数据
                String strHexCode_Epc = data.get(pos).strEPC;
                //解析后的字符数据
                //String strEpc = getDecodedStr(data.get(pos).strEPC);

                String strEpc ="";
                if(EpcDataConvertUtils.isValidEPCSkipBlanks(data.get(pos).strEPC.replace(" ",""))){
                    strEpc = EpcDataConvertUtils.getReadableDecodedEpcData(data.get(pos).strEPC);
                }else {
                    strEpc = "无效数据";
                    Toast.makeText(mContext, "电子标签原始数据:"+strHexCode_Epc, Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(mContext, "EPC 数据 :" + strEpc, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra(SCAN_RFID_RESULT,strEpc);
                intent.putExtra(SCAN_RFID_HEXCODE,strHexCode_Epc);
                setResult(1,intent);

                RFIDCaptureActivity.this.finish();
            }

        });
    }

    //解析操作
    private String getDecodedStr(String strHex) {
        String strHexSkipBlanks = strHex.replace(" ","");
        //格式规范性校验，校验不通过:1.十六进制数的长度是否为24，2.头两个二进制数是否小于或等于（24-2）
        //strHexSkipBlanks.substring(0,2);
        return getIDByEpcData(strHexSkipBlanks);
    }
    /**
     * 把EPC上的Hex进制的字符转换成身份识别码
     * 14-4e524942-21183916C030-00
     * */
    public String getIDByEpcData(String EPCData){
        //获取有效数据位数
        int validLengthHex = Integer.parseInt(EPCData.substring(0,PRE_LENGTH));
        int validLength = (validLengthHex/10)*16 + (validLengthHex%10);

        if(validLength+2>EPCData.length()){
            Toast.makeText(mContext, "身份识别码EPC数据有误！", Toast.LENGTH_SHORT).show();
            return "";
        }
        //获取到有效的EPC区数据
        String validStr = EPCData.substring(2,validLength+2);

        //获取厂家名称代号的十六进制码,TODO:转换为字符型
        String mid_factoryHex = validStr.substring(0,8);
        StringBuilder mid_factoryStr = new StringBuilder();

        byte[] mid_factoryBytes = hexStr2Bytes(mid_factoryHex);
        String mid_fac = new String(mid_factoryBytes);
        mid_factoryStr.append(mid_fac);

        //获取后边的十六进制码
        String mid_code = validStr.substring(8, validLength);//8是厂家编号占的位数

        mid_factoryStr.append(mid_code);

        return mid_factoryStr.toString();
    }
    /**
     * bytes字符串转换为Byte值
     * @param  src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src)
    {
        int m=0,n=0;
        int l=src.length()/2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++)
        {
            m=i*2+1;
            n=m+1;
            ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));
        }
        return ret;
    }

    private void initSettings() {
        //天线设置，默认使用天线1
        m_curInventoryBuffer.lAntenna.add((byte)0x00);
        /*m_curInventoryBuffer.lAntenna.add((byte)0x01);
        m_curInventoryBuffer.lAntenna.add((byte)0x02);
        m_curInventoryBuffer.lAntenna.add((byte)0x03);*/

        //设置循环次数？
        m_curInventoryBuffer.btRepeat = 1;

        //other settings
        m_curInventoryBuffer.clearInventoryRealResult();//重置实时盘存结果

        if (mReaderHelper.getInventoryFlag()) {
            mHandler.removeCallbacks(mRefreshRunnable);
            mHandler.postDelayed(mRefreshRunnable,2000);
        }

    }

    private void startScan() {
        //gy.标签列表的读写统计记录，包括个数+累计+识别速度
        m_curInventoryBuffer.clearInventoryPar();
        m_curInventoryBuffer.lAntenna.add((byte)0x00);//默认添加一条天线

        //gy.设置循环存盘标记
        m_curInventoryBuffer.bLoopInventoryReal = true;//gy.实时盘存循环标签
        m_curInventoryBuffer.btRepeat = 1;//gy.每条命令的盘存次数
        //gy.不用自定义选区
        m_curInventoryBuffer.bLoopCustomizedSession = false;

        m_curInventoryBuffer.clearInventoryRealResult();
        mReaderHelper.setInventoryFlag(true);

        mReaderHelper.clearInventoryTotal();

        //refreshText();//更新界面

        byte btWorkAntenna = m_curInventoryBuffer.lAntenna.get(m_curInventoryBuffer.nIndexAntenna);
        if (btWorkAntenna < 0) btWorkAntenna = 0;

        mReader.setWorkAntenna(m_curReaderSetting.btReadId, btWorkAntenna);

        m_curReaderSetting.btWorkAntenna = btWorkAntenna;

        mLoopHandler.removeCallbacks(mLoopRunnable);
        mLoopHandler.postDelayed(mLoopRunnable,2000);
        mHandler.removeCallbacks(mRefreshRunnable);
        mHandler.postDelayed(mRefreshRunnable,2000);
    }

    private void stopScan(){
        mReaderHelper.setInventoryFlag(false);
        m_curInventoryBuffer.bLoopInventoryReal = false;

        if (lbm != null)
            lbm.unregisterReceiver(mRecv);

        mLoopHandler.removeCallbacks(mLoopRunnable);
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    /*初始化读写器*/
    private void initReader() {
        try {
            mReaderHelper = ReaderHelper.getDefaultHelper();
            mReader = mReaderHelper.getReader();
        } catch (Exception e) {
            e.printStackTrace();
        }

        m_curReaderSetting = mReaderHelper.getCurReaderSetting();
        m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
    }

    /*初始化应用内部广播*/
    private void initBroadCast() {
        lbm  = LocalBroadcastManager.getInstance(this);

        IntentFilter itent = new IntentFilter();

        //gy.帮助文档中注释为：读写日志操作标 读写操作执行成功发送action为BROADCAST_WRITE_LOG 的广播
        //itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
        //gy.读写器实时盘存标识，盘存任何一个6C标签成功发送action为BROADCAST_REFRESH_INVENTORY_REAL 的广播
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL);
        itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
        itent.addAction("gy_guangbo");
        lbm.registerReceiver(mRecv, itent);
    }

    /*通过串口连接到扫描设备*/
    private boolean connToUHFDevice(){
        //Toast.makeText(this, "串口连接准备...", Toast.LENGTH_SHORT).show();
        try {
            //port:/dev/ttyMT2; baud:115200
            mSerialPort = new SerialPort(
                    new File("/dev/ttyMT2"),
                    115200, 0);
            //Toast.makeText(this, "串口连接准备成功!", Toast.LENGTH_SHORT).show();
            try {
                try {
                    ReaderHelper.setContext(getApplicationContext());
                } catch (Exception e) {
                    //Toast.makeText(this, "串口连接，初始化ReaderHelper失败", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                mReaderHelper = ReaderHelper.getDefaultHelper();
                mReaderHelper.setReader(mSerialPort.getInputStream(),//在函数内部设置了一个等待线程，不听的循环读数据并发送广播
                        mSerialPort.getOutputStream());
                //连接成功
                //Toast.makeText(this, "串口连接成功", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "串口连接失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            Toast.makeText(this, "不存在RFID模块,即将退出该页面!", Toast.LENGTH_SHORT).show();
            finish();
            //e.printStackTrace();
            return false;
        }

        return true;
    }

    //获取标签的EPC列表
    private List<String> getTagEPCs(){
        List list = new ArrayList();
        return list;
    }




    private Handler mHandler = new Handler();
    private Runnable mRefreshRunnable = new Runnable() {
        public void run () {
            //refreshList();
            refreshData();
            //gy获取 EPC的值
            //reGet();
            mHandler.postDelayed(this, 2000);
        }
    };

    private Handler mLoopHandler = new Handler();
    private Runnable mLoopRunnable = new Runnable() {
        public void run () {
            byte btWorkAntenna = m_curInventoryBuffer.lAntenna.get(m_curInventoryBuffer.nIndexAntenna);//默认天线1。
            if (btWorkAntenna < 0) btWorkAntenna = 0;


            //gy.设置工作天线
            mReader.setWorkAntenna(m_curReaderSetting.btReadId, btWorkAntenna);
            //mReader.getInventoryBufferTagCount(m_curReaderSetting.btReadId);
            mLoopHandler.postDelayed(this, 2000);
        }
    };

    private final BroadcastReceiver mRecv = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL)||
                            intent.getAction().equals(ReaderHelper.BROADCAST_REFRESH_READER_SETTING)||
                    intent.getAction().equals("gy_guangbo")) {
                byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
                if (intent.getStringExtra("gy") == null){
                    Log.e("gy.log 接收广播", "onReceive: intent.getAction() = "+intent.getAction());
                    Log.e("gy.log 接收广播", "onReceive: btCmd = "+btCmd);
                }
                //Toast.makeText(context, "接收广播->gy = "+intent.getStringExtra("gy"), Toast.LENGTH_SHORT).show();
                switch (btCmd) {
                    case CMD.REAL_TIME_INVENTORY:
                    case CMD.CUSTOMIZED_SESSION_TARGET_INVENTORY:
                        mLoopHandler.removeCallbacks(mLoopRunnable);
                        mLoopHandler.postDelayed(mLoopRunnable,2000);
                        refreshText();
                        //Toast.makeText(context, "接收到数据！", Toast.LENGTH_SHORT).show();
                        break;
                    case ReaderHelper.INVENTORY_ERR:
                    case ReaderHelper.INVENTORY_ERR_END:
                    case ReaderHelper.INVENTORY_END:

                        if (mReaderHelper.getInventoryFlag()) {
                            mLoopHandler.removeCallbacks(mLoopRunnable);
                            mLoopHandler.postDelayed(mLoopRunnable,2000);
                        } else {
                            mLoopHandler.removeCallbacks(mLoopRunnable);
                        }
                        //
                        refreshData();
                        refreshText();
                        break;
                }
            } else if (intent.getAction().equals(ReaderHelper.BROADCAST_WRITE_LOG)) {
                //gy. 注释 mLogList.writeLog((String)intent.getStringExtra("log"), intent.getIntExtra("type", ERROR.SUCCESS));
            }
        }
    };

    private void refreshData() {
        data.clear();
        data.addAll(m_curInventoryBuffer.lsTagList);
        mRealListAdapter.notifyDataSetChanged();
    }

    //刷新扫描数据，填写到面板
    private void refreshText() {
        Log.e("gy.log.DataInfo", "refreshText: 标签个数："+m_curInventoryBuffer.lsTagList.size());
        if (m_curInventoryBuffer.lsTagList.size()>0){
            for (InventoryBuffer.InventoryTagMap tagmap:m_curInventoryBuffer.lsTagList){
                Log.e("gy.log.DataInfo", "refreshText: 标签：" + tagmap.strEPC);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }

    /*刷新列表的数据*/
    private void refreshList() {
        //TODO
    }

    private void reGet(){
        int size = m_curInventoryBuffer.lsTagList.size();
        //Toast.makeText(this, "gy.size = "+size, Toast.LENGTH_SHORT).show();
        if (size>0){
            String s= m_curInventoryBuffer.lsTagList.get(0).strEPC;
            //Toast.makeText(this, "gy.epc = "+s, Toast.LENGTH_SHORT).show();

        }
    }

}
