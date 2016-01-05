package com.coolwheather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolwheather.app.R;
import com.coolwheather.app.service.AutoUpdateService;
import com.coolwheather.app.util.HttpCallbackListener;
import com.coolwheather.app.util.HttpUtil;
import com.coolwheather.app.util.Utility;

public class WheatherActivity extends Activity implements OnClickListener {

	private LinearLayout wheatherInfo;
	/**
	 * 显示城市名
	 */
	private TextView cityName;
	/**
	 * 用于显示发布时间
	 */
	private TextView publishText;
	/**
	 * 用于显示天气描述信息
	 */
	private TextView weatherDespText;
	/**
	 * 用于显示气温1
	 */
	private TextView temp1Text;
	/**
	 * 用于显示气温2
	 */
	private TextView temp2Text;
	/**
	 * 用于显示当前日期
	 */
	private TextView currentDateText;
	/**
	 * 切换城市按钮
	 */
	private Button switchCity;
	/**
	 * 更新天气按钮
	 */
	private Button refreshWeather;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.wheather_layout);

		wheatherInfo = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityName = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_wheather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);

		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代号时就去查询天气
			publishText.setText("同步中...");
			wheatherInfo.setVisibility(View.INVISIBLE);
			cityName.setVisibility(View.INVISIBLE);
			queryWheatherCode(countyCode);
		} else {
			// 没有县级代号时就直接显示本地天气
			showWheather();
		}
	}

	/**
	 * 查询县级代号所对应的天气代号
	 */
	private void queryWheatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	/**
	 * 查询天气代号所对应的天气
	 */
	private void queryWheatherInfo(String wheatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ wheatherCode + ".html";
		queryFromServer(address, "wheatherCode");
	}

	/**
	 * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
	 */
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if (type.equals("countyCode")) {
					if (!TextUtils.isEmpty(response)) {
						// 从服务器返回的数据中解析出天气代号
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String wheatherCode = array[1];
							queryWheatherInfo(wheatherCode);
						}
					}
				} else if (type.equals("wheatherCode")) {
					// 处理服务器返回的天气信息
					Utility.handleWheatherResponse(WheatherActivity.this,
							response);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWheather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						publishText.setText("同步失败...");
					}
				});
			}
		});
	}

	private void showWheather() {
		// TODO Auto-generated method stub
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityName.setText(sp.getString("city_name", ""));
		temp1Text.setText(sp.getString("temp1", ""));
		temp2Text.setText(sp.getString("temp2", ""));
		weatherDespText.setText(sp.getString("wheather_desp", ""));
		publishText.setText(sp.getString("publish_time", ""));
		currentDateText.setText(sp.getString("current_date", ""));
		wheatherInfo.setVisibility(View.VISIBLE);
		cityName.setVisibility(View.VISIBLE);
		
		Intent intent = new Intent(this,AutoUpdateService.class);
		startService(intent);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_wheather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_wheather:
			publishText.setText("同步中...");
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			String wheatherCode = sp.getString("wheather_code", "");
			if(!TextUtils.isEmpty(wheatherCode)){
				queryWheatherInfo(wheatherCode);
			}
			break;
		default:
			break;
		}
	}

}
