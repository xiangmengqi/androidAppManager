package com.shuame.sysapp.manager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.shuame.sysapp.manager.R;

public class MainActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_appmanager_main);
		btnAppManager = (Button) findViewById(R.id.btnAppManager);
		btnAppManager.setOnClickListener(listener);
	}

	private Button btnAppManager;

	private OnClickListener listener = new OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent(MainActivity.this, SysAppListActivity.class);
			startActivity(i);
		}
	};
}
