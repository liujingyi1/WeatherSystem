package com.gweather.app;

import java.util.ArrayList;
import java.util.List;

import com.gweather.utils.NotificationUtil;
import com.gweather.app.WeatherInfo.Forecast;
import com.gweather.utils.WeatherDataUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

public class WeatherModel {
	private static final String TAG = "Gweather.Model";
	private static final String URI_GWEATHER = "content://com.gweather.app.weather/gweather";
	
	interface OnCityInfoUpdatedListener {
		void updated();
	}
	
	
	private OnCityInfoUpdatedListener mOnCityInfoUpdatedListener;
	public void setOnCityInfoUpdatedListener(OnCityInfoUpdatedListener listener) {
		mOnCityInfoUpdatedListener = listener;
	}
	
	private static WeatherModel INSTANCE;
	
	private InternetWorker mInternetWorker;
	
	private Context mApp;
	private boolean inited = false;
	
	private List<WeatherInfo> mWeatherInfoList = new ArrayList<WeatherInfo>();
	private List<CityInfo> mCityInfos = new ArrayList<CityInfo>();
	
	private NotificationUtil notificationUtil;
	
	private InternetWorker.CallBacks mInternetCallBacks = new InternetWorker.CallBacks() {
		
		@Override
		public void queryCityFinished() {
			if (mOnCityInfoUpdatedListener != null) {
				mOnCityInfoUpdatedListener.updated();
			}
		}

		@Override
		public void queryAddWeatherFinished(WeatherInfo weatherInfo) {
			if (weatherInfo.getForecasts().size() < MainActivity.FORECAST_DAY) {
				Log.w(TAG, "Query NEW weather failed");
			} else {
				if(addWeatherInfoToDb(weatherInfo)) {
					mWeatherInfoList.add(weatherInfo);
				}
			}
			
			if(mWeatherInfoList.size() == 1){
				refreshNotification(weatherInfo);
			}
			
			Intent intent = new Intent(WeatherAction.ACTION_ADD_WEATHER_FINISH);
			mApp.sendBroadcast(intent);
			setAutoRefreshAlarm(mApp);
		}

		@Override
		public void queryLocationFinished() {
			
		}

		@Override
		public void queryAddGpsWeatherFinished(WeatherInfo weatherInfo) {
			if (weatherInfo.getForecasts().size() < MainActivity.FORECAST_DAY) {
				Log.w(TAG, "Query NEW Gps weather failed");
			} else {
				if(addWeatherInfoToDb(weatherInfo)) {
					mWeatherInfoList.add(weatherInfo);
				}
			}
			
			if(mWeatherInfoList.size() == 1){
				refreshNotification(weatherInfo);
			}
			
			Intent intent = new Intent(WeatherAction.ACTION_QUERT_GPS_WEATHER_FINISH);
			mApp.sendBroadcast(intent);
			setAutoRefreshAlarm(mApp);
		}

		@Override
		public void refreshAllWeatherFinished() {
			String defaultWoeid = WeatherDataUtil.getInstance().getDefaultCityWoeid(mApp);
			WeatherInfo defInfo = null;
			for(WeatherInfo info:mWeatherInfoList) {
				updateWeatherInfo(info);
				if (!info.isGps()
						&& defaultWoeid.equals(info.getWoeid()) 
						&& !defaultWoeid.equals(WeatherDataUtil.DEFAULT_WOEID_GPS)) {
					defInfo = info;
				}else if(info.isGps() && defaultWoeid
						.equals(WeatherDataUtil.DEFAULT_WOEID_GPS)){
					defInfo = info;
				}
			}
			
			if(notifyIsExist()){
				refreshNotification(defInfo);
			}
			
			WeatherDataUtil.getInstance().setRefreshTime(mApp, System.currentTimeMillis());
			Intent intent = new Intent(WeatherAction.ACTION_WEATHER_REFRESHED_ALL);
			mApp.sendBroadcast(intent);
			setAutoRefreshAlarm(mApp);
			//add shiyang start
			Intent tent = new Intent(WeatherAction.ACTION_REFRESH_COMPLETE);
            mApp.sendBroadcast(tent);
			//add shiyang end
		}

		@Override
		public void refreshWeatherFinished(WeatherInfo weatherInfo) {
			updateWeatherInfo(weatherInfo);
			String defaultWoeid = WeatherDataUtil.getInstance().getDefaultCityWoeid(
					mApp);
			if(notifyIsExist()){
				if (!weatherInfo.isGps()
						&& defaultWoeid.equals(weatherInfo.getWoeid()) 
						&& !defaultWoeid.equals(WeatherDataUtil.DEFAULT_WOEID_GPS)) {
					refreshNotification(weatherInfo);
				}else if(weatherInfo.isGps() && defaultWoeid
						.equals(WeatherDataUtil.DEFAULT_WOEID_GPS)){
					refreshNotification(weatherInfo);
				}
			}
			Intent intent = new Intent(WeatherAction.ACTION_WEATHER_REFRESHED);
			mApp.sendBroadcast(intent);
			setAutoRefreshAlarm(mApp);
		}
	};
	
	public void sendNotification(WeatherInfo weatherInfo){
		notificationUtil.showNotification(weatherInfo);
	}
	
	public void removeNotification(){
		notificationUtil.cancelNotification();
	}
	
	public void refreshNotification(WeatherInfo weatherInfo){
		notificationUtil.updateNotification(weatherInfo);
	}
	public void setDefaultNotification(){
		notificationUtil.setDefaultViews();
	}
	
	public boolean notifyIsExist(){
		SharedPreferences sp = mApp.getSharedPreferences(
				MainActivity.SETTINGS_SP, Context.MODE_PRIVATE);
		boolean isExist = sp.getBoolean(
				MainActivity.SETTINGS_NOTIFICATION,
				mApp.getResources().getBoolean(
						R.bool.config_notification));
		return isExist;
	}
	
	private void setAutoRefreshAlarm(Context context) {

		SharedPreferences sp = context.getSharedPreferences(
				MainActivity.SETTINGS_SP, Context.MODE_PRIVATE);
		boolean isAutoRefreshEnable = sp.getBoolean(
				MainActivity.SETTINGS_AUTO_REFRESH_ENABLE,
				context.getResources().getBoolean(
						R.bool.config_auto_refresh_enable));
		Log.d(TAG, "UpdateWidgetService - setAutoRefreshAlarm, "
				+ isAutoRefreshEnable);

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(WeatherAction.ACTION_AUTO_REFRESH);
		PendingIntent operation = PendingIntent.getBroadcast(context, 0,
				intent, 0);

		int time;
		if (isAutoRefreshEnable) {
			time = sp.getInt(MainActivity.SETTINGS_AUTO_REFRESH, context
					.getResources().getInteger(R.integer.config_auto_refresh));
			Log.d(TAG, "setAutoRefreshAlarm, " + time);
			long deltaTime = WeatherDataUtil.getRefreshDelta(
					mApp, time);
			Log.d(TAG, "setAutoRefreshAlarm, " + deltaTime);
			alarmManager.cancel(operation);
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis() + deltaTime, operation);
		} else {
			alarmManager.cancel(operation);
		}
	}
	
	public static WeatherModel  getInstance(Context app) {
		if(null == INSTANCE) {
			INSTANCE = new WeatherModel(app);
		}
		
		return INSTANCE;
	}
	
	private WeatherModel(Context app) {
		mApp = app;
		notificationUtil = new NotificationUtil(mApp);
		mInternetWorker = InternetWorker.getInstance(mApp);
		mInternetWorker.setCallBacks(mInternetCallBacks);
	}
	
	public boolean isInited() {
		return inited;
	}
	
	public void init() {
		Log.d(TAG, "init");
		loadWeatherInfos();
		mInternetWorker.init();
		inited = true;
	}
	
	public List<WeatherInfo> getWeatherInfos() {
		if(!isInited() || mWeatherInfoList.isEmpty()) {
			Log.w(TAG, "getWeatherInfos, load again");
			loadWeatherInfos();
		}
		return mWeatherInfoList;
	}
	
	private void loadWeatherInfos() {
		Log.d(TAG, "loadWeatherInfos");
		WeatherInfo.Forecast forecast = null;
		mWeatherInfoList.clear();
		
		ContentResolver mContentResolver = mApp.getContentResolver();
		Uri uri = Uri.parse(URI_GWEATHER);
		Cursor cursor = mContentResolver.query(uri, null, "gIndex=?",
				new String[] { Integer
				.toString(WeatherProvider.CONDITION_INDEX) }, null);
		
		
		WeatherInfo info;
		String woeid = "";
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int cursorIndex = cursor.getColumnIndex(WeatherProvider.WOEID);
				woeid = cursor.getString(cursorIndex);
				info = new WeatherInfo();
				info.setWoeid(woeid);
				cursorIndex = cursor.getColumnIndex(WeatherProvider.GPS);
				info.setGps(WeatherProvider.FLAG_GPS == cursor
						.getInt(cursorIndex));
				mWeatherInfoList.add(info);
			}
			cursor.close();
		}
		
		if (mWeatherInfoList.size() == 0) {
			// No infos
			Log.w(TAG, "loadWeatherInfos, no Infos");
			return;
		}
		
		
		for (WeatherInfo mInfo : mWeatherInfoList) {
			if (mInfo.isGps()) {
				cursor = mContentResolver
						.query(uri, null, WeatherProvider.GPS + "=?",
								new String[] { String
										.valueOf(WeatherProvider.FLAG_GPS) },
								WeatherProvider.INDEX);
			} else {
				cursor = mContentResolver.query(
						uri,
						null,
						"woeid=? AND " + WeatherProvider.GPS + "!=?",
						new String[] { mInfo.getWoeid(),
								String.valueOf(WeatherProvider.FLAG_GPS) },
						WeatherProvider.INDEX);
			}
			if (cursor != null) {
				while (cursor.moveToNext()) {
					int cursorIndex = cursor
							.getColumnIndex(WeatherProvider.INDEX);
					int index = cursor.getInt(cursorIndex);
					Log.d(TAG, "loadWeatherInfos, index=" + index);

					if (WeatherProvider.CONDITION_INDEX == index) {// CONDITION
						mInfo.getCondition().setIndex(index);

						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.NAME);
						mInfo.setName(cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.CODE);
						mInfo.getCondition().setCode(
								cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.DATE);
						mInfo.getCondition().setDate(
								cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.TEMP);
						mInfo.getCondition().setTemp(
								cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.TEXT);
						mInfo.getCondition().setText(
								cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.GPS);
						mInfo.setGps(WeatherProvider.FLAG_GPS == cursor
								.getInt(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.UPDATE_TIME);
						mInfo.setUpdateTime(cursor.getLong(cursorIndex));
					} else {// forecast
						forecast = mInfo.new Forecast();

						forecast.setIndex(index);
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.CODE);
						forecast.setCode(cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.DATE);
						forecast.setDate(cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.TEXT);
						forecast.setText(cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.DAY);
						forecast.setDay(cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.HIGH);
						forecast.setHigh(cursor.getString(cursorIndex));
						cursorIndex = cursor
								.getColumnIndex(WeatherProvider.LOW);
						forecast.setLow(cursor.getString(cursorIndex));

						mInfo.getForecasts().add(forecast);
					}
				}
				cursor.close();
			}
		}
	}
	
	
	
	
	/**
	 * New Code For WeatherInfo
	 */
	public boolean addWeatherInfoToDb(WeatherInfo newInfo) {
		boolean isNew = true;
		
		for(WeatherInfo info:mWeatherInfoList) {
			if (newInfo.isGps() && info.isGps()) {
				//GPS info exist
				isNew = false;
				info.copyInfo(newInfo);
				updateWeatherInfo(info);
				return isNew;
			} else if (!newInfo.isGps() && !info.isGps()
					&& (newInfo.getWoeid().equals(info.getWoeid()))) {
				//Normal info exist
				isNew = false;
				info.copyInfo(newInfo);
				updateWeatherInfo(info);
				return isNew;
			}
		}
		
		//Add to DB
		ContentResolver mContentResolver = mApp.getContentResolver();
		Uri uri = Uri.parse(URI_GWEATHER);
		ContentValues values = new ContentValues();
		values.put(WeatherProvider.INDEX,
				WeatherProvider.CONDITION_INDEX);
		values.put(WeatherProvider.WOEID, newInfo.getWoeid());
		values.put(WeatherProvider.NAME, newInfo.getName());
		values.put(WeatherProvider.CODE, newInfo.getCondition().getCode());
		values.put(WeatherProvider.DATE, newInfo.getCondition().getDate());
		values.put(WeatherProvider.TEMP, newInfo.getCondition().getTemp());
		values.put(WeatherProvider.TEXT, newInfo.getCondition().getText());
		values.put(WeatherProvider.UPDATE_TIME, newInfo.getUpdateTime());
		values.put(WeatherProvider.GPS, newInfo.isGps()?WeatherProvider.FLAG_GPS:0);
		mContentResolver.insert(uri, values);
		
		for (int i = 0; i < MainActivity.FORECAST_DAY; i++) {
			values = new ContentValues();
			values.put(WeatherProvider.INDEX, i);
			values.put(WeatherProvider.WOEID, newInfo.getWoeid());
			values.put(WeatherProvider.CODE, newInfo.getForecasts()
					.get(i).getCode());
			values.put(WeatherProvider.DATE, newInfo.getForecasts()
					.get(i).getDate());
			values.put(WeatherProvider.DAY, newInfo.getForecasts().get(i)
					.getDay());
			values.put(WeatherProvider.HIGH, newInfo.getForecasts()
					.get(i).getHigh());
			values.put(WeatherProvider.LOW, newInfo.getForecasts().get(i)
					.getLow());
			values.put(WeatherProvider.TEXT, newInfo.getForecasts()
					.get(i).getText());
			values.put(WeatherProvider.GPS, newInfo.isGps()?WeatherProvider.FLAG_GPS:0);
			mContentResolver.insert(uri, values);
		}
		
		return isNew;
	}
	
	public void updateWeatherInfo(WeatherInfo info) {
		ContentResolver mContentResolver = mApp.getContentResolver();
		Uri uri = Uri.parse(URI_GWEATHER);
		ContentValues values = new ContentValues();
		values.put(WeatherProvider.INDEX,
				WeatherProvider.CONDITION_INDEX);
		values.put(WeatherProvider.WOEID, info.getWoeid());
		values.put(WeatherProvider.NAME, info.getName());
		values.put(WeatherProvider.CODE, info.getCondition().getCode());
		values.put(WeatherProvider.DATE, info.getCondition().getDate());
		values.put(WeatherProvider.TEMP, info.getCondition().getTemp());
		values.put(WeatherProvider.TEXT, info.getCondition().getText());
		values.put(WeatherProvider.UPDATE_TIME, info.getUpdateTime());
		values.put(WeatherProvider.GPS, info.isGps()?WeatherProvider.FLAG_GPS:0);
		if (info.isGps()) {
			mContentResolver.update(
					uri,
					values,
					WeatherProvider.INDEX + " = ? AND "
							+ WeatherProvider.GPS + " = ?",
					new String[] { Integer.toString(WeatherProvider.CONDITION_INDEX),
							String.valueOf(info.isGps()?WeatherProvider.FLAG_GPS:0)});
		} else {
			mContentResolver.update(
					uri,
					values,
					WeatherProvider.INDEX + " = ? AND "
							+ WeatherProvider.GPS + " = ? AND "
							+ WeatherProvider.WOEID + " = ?",
					new String[] { Integer.toString(WeatherProvider.CONDITION_INDEX),
							String.valueOf(info.isGps()?WeatherProvider.FLAG_GPS:0),
							info.getWoeid() });
		}
		
		for (int i = 0; i < MainActivity.FORECAST_DAY; i++) {
			values = new ContentValues();
			values.put(WeatherProvider.INDEX, i);
			values.put(WeatherProvider.WOEID, info.getWoeid());
			values.put(WeatherProvider.CODE, info.getForecasts()
					.get(i).getCode());
			values.put(WeatherProvider.DATE, info.getForecasts()
					.get(i).getDate());
			values.put(WeatherProvider.DAY, info.getForecasts().get(i)
					.getDay());
			values.put(WeatherProvider.HIGH, info.getForecasts()
					.get(i).getHigh());
			values.put(WeatherProvider.LOW, info.getForecasts().get(i)
					.getLow());
			values.put(WeatherProvider.TEXT, info.getForecasts()
					.get(i).getText());
			values.put(WeatherProvider.GPS, info.isGps()?WeatherProvider.FLAG_GPS:0);
			if (info.isGps()) {
				mContentResolver.update(
						uri,
						values,
						WeatherProvider.INDEX + " = ? AND "
								+ WeatherProvider.GPS + " = ?",
						new String[] { Integer.toString(i),
								String.valueOf(info.isGps()?WeatherProvider.FLAG_GPS:0)});
			} else {
				mContentResolver.update(
						uri,
						values,
						WeatherProvider.INDEX + " = ? AND "
								+ WeatherProvider.GPS + " = ? AND "
								+ WeatherProvider.WOEID + " = ?",
						new String[] { Integer.toString(i),
								String.valueOf(info.isGps()?WeatherProvider.FLAG_GPS:0),
								info.getWoeid() });
			}
		}
	}
	
	public void deleteWeatherInfo(WeatherInfo info) {
		ContentResolver mContentResolver = mApp.getContentResolver();
		Uri uri = Uri.parse(URI_GWEATHER);
		mContentResolver.delete(
				uri,
				WeatherProvider.WOEID + "=? AND " + WeatherProvider.GPS
						+ "=?",
				new String[] { info.getWoeid(),
						String.valueOf(info.isGps()?WeatherProvider.FLAG_GPS:0) });
		
		mWeatherInfoList.remove(info);
	}
	
	public boolean refreshAllWeather() {
		return mInternetWorker.updateWeather();
	}
	
	public boolean refreshWeather(WeatherInfo weatherInfo) {
		return mInternetWorker.updateWeather(weatherInfo);
	}
	
	public String getFirstWeatherFromDB() {
		Log.d(TAG, "getFirstWeatherFromDB");
		String woeid=null;
		ContentResolver mContentResolver = mApp.getContentResolver();

		Uri weatherUri = Uri.parse(URI_GWEATHER);
		Cursor cursor = mContentResolver.query(weatherUri, null,
				WeatherProvider.INDEX+"=?", new String[] { Integer
						.toString(WeatherProvider.CONDITION_INDEX) },
				null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int cursorIndex = cursor
						.getColumnIndex(WeatherProvider.GPS);

				boolean isGps = (cursor.getInt(cursorIndex) == WeatherProvider.FLAG_GPS);
				if (isGps) {
					woeid = WeatherDataUtil.DEFAULT_WOEID_GPS;
				} else {
					cursorIndex = cursor
							.getColumnIndex(WeatherProvider.WOEID);
					woeid =	cursor.getString(cursorIndex);
				}
			}
			cursor.close();
		}
		
		return woeid;
	}
	
	
	
	/**
	 * New code for add City
	 */
	public List<CityInfo> getCityInfos() {
		return mCityInfos;
	}
	public boolean getCityInfosByNameFromInternet(String cityName) {
		mCityInfos.clear();
		return mInternetWorker.queryCity(cityName, mCityInfos);
	}

	public void stopQueryCity() {
		mInternetWorker.stopQueryCity();
	}
	
	public boolean addWeatherByCity(CityInfo info, boolean isGps) {
		WeatherInfo weatherInfo = new WeatherInfo();
		weatherInfo.setWoeid(info.getWoeid());
		weatherInfo.setName(info.getName());
		weatherInfo.setGps(isGps);
		return mInternetWorker.queryWeather(weatherInfo);
	}
	
	public boolean queryLocation(Location location, ArrayList<CityInfo> mCityInfos) {
		return mInternetWorker.queryLocation(location, mCityInfos);
	}
	
	public void addDefaultData() {
		WeatherInfo info = new WeatherInfo();
		info.setWoeid(mApp.getResources().getString(R.string.default_woeid));
		info.setName(mApp.getResources().getString(R.string.default_city_name));
		info.setGps(false);
		info.setUpdateTime(WeatherDataUtil.getInstance().getRefreshTime(mApp));
		info.getCondition().setCode("32");
		info.getCondition().setDate("Fri, 1 Jan 2016 11:00 AM PKT");
		info.getCondition().setIndex(WeatherProvider.CONDITION_INDEX);
		info.getCondition().setTemp("22");
		info.getCondition().setText("Sunny");
		
		info.getForecasts().clear();
		for (int i = 0; i < MainActivity.FORECAST_DAY; i++) {
			WeatherInfo.Forecast forecast = info.new Forecast();
			forecast.setCode("32");
			switch (i) {
			case 0:
				forecast.setDate("1 Jan 2016");
				forecast.setDay("Fri");
				break;
			case 1:
				forecast.setDate("2 Jan 2016");
				forecast.setDay("Sat");
				break;
			case 2:
				forecast.setDate("3 Jan 2016");
				forecast.setDay("Sun");
				break;
			case 3:
				forecast.setDate("4 Jan 2016");
				forecast.setDay("Mon");
				break;
			case 4:
				forecast.setDate("5 Jan 2016");
				forecast.setDay("Tue");
				break;
			}
			
			forecast.setHigh("26");
			forecast.setIndex(i);
			forecast.setLow("20");
			forecast.setText("Sunny");
			info.getForecasts().add(forecast);
		}
		
		
		ContentResolver mContentResolver = mApp.getContentResolver();
		Uri uri = Uri.parse(URI_GWEATHER);
		ContentValues values = new ContentValues();
		values.put(WeatherProvider.INDEX,
				WeatherProvider.CONDITION_INDEX);
		values.put(WeatherProvider.WOEID, info.getWoeid());
		values.put(WeatherProvider.NAME, info.getName());
		values.put(WeatherProvider.CODE, info.getCondition().getCode());
		values.put(WeatherProvider.DATE, info.getCondition().getDate());
		values.put(WeatherProvider.TEMP, info.getCondition().getTemp());
		values.put(WeatherProvider.TEXT, info.getCondition().getText());
		values.put(WeatherProvider.UPDATE_TIME, info.getUpdateTime());
		values.put(WeatherProvider.GPS, info.isGps()?WeatherProvider.FLAG_GPS:0);
		mContentResolver.insert(uri, values);
		
		for (int i = 0; i < MainActivity.FORECAST_DAY; i++) {
			values = new ContentValues();
			values.put(WeatherProvider.INDEX, i);
			values.put(WeatherProvider.WOEID, info.getWoeid());
			values.put(WeatherProvider.CODE, info.getForecasts()
					.get(i).getCode());
			values.put(WeatherProvider.DATE, info.getForecasts()
					.get(i).getDate());
			values.put(WeatherProvider.DAY, info.getForecasts().get(i)
					.getDay());
			values.put(WeatherProvider.HIGH, info.getForecasts()
					.get(i).getHigh());
			values.put(WeatherProvider.LOW, info.getForecasts().get(i)
					.getLow());
			values.put(WeatherProvider.TEXT, info.getForecasts()
					.get(i).getText());
			values.put(WeatherProvider.GPS, info.isGps()?WeatherProvider.FLAG_GPS:0);
			mContentResolver.insert(uri, values);
		}
		
		mWeatherInfoList.add(info);
		WeatherDataUtil.getInstance().updateDefaultCityWoeid(mApp, info.getWoeid());
		
		WeatherDataUtil.getInstance().setDefaultState(mApp, WeatherDataUtil.DEFAULT_STATE_NEED_UPDATE);
	}
}
