package com.uhf.uhf.tagpage;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gy.vam.uhflib.EpcDataConvertUtils;
import com.gy.vam.uhflib.R;
import com.uhf.reader.helper.InventoryBuffer;

public class RealListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;

	private Context mContext;

	private List<InventoryBuffer.InventoryTagMap> listMap;

	public final class ListItemView { // �Զ���ؼ�����
		public TextView mIdText;
		public TextView mEpcText;
		public TextView mPcText;
		public TextView mTimesText;
		public TextView mRssiText;
		public TextView mFreqText;
	}

	public RealListAdapter(Context context, List<InventoryBuffer.InventoryTagMap> listMap) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.listMap = listMap;
	}

	@Override
	public int getCount() {
		return listMap.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListItemView listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
            if (mContext.getResources().getBoolean(R.bool.use_tidy_bar_in_scan_face)) {
                convertView = mInflater.inflate(R.layout.tag_real_list_item_tidy, null);
                listItemView.mIdText = (TextView) convertView
                        .findViewById(R.id.id_text);
                listItemView.mEpcText = (TextView) convertView
                        .findViewById(R.id.epc_text);
            } else {
                convertView = mInflater.inflate(R.layout.tag_real_list_item, null);
                listItemView.mIdText = (TextView) convertView
                        .findViewById(R.id.id_text);
                listItemView.mEpcText = (TextView) convertView
                        .findViewById(R.id.epc_text);
                listItemView.mPcText = (TextView) convertView
                        .findViewById(R.id.pc_text);
                listItemView.mTimesText = (TextView) convertView
                        .findViewById(R.id.times_text);
                listItemView.mRssiText = (TextView) convertView
                        .findViewById(R.id.rssi_text);
                listItemView.mFreqText = (TextView) convertView
                        .findViewById(R.id.freq_text);
            }
            convertView.setTag(listItemView);

        } else {
			listItemView = (ListItemView) convertView.getTag();
		}

		InventoryBuffer.InventoryTagMap map = listMap.get(position);

		listItemView.mIdText.setText(String.valueOf(position+1));

		String dataSkipBlanks = map.strEPC.replace(" ","");
		if(EpcDataConvertUtils.isValidEPCSkipBlanks(dataSkipBlanks)){
			listItemView.mEpcText.setText(EpcDataConvertUtils.getReadableDecodedEpcData(dataSkipBlanks));
		}else {
			listItemView.mEpcText.setText("无效数据");
		}
		//listItemView.mEpcText.setText(map.strEPC);//TODO 此处可以转换为人为可识别的字符
        if (!mContext.getResources().getBoolean(R.bool.use_tidy_bar_in_scan_face)) {
            listItemView.mPcText.setText(map.strPC);
            listItemView.mTimesText.setText(String.valueOf(map.nReadCount));
            try {
                listItemView.mRssiText.setText((Integer.parseInt(map.strRSSI) - 129) + "dBm");
            } catch (Exception e) {
                listItemView.mRssiText.setText("");
            }
            listItemView.mFreqText.setText(map.strFreq);
        }


		return convertView;

	}
}
