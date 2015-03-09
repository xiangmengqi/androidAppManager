package com.example.appmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;

//扫描需要得到的数据
public class MAPackageInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int BOOT_COMPLETED_FLAG = 1;
	public static final int BOOT_COMPLETED_DISABLE_FLAG = 0;
	public String packageName;
	public String appName;
//	public integer versionCode;
//	public Drawable appIcon;
//	public int startupType;
//	public boolean enabled;
	public int flag;
	public List<String> classnames = new ArrayList<String>();
//	public ArrayList<ReceiverInfo> receiverInfos;
//	
//	public class ReceiverInfo {
//		public String className;
//		public List<String> actionList;
//	}
}