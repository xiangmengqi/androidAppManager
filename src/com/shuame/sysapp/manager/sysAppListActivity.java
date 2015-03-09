package com.shuame.sysapp.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.example.appmanager.R;
import com.shuame.sysapp.manager.MyAdapter.Item;
import com.shuame.sysapp.manager.ScriptUtil.ScriptHandler;

public class sysAppListActivity extends Activity implements
		OnChildClickListener, OnClickListener {
	private ExpandableListView expandableLv = null;

	private MyAdapter adapter;

	private String appInfoToString;

	private SQLiteDatabase appInfoDb;

	private ScriptUtil commandUtil;

	private String backupFilePath;

	private Button mButton1, mButton2;

	// 可以恢复的系统应用列表
	List<sysAppInfo> uninstallAppList = new ArrayList<sysAppInfo>();

	// 系统应用列表
	List<sysAppInfo> sysAppList = new ArrayList<sysAppInfo>();

	// handler
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// 收到消息队列反馈则继续UI线程的操作
			if (msg.what == 0) {
				// 显示扫描结果
				initView();
			}
		};
	};

	// checkbox点击事件回调方法接口
	private ArrayList<String> group_list;

	private Map<Integer, ArrayList<Item>> item_list;

	private ArrayList<Item> child_list;

	private final static int group_key = 0x7f01001;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sysapp_list_activity);
		mButton1 = (Button) findViewById(R.id.btnConfirm);
		mButton2 = (Button) findViewById(R.id.btnRestore);
		mButton1.setOnClickListener(this);
		mButton2.setOnClickListener(this);

		// 设置显示数据
		// expandedListView显示相关数据
		group_list = new ArrayList<String>();
		group_list.add("可卸载系统应用");
		group_list.add("可恢复系统应用");

		// 设置数据备份路径 /sdcard/rootgenuisBackup
		// 如果目录已经存在则不做处理，否则新建目录
		backupFilePath = initBackupPath();

		// 数据库创建&读取
		appInfoSql();
		// 启动后台线程，扫描系统应用程序列表
		new loadAppsThread(this, sysAppList, uninstallAppList, handler).start();
	}

	// 卸载app备份文件夹初始化
	public String initBackupPath() {
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
			StringBuilder mkdirBuilder = new StringBuilder();
			commandUtil = new ScriptUtil(mkdirBuilder.append(
				"/data/local/tmp/busybox mkdir ")
					.append(backupPath)
					.toString(), scriptHandler);
			// Log.i("mkdir backupPath command", chmodCommand);
			commandUtil.execCommand();
		}
		Log.i("test", "backupPath: " + backupFilePath.toString());
		return backupFilePath.toString();
	}

	// 数据库相关
	public void appInfoSql() {
		// 读取定义的数据库
		SystemAppSqlHelper dbHelper = new SystemAppSqlHelper(this);
		appInfoDb = dbHelper.getWritableDatabase();
		Log.i("test4", "appInfoCounts: " + getCount());
		// 判断数据库中数据个数
		if (getCount() >= 1) {
			// 读取数据库中的数据，并另起一行显示
			Cursor cursor = appInfoDb.rawQuery("select * from user", null);
			cursor.moveToFirst();
			PackageManager pm = this.getPackageManager();
			do {
				sysAppInfo sysappInfo = new sysAppInfo();
				Log.i("test5", "index 0:" + cursor.getString(0) + "");
				Log.i("test5", "index 1:" + cursor.getString(1) + "");
				sysappInfo.appName = cursor.getString(0);
				sysappInfo.packageName = cursor.getString(1);
				sysappInfo.apkBackupPath = cursor.getString(2);
				sysappInfo.sourcedir = cursor.getString(3);
				sysappInfo.appIcon = SerializableDrawableUtils.unserializeString(cursor.getString(4));
				uninstallAppList.add(sysappInfo);
			} while (cursor.moveToNext());
		}
	}

	public long getCount() {
		Cursor cursor = appInfoDb.rawQuery("select count(*) from user", null);
		cursor.moveToFirst();
		Long count = cursor.getLong(0);
		cursor.close();
		return count;
	}

	// 刷新程序显示列表,检测用户item点击时间,并变更相应程序自启状态
	public void initView() {
		expandableLv = (ExpandableListView) this.findViewById(R.id.expendList1);
		expandableLv.setOnChildClickListener(this);
		this.findViewById(R.id.btnConfirm).setOnClickListener(this);
		// 初始化子目录的数据表
		initAppListData();
		// 刷新适配器
		initAdapter();
	}

	public void initAppListData() {
		item_list = new HashMap<Integer, ArrayList<Item>>();
		// 被用户卸载的自启动app列表
		if (uninstallAppList.size() > 0) {
			child_list = new ArrayList<Item>();
			for (sysAppInfo sysappInfo : uninstallAppList) {
				child_list.add(new Item(sysappInfo.appName, false,
					sysappInfo.appIcon, sysappInfo.packageName,
					sysappInfo.sourcedir, sysappInfo.apkBackupPath));
			}
			item_list.put(1, child_list);
		}

		// 可以被卸载的系统app列表
		if (sysAppList.size() > 0) {
			child_list = new ArrayList<Item>();
			for (sysAppInfo sysappInfo : sysAppList) {
				child_list.add(new Item(sysappInfo.appName, false,
					sysappInfo.appIcon, sysappInfo.packageName,
					sysappInfo.sourcedir, null));
			}
			item_list.put(0, child_list);
		}
	}

	ScriptHandler scriptHandler = new ScriptHandler() {
		@Override
		public void onSuccess(String strCorrect) {
		}

		@Override
		public void onFailed(String strError) {
		}
	};

	private List<Item> clicked0;

	private List<Item> clicked1;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnConfirm:
			clicked0 = new ArrayList<MyAdapter.Item>();
			for (Item listItem : item_list.get(0)) {
				if (listItem.bl == true) {
					Log.i("text", "packageName: " + listItem.packageName
							+ "sourceDir: " + listItem.sourcedir);
					// 为确保命令使用正常，需要使用shuame
					// busybox，推送busybox到/data/local/tmp/buxybox

					// 确保system分区正常挂载
					ensureSystemMounted();

					// 改变相关文件权限
					StringBuilder strbuilder1 = new StringBuilder();
					String chmodCommand = strbuilder1.append(
						"/data/local/tmp/busybox chmod 755 ")
							.append(listItem.sourcedir)
							.toString();
					commandUtil = new ScriptUtil(chmodCommand, scriptHandler);
					Log.i("chmod 755 xx.apk command", chmodCommand);
					commandUtil.execCommand();

					// 备份xx.apk文件到backupFilePath
					// 在backupFilePath目录下建立listItem.sourcedir子目录
					// 从sourcedir截取apk名
					String apkName = listItem.sourcedir.substring(
						listItem.sourcedir.lastIndexOf("/"),
						listItem.sourcedir.length());
					Log.i("apkName: ", apkName);

					strbuilder1.delete(0, strbuilder1.length());
					String apkBackupPath = strbuilder1.append(backupFilePath)
							.append(apkName)
							.toString();

					strbuilder1.delete(0, strbuilder1.length());
					String backupCommand = strbuilder1.append(
						"/data/local/tmp/busybox mv ")
							.append(listItem.sourcedir)
							.append(" ")
							.append(apkBackupPath)
							.toString();
					commandUtil = new ScriptUtil(backupCommand, scriptHandler);
					Log.i("mv xx.apk command", backupCommand);
					commandUtil.execCommand();

					// 备份packageName数据文件夹到制定目录
					strbuilder1.delete(0, strbuilder1.length());
					String packageNameFilePath = strbuilder1.append(
						"/data/data/")
							.append(listItem.packageName)
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

					// 记录已选中数据到数据库
					// 首先将数据进行序列化
					// 将数据保存至db数据库
					// 将appIcon数据序列化
					String appIcon = SerializableDrawableUtils.serializeString(listItem.dw);
					appInfoDb.execSQL(
						"insert into user(appName,packageName,apkBackupPath,sourcedir,appIcon) values(?,?,?,?,?)",
						new Object[] { listItem.appName, listItem.packageName,
								apkBackupPath, listItem.sourcedir, appIcon });
					// 将listItem中的数据bool值置为false
					listItem.bl = false;
					listItem.apkBackupPath = apkBackupPath;

					clicked0.add(listItem);
					if (uninstallAppList.size() > 0) {
						item_list.get(1).add(listItem);
					} else {
						// item_list map[1,xx]还不存在，则进行以下操作
						child_list = new ArrayList<Item>();
						child_list.add(listItem);
						item_list.put(1, child_list);
					}
				}
			}
			// 将选中项从列表删除
			for (Item item : clicked0) {
				item_list.get(0).remove(item);
			}
			break;
		case R.id.btnRestore:
			// 遍历可以还原应用列表，进行还原操作
			clicked1 = new ArrayList<MyAdapter.Item>();
			for (Item listItem : item_list.get(1)) {
				if (listItem.bl == true) {
					// 确保system分区正常挂载
					ensureSystemMounted();

					// 将数据文件夹mv到/data/data/目录
					StringBuilder strbuilder = new StringBuilder();
					String appPackageDataBackupPath = strbuilder.append(
						backupFilePath)
							.append("/")
							.append(listItem.packageName)
							.toString();
					strbuilder.delete(0, strbuilder.length());
					String sysAppRestoreCommand = strbuilder.append("mv -f ")
							.append(appPackageDataBackupPath)
							.append(" ")
							.append("/data/data/")
							.toString();
					commandUtil = new ScriptUtil(sysAppRestoreCommand,
						scriptHandler);
					Log.i("restore package command", sysAppRestoreCommand);
					commandUtil.execCommand();

					// 将xx.apk文件push到/system/app/目录
					strbuilder.delete(0, strbuilder.length());
					String appApkbackupCommand = strbuilder.append("mv ")
							.append(listItem.apkBackupPath)
							.append(" ")
							.append(listItem.sourcedir)
							.toString();
					commandUtil = new ScriptUtil(appApkbackupCommand,
						scriptHandler);
					Log.i("restore xx.apk command", appApkbackupCommand);
					commandUtil.execCommand();

					// 移除数据库中数据
					// 从db数据库移除app信息
					appInfoDb.execSQL(
						"delete from user where packageName=?",
						new Object[] { listItem.packageName });
					// 将listItem中的数据bool值置为false
					listItem.bl = false;
					clicked1.add(listItem);
					item_list.get(0).add(listItem);
				}
			}
			// 将选中项从列表删除
			for (Item item : clicked1) {
				item_list.get(1).remove(item);
			}
			break;
		default:
			break;
		}

		// 根据选中项调整列表,刷新适配器
		initAdapter();
	}

	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Log.i("test3", "onChildClick groupPosition:" + groupPosition
				+ ";childPosition:" + childPosition);
		adapter.updateItemChecked(v);
		return true;
	}

	private OnClickListener mOnCheckboxClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.i("test3", "mOnCheckboxClickListener");
			adapter.updateItemChecked(v);
		}
	};

	// 刷新适配器
	public void initAdapter() {
		if (adapter == null) {
			adapter = new MyAdapter(this, mOnCheckboxClickListener, group_list,
				item_list);
			expandableLv.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
	}

	// 确保system分区挂载上
	public void ensureSystemMounted() {
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
}
