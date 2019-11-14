package com.gweather.utils;

import com.gweather.app.R;
import android.util.Log;

public class OWMWeatherDataUtil {

	private static final String TAG = "Gweather.WeatherDataUtil";
	
	public final static String WEATHER_SP = "gweather";
	
	public static final float INVALID_LOCATION = -1000;
	
	
	public static final int ICON_CODE_CLEAR_SKY = 1;
	public static final int ICON_CODE_FEW_CLOUDS = 2;
	public static final int ICON_CODE_SCATTERED_CLOUDS = 3;
	public static final int ICON_CODE_BROKEN_CLOUDS = 4;
	public static final int ICON_CODE_SHOWER_RAIN = 9;
	public static final int ICON_CODE_RAIN = 10;
	public static final int ICON_CODE_THUNDERSTORM = 11;
	public static final int ICON_CODE_SNOW = 13;
	public static final int ICON_CODE_MIST = 50;
	
	
	private static OWMWeatherDataUtil mWeatherDataUtil;
	private OWMWeatherDataUtil(){}
	public static OWMWeatherDataUtil getInstance() {
		if(mWeatherDataUtil == null) {
			mWeatherDataUtil = new OWMWeatherDataUtil();
		}
		return mWeatherDataUtil;
	}
	
	public int getWeatherImageResourceByCode(int code, int iconCode, boolean isnight, boolean isWidget) {
		Log.d(TAG, "WeatherDataUtil - updateWeatherImageByCode:"+code);
		
		switch (code) {
		case 511:
		case 611:
		case 612:
		case 615:
		case 616:
		case 620:
			if (isWidget) {
				return R.drawable.widget42_icon_yujiaxue_day;
			} else {
				return R.drawable.weather_icon_yujiaxue_day;
			}
		case 602:
			if (isWidget) {
				return R.drawable.widget42_icon_daxue_day;
			} else {
				return R.drawable.weather_icon_daxue_day;
			}

		default:
			switch (iconCode) {
			case ICON_CODE_FEW_CLOUDS:
			case ICON_CODE_SCATTERED_CLOUDS:
				if (isWidget) {
					return R.drawable.widget42_icon_cloudy_day;
				} else {
					return R.drawable.weather_icon_cloudy_day;
				}
			case ICON_CODE_BROKEN_CLOUDS:
				if (isWidget) {
					return R.drawable.widget42_icon_windy_day;
				} else {
					return R.drawable.weather_icon_windy_day;
				}
			case ICON_CODE_CLEAR_SKY:
				if (isWidget) {
					if (isnight) {
						return R.drawable.widget42_icon_sun_day_night;
					} else {
						return R.drawable.widget42_icon_sun_day;
					}
				} else {
					if (isnight) {
						return R.drawable.weather_icon_sun_day_night;
					} else {
						return R.drawable.weather_icon_sun_day;
					}
				}
			case ICON_CODE_RAIN:
				if (isWidget) {
					return R.drawable.widget42_icon_dayu_day;
				} else {
					return R.drawable.weather_icon_dayu_day;
				}
			case ICON_CODE_THUNDERSTORM:
				if (isWidget) {
					return R.drawable.widget42_icon_leizhenyu_day;
				} else {
					return R.drawable.weather_icon_leizhenyu_day;
				}
			case ICON_CODE_SHOWER_RAIN:
				if (isWidget) {
					return R.drawable.widget42_icon_rain_day;
				} else {
					return R.drawable.weather_icon_rain_day;
				}
			case ICON_CODE_SNOW:
				if (isWidget) {
					return R.drawable.widget42_icon_xue_day;
				} else {
					return R.drawable.weather_icon_xue_day;
				}
				
			case ICON_CODE_MIST:
				if (isWidget) {
					return R.drawable.widget42_icon_fog_day;
				} else {
					return R.drawable.weather_icon_fog_day;
				}
			default:
				if (code == 905 || code >= 952 && code <= 959) {
					if (isWidget) {
						return R.drawable.widget42_icon_windy_day;
					} else {
						return R.drawable.weather_icon_windy_day;
					}
				} else if (code >= 960 && code <= 962
						   || code >= 900 && code <= 902) {
					if (isWidget) {
						return R.drawable.widget42_icon_sandstorm_day;
					} else {
						return R.drawable.weather_icon_sandstorm_day;
					}
				} else if (code == 906) {
					if (isWidget) {
						return R.drawable.widget42_icon_rain_day;
					} else {
						return R.drawable.weather_icon_rain_day;
					}
				} else {
					if (isWidget) {
						return R.drawable.widget42_icon_nodata;
					} else {
						return R.drawable.weather_icon_nodata;
					}
					
				}
			}
		}
		
	}
	
}
