package com.shuame.sysapp.manager;

import android.graphics.drawable.Drawable;

public class AppInfo {
	public String packageName;

	public Drawable appIcon;

	public String appName;

	public String sourcedir;

	public String apkBackupPath;

	// package占用空间大小
	public String packageSize;
	
	// 从后台获取app一句话描述信息
	public String appIntroduction;
}
