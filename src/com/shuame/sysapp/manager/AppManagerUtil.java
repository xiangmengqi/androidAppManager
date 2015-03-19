/*
 * (#)AppManagerUtil.java 1.0 2015年3月11日 2015年3月11日 GMT+08:00
 */
package com.shuame.sysapp.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.appcompat.R.bool;
import android.util.Log;

import com.shuame.sysapp.manager.ShellUtils.CommandResult;

/**
 * @author JackXiang
 * @version $1.0, 2015年3月11日 2015年3月11日 GMT+08:00
 * @since JDK5
 */

public class AppManagerUtil {

	public static class ThreadUninstall extends Thread {

		private String mBackupFilePath;

		private Handler mHandler;

		private AppInfo mListItem;

		private int mItemPosition;

		private CommandResult mCommandResult;

		private Boolean mISysApp;

		public ThreadUninstall(Context context, String backupFilePath,
				AppInfo appInfo, int ItemPosition, Boolean iSysApp) {
			mListItem = appInfo;
			mBackupFilePath = backupFilePath;
			mISysApp = iSysApp;
			mItemPosition = ItemPosition;
			mHandler = (Handler) ((SysAppListActivity) context).getHandler();
		}

		@Override
		public void run() {
			super.run();
			if (mISysApp) {
				mCommandResult = AppManagerUtil.uninstallApp(
					mListItem.sourcedir, mListItem.apkBackupPath,
					mBackupFilePath, mListItem.packageName);
				// 卸载完成之后，向主线程发消息
				Message message = mHandler.obtainMessage();
				message.what = AppConstants.MSG_ON_UNINSTALL_SYSTEM_APP;
				mCommandResult.setAppInfo(mListItem);
				mCommandResult.setItemPosition(mItemPosition);
				message.obj = mCommandResult;
				mHandler.sendMessage(message);
			} else {
				mCommandResult = AppManagerUtil.uninstallNormalApp(mListItem.packageName);
				// 卸载完成之后，向主线程发消息
				Message message = mHandler.obtainMessage();
				message.what = AppConstants.MSG_ON_UNINSTALL_NORMAL_SYSTEM_APP;
				mCommandResult.setAppInfo(mListItem);
				mCommandResult.setItemPosition(mItemPosition);
				message.obj = mCommandResult;
				mHandler.sendMessage(message);
			}

		}
	}

	public static class ThreadRestore extends Thread {

		private String mBackupFilePath;

		private Handler mHandler;

		private AppInfo mListItem;

		private CommandResult mCommandResult;

		public ThreadRestore(Context context, String backupFilePath,
				AppInfo appInfo) {
			mListItem = appInfo;
			mBackupFilePath = backupFilePath;
			mHandler = (Handler) ((RecycleBinActivity) context).getHandler();
		}

		@Override
		public void run() {
			super.run();
			mCommandResult = AppManagerUtil.restoreApp(
				mListItem.sourcedir, mListItem.apkBackupPath, mBackupFilePath,
				mListItem.packageName);
			// 卸载完成之后，向主线程发消息
			Message message = mHandler.obtainMessage();
			message.what = AppConstants.MSG_ON_RESTORE_SYSTEM_APP;
			mCommandResult.setAppInfo(mListItem);
			message.obj = mCommandResult;
			mHandler.sendMessage(message);
		}
	}

	private static final String TAG = null;

	// private static String mBackupFilePath;

	// public static void rmBackupFile() {
	// ScriptUtil commandUtil;
	// StringBuilder strbuilder = new StringBuilder();
	// String rmCommand = strbuilder.append("/data/local/tmp/busybox rm -rf ")
	// .append(mBackupFilePath)
	// .toString();
	// commandUtil = new ScriptUtil(rmCommand, scriptHandler);
	// Log.i("rm BackupFile", rmCommand);
	// commandUtil.execCommand();
	// }

	// 实际执行卸载还原的模块
	// 直接判断app.apk文件是否存在,如system/app/app.apk文件不存在，且成功备份了
	public static CommandResult uninstallApp(String sourcedir,
			String apkBackupPath, String backupFilePath, String packageName) {
		// 改变相关文件权限
		StringBuilder strbuilder1 = new StringBuilder();
		String chmodCommand = strbuilder1.append(
			"/data/local/tmp/busybox chmod 755 ")
				.append(sourcedir)
				.toString();

		strbuilder1.delete(0, strbuilder1.length());
		String mvApkCommand = strbuilder1.append("/data/local/tmp/busybox mv ")
				.append(sourcedir)
				.append(" ")
				.append(apkBackupPath)
				.toString();

		// 备份packageName数据文件夹到制定目录,打包 (.tar.gz)
		strbuilder1.delete(0, strbuilder1.length());
		String packageNameFilePath = strbuilder1.append("/data/data/")
				.append(packageName)
				.toString();

		strbuilder1.delete(0, strbuilder1.length());
		String backupFilePathTarGz = strbuilder1.append(backupFilePath)
				.append("/")
				.append(packageName)
				.append(".tar.gz")
				.toString();

		strbuilder1.delete(0, strbuilder1.length());
		String excludeTargz = strbuilder1.append("--exclude=")
				.append(packageNameFilePath)
				.append("/lib")
				.toString();

		strbuilder1.delete(0, strbuilder1.length());
		String targzPackageCommand = strbuilder1.append(
			"/data/local/tmp/busybox tar -cvf ")
				.append(backupFilePathTarGz)
				.append(" ")
				.append(excludeTargz)
				.append(" ")
				.append(packageNameFilePath)
				.toString();

		List<String> commnandList = new ArrayList<String>();
		commnandList.add("mount -o remount,rw /system");
		commnandList.add(mvApkCommand);
		commnandList.add(targzPackageCommand);
		CommandResult commandResult = ShellUtils.execCommand(commnandList, true);
		// 判断是否成功移动apk文件
		Log.i("uninstallApp", "result : " + commandResult.getResult());
		Log.i("uninstallApp", "successMsg : " + commandResult.successMsg);
		Log.i("uninstallApp", "errorMsg : " + commandResult.errorMsg);

		Log.i("uninstallApp", "sourcedir : " + sourcedir);
		Log.i("uninstallApp", "apkBackupPath : " + apkBackupPath);
		Log.i(
			"uninstallApp",
			"sourcedir isExist: " + (new File(sourcedir)).exists());
		Log.i("uninstallApp", "apkBackupPath isExist: "
				+ (new File(apkBackupPath)).exists());

		commandResult.setActionResult(isUninstallSuccess(
			sourcedir, apkBackupPath));

		return commandResult;
	}

	public static int isUninstallSuccess(String sourcedir, String apkBackupPath) {
		// 检测文件是否存在
		StringBuilder strbuilder = new StringBuilder();
		String testCommand = strbuilder.append("test -e ")
				.append(apkBackupPath)
				.append(" -a ! -e ")
				.append(sourcedir)
				.append(" && echo \"true\" || echo \"false\"")
				.toString();
		Log.i("uninstallApp", "isUninstallSuccess testCommand: " + testCommand);
		List<String> commnandList = new ArrayList<String>();
		commnandList.add(testCommand);
		CommandResult commandResult = ShellUtils.execCommand(commnandList, true);
		Log.i(
			"uninstallApp",
			"isUninstallSuccess result : " + commandResult.getResult());
		Log.i("uninstallApp", "isUninstallSuccess successMsg: "
				+ commandResult.successMsg);
		Log.i("uninstallApp", "isUninstallSuccess errorMsg: "
				+ commandResult.errorMsg);
		if (commandResult.successMsg.equals("true")) {
			return 0;
		} else {
			return 1;
		}
	}

	public static CommandResult uninstallNormalApp(String packageName) {
		StringBuilder strbuilder = new StringBuilder();
		String uninstallCommand = strbuilder.append("pm uninstall ")
				.append(packageName)
				.toString();
		Log.i("uninstall normal app", uninstallCommand);
		List<String> commnandList = new ArrayList<String>();
		commnandList.add(uninstallCommand);
		CommandResult result = ShellUtils.execCommand(commnandList, false);
		return result;
	}

	public static String getApkBackupFilePath(String backupFilePath,
			String sourcedir) {
		// 备份xx.apk文件到backupFilePath
		// 在backupFilePath目录下建立sourcedir子目录
		// 从sourcedir截取apk名
		String apkName = sourcedir.substring(
			sourcedir.lastIndexOf("/"), sourcedir.length());
		Log.i("apkName: ", apkName);

		StringBuilder strbuilder = new StringBuilder();
		String apkBackupPath = strbuilder.append(backupFilePath)
				.append(apkName)
				.toString();
		return apkBackupPath;
	}

	// 还原app方法
	public static CommandResult restoreApp(String sourcedir,
			String apkBackupPath, String backupFilePath, String packageName) {
		StringBuilder strbuilder = new StringBuilder();
		// 将xx.apk文件push到/system/app/目录

		String restoreApkCommand = strbuilder.append("mv ")
				.append(apkBackupPath)
				.append(" ")
				.append(sourcedir)
				.toString();

		// 还原app数据（解压缩package数据）
		strbuilder.delete(0, strbuilder.length());
		String backupFilePathTarGz = strbuilder.append(backupFilePath)
				.append("/")
				.append(packageName)
				.append(".tar.gz")
				.toString();

		strbuilder.delete(0, strbuilder.length());
		String targzPackageRestoreCommand = strbuilder.append("tar -zxvf ")
				.append(backupFilePathTarGz)
				.append(" -C ")
				.append("/data/data/")
				.toString();

		List<String> commnandList = new ArrayList<String>();
		commnandList.add("mount -o remount,rw /system");
		commnandList.add(restoreApkCommand);
		commnandList.add(targzPackageRestoreCommand);
		CommandResult commandResult = ShellUtils.execCommand(commnandList, true);
		// 判断是否成功移动apk文件
		commandResult.setActionResult(isUninstallSuccess(
			apkBackupPath, sourcedir));		
		return commandResult;
	}

	// 卸载app备份文件夹初始化
	public static String initBackupPath(Context context) {
		List<String> commnandList = new ArrayList<String>();
		// // 检测/mnt/sdcard是否存在
		// StringBuilder strbuilder = new StringBuilder();
		// String sdcardPath = context.getExternalCacheDir().getAbsolutePath();
		// String backupPath0 = strbuilder.append(sdcardPath)
		// .append("/rootgeniusBackup")
		// .toString();
		//
		// strbuilder.delete(0, strbuilder.length());
		// String mkdirCommand0 = strbuilder.append(
		// "/data/local/tmp/busybox mkdir ")
		// .append(backupPath0)
		// .toString();
		//
		// String backupPath1 = new String("/sdcard/rootgeniusBackup");
		// strbuilder.delete(0, strbuilder.length());
		// String mkdirCommand1 = strbuilder.append(
		// "/data/local/tmp/busybox mkdir ")
		// .append(backupPath1)
		// .toString();
		//
		// commnandList.add(mkdirCommand0);
		// commnandList.add(mkdirCommand1);
		// CommandResult commandResult = ShellUtils.execCommand(commnandList,
		// true);
		// Log.e(TAG, "backupPath0" + backupPath0);
		// Log.e(TAG, "backupPath1" + backupPath1);
		// File file0 = new File(backupPath0);
		// File file1 = new File(backupPath1);
		//
		// if (file0.exists()) {
		// return backupPath0;
		// } else if (file1.exists()) {
		// return backupPath1;
		// } else {
		// return null;
		// }

		// 直接采用data分区保存相关数据（/data/rootgeniusBackup）
		StringBuilder strbuilder = new StringBuilder();
		String backupPath = new String("/data/rootgeniusBackup");
		String mkdirCommand = strbuilder.append(
			"/data/local/tmp/busybox mkdir ")
				.append(backupPath)
				.toString();
		commnandList.add(mkdirCommand);
		CommandResult commandResult = ShellUtils.execCommand(commnandList, true);
		return backupPath;
	}
}
