package com.shuame.sysapp.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

//设置为单例模式
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
