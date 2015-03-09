package com.shuame.sysapp.manager;

import java.util.List;

import android.R.string;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.util.Log;

public class loadAppsThread extends Thread {
	private Context context;

	private List<sysAppInfo> sysAppList;

	private List<sysAppInfo> uninstallAppList;

	private Handler handler;

	public loadAppsThread(Context context, List<sysAppInfo> sysAppList,
			List<sysAppInfo> uninstallAppList, Handler handler) {
		this.context = context;
		this.sysAppList = sysAppList;
		this.handler = handler;
		this.uninstallAppList = uninstallAppList;
	}

	public void run() {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packs = pm.getInstalledPackages(0);
		for (PackageInfo pi : packs) {
			sysAppInfo sysappInfo = new sysAppInfo();
			if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
				// 表示是系统程序，但用户更新过，也算是用户安装的程序
				//
			} else if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
				// 系统应用
				sysappInfo.packageName = pi.applicationInfo.packageName;
				if (isUninstallApp(sysappInfo.packageName) == false) {
					sysappInfo.sourcedir = pi.applicationInfo.sourceDir;
					ApplicationInfo applicationInfo = null;
					try {
						applicationInfo = pm.getApplicationInfo(
							sysappInfo.packageName, 0);
					} catch (NameNotFoundException e) {
						e.printStackTrace();
					}
					sysappInfo.appIcon = pm.getApplicationIcon(applicationInfo);
					sysappInfo.appName = pm.getApplicationLabel(applicationInfo)
							.toString();
					sysAppList.add(sysappInfo);
				}
			}
		}
		// debug
		for (sysAppInfo si : sysAppList) {
			Log.i("test1", " app name: " + si.appName + " packageName: "
					+ si.packageName + " sourceDir: " + si.sourcedir);
		}
		// 执行完毕后进行界面更新
		handler.sendEmptyMessage(0);
	}

	public Boolean isUninstallApp(String packageName) {
		for (sysAppInfo sysappInfo : uninstallAppList) {
			if (sysappInfo.packageName.equals(packageName)) {
				return true;
			}
		}
		return false;
	}
}
