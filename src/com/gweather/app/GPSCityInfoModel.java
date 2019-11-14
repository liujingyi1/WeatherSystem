package com.gweather.app;

import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class GPSCityInfoModel {
	private static final String TAG = "Gweather.GPSCityInfoModel";
	
	private static final String URI_GCITY = "content://com.gweather.app.weather/gcity";
	
	private static final int GPS_CITY_COUNT_MAX = 32;
	
	private Context mContext;
	
	public GPSCityInfoModel(Context context) {
		mContext = context;
	}
	
	public void loadGpsCityInfos(List<CityInfo> gpsCityInfos) {
		Log.d(TAG, "loadGpsCityInfos");
		gpsCityInfos.clear();
		
		ContentResolver mContentResolver = mContext.getContentResolver();
		Uri uri = Uri.parse(URI_GCITY);
		
		CityInfo info;
		Cursor cursor = mContentResolver.query(uri, null, null, null, null);
		if (cursor != null) {
			Log.d(TAG, "count:" + cursor.getCount());
			while(cursor.moveToNext()) {
				info = new CityInfo();
				int cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_NAME);
				info.setName(cursor.getString(cursorIndex));
				cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_WOEID);
				info.setWoeid(cursor.getString(cursorIndex));
				cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_LAT);
				info.getLocationInfo().setLat(cursor.getString(cursorIndex));
				cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_LON);
				info.getLocationInfo().setLon(cursor.getString(cursorIndex));
				cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_SWLAT);
				info.getLocationInfo().setSouthWestLat(cursor.getString(cursorIndex));
				cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_SWLON);
				info.getLocationInfo().setSouthWestLon(cursor.getString(cursorIndex));
				cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_NELAT);
				info.getLocationInfo().setNorthEastLat(cursor.getString(cursorIndex));
				cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_NELON);
				info.getLocationInfo().setNorthEastLon(cursor.getString(cursorIndex));
				gpsCityInfos.add(info);
			}
			cursor.close();
		}
	}
	
	public void saveGpsCityInfoToDB(CityInfo mCityInfo) {
		String woeid = "";
		String firstWoeid = "";
		boolean hasInfo = false;
		
		int count = 0;
		
		ContentResolver mContentResolver = mContext.getContentResolver();
		Uri uri = Uri.parse(URI_GCITY);
		Cursor cursor = mContentResolver.query(uri, null, 
				WeatherProvider.CITY_WOEID+"=?", new String[] {mCityInfo.getWoeid()}, null);
		
		if (cursor != null) {
			
			if (cursor.moveToNext()) {
				int cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_WOEID);
				woeid = cursor.getString(cursorIndex);
				hasInfo = true;
			}
			cursor.close();
		}
		
		cursor = mContentResolver.query(uri, null, null, null, null);
		if (cursor != null) {
			count = cursor.getCount();
			Log.d(TAG, "hasInfo=" + hasInfo);
			Log.d(TAG, "GpsCity List size:" + count);
			if (!hasInfo && count >= GPS_CITY_COUNT_MAX) {
				//delete the first info
				if (cursor.moveToFirst()) {
					int cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_WOEID);
					firstWoeid = cursor.getString(cursorIndex);
				}
			}
			cursor.close();
		}
		
		if (!firstWoeid.isEmpty()) {
			Log.d(TAG, "detelte:" + firstWoeid);
			
			mContentResolver.delete(uri, WeatherProvider.CITY_WOEID+"=?", new String[] {firstWoeid});
		}
		
		ContentValues values;
		values = new ContentValues();
		values.put(WeatherProvider.CITY_WOEID, mCityInfo.getWoeid());
		values.put(WeatherProvider.CITY_NAME, mCityInfo.getName());
		values.put(WeatherProvider.CITY_LAT, mCityInfo.getLocationInfo().getLat());
		values.put(WeatherProvider.CITY_LON, mCityInfo.getLocationInfo().getLon());
		values.put(WeatherProvider.CITY_SWLAT, mCityInfo.getLocationInfo().getSouthWestLat());
		values.put(WeatherProvider.CITY_SWLON, mCityInfo.getLocationInfo().getSouthWestLon());
		values.put(WeatherProvider.CITY_NELAT, mCityInfo.getLocationInfo().getNorthEastLat());
		values.put(WeatherProvider.CITY_NELON, mCityInfo.getLocationInfo().getNorthEastLon());
		if (hasInfo) {
			mContentResolver.update(
					uri,
					values,
					WeatherProvider.WOEID+"=?",
					new String[] {woeid});
		} else {
			mContentResolver.insert(uri, values);
		}
	}

}
