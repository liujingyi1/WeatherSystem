package com.gweather.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.gweather.app.CityInfo;
import com.gweather.app.MainActivity;
import com.gweather.app.R;
import com.gweather.app.WeatherProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class WeatherDataUtil {
	private static final String TAG = "Gweather.WeatherDataUtil";
	
	public final static String WEATHER_SP = "gweather";
	
	public static final float INVALID_LOCATION = -1000;
	
	public static final long AUTO_REFRESH_TIME_1 = 5 * 60 * 60 * 1000l;
	public static final long AUTO_REFRESH_TIME_2 = 11 * 60 * 60 * 1000l;
	public static final long AUTO_REFRESH_TIME_3 = 17 * 60 * 60 * 1000l;
	public static final long AUTO_REFRESH_TIME_4 = 23 * 60 * 60 * 1000l;
	
	public static final long TIME_ONE_DAY = 24 * 60 * 60 * 1000l;
	
	public static final int DEFAULT_STATE_NEED_CHECK = 0;
	public static final int DEFAULT_STATE_NEED_UPDATE = 1;
	public static final int DEFAULT_STATE_FINISHED = 2;
	
	private static final boolean isDebugImg = false;
	
	public static final int INVALID_WEAHTER_RESOURCE = -1;
	public static final String DEFAULT_WOEID_GPS = "woeid_gps";
	
	public static final int CODE_THUNDERSTORMS = 4;
	public static final int CODE_RAIN_AND_SNOW = 5;
	public static final int CODE_SHOWERS = 11;
	public static final int CODE_RAIN = 12;
	public static final int CODE_SNOW_SHOWERS = 14;
	public static final int CODE_BREEZY = 23;//有微风
	public static final int CODE_WINDY = 24;//有风
	public static final int CODE_CLOUDY = 26;
	public static final int CODE_MOSTLY_CLOUDY = 28;
	public static final int CODE_PARTLY_CLOUDY = 29;
	public static final int CODE_PARTLY_CLOUDY_2 = 30;
	public static final int CODE_CLEAR = 31;
	public static final int CODE_SUNNY = 32;
	public static final int CODE_MOSTLY_CLEAR = 33;
	public static final int CODE_MOSTLY_SUNNY = 34;
	public static final int CODE_SCATTERED_SHOWERS = 39;
	//public static final int CODE_SCATTERED_SHOWERS = 45;//零星阵雨
	public static final int CODE_SCATTERED_THUNDERSTORMS = 47;//零星雷雨
	//add
	public static final int CODE_MOSTLY_CLOUDY_2 = 27;
	public static final int CODE_SNOW_SHOWERS_2 = 16;
	
	public static final int NETHER_NOTIFY_WIDGET = 0;
	public static final int IS_WIDGET = 1;
	public static final int IS_NOTIFICATION = 2;
	public static final int SMALL_DARK = 3;
	public static final int SMALL_WHITE = 4;
	
	public static final String TEXT_CLOUDY = "cloudy";
	public static final String TEXT_SUNNY = "sunny";
	public static final String TEXT_CLEAR = "clear";
	public static final String TEXT_SHOWERS = "showers";
	public static final String TEXT_THUNDERSTORMS = "thunderstorms";
	public static final String TEXT_RAIN = "rain";
	//下面都是自己猜的
	public static final String TEXT_FOG = "fog";
	public static final String TEXT_SNOW = "snow";
	public static final String TEXT_SLEET = "sleet";
	public static final String TEXT_SAND = "sand";
	
	private static WeatherDataUtil mWeatherDataUtil;
	private WeatherDataUtil(){}
	public static WeatherDataUtil getInstance() {
		if(mWeatherDataUtil == null) {
			mWeatherDataUtil = new WeatherDataUtil();
		}
		return mWeatherDataUtil;
	}
	
	public int getWeatherImageResourceByCode(int code, boolean isnight, int widgetOrNotify) {
		Log.d(TAG, "WeatherDataUtil - updateWeatherImageByCode:"+code);
		if (isDebugImg) {
			isnight = true;
			switch(widgetOrNotify){
			case IS_WIDGET:
				if (isnight) {
					return R.drawable.widget42_icon_sun_day_night;
				} else {
					return R.drawable.widget42_icon_sun_day;
				}
			case IS_NOTIFICATION:
				if (isnight) {
					return R.drawable.notification_night;
				} else {
					return R.drawable.notification_sunny;
				}
			case NETHER_NOTIFY_WIDGET:
				if (isnight) {
					return R.drawable.weather_icon_sun_day_night;
				} else {
					return R.drawable.weather_icon_sun_day;
				}
			case SMALL_DARK:
				if (isnight) {
					return R.drawable.icon_sun_day_night_dark;
				} else {
					return R.drawable.icon_sun_day_dark;
				}
			case SMALL_WHITE:
				if (isnight) {
					return R.drawable.icon_sun_day_night_white;
				} else {
					return R.drawable.icon_sun_day_white;
				}
			}
		}
		switch (code) {
		case CODE_CLOUDY:
		case CODE_MOSTLY_CLOUDY:
		case CODE_MOSTLY_CLOUDY_2:
		case CODE_PARTLY_CLOUDY:
		case CODE_PARTLY_CLOUDY_2:
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_cloudy_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_cloudy;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_cloudy_day;
			case SMALL_DARK:
				return R.drawable.icon_cloudy_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_cloudy_day_white;
			}
		case CODE_BREEZY:
		case CODE_WINDY:
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_windy_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_wind;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_windy_day;
			case SMALL_DARK:
				return R.drawable.icon_windy_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_windy_day_white;
			}
		case CODE_SUNNY:
		case CODE_MOSTLY_SUNNY:
		case CODE_CLEAR:
		case CODE_MOSTLY_CLEAR:
			switch(widgetOrNotify){
			case IS_WIDGET:
				if (isnight) {
					return R.drawable.widget42_icon_sun_day_night;
				} else {
					return R.drawable.widget42_icon_sun_day;
				}
			case IS_NOTIFICATION:
				if (isnight) {
					return R.drawable.notification_night;
				} else {
					return R.drawable.notification_sunny;
				}
			case NETHER_NOTIFY_WIDGET:
				if (isnight) {
					return R.drawable.weather_icon_sun_day_night;
				} else {
					return R.drawable.weather_icon_sun_day;
				}
			case SMALL_DARK:
				if (isnight) {
					return R.drawable.icon_sun_day_night_dark;
				} else {
					return R.drawable.icon_sun_day_dark;
				}
			case SMALL_WHITE:
				if (isnight) {
					return R.drawable.icon_sun_day_night_white;
				} else {
					return R.drawable.icon_sun_day_white;
				}
			}
		case CODE_SCATTERED_SHOWERS:
		case CODE_SHOWERS:
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_dayu_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_heavy_rain;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_dayu_day;
			case SMALL_DARK:
				return R.drawable.icon_dayu_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_dayu_day_white;
			}
		case CODE_SNOW_SHOWERS:
		case CODE_SNOW_SHOWERS_2:
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_daxue_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_snow;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_daxue_day;
			case SMALL_DARK:
				return R.drawable.icon_daxue_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_daxue_day_white;
			}
		case CODE_THUNDERSTORMS:
		case CODE_SCATTERED_THUNDERSTORMS:
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_leizhenyu_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_thunder_shower;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_leizhenyu_day;
			case SMALL_DARK:
				return R.drawable.icon_leizhenyu_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_leizhenyu_day_white;
			}
		case CODE_RAIN:
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_rain_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_rainy;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_rain_day;
			case SMALL_DARK:
				return R.drawable.icon_rain_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_rain_day_white;
			}
		case CODE_RAIN_AND_SNOW:
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_yujiaxue_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_sleet;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_yujiaxue_day;
			case SMALL_DARK:
				return R.drawable.icon_yujiaxue_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_yujiaxue_day_white;
			}
		default:
			return INVALID_WEAHTER_RESOURCE;
		}

	}
	
	
	public int getWeatherTextResByCode(int code) {
		Log.d(TAG, "WeatherDataUtil - updateWeatherImageByCode:"+code);
		if (isDebugImg) {

		}
		switch (code) {
		case CODE_CLOUDY:
		case CODE_MOSTLY_CLOUDY:
		case CODE_MOSTLY_CLOUDY_2:
		case CODE_PARTLY_CLOUDY:
		case CODE_PARTLY_CLOUDY_2:
			return R.string.weather_cloudy;
		case CODE_BREEZY:
			return R.string.weather_breezy;
		case CODE_WINDY:
			return R.string.weather_windy;
		case CODE_SUNNY:
		case CODE_MOSTLY_SUNNY:
			return R.string.weather_sunny;
		case CODE_CLEAR:
		case CODE_MOSTLY_CLEAR:
			return R.string.weather_clear;
		case CODE_SCATTERED_SHOWERS:
		case CODE_SHOWERS:
			return R.string.weather_showers;
		case CODE_SNOW_SHOWERS:
		case CODE_SNOW_SHOWERS_2:
			return R.string.weather_snow_showers;
		case CODE_THUNDERSTORMS:
		case CODE_SCATTERED_THUNDERSTORMS:
			return R.string.weather_thunderstorms;
		case CODE_RAIN:
			return R.string.weather_rain;
		case CODE_RAIN_AND_SNOW:
			return R.string.weather_rain_and_snow;
		default:
			return INVALID_WEAHTER_RESOURCE;
		}

	}
	
	public int getWeatherImageResourceByText(String text, boolean isnight, int widgetOrNotify) {
		Log.d(TAG, "WeatherDataUtil - updateWeatherImageByText:"+text);
		if (isCondition(TEXT_SUNNY, text) || isCondition(TEXT_CLEAR, text)) {
			switch(widgetOrNotify){
			case IS_WIDGET:
				if (isnight) {
					return R.drawable.widget42_icon_sun_day_night;
				} else {
					return R.drawable.widget42_icon_sun_day;
				}
			case IS_NOTIFICATION:
				if (isnight) {
					return R.drawable.notification_night;
				} else {
					return R.drawable.notification_sunny;
				}
			case NETHER_NOTIFY_WIDGET:
				if (isnight) {
					return R.drawable.weather_icon_sun_day_night;
				} else {
					return R.drawable.weather_icon_sun_day;
				}
			case SMALL_DARK:
				if (isnight) {
					return R.drawable.icon_sun_day_night_dark;
				} else {
					return R.drawable.icon_sun_day_dark;
				}
			case SMALL_WHITE:
				if (isnight) {
					return R.drawable.icon_sun_day_night_white;
				} else {
					return R.drawable.icon_sun_day_white;
				}
			}
		} else if (isCondition(TEXT_CLOUDY, text)) {
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_cloudy_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_cloudy;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_cloudy_day;
			case SMALL_DARK:
				return R.drawable.icon_cloudy_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_cloudy_day_white;
			}
		} else if (isCondition(TEXT_THUNDERSTORMS, text)) {
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_leizhenyu_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_thunder_shower;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_leizhenyu_day;
			case SMALL_DARK:
				return R.drawable.icon_leizhenyu_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_leizhenyu_day_white;
			}
		} else if (isCondition(TEXT_SHOWERS, text)) {
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_dayu_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_heavy_rain;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_dayu_day;
			case SMALL_DARK:
				return R.drawable.icon_dayu_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_dayu_day_white;
			}
		} else if (isCondition(TEXT_RAIN, text)) {
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_rain_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_rainy;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_rain_day;
			case SMALL_DARK:
				return R.drawable.weather_icon_rain_day;
			case SMALL_WHITE:
				return R.drawable.icon_rain_day_white;
			}
		} else if (isCondition(TEXT_SNOW, text)) {
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_xue_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_snow;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_xue_day;
			case SMALL_DARK:
				return R.drawable.icon_xue_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_xue_day_white;
			}
		} else if (isCondition(TEXT_FOG, text)) {
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_fog_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_fog;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_fog_day;
			case SMALL_DARK:
				return R.drawable.icon_fog_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_fog_day_white;
			}
		} else if (isCondition(TEXT_SLEET, text)) {
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_yujiaxue_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_sleet;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_yujiaxue_day;
			case SMALL_DARK:
				return R.drawable.icon_yujiaxue_day_dark;
			case SMALL_WHITE:
				return R.drawable.icon_yujiaxue_day_white;
			}
		} else if (isCondition(TEXT_SAND, text)) {
			switch(widgetOrNotify){
			case IS_WIDGET:
				return R.drawable.widget42_icon_sandstorm_day;
			case IS_NOTIFICATION:
				return R.drawable.notification_sandstorm;
			case NETHER_NOTIFY_WIDGET:
				return R.drawable.weather_icon_sandstorm_day;
			case SMALL_DARK:
				return R.drawable.icon_sandstorm_dark;
			case SMALL_WHITE:
				return R.drawable.icon_sandstorm_white;
			}
		}
		Log.w(TAG, "WeatherDataUtil - updateWeatherImageByText:["+text+"] No match!");
		switch(widgetOrNotify){
		case IS_WIDGET:
			return R.drawable.widget42_icon_nodata;
		case IS_NOTIFICATION:
			return R.drawable.notification_unknown;
		case NETHER_NOTIFY_WIDGET:
			return R.drawable.weather_icon_nodata;
		case SMALL_DARK:
			return R.drawable.icon_nodata_dark;
		case SMALL_WHITE:
			return R.drawable.icon_nodata_white;
		}
		return INVALID_WEAHTER_RESOURCE;
	}
	
	private boolean isCondition(String condition, String text) {
		text = text.toLowerCase();
		if (text.contains(condition)) {
			return true;
		}
		return false;
	}
	
	public boolean isNight() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
    	String hour = sdf.format(new Date());
    	int h = Integer.parseInt(hour);

    	if(h < 7 || h > 18) {
    		return true;
    	}
    	
    	return false;
	}
	
	public String getDefaultCityWoeid(Context context) {
		SharedPreferences sp = context.getSharedPreferences(
				WEATHER_SP, Context.MODE_PRIVATE);
		return sp.getString("woeid", "");
	}
	
	public void updateDefaultCityWoeid(Context context, String woeid) {
		Log.d(TAG, "updateDefaultCityWoeid:"+woeid);
		SharedPreferences.Editor editor = context.getSharedPreferences(
				WEATHER_SP, Context.MODE_PRIVATE).edit();
		editor.putString("woeid", woeid);
		editor.commit();
	}
	
	public boolean getNeedUpdateMainUI(Context context) {
		SharedPreferences sp = context.getSharedPreferences(
				WEATHER_SP, Context.MODE_PRIVATE);
		return sp.getBoolean("main_update", false);
	}
	
	public void setNeedUpdateMainUI(Context context, boolean needUpdate) {
		Log.d(TAG, "setNeedUpdateMainUI:"+needUpdate);
		SharedPreferences.Editor editor = context.getSharedPreferences(
				WEATHER_SP, Context.MODE_PRIVATE).edit();
		editor.putBoolean("main_update", needUpdate);
		editor.commit();
	}
	
	private long getDefaultRefreshTime(Context context) {
		String defaultTimeString = context.getResources().getString(R.string.default_refresh_time);
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(new SimpleDateFormat("yyyyMMddHHmmss").parse(defaultTimeString));
		} catch (ParseException e) {
			e.printStackTrace();
			return 0l;
		}
		return calendar.getTimeInMillis();
	}
	
	public long getRefreshTime(Context context) {
		long defaultRefreshTime = getDefaultRefreshTime(context);
		SharedPreferences sp = context.getSharedPreferences(
				WEATHER_SP, Context.MODE_PRIVATE);
		return sp.getLong("refresh_time", defaultRefreshTime);
	}
	
	public void setRefreshTime(Context context, long time) {
		Log.d(TAG, "setRefreshTime:"+time);
		SharedPreferences.Editor editor = context.getSharedPreferences(
				WEATHER_SP, Context.MODE_PRIVATE).edit();
		editor.putLong("refresh_time", time);
		editor.commit();
	}
	
	/*public static void updateLocationCityInfo(Context context, CityInfo info) {
		ContentResolver mContentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://com.gweather.app.weather/gcity");
		Cursor cursor = mContentResolver.query(uri, null, null, null, null);
		String woeid = "";
		boolean hasInfo = false;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int cursorIndex = cursor.getColumnIndex(WeatherProvider.CITY_WOEID);
				woeid = cursor.getString(cursorIndex);
				hasInfo = true;
			}
			cursor.close();
		}
		Log.d(TAG, "updateLocationCityInfo, hasInfo=" + hasInfo);
		ContentValues values;
		values = new ContentValues();
		values.put(WeatherProvider.CITY_WOEID, info.getWoeid());
		values.put(WeatherProvider.CITY_NAME, info.getName());
		values.put(WeatherProvider.CITY_LAT, info.getLocationInfo().getLat());
		values.put(WeatherProvider.CITY_LON, info.getLocationInfo().getLon());
		values.put(WeatherProvider.CITY_SWLAT, info.getLocationInfo().getSouthWestLat());
		values.put(WeatherProvider.CITY_SWLON, info.getLocationInfo().getSouthWestLon());
		values.put(WeatherProvider.CITY_NELAT, info.getLocationInfo().getNorthEastLat());
		values.put(WeatherProvider.CITY_NELON, info.getLocationInfo().getNorthEastLon());
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
	
	public static CityInfo getLocationCityInfo(Context context) {
		CityInfo info = new CityInfo();
		ContentResolver mContentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://com.gweather.app.weather/gcity");
		Cursor cursor = mContentResolver.query(uri, null, null, null, null);
		if (cursor != null) {
			if(cursor.moveToFirst()) {
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
			}
			
			cursor.close();
		}
		
		return info;
	}*/
	
	public static long getRefreshDelta(Context context, int time) {
		long deltaTime = 0l;
		switch (time) {
		case MainActivity.SETTINGS_AUTO_REFRESH_6H:
			deltaTime = MainActivity.TIME_6H;
			break;
		case MainActivity.SETTINGS_AUTO_REFRESH_12H:
			deltaTime = MainActivity.TIME_12H;
			break;
		case MainActivity.SETTINGS_AUTO_REFRESH_24H:
			deltaTime = MainActivity.TIME_24H;
			break;
		default:
			Log.w(TAG, "getRefreshDelta default");
			return MainActivity.TIME_24H;
		}
    	Log.i(TAG, "getRefreshDelta deltaTime="+deltaTime);
    	return deltaTime;
	}
	/*public static long getRefreshDelta(Context context, int time) {
		long currentTime = System.currentTimeMillis();
		long refreshtimeOld = WeatherDataUtil.getInstance().getRefreshTime(context);
		
		SimpleDateFormat sdf = new SimpleDateFormat("HH,mm,ss");
		String date = sdf.format(new Date());
		System.out.println("date=" + date);
		String[] s = date.split(",");
		long now = (((Integer.parseInt(s[0]) * 60) + Integer
				.parseInt(s[1])) * 60 + Integer.parseInt(s[2])) * 1000l;
		
    	Log.d(TAG, "getRefreshDelta, date="+date);
    	long deltaTime = 0l;
    	switch (time) {
		case MainActivity.SETTINGS_AUTO_REFRESH_6H:
			if (currentTime - refreshtimeOld > MainActivity.TIME_6H) {
				deltaTime = 0;
			} else if (now >= AUTO_REFRESH_TIME_1 && now < AUTO_REFRESH_TIME_2) {
				deltaTime = AUTO_REFRESH_TIME_2 - now;
			} else if (now >= AUTO_REFRESH_TIME_2 && now < AUTO_REFRESH_TIME_3) {
				deltaTime = AUTO_REFRESH_TIME_3 - now;
			} else if (now >= AUTO_REFRESH_TIME_3 && now < AUTO_REFRESH_TIME_4) {
				deltaTime = AUTO_REFRESH_TIME_4 - now;
			} else if (now >= AUTO_REFRESH_TIME_4) {
				deltaTime = TIME_ONE_DAY + AUTO_REFRESH_TIME_1 - now;
			} else {
				deltaTime = AUTO_REFRESH_TIME_1 - now;
			}
			if (deltaTime == 0 && (currentTime - refreshtimeOld) < 600000l) {
				deltaTime = MainActivity.TIME_6H;
			}
			break;
		case MainActivity.SETTINGS_AUTO_REFRESH_12H:
			if (currentTime - refreshtimeOld > MainActivity.TIME_12H) {
				deltaTime = 0;
			} else if (now >= AUTO_REFRESH_TIME_1 && now < AUTO_REFRESH_TIME_3) {
				deltaTime = AUTO_REFRESH_TIME_3 - now;
			} else if (now >= AUTO_REFRESH_TIME_3) {
				deltaTime = TIME_ONE_DAY + AUTO_REFRESH_TIME_1 - now;
			} else {
				deltaTime = AUTO_REFRESH_TIME_1 - now;
			}
			if (deltaTime == 0 && (currentTime - refreshtimeOld) < 600000l) {
				deltaTime = MainActivity.TIME_12H;
			}
			break;
		case MainActivity.SETTINGS_AUTO_REFRESH_24H:
			if (currentTime - refreshtimeOld > MainActivity.TIME_24H) {
				deltaTime = 0;
			} else if (now >= AUTO_REFRESH_TIME_1) {
				deltaTime = TIME_ONE_DAY + AUTO_REFRESH_TIME_1 - now;
			} else {
				deltaTime = AUTO_REFRESH_TIME_1 - now;
			}
			
			if (deltaTime == 0 && (currentTime - refreshtimeOld) < 600000l) {
				deltaTime = MainActivity.TIME_24H;
			}
			break;
		default:
			Log.w(TAG, "getRefreshDelta default");
			return MainActivity.TIME_24H;
		}
    	Log.i(TAG, "getRefreshDelta deltaTime="+deltaTime);
    	return deltaTime;
	}*/
	
	public int getDefaultState(Context context) {
		SharedPreferences sp = context.getSharedPreferences(
				WEATHER_SP, Context.MODE_PRIVATE);
		return sp.getInt("default_state", DEFAULT_STATE_NEED_CHECK);
	}
	
	public void setDefaultState(Context context, int state) {
		Log.d(TAG, "setDefaultState:"+state);
		SharedPreferences.Editor editor = context.getSharedPreferences(
				WEATHER_SP, Context.MODE_PRIVATE).edit();
		editor.putInt("default_state", state);
		editor.commit();
	}
}
