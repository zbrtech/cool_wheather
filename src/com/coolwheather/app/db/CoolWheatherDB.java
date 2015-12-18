package com.coolwheather.app.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.coolwheather.app.model.City;
import com.coolwheather.app.model.County;
import com.coolwheather.app.model.Province;

public class CoolWheatherDB {
	/**
	 * 数据库名称
	 */
	private static String DB_NAME = "cool_wheater";
	/**
	 * 数据库版本号
	 */
	private static final int VERSION = 1;

	private static CoolWheatherDB coolWheatherDB;
	private SQLiteDatabase db;

	/**
	 * 构造方法私有化
	 */
	public CoolWheatherDB(Context context) {
		CoolWheatherOpenHelper dbHelper = new CoolWheatherOpenHelper(context,
				DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 获取CoolWheatherDB的实例
	 */
	public synchronized static CoolWheatherDB getInstance(Context context) {
		if (coolWheatherDB == null) {
			coolWheatherDB = new CoolWheatherDB(context);
		}
		return coolWheatherDB;
	}

	/**
	 * 将province实例存储到数据库
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("province_name", province.getProvinceName());
			contentValues.put("province_code", province.getProvinceCode());
			db.insert("Province", null, contentValues);
		}
	}

	/**
	 * 从数据库读取全国所有省份的信息
	 */
	public List<Province> loadProvince() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db
				.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor
						.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor
						.getColumnIndex("province_code")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		return list;
	}

	/**
	 * 将city实例存储到数据库
	 */
	public void saveCity(City city) {
		if (city != null) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("city_name", city.getCityName());
			contentValues.put("city_code", city.getCityCode());
			contentValues.put("province_id", city.getProvinceId());
			db.insert("City", null, contentValues);
		}
	}

	/**
	 * 从数据库中读取某省份下所有的城市信息
	 */
	public List<City> loadCity(int provinceId) {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id = ?",
				new String[] { String.valueOf(provinceId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor
						.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor
						.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			} while (cursor.moveToNext());
		}
		return null;
	}

	/**
	 * 将county实例存储到数据库
	 */
	public void saveCounty(County county) {
		if (county != null) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("county_name", county.getCountyName());
			contentValues.put("county_code", county.getCountyCode());
			contentValues.put("city_id", county.getCityId());
			db.insert("County", null, contentValues);
		}
	}

	/**
	 * 从数据库中读取某城市下所有的县城信息
	 */
	public List<County> loadCounty(int cityId) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id = ?",
				new String[] { String.valueOf(cityId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor
						.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor
						.getColumnIndex("county_code")));
				county.setCityId(cityId);
			} while (cursor.moveToNext());
		}
		return list;
	}
}
