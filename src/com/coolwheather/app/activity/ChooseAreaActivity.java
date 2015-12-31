package com.coolwheather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolwheather.app.R;
import com.coolwheather.app.db.CoolWheatherDB;
import com.coolwheather.app.model.City;
import com.coolwheather.app.model.County;
import com.coolwheather.app.model.Province;
import com.coolwheather.app.util.HttpCallbackListener;
import com.coolwheather.app.util.HttpUtil;
import com.coolwheather.app.util.Utility;

public class ChooseAreaActivity extends Activity {

	private static final int LEVEL_PROVINCE = 0;
	private static final int LEVEL_CITY = 1;
	private static final int LEVEL_COUNTY = 2;
	private TextView textView;
	private ListView listView;
	private ProgressDialog progressDialog;
	private ArrayAdapter<String> adapter;
	private List<String> dataList = new ArrayList<String>();
	private CoolWheatherDB coolWheatherDB;
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	/**
	 * 选中的城市
	 */
	private City selectedCity;
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	/**
	 * 市列表
	 */
	private List<City> cityList;
	/**
	 * 县列表
	 */
	private List<County> countyList;
	/**
	 * 是否从WheatherActivity中跳转过来
	 */
	private boolean isFromWheatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWheatherActivity = getIntent().getBooleanExtra("from_wheather_activity", false);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		// 已经选择了城市且不是从WeatherActivity跳转过来,才会直接跳转到WheatherActivity
		if(sp.getBoolean("city_selected", false) && !isFromWheatherActivity){
			Intent intent = new Intent(this,WheatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);

		textView = (TextView) findViewById(R.id.title_text);
		listView = (ListView) findViewById(R.id.list_view);
		coolWheatherDB = CoolWheatherDB.getInstance(this);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int index,
					long arg3) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCity();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounty();
				} else if(currentLevel == LEVEL_COUNTY){
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent();
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvince();
	}

	/**
	 * 查询全国所有的省，优先从数据库中查询，如果没有再从服务器上查询
	 */
	private void queryProvince() {
		// TODO Auto-generated method stub
		provinceList = coolWheatherDB.loadProvince();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province p : provinceList) {
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			textView.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/**
	 * 查询选中省内所有的市，优先从数据库查询，如果没有查询到则再从服务器上查询
	 */
	private void queryCity() {
		cityList = coolWheatherDB.loadCity(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City c : cityList) {
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			textView.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	/**
	 * 查询选中城市内所有的县，优先从数据库中查询，如果没有则从服务器上查询
	 */
	private void queryCounty() {
		countyList = coolWheatherDB.loadCounty(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(selectedCity.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			textView.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}

	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if (type.equals("province")) {
					result = Utility.handleProvicneResponse(coolWheatherDB,
							response);
				} else if (type.equals("city")) {
					result = Utility.handleCityResponse(coolWheatherDB,
							response, selectedProvince.getId());
				} else if (type.equals("county")) {
					result = Utility.handleCountyResponse(coolWheatherDB,
							response, selectedCity.getId());
				}
				if (result) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							closeProgressDialog();
							if (type.equals("province")) {
								queryProvince();
							} else if (type.equals("city")) {
								queryCity();
							} else if (type.equals("county")) {
								queryCounty();
							}
						}
					});
				}
			}
			@Override
			public void onError(Exception e) {
				// 通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	/**
	 *显示进度对话框
	 */
	private void showProgressDialog() {
		// TODO Auto-generated method stub
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * 隐藏进度对话框
	 */
	private void closeProgressDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	/**
	 * 捕获back按键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if(currentLevel == LEVEL_COUNTY){
			queryCity();
		}else if(currentLevel == LEVEL_CITY){
			queryProvince();
		}else{
			if(isFromWheatherActivity){
				Intent intent = new Intent(this,WheatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
