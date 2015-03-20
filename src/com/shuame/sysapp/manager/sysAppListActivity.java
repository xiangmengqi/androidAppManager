package com.shuame.sysapp.manager;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shuame.sysapp.manager.ShellUtils.CommandResult;

public class SysAppListActivity extends Activity implements OnClickListener {
	private static final String TAG = "sysAppListActivity";

	private String backupFilePath;

	// 非系统应用列表
	private List<AppInfo> appList;

	// 系统应用列表
	private List<AppInfo> sysAppList;

	// 回调消息
	private CommandResult commandResult;

	private Context mContext = this;

	// 主线程消息队列
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			// 收到消息队列反馈则继续UI线程的操作
			switch (msg.what) {
			case AppConstants.MSG_ON_BACKGROUND_CALCULATION:
				// 显示扫描结果
				initView();
				break;
			// 系统应用卸载队列
			case AppConstants.MSG_ON_UNINSTALL_SYSTEM_APP:
				commandResult = (CommandResult) msg.obj;
				// 判断卸载结果并分别处理
				// 此处加上动画
				if (commandResult.getActionResult() == 0) {
					// 记录已选中数据到数据库, 首先将数据进行序列化将数据保存至db数据库,将appIcon数据序列化
					dataSqlManager = DataSqlManager.getInstance(mContext);
					dataSqlManager.dbInsertItem(commandResult.getAppInfo());

					// 动画逻辑后续在此添加
					// ....

					// 更新回收站button显示
					((SysAppListActivity) mContext).setRecycleBinShow();
					// 刷新listView列表显示
					mListViewAdapter1.notifyDataSetChanged();
				} else {
					// toast提示失败
					Toast.makeText(mContext, "卸载应用失败", Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				break;
			}
		};
	};

	public Handler getHandler() {
		return this.mHandler;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_manager);

		// 数据库创建&读取
		getSqlData();

		// 设置数据备份路径
		// 如果目录已经存在则不做处理，否则新建目录
		backupFilePath = AppManagerUtil.initBackupPath(this);

		// 不是第一次进入此activity
		if (isInitView == false) {
			initView();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 刷新button显示
		setRecycleBinShow();
		mListViewAdapter1.notifyDataSetChanged();
	}

	private DataSqlManager dataSqlManager;

	// 数据库相关
	public void getSqlData() {
		// 读取定义的数据库
		dataSqlManager = DataSqlManager.getInstance(this);

		Log.i(TAG, "appInfoCounts: " + dataSqlManager.getCount());
		// 同步数据库与内存数据（回收站信息）
		sysAppList = dataSqlManager.getSysAppList();
		appList = dataSqlManager.getAppList();

	}

	private ImageView cursor;// 动画图片

	private int offset = 0;// 动画图片偏移量

	private TextView tvSystemApp, tvNormalApp;

	private ViewPager mPager;// 页卡内容

	private List<View> listViews; // Tab页面列表

	private int currIndex = 0;// 当前页卡编号

	private ListView mSysAppListView, mAppListView;

	private View pageView1, pageView2;

	private Button btnRecycle;

	private ImageView ivBack;

	private MyListViewAdapter mListViewAdapter1, mListViewAdapter2;

	private boolean isInitView = false;

	// 刷新程序显示列表,检测用户item点击时间,并变更相应程序自启状态
	public void initView() {
		// 初始化动画
		InitImageView();
		// 初始化头标
		InitTextView();
		// 初始化ViewPager
		InitViewPager();
		// 初始化listView
		InitListView();
		isInitView = true;
	}

	public void InitListView() {
		LayoutInflater mInflater = getLayoutInflater();
		// 获取listView对象
		mSysAppListView = (ListView) pageView1.findViewById(R.id.lv_sysapp);
		mAppListView = (ListView) pageView2.findViewById(R.id.lv_applist);
		// 回收站按钮
		btnRecycle = (Button) pageView1.findViewById(R.id.btn_recycle);
		btnRecycle.setOnClickListener(this);
		setRecycleBinShow();

		mListViewAdapter1 = new MyListViewAdapter(sysAppList, backupFilePath,
			this, true, true);
		mListViewAdapter2 = new MyListViewAdapter(appList, backupFilePath,
			this, false, true);
		// 设置Adapter
		mSysAppListView.setAdapter(mListViewAdapter1);
		mAppListView.setAdapter(mListViewAdapter2);

	}

	public void setRecycleBinShow() {
		long sqlDataNum = dataSqlManager.getCount();
		String btnRecycleText;
		if (sqlDataNum == 0) {
			btnRecycleText = "回收站";
		} else {
			btnRecycleText = "回收站(" + sqlDataNum + ")";
		}
		btnRecycle.setText(btnRecycleText);
	}

	/**
	 * 初始化ViewPager
	 */
	public void InitViewPager() {
		LayoutInflater mInflater = getLayoutInflater();
		mPager = (ViewPager) findViewById(R.id.viewPager);
		listViews = new ArrayList<View>();
		pageView1 = mInflater.inflate(R.layout.sysapp_list, null);
		pageView2 = mInflater.inflate(R.layout.app_list, null);
		listViews.add(pageView1);
		listViews.add(pageView2);
		mPager.setAdapter(new MyPagerAdapter(listViews));
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * 页卡切换监听
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			switch (arg0) {
			case 0:
				if (currIndex == 1) {
					animation = new TranslateAnimation(offset, 0, 0, 0);
				}
				break;
			case 1:
				if (currIndex == 0) {
					animation = new TranslateAnimation(0, offset, 0, 0);
				}
				break;
			}
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
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

	/**
	 * * 初始化动画
	 */
	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cusor);
		// 获取并设置图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = screenW / 2; // 计算偏移量
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);// 设置动画初始位置
	}

	/**
	 * 初始化头标
	 */
	private void InitTextView() {
		tvSystemApp = (TextView) findViewById(R.id.tv_systemApp);
		tvNormalApp = (TextView) findViewById(R.id.tv_app);

		// 设置返回图标点击事件
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);

		tvSystemApp.setOnClickListener(new MyOnClickListener(0));
		tvNormalApp.setOnClickListener(new MyOnClickListener(1));
	}

	/**
	 * 头标点击监听
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

	// 点击事件
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_recycle) {
			// 启动回收站activity
			Intent i = new Intent(SysAppListActivity.this,
				RecycleBinActivity.class);
			startActivity(i);
		} else if (id == R.id.iv_back) {
			// 退出当前activity
			finish();
		}
		// 根据选中项调整列表,刷新适配器
		// initAdapter();
	}
}
