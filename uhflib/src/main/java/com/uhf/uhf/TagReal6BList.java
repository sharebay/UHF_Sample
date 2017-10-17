package com.uhf.uhf;

import java.util.ArrayList;
import java.util.List;

import com.gy.vam.uhflib.R;
import com.uhf.reader.helper.ISO180006BOperateTagBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.uhf.tagpage.Real6BListAdapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class TagReal6BList extends LinearLayout {
	private Context mContext;
	private TableRow mTagReal6BRow;
	private ImageView mTagReal6BImage;
	private TextView mListTextInfo;
	
	private TextView mTagsCountText;
	
	private ReaderHelper mReaderHelper;
	
	private List<ISO180006BOperateTagBuffer.ISO180006BOperateTagMap> data;
	private Real6BListAdapter mReal6BListAdapter;
	private ListView mTagReal6BList;
	
	private View mTagsReal6BListScrollView;
	
	private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;
	
	private OnItemSelectedListener mOnItemSelectedListener;
	public interface OnItemSelectedListener {
		public void onItemSelected(View arg1, int arg2,
				long arg3);
	}
	
	public TagReal6BList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initContext(context);
	}
	
	public TagReal6BList(Context context) {  
        super(context);
        initContext(context);
    }

	private void initContext(Context context) {
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.tag_real_6b_list, this);
		
		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		data = new ArrayList<ISO180006BOperateTagBuffer.ISO180006BOperateTagMap>();
		m_curOperateTagISO18000Buffer = mReaderHelper.getCurOperateTagISO18000Buffer();
		
		mTagsReal6BListScrollView = findViewById(R.id.tags_real_6b_list_scroll_view);
		mTagsReal6BListScrollView.setVisibility(View.GONE);
		
		mTagReal6BRow = (TableRow) findViewById(R.id.table_row_tag_real_6b);
		mTagReal6BImage = (ImageView) findViewById(R.id.image_prompt);
		mTagReal6BImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
		mListTextInfo = (TextView) findViewById(R.id.list_text_info);
		mListTextInfo.setText(getResources().getString(R.string.open_tag_list));

		mTagsCountText = (TextView) findViewById(R.id.tags_count_text);
		
		mTagReal6BRow.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (mTagsReal6BListScrollView.getVisibility() != View.VISIBLE) {
					mTagsReal6BListScrollView.setVisibility(View.VISIBLE);
					mTagReal6BImage.setImageDrawable(getResources().getDrawable(R.drawable.down));
					mListTextInfo.setText(getResources().getString(R.string.close_tag_list));
				} else {
					mTagsReal6BListScrollView.setVisibility(View.GONE);
					mTagReal6BImage.setImageDrawable(getResources().getDrawable(R.drawable.up));
					mListTextInfo.setText(getResources().getString(R.string.open_tag_list));
				}
			}
		});
		
		mTagReal6BList = (ListView) findViewById(R.id.tag_real_6b_list_view);
		mReal6BListAdapter = new Real6BListAdapter(mContext, data);
		mTagReal6BList.setAdapter(mReal6BListAdapter);
		
		mTagReal6BList.setOnItemClickListener(new OnItemClickListener() {

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
	}
	
	public final void refreshText() {
		mTagsCountText.setText(String.valueOf(m_curOperateTagISO18000Buffer.lsTagList.size()));
	}
	
	public final void refreshList() {
		data.clear();
		data.addAll(m_curOperateTagISO18000Buffer.lsTagList);
		mReal6BListAdapter.notifyDataSetChanged();
	}	
}
