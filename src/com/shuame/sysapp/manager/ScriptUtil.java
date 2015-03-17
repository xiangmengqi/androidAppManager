package com.shuame.sysapp.manager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Handler;
import android.util.Log;

public class ScriptUtil {
	public static final String REMOVE_APP_LIMIT = "chmod 771 /system/app \n";

	public static final String ADD_APP_LIMIT = "chmod 551 /system/app \n";

	public static final String TAG = "ScriptUtil";

	/**
	 * 输出信息接口的对象
	 */
	private ScriptHandler scriptHandler = null;

	/**
	 * Handler对象
	 */
	private Handler handler = null;

	/**
	 * 输入的命令
	 */
	private String strCommand = null;

	/**
	 * constructor
	 * 
	 * @param strCommand
	 *            所执行的命令
	 * @param scriptHandler
	 *            输出信息接口对象
	 */
	public ScriptUtil(String strCommand, ScriptHandler scriptHandler) {
		this.scriptHandler = scriptHandler;
		this.strCommand = strCommand;
	}

	public ScriptUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 关键方法:执行命令
	 */
	public void execCommand() {
		handler = new Handler();
		ShellCommandThread thread = new ShellCommandThread();
		thread.start();
	}

	/**
	 * 该线程进行io操作
	 */
	private class ShellCommandThread extends Thread {
		/**
		 * 进程对象
		 */
		private Process process = null;

		/**
		 * 正确信息缓冲流
		 */
		private BufferedReader brCorrect = null;

		/**
		 * 错误信息缓冲流
		 */
		private BufferedReader brError = null;

		private DataOutputStream dos = null;

		/**
		 * 0：成功 非0：失败
		 */
		int code = -1;

		@Override
		public void run() {
			try {
				process = Runtime.getRuntime().exec("su");

				dos = new DataOutputStream(process.getOutputStream());

				dos.writeBytes(strCommand + " ");

				brCorrect = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
				brError = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
				String strTemp = new String();

				dos.flush();

				dos.close();

				StringBuffer strTempCorrect = new StringBuffer();
				StringBuffer strTempError = new StringBuffer();
				while ((strTemp = brCorrect.readLine()) != null) {
					strTempCorrect = strTempCorrect.append(strTemp + "\n");
				}
				strTemp = new String();
				while ((strTemp = brError.readLine()) != null) {
					strTempError = strTempError.append(strTemp + "\n");
				}
				code = process.waitFor();
				Log.d(TAG, "code   :" + code);
				Log.d(TAG, "strTempCorrect " + strTempCorrect.toString());
				Log.e(TAG, "strTempError " + strTempError.toString());
				handler.post(new ScriptRunnable(code,
					strTempCorrect.toString(), strTempError.toString()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				try {
					if (brCorrect != null) {
						brCorrect.close();
						brCorrect = null;
					}
					if (brError != null) {
						brError.close();
						brError = null;
					}
					if (dos != null) {
						dos.close();
						dos = null;
					}
					if (process != null) {
						process = null;
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private class ScriptRunnable implements Runnable {
		private int resultCode;

		private String strCorrect;

		private String strError;

		/**
		 * constructor
		 * 
		 * @param resultCode
		 *            待定
		 * @param strCorrect
		 *            正确操作的信息输出
		 * @param strError
		 *            错误操作的信息输出
		 */
		public ScriptRunnable(int resultCode, String strCorrect, String strError) {
			this.resultCode = resultCode;
			this.strCorrect = strCorrect;
			this.strError = strError;
		}

		@Override
		public void run() {
			if ((strCorrect.equals("")) && (!strError.equals(""))
					&& (resultCode != 0)) {
				scriptHandler.onFailed(strError);
			} else {
				scriptHandler.onSuccess(strCorrect);
			}
		}
	}

	/**
	 * 操作正确及错误信息的接口
	 */
	public interface ScriptHandler {
		/**
		 * 
		 * @param succesMsg
		 *            正确操作的输出信息
		 */
		void onSuccess(String strCorrect);

		/**
		 * 
		 * @param failedMsg
		 *            错误操作的输出信息
		 */
		void onFailed(String strError);

		// void onTimeOut(String timeOutMsg);
	}
}
