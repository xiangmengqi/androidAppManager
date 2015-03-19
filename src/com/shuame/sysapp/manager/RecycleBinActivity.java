/*
 * (#)RecycleBinActivity.java 1.0 2015年3月13日 2015年3月13日 GMT+08:00
 */
package com.shuame.sysapp.manager;

import java.util.List;

import com.shuame.sysapp.manager.ShellUtils.CommandResult;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author JackXiang
 * @version $1.0, 2015年3月13日 2015年3月13日 GMT+08:00
 */
public class RecycleBinActivity extends Activity implements OnClickListener {
	private TextView tvTitle;

	private ListView lvRecycleBin;

	private MyListViewAdapter lvRecycleBinAdapter;

	// 可以恢复的系统应用列表
	List<AppInfo> uninstallAppList;

	DataSqlManager dataSqlManager;

	private Button btnCleanAll;

	private ImageView ivBack;

	// 回调消息
	private CommandResult commandResult;

	Context mContext = this;

	// 主线程消息队列
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == AppConstants.MSG_ON_RESTORE_SYSTEM_APP) {
				// 回收站卸载命令回收站
				commandResult = (CommandResult) msg.obj;
				// 打印后台结果
				Log.i("CommandResult", "CommandResult: " + commandResult.result);
				Log.i("CommandResult", "CommandResultSuccess: "
						+ commandResult.successMsg);
				Log.i("CommandResult", "CommandResultError: "
						+ commandResult.errorMsg);
				// 判断卸载结果并分别处理
				// 此处加上动画

				if (commandResult.getActionResult() == 0) {
					// 记录已选中数据到数据库, 首先将数据进行序列化将数据保存至db数据库,将appIcon数据序列化
					dataSqlManager = DataSqlManager.getInstance(mContext);
					dataSqlManager.dbDeleteItem(commandResult.getAppInfo());

					// 动画逻辑后续在此添加
					// 目前对于回收站没有动画需求

					// 此处加上转圈动画

					// 刷新listView列表显示
					lvRecycleBinAdapter.notifyDataSetChanged();
				} else {
					// toast提示失败
					Toast.makeText(mContext, "卸载应用失败", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	};

	public Handler getHandler() {
		return this.mHandler;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recycle_bin);
		init();
	}

	public void init() {
		// 设置title为回收站
		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvTitle.setText("回收站");

		lvRecycleBin = (ListView) findViewById(R.id.lv_recycle_list);
		// 设置清空回收站按钮
		btnCleanAll = (Button) findViewById(R.id.btn_clean_all);
		btnCleanAll.setOnClickListener(this);

		// 设置返回图标点击事件
		ivBack = (ImageView) findViewById(R.id.iv_back);
		ivBack.setOnClickListener(this);

		LayoutInflater mInflater = getLayoutInflater();
		dataSqlManager = DataSqlManager.getInstance(this);
		uninstallAppList = dataSqlManager.getUninstallAppList();
		lvRecycleBinAdapter = new MyListViewAdapter(uninstallAppList,
			AppManagerUtil.initBackupPath(this), this, false, false);
		// 设置Adapter
		lvRecycleBin.setAdapter(lvRecycleBinAdapter);
	}

	public void onClick(View v) {
		// 点击事件处理
		int id = v.getId();
		if (id == R.id.btn_clean_all) {
			// 清空回收站所有数据(直接删除备份文件,清空数据库，清空内存中卸载app数据，最后刷下列表显示)
			// AppManagerUtil.rmBackupFile();
			dataSqlManager.resetData();
			lvRecycleBinAdapter.notifyDataSetChanged();
		} else if (id == R.id.iv_back) {
			// 退出当前activity
			finish();
		}
	}
}
