package com.shuame.sysapp.manager;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.shuame.sysapp.manager.AppManagerUtil.ThreadRestore;
import com.shuame.sysapp.manager.ShellUtils.CommandResult;

public class MyListViewAdapter extends BaseAdapter implements
		View.OnClickListener {
	private List<AppInfo> mListViewData;

	private boolean mBottomVisiable;

	private boolean mIsUninstall;

	private String mBackupFilePath;

	private static final int VIEW_TAG_ID = 0x7f01001;

	private Context mContext;

	private DataSqlManager mDataSqlManager;

	// 传入ListView显示需要的数据
	public MyListViewAdapter(List<AppInfo> ListViewData, String backupFilePath,
			Context context, boolean visiable, boolean isUninstall) {
		mListViewData = ListViewData;
		mBackupFilePath = backupFilePath;
		mContext = context;
		mBottomVisiable = visiable;
		mIsUninstall = isUninstall;
	}

	public int getCount() {
		return mListViewData.size();
	}

	public AppInfo getItem(int arg0) {
		return mListViewData.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

	public View getView(int position, View view, ViewGroup arg2) {
		ViewHolder holder = null;
		if (view == null) {
			holder = new ViewHolder();
			view = ((Activity) mContext).getLayoutInflater().inflate(
				R.layout.list_view_item, null);
			holder.appName = (TextView) view.findViewById(R.id.tv_appName);
			holder.appIcon = (ImageView) view.findViewById(R.id.iv_appIcon);
			holder.packageSize = (TextView) view.findViewById(R.id.tv_appSize);
			holder.appIntroduction = (TextView) view.findViewById(R.id.tv_appIntro);
			holder.uninstall = (Button) view.findViewById(R.id.btn_uninstall);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		AppInfo item = getItem(position);
		// set viewHolder
		holder.appName.setText(item.appName);
		holder.packageSize.setText(item.packageSize);
		holder.appIcon.setImageDrawable(item.appIcon);
		holder.appIntroduction.setText("good app");
		if (mIsUninstall == false) {
			// 此时设置button为还原
			holder.uninstall.setText("还原");
		}

		if (!mBottomVisiable) {
			holder.appIntroduction.setVisibility(View.GONE);
		}

		holder.uninstall.setOnClickListener(this);
		holder.uninstall.setTag(VIEW_TAG_ID, position);
		return view;
	}

	public class ViewHolder {
		public ImageView appIcon;

		public TextView appName;

		// package占用空间大小
		public TextView packageSize;

		// 卸载按钮
		public Button uninstall;

		// 后台下发app一句话介绍
		public TextView appIntroduction;
	}

	CommandResult result;

	public void onClick(View view) {
		int position = (Integer) view.getTag(VIEW_TAG_ID);
		AppInfo listItem = getItem(position);
		if ((mBottomVisiable == true) && (mIsUninstall == true)) {
			// 预装应用卸载
			listItem = mListViewData.get(position);
			listItem.apkBackupPath = AppManagerUtil.getApkBackupFilePath(
				mBackupFilePath, listItem.sourcedir);

			// 后台启动新线程，后台执行卸载命令
			new AppManagerUtil.ThreadUninstall(mContext, mBackupFilePath,
				listItem, position, true).start();

			// 被点击item开始显示卸载中动画(button键位置显示转圈动画)
			// 转圈动画

			// 如卸载成功则刷新列表，显示卸载动画，更新数据库数据；如下载失败则弹窗提示

		} else if ((mBottomVisiable == false) && (mIsUninstall == true)) {
			// 普通应用卸载,直接采用pm uninstall packageName
			listItem = mListViewData.get(position);
			AppManagerUtil.uninstallNormalApp(listItem.packageName);
			mListViewData.remove(listItem);
			this.notifyDataSetChanged();
		} else if ((mBottomVisiable == false) && (mIsUninstall == false)) {
			// 系统应用还原
			listItem = mListViewData.get(position);
			listItem.apkBackupPath = AppManagerUtil.getApkBackupFilePath(
				mBackupFilePath, listItem.sourcedir);

			// 后台启动新线程，后台执行卸载命令
			new AppManagerUtil.ThreadRestore(mContext, mBackupFilePath,
				listItem).start();

		}
	}
}
