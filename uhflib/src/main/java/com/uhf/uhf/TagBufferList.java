package com.uhf.uhf;

import java.util.ArrayList;
import java.util.List;

import com.gy.vam.uhflib.R;
import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.uhf.tagpage.BufferListAdapter;

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

public class TagBufferList extends LinearLayout {
	private Context mContext;
	private TableRow mTagBufferRow;
	private ImageView mTagBufferImage;
	private TextView mListTextInfo;
	
	private TextView mTagsCountText, mTagsTotalText;
	private TextView mTagsSpeedText, mTagsTimeText;
	
	private ReaderHelper mReaderHelper;
	
	private List<InventoryBuffer.InventoryTagMap> data;
	private BufferListAdapter mBufferListAdapter;
	private ListView mTagBufferList;
	
	private View mTagsBufferListScrollView;
	
	private static InventoryBuffer m_curInventoryBuffer;
	
	private OnItemSelectedListener mOnItemSelectedListener;
	
	public interface OnItemSelectedListener {
		public void onItemSelected(View arg1, int arg2,
				long arg3);
	}
	
	public TagBufferList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initContext(context);
	}
	
	public TagBufferList(Context context) {  
        super(context);
        initContext(context);
    }

	private void initContext(Context context) {
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.tag_buffer_list, this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		data = new ArrayList<InventoryBuffer.InventoryTagMap>();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		
		mTagsBufferListScrollView = findViewById(R.id.tags_buffer_list_scroll_view);
		mTagsBufferListScrollView.setVisibility(View.GONE);
		
		mTagBufferRow = (TableRow) findViewById(R.id.table_row_tag_buffer);
		mTagBufferImage = (ImageView) findViewById(R.id.image_prompt);
		mTagBufferImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
		mListTextInfo = (TextView) findViewById(R.id.list_text_info);
		mListTextInfo.setText(getResources().getString(R.string.open_tag_list));

		mTagsCountText = (TextView) findViewById(R.id.tags_count_text);
		mTagsTotalText = (TextView) findViewById(R.id.tags_total_text);
		mTagsSpeedText = (TextView) findViewById(R.id.tags_speed_text);
		mTagsTimeText = (TextView) findViewById(R.id.tags_time_text);

		mTagBufferRow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (mTagsBufferListScrollView.getVisibility() != View.VISIBLE) {
					mTagsBufferListScrollView.setVisibility(View.VISIBLE);
					mTagBufferImage.setImageDrawable(getResources().getDrawable(R.drawable.down));
					mListTextInfo.setText(getResources().getString(R.string.close_tag_list));
				} else {
					mTagsBufferListScrollView.setVisibility(View.GONE);
					mTagBufferImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
					mListTextInfo.setText(getResources().getString(R.string.open_tag_list));
				}
			}
		});
		
		mTagBufferList = (ListView) findViewById(R.id.tag_buffer_list_view);
		mBufferListAdapter = new BufferListAdapter(mContext, data);
		mTagBufferList.setAdapter(mBufferListAdapter);
		
		mTagBufferList.setOnItemClickListener(new OnItemClickListener() {

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
	}
	
	public final void refreshText() {
		mTagsCountText.setText(String.valueOf(m_curInventoryBuffer.nTagCount));
		mTagsTotalText.setText(String.valueOf(m_curInventoryBuffer.nTotalRead));
		//if (m_curInventoryBuffer.nReadRate > 0) {
			mTagsSpeedText.setText(String.valueOf(m_curInventoryBuffer.nReadRate));
		//}
		mTagsTimeText.setText(String.valueOf(m_curInventoryBuffer.dtEndInventory.getTime() - m_curInventoryBuffer.dtStartInventory.getTime()));
	}
	
	public final void refreshList() {
		data.clear();
		data.addAll(m_curInventoryBuffer.lsTagList);
		mBufferListAdapter.notifyDataSetChanged();
	}	
}
