package com.example.appmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.appmanager.AutoBootAppInfo.ReceiverInfo;
import com.shuame.sysapp.manager.sysAppListActivity;

public class MainActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnAppManager = (Button) findViewById(R.id.btnAppManager);
		btnAppManager.setOnClickListener(listener);
	}

	private Button btnAppManager;

	private OnClickListener listener = new OnClickListener() {
		public void onClick(View v) {
			// 以下是自启动管理模块demo
			// Intent i = new Intent(MainActivity.this,AppListActivity.class);
			// startActivity(i);
			// testXmlPullParser();
			// 以下是系统软件管理模块
			Intent i = new Intent(MainActivity.this, sysAppListActivity.class);
			startActivity(i);
		}
	};

	private void testXmlPullParser() {
		try {
			PackageManager pm = getPackageManager();

			final String ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";
			XmlResourceParser xrp = getResources().getXml(R.xml.test);
			AutoBootAppInfo appInfo = new AutoBootAppInfo();
			appInfo.receivers = new ArrayList<ReceiverInfo>();
			String tagName = null;
			String className = null;
			String actionName = null;
			ReceiverInfo receiverInfo = null;
			int eventType = xrp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					Log.i("test", "START_DOCUMENT");
					break;
				case XmlPullParser.START_TAG:
					tagName = xrp.getName();
					Log.i("test", "START_TAG:" + tagName);
					if ("manifest".equals(tagName)) {
						appInfo.packageName = xrp.getAttributeValue(
							null, "package");
						try {
							ApplicationInfo applicationInfo = pm.getApplicationInfo(
								appInfo.packageName, 0);
							appInfo.appIcon = pm.getApplicationIcon(applicationInfo);
							appInfo.appName = pm.getApplicationLabel(
								applicationInfo).toString();
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}
					} else if ("receiver".equals(tagName)) {
						receiverInfo = new ReceiverInfo();
						className = xrp.getAttributeValue(
							ANDROID_NAMESPACE, "name");
						Log.i("test", "START_TAG:" + tagName + ";className:"
								+ className);
						receiverInfo.packageName = appInfo.packageName;
						receiverInfo.className = className;
						receiverInfo.fullName = receiverInfo.packageName + "/"
								+ receiverInfo.className;
						receiverInfo.actions = new HashSet<String>();
					} else if ("action".equals(tagName)) {
						if (receiverInfo != null) {
							actionName = xrp.getAttributeValue(
								ANDROID_NAMESPACE, "name");
							Log.i("test", "START_TAG:" + tagName
									+ ";actionName:" + actionName);
							if (Intent.ACTION_BOOT_COMPLETED.equals(actionName)) {
								appInfo.bootType |= AutoBootAppInfo.TYPE_BOOT_COMPLETED; // 开机自启动
							} else {
								appInfo.bootType |= AutoBootAppInfo.TYPE_BOOT_BACKGROUND; // 后台自启动
							}
							receiverInfo.actions.add(actionName);
						}
					}
					break;
				case XmlPullParser.END_TAG:
					tagName = xrp.getName();
					Log.i("test", "END_TAG:" + tagName);
					if ("receiver".equals(tagName)) {
						appInfo.receivers.add(receiverInfo);
						receiverInfo = null;
					}
					break;
				}

				eventType = xrp.next();
			}

			// debug
			Log.i("test", "packageName:" + appInfo.packageName + ";appName:"
					+ appInfo.appName + ";appIcon:" + appInfo.appIcon);
			for (ReceiverInfo receiver : appInfo.receivers) {
				Log.i("test", "receiver className:" + receiver.className);
				for (String action : receiver.actions) {
					Log.i("test", "receiver action:" + action);
				}
			}

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
