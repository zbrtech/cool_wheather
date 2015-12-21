package com.coolwheather.app.util;

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
}
