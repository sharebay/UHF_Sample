package com.uhf.uhf;

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

import com.gy.vam.uhflib.R;
import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.uhf.tagpage.FastListAdapter;

public class TagFastList extends LinearLayout {
	private Context mContext;
	private TableRow mTagFastRow;
	private ImageView mTagFastImage;
	private TextView mListTextInfo;
	
	private TextView mTagsCountText, mTagsTotalText;
	private TextView mTagsSpeedText, mTagsTimeText;
	private TextView mMinRSSIText, mMaxRSSIText;
	
	private ReaderHelper mReaderHelper;
	private List<InventoryBuffer.InventoryTagMap> data;
	private FastListAdapter mFastListAdapter;
	private ListView mTagFastList;
	
	private View mTagsFastListScrollView;
	
	private static InventoryBuffer m_curInventoryBuffer;
	
	private OnItemSelectedListener mOnItemSelectedListener;
	public interface OnItemSelectedListener {
		public void onItemSelected(View arg1, int arg2,
				long arg3);
	}
	
	public TagFastList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initContext(context);
	}
	
	public TagFastList(Context context) {  
        super(context);
        initContext(context);
    }

	private void initContext(Context context) {
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.tag_fast_list, this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		data = new ArrayList<InventoryBuffer.InventoryTagMap>();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		
		mTagsFastListScrollView = findViewById(R.id.tags_fast_list_scroll_view);
		mTagsFastListScrollView.setVisibility(View.GONE);
		
		mTagFastRow = (TableRow) findViewById(R.id.table_row_tag_fast);
		mTagFastImage = (ImageView) findViewById(R.id.image_prompt);
		mTagFastImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
		mListTextInfo = (TextView) findViewById(R.id.list_text_info);
		mListTextInfo.setText(getResources().getString(R.string.open_tag_list));

		mTagsCountText = (TextView) findViewById(R.id.tags_count_text);
		mTagsTotalText = (TextView) findViewById(R.id.tags_total_text);
		mTagsSpeedText = (TextView) findViewById(R.id.tags_speed_text);
		mTagsTimeText = (TextView) findViewById(R.id.tags_time_text);
		mMinRSSIText = (TextView) findViewById(R.id.min_rssi_text);
		mMaxRSSIText = (TextView) findViewById(R.id.max_rssi_text);
		
		mTagFastRow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (mTagsFastListScrollView.getVisibility() != View.VISIBLE) {
					mTagsFastListScrollView.setVisibility(View.VISIBLE);
					mTagFastImage.setImageDrawable(getResources().getDrawable(R.drawable.down));
					mListTextInfo.setText(getResources().getString(R.string.close_tag_list));
				} else {
					mTagsFastListScrollView.setVisibility(View.GONE);
					mTagFastImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
					mListTextInfo.setText(getResources().getString(R.string.open_tag_list));
				}
			}
		});
		
		mTagFastList = (ListView) findViewById(R.id.tag_fast_list_view);
		mFastListAdapter = new FastListAdapter(mContext, data);
		mTagFastList.setAdapter(mFastListAdapter);
		
		mTagFastList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				if (mOnItemSelectedListener != null)
					mOnItemSelectedListener.onItemSelected(arg1, arg2, arg3);
			}
			
		});
	}
	
	public void setOnItemSelectedListener(
			OnItemSelectedListener onItemSelectedListener) {
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
		mTagsCountText.setText(String.valueOf(m_curInventoryBuffer.lsTagList.size()));
		mTagsTotalText.setText(String.valueOf(mReaderHelper.getInventoryTotal()));
		if (m_curInventoryBuffer.nCommandDuration > 0) {
			mTagsSpeedText.setText(String.valueOf(m_curInventoryBuffer.nDataCount * 1000 / m_curInventoryBuffer.nCommandDuration));
        }
		mTagsTimeText.setText(String.valueOf(m_curInventoryBuffer.dtEndInventory.getTime() - m_curInventoryBuffer.dtStartInventory.getTime()));
		mMinRSSIText.setText(String.valueOf(m_curInventoryBuffer.nMinRSSI) + "dBm");
		mMaxRSSIText.setText(String.valueOf(m_curInventoryBuffer.nMaxRSSI) + "dBm");
	}
	
	public final void refreshList() {
		data.clear();
		data.addAll(m_curInventoryBuffer.lsTagList);
		mFastListAdapter.notifyDataSetChanged();
	}	
}
