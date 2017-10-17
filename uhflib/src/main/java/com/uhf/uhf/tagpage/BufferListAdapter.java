package com.uhf.uhf.tagpage;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gy.vam.uhflib.R;
import com.uhf.reader.helper.InventoryBuffer;

public class BufferListAdapter extends BaseAdapter {
	private LayoutInflater mInflater;

	private Context mContext;
	
	private List<InventoryBuffer.InventoryTagMap> listMap;
	
	public final class ListItemView{                //�Զ���ؼ�����     
		public TextView mIdText;
		public TextView mPcText;
		public TextView mCrcText;
		public TextView mEpcText;
		public TextView mAntennaText;
		public TextView mRssiText;
		public TextView mTimesText;
    }

	public BufferListAdapter(Context context, List<InventoryBuffer.InventoryTagMap> listMap) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		this.listMap = listMap;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listMap.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListItemView  listItemView = null;
		if (convertView == null) {
			listItemView = new ListItemView();
			convertView = mInflater.inflate(R.layout.tag_buffer_list_item, null);
			listItemView.mIdText = (TextView)convertView.findViewById(R.id.id_text);
			listItemView.mPcText = (TextView)convertView.findViewById(R.id.pc_text);
			listItemView.mCrcText = (TextView)convertView.findViewById(R.id.crc_text);
			listItemView.mEpcText = (TextView)convertView.findViewById(R.id.epc_text);
			listItemView.mAntennaText = (TextView)convertView.findViewById(R.id.antenna_text);
			listItemView.mRssiText = (TextView)convertView.findViewById(R.id.rssi_text);
			listItemView.mTimesText = (TextView)convertView.findViewById(R.id.times_text);
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}
		
		InventoryBuffer.InventoryTagMap map = listMap.get(position);
		
		listItemView.mIdText.setText(String.valueOf(position));
		listItemView.mPcText.setText(map.strPC);
		listItemView.mCrcText.setText(map.strCRC);
		listItemView.mEpcText.setText(map.strEPC);
		listItemView.mAntennaText.setText(String.valueOf(map.btAntId));
		listItemView.mTimesText.setText(String.valueOf(map.nReadCount));
		try {
			listItemView.mRssiText.setText((Integer.parseInt(map.strRSSI) - 129) + "dBm");
		} catch (Exception e) {
			listItemView.mRssiText.setText("");
		}
		
		return convertView;

	}	
}
