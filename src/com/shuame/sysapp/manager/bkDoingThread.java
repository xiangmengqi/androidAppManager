package com.shuame.sysapp.manager;

import java.util.List;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.shuame.sysapp.manager.MyAdapter.Item;

public class bkDoingThread extends Thread {
	private Map<Integer, List<Item>> mitemList;
	private Handler mhandler;
	private SQLiteDatabase mappInfoDb;
	public bkDoingThread(Map<Integer, List<Item>> itemList, Handler handler,
		   SQLiteDatabase appInfoDb) {
		mitemList = itemList;
		mhandler = handler;
		mappInfoDb = appInfoDb;
	}

	@Override
	public void run() {
		super.run();
		// 遍历可以卸载应用列表
		for (Item listItem : mitemList.get(0)) {
			if (listItem.bl == true) {
				// 删除system/app下appName.apk文件，并备份/data/data/packageName文件夹，并记录到数据库中
				
			}
		}
		// 遍历可以还原应用列表
		for (Item listItem : mitemList.get(1)) {

		}
	}
	
	
}
