/*
 * (#)AppManagerUtil.java 1.0 2015年3月11日 2015年3月11日 GMT+08:00
 */
package com.shuame.sysapp.manager;

import java.io.File;

import android.R.integer;
import android.R.string;
import android.os.Environment;
import android.text.GetChars;
import android.util.Log;

import com.shuame.sysapp.manager.ScriptUtil.ScriptHandler;

/**
 * @author JackXiang
 * @version $1.0, 2015年3月11日 2015年3月11日 GMT+08:00
 * @since JDK5
 */

public class AppManagerUtil {

//	private static String mBackupFilePath;

//	public static void rmBackupFile() {
//		ScriptUtil commandUtil;
//		StringBuilder strbuilder = new StringBuilder();
//		String rmCommand = strbuilder.append("/data/local/tmp/busybox rm -rf ")
//				.append(mBackupFilePath)
//				.toString();
//		commandUtil = new ScriptUtil(rmCommand, scriptHandler);
//		Log.i("rm BackupFile", rmCommand);
//		commandUtil.execCommand();
//	}

	// 实际执行卸载还原的模块
	// 直接判断app.apk文件是否存在,如system/app/app.apk文件不存在，且成功备份了则
	public static Boolean uninstallApp(String sourcedir, String apkBackupPath,
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
		// 打包 (.tar.gz)
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
		String backupCommand1 = strbuilder1.append(
			"/data/local/tmp/busybox tar -cvf ")
				.append(backupFilePathTarGz)
				.append(" ")
				.append(excludeTargz)
				.append(" ")
				.append(packageNameFilePath)
				.toString();

		commandUtil = new ScriptUtil(backupCommand1, scriptHandler);
		Log.i("mv packageFile command", backupCommand1);
		commandUtil.execCommand();

		// 不是秒速完成的，所以需要等待
		if (((new File(sourcedir)).exists() == false)
				&& ((new File(apkBackupPath)).exists() == true)) {
			return true;
		}
		return false;
	}

	public static void uninstallNormalApp(String packageName) {
		ScriptUtil commandUtil;
		StringBuilder strbuilder = new StringBuilder();
		String uninstallCommand = strbuilder.append(
			"pm uninstall ")
				.append(packageName)
				.toString();
		commandUtil = new ScriptUtil(uninstallCommand, scriptHandler);
		Log.i("uninstall normal app", uninstallCommand);
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
	public static boolean restoreApp(String sourcedir, String apkBackupPath,
			String backupFilePath, String packageName) {
		ScriptUtil commandUtil;
		// 确保system分区正常挂载
		ensureSystemMounted();

		// 解压缩数据
		StringBuilder strbuilder = new StringBuilder();
		String backupFilePathTarGz = strbuilder.append(backupFilePath)
				.append("/")
				.append(packageName)
				.append(".tar.gz")
				.toString();

		strbuilder.delete(0, strbuilder.length());
		String sysAppRestoreCommand = strbuilder.append("tar -zxvf ")
				.append(backupFilePathTarGz)
				.append(" -C ")
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

		// 判断是否还原成功
		if (((new File(sourcedir)).exists() == true)
				&& ((new File(apkBackupPath)).exists() == false)) {
			return true;
		}
		return false;
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
			// backupFilePath.mkdirs();
			// 有时候上面的方法会失效？
			strbuilder.delete(0, strbuilder.length());
			String mkdirCommand = strbuilder.append(
				"/data/local/tmp/busybox mkdir ")
					.append(backupPath)
					.toString();
			commandUtil = new ScriptUtil(mkdirCommand, scriptHandler);
			Log.i("mkdirCommand", mkdirCommand);
			commandUtil.execCommand();
		}
		// 如以上的路径不存在则直接使用/sdcard路径
		if (!backupFilePath.exists()) {
			backupPath = new String("/sdcard/rootgeniusBackup");
			backupFilePath = new File(backupPath);
			if (!backupFilePath.exists()) {
				// backupFilePath.mkdirs();
				// 有时候上面的方法会失效？
				strbuilder.delete(0, strbuilder.length());
				String mkdirSdcard = strbuilder.append(
					"/data/local/tmp/busybox mkdir ")
						.append(backupPath)
						.toString();
				Log.i("mkdirCommand", mkdirSdcard);
				commandUtil = new ScriptUtil(mkdirSdcard, scriptHandler);
				commandUtil.execCommand();
			}
		}
		Log.i("test", "backupPath: " + backupFilePath.toString());
//		mBackupFilePath = backupFilePath.toString();
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
