package com.example.androidrank;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.umeng.ad.cloud.UmengCloud;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.i("TAG", "model:" + Build.MODEL);
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Log.i("TAG", "imei:" + telephonyManager.getDeviceId());
		Log.i("TAG", "brand:" + Build.BRAND);
		Log.i("TAG", "model:" + Build.MODEL);
		Log.i("TAG", "manufacture:" + Build.MANUFACTURER);

		UmengCloud.setLogEnable(true);
		UmengCloud.init(this, "2d4c5104","360软件管家");
		UmengCloud.start(this, "2d4c5104","360软件管家");
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			UmengCloud.postData(this, "2d4c5104","360软件管家");
		}
		return super.onKeyDown(keyCode, event);
	}
}
