package com.gweather.app;

import java.util.ArrayList;
import java.util.List;

import com.gweather.utils.WeatherDataUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

public class SettingsActivity extends BaseActivity {
	private static final String TAG = "Gweather.SettingsActivity";
	
	private Switch autoRefreshSwitch;
	private View auto6HView;
	private View auto12HView;
	private View auto24HView;
	private ImageView auto6HRadio;
	private ImageView auto12HRadio;
	private ImageView auto24HRadio;
	private View temperatureCView;
	private View temperatureFView;
	private ImageView temperatureCRadio;
	private ImageView temperatureFRadio;
	private Switch notificationSwitch;
	private Switch wifionlySwitch;
	private View cityManagerView;
	
	private WeatherModel mModel;
	private List<WeatherInfo> mWeatherInfos = new ArrayList<WeatherInfo>();
	private WeatherInfo defWeatherInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomTitle(R.string.settings);
		setContentView(R.layout.settings);
		
		initUI();
		
		mModel = WeatherModel.getInstance(this);
		mWeatherInfos = mModel.getWeatherInfos();
		
		defWeatherInfo = getDefaultCityWeatherInfo();
	}

	private void initUI() {
		autoRefreshSwitch = (Switch) findViewById(R.id.settings_auto_refresh);
		autoRefreshSwitch.setOnClickListener(mOnClickListener);
		
		auto6HView = findViewById(R.id.settings_auto_6h);
		auto6HView.setOnClickListener(mOnClickListener);
		auto12HView = findViewById(R.id.settings_auto_12h);
		auto12HView.setOnClickListener(mOnClickListener);
		auto24HView = findViewById(R.id.settings_auto_24h);
		auto24HView.setOnClickListener(mOnClickListener);
		auto6HRadio = (ImageView) findViewById(R.id.settings_radio_auto_6h);
		auto12HRadio = (ImageView) findViewById(R.id.settings_radio_auto_12h);
		auto24HRadio = (ImageView) findViewById(R.id.settings_radio_auto_24h);
		
		temperatureCView = findViewById(R.id.settings_temperature_c);
		temperatureCView.setOnClickListener(mOnClickListener);
		temperatureFView = findViewById(R.id.settings_temperature_f);
		temperatureFView.setOnClickListener(mOnClickListener);
		temperatureCRadio = (ImageView) findViewById(R.id.settings_radio_temperature_c);
		temperatureFRadio = (ImageView) findViewById(R.id.settings_radio_temperature_f);
		
		notificationSwitch = (Switch) findViewById(R.id.settings_notification);
		notificationSwitch.setOnClickListener(mOnClickListener);
		wifionlySwitch = (Switch) findViewById(R.id.settings_wifionly);
		wifionlySwitch.setOnClickListener(mOnClickListener);
		
		cityManagerView = findViewById(R.id.settings_citymanager);
		cityManagerView.setOnClickListener(mOnClickListener);
		
		
		SharedPreferences sp = getSharedPreferences(MainActivity.SETTINGS_SP,
				Context.MODE_PRIVATE);
		boolean isAutoRefreshEnable = sp.getBoolean(
				MainActivity.SETTINGS_AUTO_REFRESH_ENABLE, getResources()
						.getBoolean(R.bool.config_auto_refresh_enable));
		int time = sp.getInt(MainActivity.SETTINGS_AUTO_REFRESH, getResources()
				.getInteger(R.integer.config_auto_refresh));
		updateAutoReflashTimeUI(time);
		
		boolean wifionly = sp.getBoolean(MainActivity.SETTINGS_WIFI_ONLY, getResources()
				.getBoolean(R.bool.config_wifi_only_enable));
		boolean isTemperatureC = sp.getBoolean(MainActivity.SETTINGS_TEMPERATURE_TYPE, 
				getResources().getBoolean(R.bool.config_default_temperature_c));
		boolean notify = sp.getBoolean(MainActivity.SETTINGS_NOTIFICATION, getResources()
						.getBoolean(R.bool.config_notification));
		autoRefreshSwitch.setChecked(isAutoRefreshEnable);
		auto6HView.setEnabled(isAutoRefreshEnable);
		auto12HView.setEnabled(isAutoRefreshEnable);
		auto24HView.setEnabled(isAutoRefreshEnable);
		auto6HRadio.setEnabled(isAutoRefreshEnable);
		auto12HRadio.setEnabled(isAutoRefreshEnable);
		auto24HRadio.setEnabled(isAutoRefreshEnable);
		wifionlySwitch.setChecked(wifionly);
		notificationSwitch.setChecked(notify);
		updateTemperatureTypeUI(isTemperatureC);
	}
	
	View.OnClickListener mOnClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.settings_auto_refresh: {
				SharedPreferences sp = getSharedPreferences(MainActivity.SETTINGS_SP,
						Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				boolean isAutoRefreshEnable = sp.getBoolean(
						MainActivity.SETTINGS_AUTO_REFRESH_ENABLE, getResources()
								.getBoolean(R.bool.config_auto_refresh_enable));

				editor.putBoolean(MainActivity.SETTINGS_AUTO_REFRESH_ENABLE, !isAutoRefreshEnable);
				autoRefreshSwitch.setChecked(!isAutoRefreshEnable);
				auto6HView.setEnabled(!isAutoRefreshEnable);
				auto12HView.setEnabled(!isAutoRefreshEnable);
				auto24HView.setEnabled(!isAutoRefreshEnable);
				auto6HRadio.setEnabled(!isAutoRefreshEnable);
				auto12HRadio.setEnabled(!isAutoRefreshEnable);
				auto24HRadio.setEnabled(!isAutoRefreshEnable);
				
				int time = sp.getInt(MainActivity.SETTINGS_AUTO_REFRESH, getResources()
						.getInteger(R.integer.config_auto_refresh));

				editor.commit();
				if (isAutoRefreshEnable) {
					setAutoRefreshAlarm(SettingsActivity.this,
							MainActivity.SETTINGS_AUTO_REFRESH_INVALID);
				} else {
					setAutoRefreshAlarm(SettingsActivity.this, time);
				}
				break;
			}
			
			case R.id.settings_auto_6h: {
				autoReflashTimeChecked(MainActivity.SETTINGS_AUTO_REFRESH_6H);
				break;
			}
			case R.id.settings_auto_12h: {
				autoReflashTimeChecked(MainActivity.SETTINGS_AUTO_REFRESH_12H);
				break;
			}
			case R.id.settings_auto_24h: {
				autoReflashTimeChecked(MainActivity.SETTINGS_AUTO_REFRESH_24H);
				break;
			}
			case R.id.settings_temperature_c: {
				SharedPreferences sp = getSharedPreferences(MainActivity.SETTINGS_SP,
						Context.MODE_PRIVATE);
				if (!sp.getBoolean(MainActivity.SETTINGS_TEMPERATURE_TYPE, 
						getResources().getBoolean(R.bool.config_default_temperature_c))) {
					SharedPreferences.Editor editor = sp.edit();
					editor.putBoolean(MainActivity.SETTINGS_TEMPERATURE_TYPE, true);
					editor.commit();
					updateTemperatureTypeUI(true);
					if (mModel.notifyIsExist()) {
						mModel.refreshNotification(defWeatherInfo);
					}
					WeatherDataUtil.getInstance().setNeedUpdateMainUI(SettingsActivity.this, true);
					startUpdateService(SettingsActivity.this,
							WeatherWidget.ACTION_UPDATE,
							AppWidgetManager.INVALID_APPWIDGET_ID);
				}
				break;
			}
			case R.id.settings_temperature_f: {
				SharedPreferences sp = getSharedPreferences(MainActivity.SETTINGS_SP,
						Context.MODE_PRIVATE);
				if (sp.getBoolean(MainActivity.SETTINGS_TEMPERATURE_TYPE, 
						getResources().getBoolean(R.bool.config_default_temperature_c))) {
					SharedPreferences.Editor editor = sp.edit();
					editor.putBoolean(MainActivity.SETTINGS_TEMPERATURE_TYPE, false);
					editor.commit();
					updateTemperatureTypeUI(false);
					if (mModel.notifyIsExist()) {
						mModel.refreshNotification(defWeatherInfo);
					}
					WeatherDataUtil.getInstance().setNeedUpdateMainUI(SettingsActivity.this, true);
					startUpdateService(SettingsActivity.this,
							WeatherWidget.ACTION_UPDATE,
							AppWidgetManager.INVALID_APPWIDGET_ID);
				}
				break;
			}
			case R.id.settings_notification: {
				SharedPreferences sp = getSharedPreferences(MainActivity.SETTINGS_SP, 
						Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				
				if(sp.getBoolean(MainActivity.SETTINGS_NOTIFICATION, getResources()
						.getBoolean(R.bool.config_notification))){
					editor.putBoolean(MainActivity.SETTINGS_NOTIFICATION, false);
					notificationSwitch.setChecked(false);
					mModel.removeNotification();
				}else {
					editor.putBoolean(MainActivity.SETTINGS_NOTIFICATION, true);
					notificationSwitch.setChecked(true);
					mModel.sendNotification(defWeatherInfo);
				}
				
				editor.commit();
				break;
			}
			case R.id.settings_wifionly: {
				SharedPreferences sp = getSharedPreferences(MainActivity.SETTINGS_SP,
						Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();

				if (sp.getBoolean(MainActivity.SETTINGS_WIFI_ONLY, getResources()
						.getBoolean(R.bool.config_wifi_only_enable))) {
					editor.putBoolean(MainActivity.SETTINGS_WIFI_ONLY, false);
					wifionlySwitch.setChecked(false);
				} else {
					editor.putBoolean(MainActivity.SETTINGS_WIFI_ONLY, true);
					wifionlySwitch.setChecked(true);
				}
				editor.commit();
				break;
			}
			case R.id.settings_citymanager: {
				Intent intent = new Intent(SettingsActivity.this, CityMangerActivity.class);
				startActivity(intent);
				break;
			}

			default:
				break;
			}
		}
	};
	
	private void updateTemperatureTypeUI(boolean isTemperatureC) {
		if (isTemperatureC) {
			temperatureCRadio.setImageResource(R.drawable.radio_btn_on);
			temperatureFRadio.setImageResource(R.drawable.radio_btn_off);
		} else {
			temperatureCRadio.setImageResource(R.drawable.radio_btn_off);
			temperatureFRadio.setImageResource(R.drawable.radio_btn_on);
		}
	}
	
	private void updateAutoReflashTimeUI(int checkedTime) {
		switch (checkedTime) {
		case MainActivity.SETTINGS_AUTO_REFRESH_6H:
			auto6HRadio.setImageResource(R.drawable.radio_btn_on);
			auto12HRadio.setImageResource(R.drawable.radio_btn_off);
			auto24HRadio.setImageResource(R.drawable.radio_btn_off);
			break;
		case MainActivity.SETTINGS_AUTO_REFRESH_12H:
			auto6HRadio.setImageResource(R.drawable.radio_btn_off);
			auto12HRadio.setImageResource(R.drawable.radio_btn_on);
			auto24HRadio.setImageResource(R.drawable.radio_btn_off);
			break;
		case MainActivity.SETTINGS_AUTO_REFRESH_24H:
			auto6HRadio.setImageResource(R.drawable.radio_btn_off);
			auto12HRadio.setImageResource(R.drawable.radio_btn_off);
			auto24HRadio.setImageResource(R.drawable.radio_btn_on);
			break;

		default:
			break;
		}
	}
	
	private void autoReflashTimeChecked(int checkedTime) {
		SharedPreferences sp = getSharedPreferences(MainActivity.SETTINGS_SP,
				Context.MODE_PRIVATE);
		if (!sp.getBoolean(MainActivity.SETTINGS_AUTO_REFRESH_ENABLE, getResources()
				.getBoolean(R.bool.config_auto_refresh_enable))) {
			Log.i(TAG, "Auto reflash NOT enable.");
			return;
		}
		SharedPreferences.Editor editor = sp.edit();
		switch (checkedTime) {
		case MainActivity.SETTINGS_AUTO_REFRESH_6H:
			auto6HRadio.setImageResource(R.drawable.radio_btn_on);
			auto12HRadio.setImageResource(R.drawable.radio_btn_off);
			auto24HRadio.setImageResource(R.drawable.radio_btn_off);

			editor.putInt(MainActivity.SETTINGS_AUTO_REFRESH, checkedTime);
			break;
		case MainActivity.SETTINGS_AUTO_REFRESH_12H:
			auto6HRadio.setImageResource(R.drawable.radio_btn_off);
			auto12HRadio.setImageResource(R.drawable.radio_btn_on);
			auto24HRadio.setImageResource(R.drawable.radio_btn_off);

			editor.putInt(MainActivity.SETTINGS_AUTO_REFRESH, checkedTime);
			break;
		case MainActivity.SETTINGS_AUTO_REFRESH_24H:
			auto6HRadio.setImageResource(R.drawable.radio_btn_off);
			auto12HRadio.setImageResource(R.drawable.radio_btn_off);
			auto24HRadio.setImageResource(R.drawable.radio_btn_on);

			editor.putInt(MainActivity.SETTINGS_AUTO_REFRESH, checkedTime);
			break;

		default:
			break;
		}
		editor.commit();

		setAutoRefreshAlarm(SettingsActivity.this, checkedTime);
	}
	
	
	private void setAutoRefreshAlarm(Context context, int time) {
		Log.d(TAG, "setAutoRefreshAlarm, " + time);

		long deltaTime = WeatherDataUtil.getRefreshDelta(SettingsActivity.this,
				time);

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(WeatherAction.ACTION_AUTO_REFRESH);
		PendingIntent operation = PendingIntent.getBroadcast(context, 0,
				intent, 0);

		switch (time) {
		case MainActivity.SETTINGS_AUTO_REFRESH_6H:
		case MainActivity.SETTINGS_AUTO_REFRESH_12H:
		case MainActivity.SETTINGS_AUTO_REFRESH_24H:
			alarmManager.cancel(operation);
			alarmManager.set(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis() + deltaTime, operation);
			break;

		default:
			Log.d(TAG, "setAutoRefreshAlarm, " + time);
			alarmManager.cancel(operation);
		}
	}
	
	private void startUpdateService(Context context, String action, int widgetId) {
		Log.d(TAG, "startUpdateService");
		Intent intent = new Intent(context, UpdateWidgetService.class);
		intent.setAction(action);
		intent.setData(Uri.parse(String.valueOf(widgetId)));
		context.startService(intent);
	}
	
	private WeatherInfo getDefaultCityWeatherInfo() {
		String defWoeid =  WeatherDataUtil.getInstance().getDefaultCityWoeid(this);
		
		for(WeatherInfo info:mWeatherInfos) {
			if (!info.isGps()
					&& defWoeid.equals(info.getWoeid()) 
					&& !defWoeid.equals(WeatherDataUtil.DEFAULT_WOEID_GPS)) {
				return info;
			}else if(info.isGps() && defWoeid
					.equals(WeatherDataUtil.DEFAULT_WOEID_GPS)){
				return info;
			}
		}
		
		return null;
	}
}
