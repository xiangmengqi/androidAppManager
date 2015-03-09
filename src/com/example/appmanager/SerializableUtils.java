package com.example.appmanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;

import android.text.TextUtils;

/**
 * 
 * @author trentyang
 *
 */
public class SerializableUtils {
    /**
     * 序列化
     * @param object
     * @return
     */
    public static byte[] serializeBytes(Object object) {
        if (object != null) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos);
                oos.writeObject(object);
                return bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 序列化
     * @param object
     * @return
     */
    public static String serializeString(Object object) {
        if (object != null) {
            byte[] data = serializeBytes(object);
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

    /**
     * 反序列化
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T unserializeBytes(byte[] data) {
        if (data != null) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bis);
                return (T) ois.readObject();
            } catch (StreamCorruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 反序列化
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
