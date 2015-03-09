package com.shuame.sysapp.manager;

import java.io.DataOutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SysAppManager{
	public static Boolean RootCommand(String command){
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.flush();
			process.waitFor();	
		} catch (Exception e) {
			Log.d("**DEBUG**", "ROOT REE" + e.getMessage());
			return false;
		}finally{
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		Log.d("**DEBUG**", "ROOT SUC");
		return true;	
	}
}
