package com.example.appmanager;

import java.util.List;
import java.util.Set;

import android.R.drawable;
import android.graphics.drawable.Drawable;

/**
 * 
 * @author trentyang
 *
 */
public class AutoBootAppInfo {

    public static int TYPE_BOOT_COMPLETED = 1; // 开机自启动类型
    public static int TYPE_BOOT_BACKGROUND = 2; // 后台启动类型

    public String appName;
    public Drawable appIcon;
    public String packageName;
    public int bootType;
    public List<ReceiverInfo> receivers;
    
    public static class ReceiverInfo {
        public String fullName; // packageName/className
        public String packageName;
        public String className;
        public Set<String> actions;
    }

}
