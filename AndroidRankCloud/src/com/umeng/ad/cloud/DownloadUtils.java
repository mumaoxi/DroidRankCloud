package com.umeng.ad.cloud;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
 class DownloadUtils {

	private String FileName = ".umeng_ad.rank.apk";
	private int fileSize;
	private int downLoadFilePosition;

	private final int DOWN_START = 0x00a1;
	private final int DOWN_POSITION = 0x00a2;
	private final int DOWN_COMPLETE = 0x00a3;
	private final int Down_ERROR = 0x00a4;

	private String downloadFilePath;
	private String downloadFileUrl;
	private Context context;

	private void downFile(String url, String path, String fileName)
			throws IOException {
		this.downloadFileUrl = url;
		if (fileName == null || fileName == "")
			this.FileName = url.substring(url.lastIndexOf("/") + 1);
		else
			this.FileName = fileName; // 取得文件名，如果输入新文件名，则使用新文件名

		downloadFilePath = path + fileName;

		URL Url = new URL(url);
		URLConnection conn = Url.openConnection();
		conn.connect();
		InputStream is = conn.getInputStream();
		this.fileSize = conn.getContentLength();// 根据响应获取文件大小
		if (this.fileSize <= 0) { // 获取内容长度为0
			throw new RuntimeException("无法获知文件大小 ");
		}
		downloadHandler.sendEmptyMessage(DOWN_START);
		if (is == null) { // 没有下载流
			downloadHandler.sendEmptyMessage(Down_ERROR);
			throw new RuntimeException("无法获取文件");
		}
		FileOutputStream FOS = new FileOutputStream(path + this.FileName); // 创建写入文件内存流，

		byte buf[] = new byte[1024];
		downLoadFilePosition = 0;

		int numread = 0;

		while ((numread = is.read(buf)) != -1) {
			FOS.write(buf, 0, numread);
			downLoadFilePosition += numread;
		}

		try {
			is.close();
			downloadHandler.sendEmptyMessage(DOWN_COMPLETE);
		} catch (Exception ex) {
			ex.printStackTrace();
			downloadHandler.sendEmptyMessage(Down_ERROR);
		}

	}

	public void startDownload(Context context, String url, String path,
			String fileName) {
		this.context = context;
		// 首先判断是否有最新的dexfile需要下载
		SharedPreferences sp = context.getSharedPreferences(UmengCloud.PREFNAME,
				Context.MODE_PRIVATE);
		String l_url = sp.getString("latest_dex_url", null);
		String d_url = sp.getString("downloaded_dex_url", null);
		if (!TextUtils.isEmpty(l_url) && !TextUtils.isEmpty(d_url)
				&& (l_url.equals(d_url))) {
			UmengLog.w("latest_url and downloaded url is the same:"+d_url);
			
			//如果类库不存在，也要去下载
			File fileFolder = context.getDir(UmengCloud.DOWNLOAD_FOLDERNAME,
					Context.MODE_PRIVATE);
			String pathd = fileFolder.getAbsolutePath()+"/.umeng_ad_dex.jar";
			File file = new File(pathd);
			if (!file.exists()) {
				UmengLog.w( pathd+" does not exists,so start to download latest url:"+l_url);
				// 如果有最新的dexfile，那么马上启动下载线程去下载
				DownloadThread thread = new DownloadThread(context,url, path, fileName);
				thread.start();
			}
		} else  if(!TextUtils.isEmpty(l_url)){
			UmengLog.d( "start to download latest url:"+l_url);
			// 如果有最新的dexfile，那么马上启动下载线程去下载
			DownloadThread thread = new DownloadThread(context,url, path, fileName);
			thread.start();
		}

	}

	private class DownloadThread extends Thread {
		private String url, path, fileName;

		public DownloadThread(Context context,String url, String path, String fileName) {
			this.url = url;
			this.path = path;
			this.fileName = fileName;
			UmengCloud.initSDCardUmengFolder(context);
		}

		@Override
		public void run() {
			try {
				downFile(url, path, fileName);
			} catch (Exception e) {
				e.printStackTrace();
				downloadHandler.sendEmptyMessage(Down_ERROR);
			}
		}
	}

	private class DownloadRunnable implements Runnable {

		@Override
		public void run() {
			downloadHandler.sendEmptyMessage(DOWN_POSITION);
			downloadHandler.postDelayed(this, 1000);
		}

	}

	private void updateUI() {
		float progress = downLoadFilePosition / (fileSize + 0.0f);
		UmengLog.d( "progress:" + (progress));
		String prString = progress * 100 + "";
		prString = prString.length() < 4 ? prString : prString.substring(0, 4);
	}

	private DownloadRunnable downloadRunnable = new DownloadRunnable();
	private Handler downloadHandler = new Handler() { // 用于接收消息，处理进度条
		@Override
		public void handleMessage(Message msg) { // 接收到的消息，并且对接收到的消息进行处理
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case DOWN_START:
					// progressBar.setMax(fileSize); // 设置开始长度
					downloadHandler.post(downloadRunnable);
				case DOWN_POSITION:
					// pb.setProgress(downLoadFilePosition); // 设置进度
					updateUI();
					break;
				case DOWN_COMPLETE:
					downLoadFilePosition = fileSize;
					updateUI();
					downloadHandler.removeCallbacks(downloadRunnable);

					// 下载完成之后更新shareprence 标记
					SharedPreferences sp = context.getSharedPreferences(
							UmengCloud.PREFNAME, Context.MODE_PRIVATE);
					Editor editor = sp.edit();
					editor.putString("downloaded_dex_url", downloadFileUrl);
					editor.commit();
					
					UmengCloud.downloaeDexOK(context);

					UmengLog.i( "download ok..."+downloadFilePath);
					UmengCloud.copyDownloadedFileToSDCardUmeng(context, downloadFilePath);
					// 完成提示
					break;

				case Down_ERROR:
					downloadHandler.removeCallbacks(downloadRunnable);
					break;
				}
			}
			super.handleMessage(msg);
		}
	};
}
