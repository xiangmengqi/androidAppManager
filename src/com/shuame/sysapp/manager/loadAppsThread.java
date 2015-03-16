package com.shuame.sysapp.manager;

import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;

public class loadAppsThread extends Thread {
	private static final String TAG = "loadAppsThread";

	private long size;

	private Context context;

	private List<AppInfo> sysAppList;

	private List<AppInfo> uninstallAppList;

	private List<AppInfo> appList;

	private Handler handler;

	public loadAppsThread(Context context, List<AppInfo> appList,
			List<AppInfo> sysAppList, List<AppInfo> uninstallAppList) {
		this.context = context;
		this.sysAppList = sysAppList;
		this.appList = appList;
		this.uninstallAppList = uninstallAppList;
		this.sysAppList = sysAppList;
		this.handler = (Handler) ((SysAppListActivity) context).getHandler();
	}

	public void run() {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packs = pm.getInstalledPackages(0);
		for (PackageInfo pi : packs) {
			if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
				// 表示是系统程序，但用户更新过，也算是用户安装的程序
				// 这种情况下如何卸载？

			} else if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
				// 系统应用
				if (isUninstallApp(pi.applicationInfo.packageName) == false) {
					sysAppList.add(getAppInfo(pi, pm));
				}
			} else {
				// 普通应用
				appList.add(getAppInfo(pi, pm));
			}
		}
		// debug
		for (AppInfo si : sysAppList) {
			Log.i("test1", " app name: " + si.appName + " packageName: "
					+ si.packageName + " sourceDir: " + si.sourcedir);
		}
		// 执行完毕后进行界面更新
		handler.sendEmptyMessage(0);
	}

	public Boolean isUninstallApp(String packageName) {
		for (AppInfo sysappInfo : uninstallAppList) {
			if (sysappInfo.packageName.equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	public AppInfo getAppInfo(PackageInfo pi, PackageManager pm) {
		AppInfo appInfo = new AppInfo();
		appInfo.packageName = pi.applicationInfo.packageName;
		appInfo.sourcedir = pi.applicationInfo.sourceDir;
		ApplicationInfo applicationInfo = null;
		try {
			applicationInfo = pm.getApplicationInfo(appInfo.packageName, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		appInfo.appIcon = pm.getApplicationIcon(applicationInfo);
		appInfo.appName = pm.getApplicationLabel(applicationInfo).toString();
		// 计算package大小
		try {
			queryPacakgeSize(appInfo.packageName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		appInfo.packageSize = Formatter.formatFileSize(context, size);
		return appInfo;
	}

	public class PkgSizeObserver extends IPackageStatsObserver.Stub {
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			Long cachesize, datasize, codesize;
			cachesize = pStats.cacheSize;
			datasize = pStats.dataSize;
			codesize = pStats.codeSize;
			size = cachesize + datasize + codesize;
			Log.i(TAG, "cachesize--->" + cachesize + " datasize---->"
					+ datasize + " codeSize---->" + codesize);
		}
	}

	// 计算package大小
	public void queryPacakgeSize(String pkgName) throws Exception {
		if (pkgName != null) {
			PackageManager pm = context.getPackageManager();
			try {
				Method getPackageSizeInfo = pm.getClass().getMethod(
					"getPackageSizeInfo", String.class,
					IPackageStatsObserver.class);
				getPackageSizeInfo.invoke(pm, pkgName, new PkgSizeObserver());
			} catch (Exception ex) {
				Log.e(TAG, "NoSuchMethodException");
				ex.printStackTrace();
				throw ex;
			}
		}
	}
}
