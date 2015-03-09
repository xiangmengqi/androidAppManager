package com.example.appmanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySqlHelper extends SQLiteOpenHelper {
	private static final String dbName = "appInfo.db";
	private static final int version = 1; //数据库版本
	
	
	public MySqlHelper(Context context) {
		super(context, dbName, null, version);
	}

	public void onCreate(SQLiteDatabase db) {
        //创建数据库sql语句  
        String sql = "create table user(data String,packageName String)";  
        //执行创建数据库操作  
        db.execSQL(sql);  		
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
