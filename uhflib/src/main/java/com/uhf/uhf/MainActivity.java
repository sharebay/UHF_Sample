package com.uhf.uhf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.gy.vam.uhflib.R;
import com.uhf.reader.base.ERROR;
import com.uhf.reader.base.ReaderBase;
import com.uhf.reader.helper.ISO180006BOperateTagBuffer;
import com.uhf.reader.helper.InventoryBuffer;
import com.uhf.reader.helper.OperateTagBuffer;
import com.uhf.reader.helper.ReaderHelper;
import com.uhf.reader.helper.ReaderSetting;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	// ViewPager��google SDk���Դ���һ�����Ӱ���һ���࣬��������ʵ����Ļ����л���
	// android-support-v4.jar
	private ViewPager mPager;// ҳ������
	private List<View> listViews; // Tabҳ���б�
	private ImageView cursor;// ����ͼƬ
	private TextView t1, t2, t3;// ҳ��ͷ��
	private int offset = 0;// ����ͼƬƫ����
	private int currIndex = 0;// ��ǰҳ�����
	private int bmpW;// ����ͼƬ���

	private ReaderBase mReader;
	private ReaderHelper mReaderHelper;

	private static ReaderSetting m_curReaderSetting;
	private static InventoryBuffer m_curInventoryBuffer;
	private static OperateTagBuffer m_curOperateTagBuffer;
	private static ISO180006BOperateTagBuffer m_curOperateTagISO18000Buffer;

	private Setting mViewSetting;
	private Tag mTag;
	private Monitor mMonitor;

	private LogList mLogList;

	private LocalBroadcastManager lbm;

	@Override
	protected void onResume() {

		if (mReader != null) {
			if (!mReader.IsAlive())
				mReader.StartWait();
		}
		super.onResume();
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((UHFApplication) getApplication()).addActivity(this);

		InitImageView();
		InitTextView();
		InitViewPager();

		try {
			mReaderHelper = ReaderHelper.getDefaultHelper();
			mReader = mReaderHelper.getReader();
		} catch (Exception e) {
			return;
		}

		m_curReaderSetting = mReaderHelper.getCurReaderSetting();
		m_curInventoryBuffer = mReaderHelper.getCurInventoryBuffer();
		m_curOperateTagBuffer = mReaderHelper.getCurOperateTagBuffer();
		m_curOperateTagISO18000Buffer = mReaderHelper
				.getCurOperateTagISO18000Buffer();

		mLogList = (LogList) findViewById(R.id.log_list);

		mViewSetting = (Setting) listViews.get(0).findViewById(
				R.id.view_setting);
		mTag = (Tag) listViews.get(1).findViewById(R.id.view_tag);
		mMonitor = (Monitor) listViews.get(2).findViewById(R.id.view_monitor);

		mViewSetting.setLogList(mLogList);

		lbm = LocalBroadcastManager.getInstance(this);

		IntentFilter itent = new IntentFilter();
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_ISO18000_6B);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG);
		itent.addAction(ReaderHelper.BROADCAST_REFRESH_READER_SETTING);
		itent.addAction(ReaderHelper.BROADCAST_WRITE_LOG);
		itent.addAction(ReaderHelper.BROADCAST_WRITE_DATA);

		lbm.registerReceiver(mRecv, itent);
	}

	private final BroadcastReceiver mRecv = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					ReaderHelper.BROADCAST_REFRESH_FAST_SWITCH)) {

			} else if (intent.getAction().equals(
					ReaderHelper.BROADCAST_REFRESH_INVENTORY)) {

			} else if (intent.getAction().equals(
					ReaderHelper.BROADCAST_REFRESH_INVENTORY_REAL)) {

			} else if (intent.getAction().equals(
					ReaderHelper.BROADCAST_REFRESH_ISO18000_6B)) {

			} else if (intent.getAction().equals(
					ReaderHelper.BROADCAST_REFRESH_OPERATE_TAG)) {

			} else if (intent.getAction().equals(
					ReaderHelper.BROADCAST_REFRESH_READER_SETTING)) {
				byte btCmd = intent.getByteExtra("cmd", (byte) 0x00);
				mViewSetting.refreshReaderSetting(btCmd);
			} else if (intent.getAction().equals(
					ReaderHelper.BROADCAST_WRITE_LOG)) {
				mLogList.writeLog((String) intent.getStringExtra("log"),
						intent.getIntExtra("type", ERROR.SUCCESS));
			} else if (intent.getAction().equals(
					ReaderHelper.BROADCAST_WRITE_DATA)) {
				mMonitor.writeMonitor((String) intent.getStringExtra("log"),
						intent.getIntExtra("type", ERROR.SUCCESS));
			}
		}
	};

	/**
	 * ��ʼ��ͷ��
	 */
	private void InitTextView() {
		t1 = (TextView) findViewById(R.id.tab_index1);
		t2 = (TextView) findViewById(R.id.tab_index2);
		t3 = (TextView) findViewById(R.id.tab_index3);

		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
		t3.setOnClickListener(new MyOnClickListener(2));
	}

	/**
	 * ��ʼ��ViewPager
	 */
	private void InitViewPager() {
		mPager = (ViewPager) findViewById(R.id.vPager);
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
		listViews.add(mInflater.inflate(R.layout.lay1, null));
		listViews.add(mInflater.inflate(R.layout.lay2, null));
		listViews.add(mInflater.inflate(R.layout.lay3, null));
		mPager.setAdapter(new MyPagerAdapter(listViews));
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * ��ʼ������
	 */
	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.a)
				.getWidth();// ��ȡͼƬ���
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// ��ȡ�ֱ��ʿ��
		offset = (screenW / 3 - bmpW) / 2;// ����ƫ����
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);// ���ö�����ʼλ��
	}

	/**
	 * ViewPager������
	 */
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}

	/**
	 * ͷ��������
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			mPager.setCurrentItem(index);
		}
	};

	/**
	 * ҳ���л�����
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// ҳ��1 -> ҳ��2 ƫ����
		int two = one * 2;// ҳ��1 -> ҳ��3 ƫ����

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			switch (arg0) {
			case 0:
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, 0, 0, 0);
				}
				break;
			case 1:
				if (currIndex == 0) {
					animation = new TranslateAnimation(offset, one, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, one, 0, 0);
				}
				break;
			case 2:
				if (currIndex == 0) {
					animation = new TranslateAnimation(offset, two, 0, 0);
				} else if (currIndex == 1) {
					animation = new TranslateAnimation(one, two, 0, 0);
				}
				break;
			}
			currIndex = arg0;
			animation.setFillAfter(true);// True:ͼƬͣ�ڶ�������λ��
			animation.setDuration(300);
			cursor.startAnimation(animation);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (!mLogList.tryClose())
				askForOut();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void askForOut() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(getString(R.string.alert_diag_title))
				.setMessage(getString(R.string.are_you_sure_to_exit))
				.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								getApplication().onTerminate();

							}
						})
				.setNegativeButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						}).setCancelable(false).show();
	}

/*	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}*/

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (lbm != null)
			lbm.unregisterReceiver(mRecv);
	}
}
