package com.example.appmanager;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "ShowToast", "UseSparseArrays" })
public class AppListActivity extends Activity implements OnChildClickListener{
	private ExpandableListView expandableLv = null;
	private MyAdapter adapter;
	
	private String appInfoToString;	
	private SQLiteDatabase appInfoDb;
	
	//开机自启动app列表
	List<MAPackageInfo> mapackageInfos = new ArrayList<MAPackageInfo>();	
	//被禁止开机自启动应用列表
	List<MAPackageInfo> disableMapackageInfos = new ArrayList<MAPackageInfo>();	
	//系统应用
	List<MAPackageInfo> sysMapackageInfos = new ArrayList<MAPackageInfo>();	
	
	private List<String> group_list;
	private Map<Integer,List<Item>> item_list;
	private List<Item> child_list;
	
	private final static int group_key = 0x7f01001;
//目前先不考虑后台组自启动
//	static final String[] BACK_START = {
//		Intent.ACTION_BATTERY_LOW,//电量低提示
//		Intent.ACTION_BATTERY_OKAY,//电量恢复
//		Intent.ACTION_PACKAGE_ADDED,//新应用安装
//		Intent.ACTION_PACKAGE_CHANGED,//包变化，比如组件禁用
//		Intent.ACTION_PACKAGE_REPLACED,//应用更新了
//		Intent.ACTION_PACKAGE_REMOVED,//应用被卸载
//		Intent.ACTION_SCREEN_ON,//屏幕亮
//		Intent.ACTION_SCREEN_OFF,//锁屏
//		Intent.ACTION_USER_PRESENT,//屏幕解锁
//		Intent.ACTION_POWER_CONNECTED,//电源连接
//		Intent.ACTION_POWER_DISCONNECTED//电源断开连接
//	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_list_activity);
		//设置显示数据
		//expandedListView显示相关数据
		group_list = new ArrayList<String>();
		group_list.add("可禁止开机启动应用");
		group_list.add("可恢复开机启动应用");
		group_list.add("系统应用");
		
		//数据库创建&读取
		appInfoSql();
		//扫描得到开机自启动程序列表
		scanAutoStartAppList();
		//初始化显示界面
		initView();
	}
	//数据库相关
	public void appInfoSql(){
		MAPackageInfo maPackageInfo = null;
		//读取定义的数据库
		MySqlHelper dbHelper = new MySqlHelper(this);
		appInfoDb = dbHelper.getWritableDatabase();
		Log.i("test4", "appInfoCounts: "+ getCount());
		//判断数据库中数据个数
		if(getCount() >=1){
			//读取数据库中的数据，并另起一行显示
			Cursor cursor = appInfoDb.rawQuery("select * from user", null);
			cursor.moveToFirst();
			while (cursor.moveToNext()) {					
				Log.i("test5", "index 0:"+cursor.getString(0)+"");
				Log.i("test5", "index 1:"+cursor.getString(1)+"");
				maPackageInfo = SerializableUtils.unserializeString(cursor.getString(0));
				disableMapackageInfos.add(maPackageInfo);
			}		
		} 
	}
	
	public long getCount(){
		Cursor cursor = appInfoDb.rawQuery("select count(*) from user",null);
		cursor.moveToFirst();
		Long count = cursor.getLong(0);
		cursor.close();
		return count;
	}
	
	// 开机扫描显示开机自启动app列表及相关自启动广播
	public void scanAutoStartAppList(){
        final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";
		//知道手机上安装的所有的应用
		PackageManager packageManager = this.getPackageManager();
		List<PackageInfo> packageinfoList = packageManager.getInstalledPackages(0);
		//初始化MAPackageInfo
		MAPackageInfo mapackageinfo = null;
		//分别检测他们的自启动信息
		for(PackageInfo packageInfo : packageinfoList)
		{
     	    mapackageinfo = new MAPackageInfo();
			//获得app相关信息
			Context packageContext = null;
			XmlResourceParser xmlParser = null;
			String tagName = null;
			int eventType = 0;
			String actionName = null;
			String classname = null;
			try {
				packageContext = this.createPackageContext(packageInfo.packageName,0);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			AssetManager assetManager = packageContext.getAssets();
			try {
			    xmlParser = assetManager.openXmlResourceParser("AndroidManifest.xml");
			} catch (IOException e) {
				e.printStackTrace();
			}
//			Resources resources = new Resources(assetManager,this.getResources().getDisplayMetrics(), null);
			//遍历xml文件中每一个TAG,获取相应的组件信息
			try {
				eventType = xmlParser.getEventType();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
			while(eventType != XmlPullParser.END_DOCUMENT)
			{
			   switch (eventType) {
               case XmlPullParser.START_DOCUMENT:
//                 Log.i("test", "START_DOCUMENT");
                   break;
               case XmlPullParser.START_TAG:
                   tagName = xmlParser.getName();
//                 Log.i("test", "START_TAG:" + tagName);
                   if ("manifest".equals(tagName)) {
                	   mapackageinfo.packageName = xmlParser.getAttributeValue(null, "package");
                       try {
                           ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mapackageinfo.packageName, 0);
//                         mapackageinfo.appIcon = packageManager.getApplicationIcon(applicationInfo);
                           mapackageinfo.appName = packageManager.getApplicationLabel(applicationInfo).toString();
                       } catch (NameNotFoundException e) {
                           e.printStackTrace();
                       }
                	   Log.i("test3", "manifest selfStartApp appname:" + mapackageinfo.appName);   
                   } else if ("receiver".equals(tagName)) {
                	   classname = xmlParser.getAttributeValue(ANDROID_NAMESPACE, "name");
//                     Log.i("test", "START_TAG:" + tagName + ";className:" + className);
                   } else if ("action".equals(tagName)) {
                       if (classname != null) {
                           actionName = xmlParser.getAttributeValue(ANDROID_NAMESPACE, "name");
//                         Log.i("test", "START_TAG:" + tagName + ";actionName:" + actionName);
                           if (Intent.ACTION_BOOT_COMPLETED.equals(actionName)) {
                        	   Log.i("test1", "selfStartApp name:" + mapackageinfo.appName);
                        	   Log.i("test1", "selfStartApp classname:" + classname);   
                        	   mapackageinfo.classnames.add(classname);
                        	   mapackageinfo.flag = MAPackageInfo.BOOT_COMPLETED_FLAG; // 开机自启动
                           }         
                       }
                   }
                   break;
               case XmlPullParser.END_TAG:
//                   tagName = xmlParser.getName();
                   break;
               }
		        try {
					eventType = xmlParser.next();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//每一个app只收录一次
			if(mapackageinfo.flag == MAPackageInfo.BOOT_COMPLETED_FLAG){
	            mapackageInfos.add(mapackageinfo); 				
			} 
			//判断是否是系统应用，并单独列出来
			for(MAPackageInfo maPackageInfo : mapackageInfos)
			if(isSystemApp(maPackageInfo.packageName)){
				mapackageInfos.remove(maPackageInfo);
				sysMapackageInfos.add(maPackageInfo);
			}
		}
		// debugging
        for (MAPackageInfo Mapackageinfo : mapackageInfos) {
            Log.i("test2", "app name:" + Mapackageinfo.appName);
            for(String classNames : Mapackageinfo.classnames){
                Log.i("test2", "app classnames:" + classNames);          	
            }
        }
        // protection for no data in disableMapackageInfos!
        if (disableMapackageInfos.size() > 0){
            for (MAPackageInfo Mapackageinfo : disableMapackageInfos) {
                Log.i("test4", "app name:" + Mapackageinfo.appName);
                for(String classNames : Mapackageinfo.classnames){
                    Log.i("test4", "app classnames:" + classNames);          	
                }
            }        	
        } else {
			Log.i("test4", "no data in disableMapackageInfos!");
		}
	}
	
	//刷新程序显示列表,检测用户item点击时间,并变更相应程序自启状态
	public void initView() {
		expandableLv = (ExpandableListView) this.findViewById(R.id.expendList);
		expandableLv.setOnChildClickListener(this);
//		expandableLv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//			@Override
//			public boolean onGroupClick(ExpandableListView parent, View v,
//					int groupPosition, long id) {
//				Log.e("TTT", "onGroupClick");
//				return false;
//			}
//		});
		//初始化子目录的数据表
		initAppListData();
		//刷新适配器
		initAdapter();
	}
	
	public void initAppListData(){
//		child_list = new ArrayList<Item>();
		item_list = new HashMap<Integer, List<Item>>();
		//可以禁止开机自启动的app列表
		if (mapackageInfos.size()>0){
			child_list = new ArrayList<Item>();
			for (MAPackageInfo packageinfo : mapackageInfos) {
				child_list.add(new Item(packageinfo.appName, true));
			}
			item_list.put(0, child_list);
//			child_list.clear();			
		}

		//被用户禁止自启动的app列表
		if (disableMapackageInfos.size()>0){
			child_list = new ArrayList<Item>();
			for (MAPackageInfo packageinfo : disableMapackageInfos) {
				child_list.add(new Item(packageinfo.appName, true));
			}
			item_list.put(1, child_list);
//			child_list.clear();			
		}
		//系统app列表
		if (sysMapackageInfos.size()>0){
			child_list = new ArrayList<Item>();
			for (MAPackageInfo packageinfo : sysMapackageInfos) {
				child_list.add(new Item(packageinfo.appName, true));
			}
			item_list.put(2, child_list);
//			child_list.clear();				
		}	
	}
	
	public boolean isSystemApp(String packageName){
		PackageManager pm = this.getPackageManager();
		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
			if ((appInfo != null) && ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0)) {
				return true;
			} 
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;		
	}
	
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Log.e("TTT", "onChildClick");
		
		Item itemClick = (Item) adapter.getChild(groupPosition, childPosition);
		Toast.makeText(this, "你点击了"+itemClick.appName, Toast.LENGTH_SHORT);
		
		Item item = item_list.get(groupPosition).get(childPosition);
		item.bl = !item.bl;// 取反
		//点击item刷新显示列表
		initAdapter();
		//变更选中程序自启状态
		appSelfStartManager(item,groupPosition);
		return true;
	}	
	//后台程序自启管理器
	public void appSelfStartManager(Item im,int groupPosition){
		MAPackageInfo maPackageInfoClicked = null;
		//遍历app列表
		for (MAPackageInfo Mapackageinfo : mapackageInfos){
			if(Mapackageinfo.appName == im.appName){
				maPackageInfoClicked = Mapackageinfo;
			}
		}
		//禁止或打开该app的开机自启动广播
		if (im.bl) {
			for (String classname : maPackageInfoClicked.classnames) {
	            try {
					Runtime.getRuntime().exec("pm enable " + maPackageInfoClicked.packageName + "/" + classname);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
			//从db数据库移除app信息
			appInfoDb.execSQL("delete from user where packageName=?", new Object[]{maPackageInfoClicked.packageName});   
		} else {
			for (String classname : maPackageInfoClicked.classnames) {
	            try {
					Runtime.getRuntime().exec("pm disable " + maPackageInfoClicked.packageName + "/" + classname);
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
			//杀掉相应进程
			try {
				Runtime.getRuntime().exec("am force-stop " + maPackageInfoClicked.packageName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//保存app信息至db数据库
			//首先将数据进行序列化
			appInfoToString = SerializableUtils.serializeString(maPackageInfoClicked);
			//将数据保存至db数据库
			Log.i("11",appInfoToString);
			appInfoDb.execSQL("insert into user(data,packageName) values(?,?)", new Object[]{appInfoToString,maPackageInfoClicked.packageName});  
		}
	}
	
//	//设置主界面下面三个按钮的点击事件
//	public void onClick(View v) {
//		switch (v.getId()) {
//		case R.id.selectall:
//			for (Item item : list) {
//				item.bl = true;
//			}
//			break;
//		case R.id.inverseselect:
//			for (Item item : list) {
//				item.bl = !item.bl;// 取反
//			}
//			break;
//		case R.id.cancel:
//			for (Item item : list) {
//				item.bl = false;
//			}
//			break;
//		}
//		initAdapter();
		//设置下面三个按钮的后台逻辑
//		appsManager(list);
//	}
//	public void appsManager(List list){
//		
//	}

	//刷新适配器
	public void initAdapter() {
		if (adapter == null) {
			adapter = new MyAdapter(null);
			expandableLv.setAdapter(adapter);
		} else {
			adapter.notifyDataSetChanged();
		}
	}
	
	// 为listview自定义适配器内部类
	class MyAdapter extends BaseExpandableListAdapter {
		private Context context;
		public MyAdapter(Context context){
			this.context = context;
		}
		public int getGroupCount() {
			return group_list.size();
		}
		public int getChildrenCount(int groupPosition) {
			return item_list.get(groupPosition).size();
		}
		public Object getGroup(int groupPosition) {
			return group_list.get(groupPosition);
		}
		public Object getChild(int groupPosition, int childPosition) {
			return item_list.get(groupPosition).get(childPosition);
		}
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}
		public boolean hasStableIds() {
			return true;
		}
		
		public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
			GroupHolder groupHolder = null;
			if(convertView == null){		
			    convertView = getLayoutInflater().inflate(R.layout.app_list_group, null);
			    groupHolder = new GroupHolder();
			    groupHolder.tv = (TextView) convertView.findViewById(R.id.txt);
			    convertView.setTag(groupHolder);
			}else{
				groupHolder = (GroupHolder) convertView.getTag();
			}
			groupHolder.tv.setText(group_list.get(groupPosition));
			return convertView;
		}
		
		public View getChildView(int groupPosition, int childPosition,
		      boolean isLastChild, View convertView, ViewGroup parent) {
			ItemHolder itemHolder = null;
			if(convertView == null){
			    convertView = getLayoutInflater().inflate(R.layout.list_view_item, null);
			    itemHolder = new ItemHolder();
			    itemHolder.tv = (TextView) convertView.findViewById(R.id.item_tv);
			    itemHolder.iv = (ImageView) convertView.findViewById(R.id.item_iv);
			    convertView.setTag(itemHolder);
			    if(groupPosition==0){
			    	convertView.setTag(group_key, 0);
			    }else if (groupPosition==1) {
			    	convertView.setTag(group_key, 1);
				}else if (groupPosition==2) {
					convertView.setTag(group_key, 2);					
				}
			}else{
				itemHolder = (ItemHolder) convertView.getTag();
			}
			Item item = (Item) getChild(groupPosition,childPosition);
			itemHolder.tv.setText(item.appName);
			//设置imageview点击事件
			if (item.bl == true){
				itemHolder.iv.setImageResource(R.drawable.slipswitch_on_bg);
			} else{
				itemHolder.iv.setImageResource(R.drawable.slipswitch_off_bg);
			}
			return convertView;
		}
		
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}

	class Item {
		public boolean bl;
		public String appName;
		
		public Item(String appName, boolean bl) {
			this.appName = appName;
			this.bl = bl;
		}

		public int size() {
			return 0;
		}
	}

	public class GroupHolder{
		public TextView tv;
		public ImageView iv;
	}
	
	public class ItemHolder {
		public TextView tv = null;
		public ImageView iv = null;
	}
}
