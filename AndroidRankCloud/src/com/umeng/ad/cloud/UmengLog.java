package com.umeng.ad.cloud;

import android.util.Log;

class UmengLog {
	protected static boolean logEnable = false;

	protected static void i(Object object) {
		if (UmengLog.logEnable) {
			Log.i("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
	
	protected static void d(Object object) {
		if (UmengLog.logEnable) {
			Log.d("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
	
	protected static void e(Object object) {
		if (UmengLog.logEnable) {
			Log.e("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
	
	protected static void v(Object object) {
		if (UmengLog.logEnable) {
			Log.v("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
	
	protected static void w(Object object) {
		if (UmengLog.logEnable) {
			Log.w("MobclickAgent", object != null ? object.toString() : "null");
		}
	}
}
