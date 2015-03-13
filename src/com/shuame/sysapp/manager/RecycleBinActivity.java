/*
 * (#)RecycleBinActivity.java 1.0 2015年3月13日 2015年3月13日 GMT+08:00
 */
package com.shuame.sysapp.manager;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.shuame.sysapp.manager.R;

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

	// 系统应用列表
	List<AppInfo> sysAppList;

	DataSqlManager dataSqlManager;

	private View itemView;

	private Button btnCleanAll;

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
		btnCleanAll = (Button) findViewById(R.id.btn_clean_all);

		LayoutInflater mInflater = getLayoutInflater();
		itemView = mInflater.inflate(R.layout.list_view_item, null);
		dataSqlManager = DataSqlManager.getInstance(this);
		uninstallAppList = dataSqlManager.getUninstallAppList();
		sysAppList = dataSqlManager.getSysAppList();
		lvRecycleBinAdapter = new MyListViewAdapter(uninstallAppList, itemView,
			AppManagerUtil.initBackupPath(), this, false, false);
		// 设置Adapter
		lvRecycleBin.setAdapter(lvRecycleBinAdapter);
	}

	public void onClick(View v) {
		// 点击事件处理
		int id = v.getId();
		if (id == R.id.btn_clean_all) {
			// 清空回收站所有数据

		} else if (id == R.id.iv_back) {
			// 退出当前activity
			finish();
		}
	}

}
