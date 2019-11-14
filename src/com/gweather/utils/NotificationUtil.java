package com.gweather.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.gweather.app.MainActivity;
import com.gweather.app.R;
import com.gweather.app.WeatherInfo;

public class NotificationUtil {
	private static final int NOTIFY_ID = 525;
	private static final String TAG = "Gweather.NotificationUtil";
	
	private NotificationManager mNotificationManager = null;
	private Notification.Builder mBuilder = null;
	private Context mContext = null;
	
	private WeatherInfo mNotificationWeatherInfo;
	private boolean isTemperatureC = true;
	private String temperatureType = "";
	private Notification notification = null;
	
	public NotificationUtil(Context context){
		mContext = context;
		mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
	}
	
	public void showNotification(WeatherInfo weatherInfo){
		initNotification(weatherInfo);
	}
	
	public void cancelNotification(){
		mNotificationManager.cancel(NOTIFY_ID);
	}
	
	public void updateNotification(WeatherInfo weatherInfo){
		initNotification(weatherInfo);
	}
	
	private void initNotification(WeatherInfo weatherInfo){
		if(weatherInfo == null){
			setDefaultViews();
		}else{
			if (weatherInfo.getForecasts().size() < MainActivity.FORECAST_DAY) {
				Log.w(TAG, "initNotification--Query NEW weather failed");
			} else {
				mBuilder = new Notification.Builder(mContext);
				
			    mBuilder.setOngoing(true);
			    
			    mBuilder.setSmallIcon(R.drawable.notification_small_icon);
			    
			    Intent intent = new Intent(mContext, MainActivity.class);
			    PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
			    mBuilder.setContentIntent(pIntent);
			    
			    RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.weather_notification);
			    mNotificationWeatherInfo = weatherInfo;
			    setRomoteViews(remoteViews);
			    
			    mBuilder.setContent(remoteViews);
			    
			    notification = mBuilder.build();
			    mNotificationManager.notify(NOTIFY_ID, notification);
			}
		}
	}
	
	
	private void setRomoteViews(RemoteViews mRemoteViews){
		RemoteViews remoteViews = mRemoteViews;
		SharedPreferences sp = mContext.getSharedPreferences(MainActivity.SETTINGS_SP,
				Context.MODE_PRIVATE);
		isTemperatureC = sp.getBoolean(MainActivity.SETTINGS_TEMPERATURE_TYPE, 
				mContext.getResources().getBoolean(R.bool.config_default_temperature_c));
		temperatureType = isTemperatureC?mContext.getResources().getString(R.string.temperature_c):
			mContext.getResources().getString(R.string.temperature_f);
		remoteViews.setTextViewText(R.id.notification_minmax_temperature,
				(isTemperatureC?mNotificationWeatherInfo.getForecasts().get(0).getLow()
						:mNotificationWeatherInfo.getForecasts().get(0).getLowF())
				+ temperatureType
				+ "/"
				+ (isTemperatureC?mNotificationWeatherInfo.getForecasts().get(0).getHigh()
						:mNotificationWeatherInfo.getForecasts().get(0).getHighF())
				+ temperatureType);
		remoteViews.setTextViewText(R.id.notification_temperature,
				(isTemperatureC?mNotificationWeatherInfo.getCondition().getTemp()
						:mNotificationWeatherInfo.getCondition().getTempF())
				+ temperatureType);
	    
	    String name = mNotificationWeatherInfo.getName();
	    remoteViews.setTextViewText(R.id.notification_city, name); 
	    
	    String weather = mNotificationWeatherInfo
				.getCondition().getText();
	    remoteViews.setTextViewText(R.id.notification_weather, weather);
	    
	    int code = Integer.parseInt(mNotificationWeatherInfo.getCondition().getCode());
		int resId;
		boolean isnight = WeatherDataUtil.getInstance().isNight();
		resId = WeatherDataUtil.getInstance().getWeatherImageResourceByCode(
				code, isnight, WeatherDataUtil.IS_NOTIFICATION);
		if(resId == -1){
			Log.d(TAG, "WeatherImage get by code is invalid,resId == -1");
		}
		if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE == resId) {
			resId = WeatherDataUtil.getInstance()
					.getWeatherImageResourceByText(
							mNotificationWeatherInfo.getCondition().getText(),
							isnight, WeatherDataUtil.IS_NOTIFICATION);
			if(resId == -1){
				Log.d(TAG, "WeatherImage get by text is invalid,resId == -1");
			}
		}
	    remoteViews.setImageViewResource(R.id.notification_weather_image, resId);
	}
	public void setDefaultViews() {
		RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(),
				R.layout.weather_notification);

		String defaultData = mContext.getResources().getString(
				R.string.weather_data_default);
		remoteViews.setTextViewText(R.id.notification_city, defaultData);
		remoteViews.setTextViewText(R.id.notification_temperature, defaultData);
		remoteViews.setTextViewText(R.id.notification_weather, defaultData);
		remoteViews.setTextViewText(R.id.notification_minmax_temperature, defaultData);

		int resId;
		boolean isnight = WeatherDataUtil.getInstance().isNight();
		resId = WeatherDataUtil.getInstance().getWeatherImageResourceByText(
				defaultData, isnight, WeatherDataUtil.IS_NOTIFICATION);
		if(resId == -1){
			Log.d(TAG, "WeatherImage get by text is invalid,resId == -1");
		}
		remoteViews.setImageViewResource(R.id.notification_weather_image, resId);
		mBuilder.setContent(remoteViews);
	    
	    Notification notification = mBuilder.build();
	    mNotificationManager.notify(NOTIFY_ID, notification);
	}
}


	
	
	

