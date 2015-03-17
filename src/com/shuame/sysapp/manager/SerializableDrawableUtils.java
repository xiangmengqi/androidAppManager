/*
 * (#)SerializableDrawableUtils.java 1.0 2015年3月2日 2015年3月2日 GMT+08:00
 */
package com.shuame.sysapp.manager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

/**
 * @author jackXiang
 */

public class SerializableDrawableUtils {
	// 序列化方法
	public static String serializeString(Drawable db) {
		if (db != null) {
			byte[] data = serializeBytes(db);
			if (data != null) {
				try {
					return new String(data, "ISO-8859-1"); // NOTE：这里必须使用单字节编码，保证以原始bytes保存
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static byte[] serializeBytes(Drawable db) {
		BitmapDrawable mBitmapDrawable;
		if (db != null) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			mBitmapDrawable = (BitmapDrawable) db;
			mBitmapDrawable.getBitmap().compress(CompressFormat.PNG, 0, bos);
			return bos.toByteArray();
		}
		return null;
	}

	/**
	 * 反序列化
	 * 
	 * @param data
	 * @return
	 */
	public static <T> T unserializeBytes(byte[] data) {
		if (data != null) {
			try {
				ByteArrayInputStream bis = new ByteArrayInputStream(data);
				return (T) new BitmapDrawable(BitmapFactory.decodeStream(bis));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 反序列化
	 * 
	 * @param data
	 * @return
	 */
	public static <T> T unserializeString(String string) {
		if (!TextUtils.isEmpty(string)) {
			try {
				return unserializeBytes(string.getBytes("ISO-8859-1")); // NOTE：这里必须使用单字节编码，保证以原始bytes恢复
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
