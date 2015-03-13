/*
 * (#)DataSqlManager.java 1.0 2015年3月13日 2015年3月13日 GMT+08:00
 */
package com.shuame.sysapp.manager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.inputmethod.InputBinding;

/**
 * @author JackXiang
 * @version $1.0, 2015年3月13日 2015年3月13日 GMT+08:00
 * @since JDK5
 */
public class DataSqlManager {

	private static DataSqlManager dataManagerSingle = null;

	private SystemAppSqlHelper sqlHelper;

	private SQLiteDatabase db;

	// 可以恢复的系统应用列表
	List<AppInfo> uninstallAppList;

	// 系统应用列表
	List<AppInfo> sysAppList;

	// 单例化DataSqlManager
	private DataSqlManager(Context context) {
		sqlHelper = new SystemAppSqlHelper(context);
		db = sqlHelper.getReadableDatabase();
		uninstallAppList = new ArrayList<AppInfo>();
		sysAppList = new ArrayList<AppInfo>();
	};

	public SQLiteDatabase getSqlDataBase(Context context) {
		return db;
	}

	public long getCount() {
		Cursor cursor = db.rawQuery("select count(*) from user", null);
		cursor.moveToFirst();
		Long count = cursor.getLong(0);
		cursor.close();
		return count;
	}

	public Cursor getCursor() {
		return db.rawQuery("select * from user", null);
	}

	public void readFromDb(Cursor cursor, AppInfo sysappInfo) {
		sysappInfo.appName = cursor.getString(0);
		sysappInfo.packageName = cursor.getString(1);
		sysappInfo.apkBackupPath = cursor.getString(2);
		sysappInfo.sourcedir = cursor.getString(3);
		sysappInfo.appIcon = SerializableDrawableUtils.unserializeString(cursor.getString(4));
	}

	public synchronized static DataSqlManager getInstance(Context context) {
		if (dataManagerSingle == null) {
			dataManagerSingle = new DataSqlManager(context);
		}
		return dataManagerSingle;
	}

	// 每次打开app时候，同步内存和数据库中的数据
	public void sync(){
		if (getCount() >= 1) {
			// 读取数据库中的数据，并另起一行显示
			Cursor cursor = this.getCursor();
			cursor.moveToFirst();
			do {
				AppInfo sysappInfo = new AppInfo();
				Log.i("test5", "index 0:" + cursor.getString(0) + "");
				Log.i("test5", "index 1:" + cursor.getString(1) + "");
				this.readFromDb(cursor, sysappInfo);
				uninstallAppList.add(sysappInfo);
			} while (cursor.moveToNext());	
		}
	}

	public List<AppInfo> getSysAppList() {
		return sysAppList;
	}

	public List<AppInfo> getUninstallAppList() {
		return uninstallAppList;
	}

	public void dbInsertItem(AppInfo sysappInfo) {
		// 数据存入数据库
		String appIcon = SerializableDrawableUtils.serializeString(sysappInfo.appIcon);
		db.execSQL(
			"insert into user(appName,packageName,apkBackupPath,sourcedir,appIcon) values(?,?,?,?,?)",
			new Object[] { sysappInfo.appName, sysappInfo.packageName,
					sysappInfo.apkBackupPath, sysappInfo.sourcedir, appIcon });
		// 同步更新内存中数据
		sysAppList.remove(sysappInfo);
		uninstallAppList.add(sysappInfo);
	}

	public void dbDeleteItem(AppInfo sysappInfo) {
		// 移除数据库中数据
		db.execSQL(
			"delete from user where packageName=?",
			new Object[] { sysappInfo.packageName });
		// 同步更新内存中数据
		sysAppList.add(sysappInfo);
		uninstallAppList.remove(sysappInfo);
	}

	public class SystemAppSqlHelper extends SQLiteOpenHelper {

		private static final String dbName = "sysAppInfo.db";

		private static final int version = 1; // 数据库版本

		public SystemAppSqlHelper(Context context) {
			super(context, dbName, null, version);
		}

		public void onCreate(SQLiteDatabase db) {
			// 创建数据库sql语句
			// drawable类型的appIcon需要序列化后才能存入sqlite数据库
			String sql = "create table user(appName String,packageName String,apkBackupPath String,sourcedir String,appIcon String)";
			// 执行创建数据库操作
			db.execSQL(sql);
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}
}
