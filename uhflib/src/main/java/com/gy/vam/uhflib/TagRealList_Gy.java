package com.gy.vam.uhflib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.uhf.TagRealList;
import com.uhf.uhf.tagpage.RealListAdapter;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.gy.vam.uhflib.R;
import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.uhf.tagpage.RealListAdapter;

	public class TagRealList_Gy extends LinearLayout {
		private Context mContext;
		private TableRow mTagRealRow;
		private ImageView mTagRealImage;
		private TextView mListTextInfo;

		private TextView mTagsCountText, mTagsTotalText;
		private TextView mTagsSpeedText, mTagsTimeText;
		private TextView mMinRSSIText, mMaxRSSIText;

		private ReaderHelper mReaderHelper;

		private List<InventoryBuffer.InventoryTagMap> data;
		private RealListAdapter mRealListAdapter;
		private ListView mTagRealList;

		private View mTagsRealListScrollView;

		private InventoryBuffer m_curInventoryBuffer;

		private com.uhf.uhf.TagRealList.OnItemSelectedListener mOnItemSelectedListener;
		public interface OnItemSelectedListener {
			public void onItemSelected(View arg1, int arg2,
									   long arg3);
		}

		public TagRealList_Gy(Context context, AttributeSet attrs) {
			super(context, attrs);
			initContext(context);
		}

		public TagRealList_Gy(Context context) {
			super(context);
			initContext(context);
		}

		private void initContext(Context context) {
			mContext = context;
			LayoutInflater.from(context).inflate(R.layout.tag_real_list, this);


			mTagsRealListScrollView = findViewById(R.id.tags_real_list_scroll_view);
			//mTagsRealListScrollView.setVisibility(View.GONE);

			mTagRealRow = (TableRow) findViewById(R.id.table_row_tag_real);
			//mTagRealImage = (ImageView) findViewById(R.id.image_prompt);
			//mTagRealImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
			mListTextInfo = (TextView) findViewById(R.id.list_text_info);
			mListTextInfo.setText(getResources().getString(R.string.tag_list));

			mTagsCountText = (TextView) findViewById(R.id.tags_count_text);
			mTagsTotalText = (TextView) findViewById(R.id.tags_total_text);
			mTagsSpeedText = (TextView) findViewById(R.id.tags_speed_text);
			mTagsTimeText = (TextView) findViewById(R.id.tags_time_text);
			mMinRSSIText = (TextView) findViewById(R.id.min_rssi_text);
			mMaxRSSIText = (TextView) findViewById(R.id.max_rssi_text);

            data = new ArrayList<InventoryBuffer.InventoryTagMap>();

			mTagRealList = (ListView) findViewById(R.id.tag_real_list_view);
			mRealListAdapter = new RealListAdapter(mContext, data);
			mTagRealList.setAdapter(mRealListAdapter);

			mTagRealList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
										long arg3) {
					//gy.add
					Toast.makeText(mContext, "onclicked "+arg2, Toast.LENGTH_SHORT).show();

					if (mOnItemSelectedListener != null)
						mOnItemSelectedListener.onItemSelected(arg1, arg2, arg3);
				}

			});
		}

		public void setData(List<InventoryBuffer.InventoryTagMap> data, InventoryBuffer m_curInventoryBuffer){
            this.data = data;
            this.m_curInventoryBuffer = m_curInventoryBuffer;
        }


		public void setOnItemSelectedListener(
				com.uhf.uhf.TagRealList.OnItemSelectedListener onItemSelectedListener) {
			mOnItemSelectedListener = onItemSelectedListener;
		}

		public final void clearText() {
			mTagsCountText.setText("0");
			mTagsTotalText.setText("0");
			mTagsSpeedText.setText("0");
			mTagsTimeText.setText("0");
			mMinRSSIText.setText("");
			mMaxRSSIText.setText("");
		}

		public final void refreshText() {
			/*mTagsCountText.setText(String.valueOf(m_curInventoryBuffer.lsTagList.size()));
			mTagsTotalText.setText(String.valueOf(mReaderHelper.getInventoryTotal()));
			mTagsSpeedText.setText(String.valueOf(m_curInventoryBuffer.nReadRate));
			mTagsTimeText.setText(String.valueOf(m_curInventoryBuffer.dtEndInventory.getTime() - m_curInventoryBuffer.dtStartInventory.getTime()));
			mMinRSSIText.setText(String.valueOf(m_curInventoryBuffer.nMinRSSI) + "dBm");
			mMaxRSSIText.setText(String.valueOf(m_curInventoryBuffer.nMaxRSSI) + "dBm");*/
		}

		public final void refreshList() {
			data.clear();
			data.addAll(m_curInventoryBuffer.lsTagList);
			mRealListAdapter.notifyDataSetChanged();
		}
	}

