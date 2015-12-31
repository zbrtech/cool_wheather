package com.coolwheather.app.util;

import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.coolwheather.app.db.CoolWheatherDB;
import com.coolwheather.app.model.City;
import com.coolwheather.app.model.County;
import com.coolwheather.app.model.Province;

public class Utility {
	/**
	 * 解析和处理服务器返回的省级数据
	 * 
	 * @param coolWheatherDB
	 * @param response
	 * @return
	 */
	public synchronized static boolean handleProvicneResponse(
			CoolWheatherDB coolWheatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvince = response.split(",");
			if (allProvince != null && allProvince.length > 0) {
				for (String p : allProvince) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceName(array[0]);
					province.setProvinceName(array[1]);
					coolWheatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的市级数据
	 */
	public synchronized static boolean handleCityResponse(
			CoolWheatherDB coolWheatherDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCity = response.split(",");
			if (allCity != null && allCity.length > 0) {
				for (String c : allCity) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					coolWheatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 解析和处理服务器返回的县级数据
	 */
	public synchronized static boolean handleCountyResponse(
			CoolWheatherDB coolWheatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounty = response.split(",");
			if (allCounty != null && allCounty.length > 0) {
				for (String c : allCounty) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[0]);
					county.setCityId(cityId);
					coolWheatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * 解析服务器返回的json数据，并将解析的数据存储到本地
	 */
	public static void handleWheatherResponse(Context context,String response){
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject wheatherInfo = jsonObject.getJSONObject("wheatherinfo");
			String cityName = wheatherInfo.getString("city");
			String cityCode = wheatherInfo.getString("cityid");
			String temp1  =wheatherInfo.getString("temp1");
			String temp2 = wheatherInfo.getString("temp2");
			String wheatherDesp  =wheatherInfo.getString("weather");
			String publishTime = wheatherInfo.getString("ptime");
			saveWheatherInfo(context,cityName,cityCode,temp1,temp2,wheatherDesp,publishTime);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 将服务器返回的所有天气信息存储到SharedPreferences文件中
	 * @param context
	 * @param cityName
	 * @param cityCode
	 * @param temp1
	 * @param temp2
	 * @param wheatherDesp
	 * @param publishTime
	 */
	private static void saveWheatherInfo(Context context, String cityName,
			String wheatherCode, String temp1, String temp2, String wheatherDesp,
			String publishTime) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf  = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("wheather_code", wheatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("wheather_desp", wheatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}
}
