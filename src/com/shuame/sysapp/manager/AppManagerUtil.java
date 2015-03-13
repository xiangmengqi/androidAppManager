/*
 * (#)AppManagerUtil.java 1.0 2015年3月11日 2015年3月11日 GMT+08:00
 */
package com.shuame.sysapp.manager;

import java.io.File;

import android.R.integer;
import android.os.Environment;
import android.util.Log;

import com.shuame.sysapp.manager.ScriptUtil.ScriptHandler;

/**
 * @author JackXiang
 * @version $1.0, 2015年3月11日 2015年3月11日 GMT+08:00
 * @since JDK5
 */
public class AppManagerUtil {

	// 卸载app方法
	public static void uninstallApp(String sourcedir, String apkBackupPath,
			String backupFilePath, String packageName) {
		ScriptUtil commandUtil;
		// 为确保命令使用正常，需要使用shuame
		// busybox，推送busybox到/data/local/tmp/buxybox
		// 确保system分区正常挂载
		ensureSystemMounted();
		// 改变相关文件权限
		StringBuilder strbuilder1 = new StringBuilder();
		String chmodCommand = strbuilder1.append(
			"/data/local/tmp/busybox chmod 755 ")
				.append(sourcedir)
				.toString();
		commandUtil = new ScriptUtil(chmodCommand, scriptHandler);
		Log.i("chmod 755 xx.apk command", chmodCommand);
		commandUtil.execCommand();

		strbuilder1.delete(0, strbuilder1.length());
		String backupCommand = strbuilder1.append("/data/local/tmp/busybox mv ")
				.append(sourcedir)
				.append(" ")
				.append(apkBackupPath)
				.toString();
		commandUtil = new ScriptUtil(backupCommand, scriptHandler);
		Log.i("mv xx.apk command", backupCommand);
		commandUtil.execCommand();

		// 备份packageName数据文件夹到制定目录
		strbuilder1.delete(0, strbuilder1.length());
		String packageNameFilePath = strbuilder1.append("/data/data/")
				.append(packageName)
				.toString();

		strbuilder1.delete(0, strbuilder1.length());
		String backupCommand1 = strbuilder1.append(
			"/data/local/tmp/busybox mv -f ")
				.append(packageNameFilePath)
				.append(" ")
				.append(backupFilePath)
				.toString();
		commandUtil = new ScriptUtil(backupCommand1, scriptHandler);
		Log.i("mv packageFile command", backupCommand1);
		commandUtil.execCommand();
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
	public static void restoreApp(String sourcedir, String apkBackupPath,
			String backupFilePath, String packageName) {
		ScriptUtil commandUtil;
		// 确保system分区正常挂载
		ensureSystemMounted();

		// 将数据文件夹mv到/data/data/目录
		StringBuilder strbuilder = new StringBuilder();
		String appPackageDataBackupPath = strbuilder.append(backupFilePath)
				.append("/")
				.append(packageName)
				.toString();
		strbuilder.delete(0, strbuilder.length());
		String sysAppRestoreCommand = strbuilder.append("mv -f ")
				.append(appPackageDataBackupPath)
				.append(" ")
				.append("/data/data/")
				.toString();
		commandUtil = new ScriptUtil(sysAppRestoreCommand, scriptHandler);
		Log.i("restore package command", sysAppRestoreCommand);
		commandUtil.execCommand();

		// 将xx.apk文件push到/system/app/目录
		strbuilder.delete(0, strbuilder.length());
		String appApkbackupCommand = strbuilder.append("mv ")
				.append(apkBackupPath)
				.append(" ")
				.append(sourcedir)
				.toString();
		commandUtil = new ScriptUtil(appApkbackupCommand, scriptHandler);
		Log.i("restore xx.apk command", appApkbackupCommand);
		commandUtil.execCommand();
	}

	// 确保system分区挂载上
	public static void ensureSystemMounted() {
		ScriptUtil commandUtil;
		// 挂载system分区
		StringBuilder strbuilder = new StringBuilder();
		String mountSystem = strbuilder.append(
			"/data/local/tmp/busybox mount -o remount,rw /system").toString();
		commandUtil = new ScriptUtil(mountSystem, scriptHandler);
		Log.i("mount system command", mountSystem);
		commandUtil.execCommand();

		// 使用系统自带命令再挂载一次system，确保system正确挂载
		strbuilder.delete(0, strbuilder.length());
		String reMountSystem = strbuilder.append("mount -o remount,rw /system")
				.toString();
		commandUtil = new ScriptUtil(reMountSystem, scriptHandler);
		Log.i("remount system command", reMountSystem);
		commandUtil.execCommand();
	}

	// 卸载app备份文件夹初始化
	public static String initBackupPath() {
		ScriptUtil commandUtil;
		// 检测/mnt/sdcard是否存在
		StringBuilder strbuilder = new StringBuilder();
		String sdcardPath = Environment.getExternalStorageDirectory()
				.toString();
		String backupPath = strbuilder.append(sdcardPath)
				.append("/rootgeniusBackup")
				.toString();
		// 判断文件是否存在
		File backupFilePath = new File(backupPath);
		if (!backupFilePath.exists()) {
			Log.i("test", "1111111111=====");
			// backupFilePath.mkdirs();
			// 有时候上面的方法会失效？
			strbuilder.delete(0, strbuilder.length());
			commandUtil = new ScriptUtil(strbuilder.append(
				"/data/local/tmp/busybox mkdir ")
					.append(backupPath)
					.toString(), scriptHandler);
			// Log.i("mkdir backupPath command", chmodCommand);
			commandUtil.execCommand();
		}
		Log.i("test", "backupPath: " + backupFilePath.toString());
		return backupFilePath.toString();
	}

	static ScriptHandler scriptHandler = new ScriptHandler() {
		@Override
		public void onSuccess(String strCorrect) {
		}

		@Override
		public void onFailed(String strError) {
		}
	};
}
