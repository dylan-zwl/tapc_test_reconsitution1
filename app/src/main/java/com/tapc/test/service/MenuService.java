package com.tapc.test.service;

import com.tapc.test.R;
import com.tapc.test.widget.MenuBar;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class MenuService extends Service {
	private WindowManager mWindowManager;
	private LayoutParams mMenuBarParams;
	private MenuBar mMenuBar;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate();
	}

	public void onStart(Intent intent, int startId) {
		mWindowManager = (WindowManager) getSystemService("window");
		mMenuBarParams = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT, (int) getResources()
						.getDimension(R.dimen.menuBar),
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
						| WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
						| WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
				PixelFormat.TRANSPARENT);
		mMenuBarParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		mMenuBarParams.x = 0;
		mMenuBarParams.y = 0;
		mMenuBar = new MenuBar(this);
		mWindowManager.addView(mMenuBar, mMenuBarParams);
	}

	public void onDestroy() {
		super.onDestroy();
		mWindowManager.removeView(mMenuBar);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
