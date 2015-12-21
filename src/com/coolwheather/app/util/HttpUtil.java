package com.coolwheather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

	public static void sendHttpRequest(final String address,
			final HttpCallbackListener listener) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("get");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);

					InputStream is = connection.getInputStream();
					BufferedReader br = new BufferedReader(
							new InputStreamReader(is));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					if (listener != null) {
						// 回调onFinish()方法
						listener.onFinish(sb.toString());

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					if (listener != null) {
						listener.onError(e);
					}
					e.printStackTrace();
				}
			}
		}).start();
	}

}
