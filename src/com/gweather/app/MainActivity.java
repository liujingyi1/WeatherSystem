package com.gweather.app;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import com.gweather.utils.Utils;
import com.gweather.utils.WeatherDataUtil;
import com.gweather.view.RefreshHeader;
import com.gweather.view.RefreshLayout;
import com.gweather.view.RefreshLayout.CanMoveCallBack;
import com.gweather.view.ScrollControlLayout;
import com.gweather.view.ScrollControlLayout.CanScrollCallBack;
import com.gweather.view.State;
import com.gweather.view.WeatherInfoMainView;
import com.gweather.view.WeatherInfoMainView.AnimateEndListener;
import com.gweather.app.R;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener,RefreshLayout.OnRefreshListener {
	private static final String TAG = "Gweather.MainActivity";
	
	public static final int FORECAST_DAY = 5;
	public static final String SETTINGS_SP = "settings_sp";
	public static final String SETTINGS_AUTO_REFRESH_ENABLE = "settings_auto_enable";
	public static final String SETTINGS_AUTO_REFRESH = "settings_auto_refresh";
	public static final String SETTINGS_WIFI_ONLY = "settings_wifi_only";
	public static final String SETTINGS_TEMPERATURE_TYPE = "settings_temperature_type";
	public static final String SETTINGS_NOTIFICATION = "settings_notification";
	public static final int SETTINGS_AUTO_REFRESH_INVALID = -1;
	public static final int SETTINGS_AUTO_REFRESH_6H = 6;
	public static final int SETTINGS_AUTO_REFRESH_12H = 12;
	public static final int SETTINGS_AUTO_REFRESH_24H = 24;

	public static final long TIME_6H = 6 * 60 * 60 * 1000L;
	public static final long TIME_12H = 12 * 60 * 60 * 1000L;
	public static final long TIME_24H = 24 * 60 * 60 * 1000L;

	private ScrollControlLayout weatherInfoMainContainer;
	private WeatherInfoMainView weatherInfoMainView;

	private View mainContentView;
	private ImageView refresh;
	private ImageView setting;
	private TextView refreshTimeText;
	private View loadProgressView;
	private TextView progressText;
	private LinearLayout indicatorBar;
	RefreshLayout refreshLayout;

	private WeakReference<List<WeatherInfo>> mSoftWeatherInfoList;
	private List<WeatherInfo> mWeatherInfoList;

	private WeatherRefreshedReceiver mWeatherRefreshedReceiver;

	private int defScreen;
	private boolean isTemperatureC = true;
	private String temperatureType = "";

	private class WeatherRefreshedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}

			String action = intent.getAction();
			Log.d(TAG, "WeatherRefreshedReceiver, " + action);
			if (WeatherAction.ACTION_WEATHER_REFRESHED.equals(action)) {
				setWeatherFromBD();
				showLoadingProgress(false, R.string.progress_refresh);
			} else if (WeatherAction.ACTION_WEATHER_REFRESHED_ALL
					.equals(action)) {
				setWeatherFromBD();
			}

		}

	}
	//add shiyang start
	BroadcastReceiver broadcast =new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
				refreshLayout.refreshComplete();
		}
	};
	//add shiyang end
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		Log.d(TAG, "onCreate");
		
		int dimenTest = getResources().getDimensionPixelSize(R.dimen.test);
		Log.e(TAG, "dimenTest="+dimenTest);
		
		WeatherApp.mModel = WeatherModel.getInstance(getApplicationContext());
		initUI();
		//add shiyang start
		IntentFilter filter2 = new IntentFilter();
        filter2.addAction(WeatherAction.ACTION_REFRESH_COMPLETE);
        registerReceiver(broadcast, filter2);

		MyRefreshHeader header = new MyRefreshHeader(this);
		refreshLayout.setRefreshHeader(header);
		refreshLayout.autoRefresh();
		//add shiyang end
		boolean isFirstRun = isFirstRun();
		Log.d(TAG, "isFirstRun=" + isFirstRun);
		if (isFirstRun) {
			Intent intent = new Intent(MainActivity.this,
					CityMangerActivity.class);
			intent.putExtra("isFirstRun", isFirstRun);
			startActivity(intent);
		} else {
			setWeatherFromBD();
			WeatherDataUtil.getInstance().setNeedUpdateMainUI(
					MainActivity.this, false);
		}

		mWeatherRefreshedReceiver = new WeatherRefreshedReceiver();
		IntentFilter filter = new IntentFilter(
				WeatherAction.ACTION_WEATHER_REFRESHED);
		filter.addAction(WeatherAction.ACTION_WEATHER_REFRESHED_ALL);
		registerReceiver(mWeatherRefreshedReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mWeatherRefreshedReceiver);
		mWeatherRefreshedReceiver = null;
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	private boolean isFirstRun() {
		ContentResolver mContentResolver = getContentResolver();
		Uri uri = Uri.parse("content://com.gweather.app.weather/gweather");
		Cursor cursor = mContentResolver.query(uri, null, null, null,
				WeatherProvider.INDEX);
		if (cursor != null) {
			if (cursor.getCount() == 0) {
				return true;
			}
			cursor.close();
		}
		return false;
	}

	private void initUI() {
		mainContentView = findViewById(R.id.main_content);
		
		weatherInfoMainContainer = (ScrollControlLayout) findViewById(R.id.main_container);
		weatherInfoMainContainer
				.setOnScreenChangedListener(new ScrollControlLayout.OnScreenChangedListener() {
					@Override
					public void screenChange(int curScreen) {
						Log.d(TAG, "screenChange = " + curScreen);

						ImageView indicatorImage = (ImageView) indicatorBar
								.getChildAt(defScreen);
						if (mWeatherInfoList.get(defScreen).isGps()) {
							indicatorImage.setImageResource(R.drawable.point_gps);
						} else {
							indicatorImage.setImageResource(R.drawable.point);
						}
						indicatorImage = (ImageView) indicatorBar
								.getChildAt(curScreen);
						if (mWeatherInfoList.get(curScreen).isGps()) {
							indicatorImage.setImageResource(R.drawable.point_gps_current);
						} else {
							indicatorImage.setImageResource(R.drawable.point_current);
						}
						
						defScreen = curScreen;
						/*del jingyi
						SimpleDateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						Date mDate = new Date(mWeatherInfoList.get(defScreen)
								.getUpdateTime());
						String refreshTime = getResources().getString(
								R.string.refresh_time, format.format(mDate));
						refreshTimeText.setText(refreshTime);
						del jingyi*/
						if (mWeatherInfoList.get(defScreen).isGps()) {
							WeatherDataUtil.getInstance()
									.updateDefaultCityWoeid(MainActivity.this,
											WeatherDataUtil.DEFAULT_WOEID_GPS);
						} else {
							WeatherDataUtil.getInstance()
									.updateDefaultCityWoeid(
											MainActivity.this,
											mWeatherInfoList.get(defScreen)
													.getWoeid());
						}
						
						if(WeatherApp.mModel.notifyIsExist()){
							WeatherApp.mModel.refreshNotification(mWeatherInfoList.get(defScreen));
						}
						
						startUpdateService(MainActivity.this,
								WeatherWidget.ACTION_UPDATE,
								AppWidgetManager.INVALID_APPWIDGET_ID);
					}
				});

		refresh = (ImageView) findViewById(R.id.refresh);
		refresh.setOnClickListener(this);
		setting = (ImageView) findViewById(R.id.settings);
		setting.setOnClickListener(this);


		//refreshTimeText = (TextView) findViewById(R.id.latest_refresh_time);

		loadProgressView = findViewById(R.id.loading_progress_view);
		progressText = (TextView) findViewById(R.id.progress_text);
		indicatorBar = (LinearLayout) findViewById(R.id.indicator_bar);
		//add shiyang start
		refreshLayout = (RefreshLayout) findViewById(R.id.refreshLayout);
		refreshLayout.setRefreshListener(this);
		//add shiyang end
		refreshLayout.setCanMove(new CanMoveCallBack() {
			
			@Override
			public boolean getCanMove() {
				
				return !weatherInfoMainContainer.isScrolling();
			}
		});
		
		weatherInfoMainContainer.setCanScrollCallBack(new CanScrollCallBack() {
			
			@Override
			public boolean canScroll() {
				return !refreshLayout.getIsDraging();
				
			}
		});

	}
	
	private void updateUI() {
		Log.d(TAG, "updateUI");
		Log.d(TAG, "Weather Info Size: " + mWeatherInfoList.size());
		if (mWeatherInfoList.size() < 1
				&& mWeatherInfoList.get(0).getForecasts().size() < FORECAST_DAY) {
			Log.w(TAG, "update Failed");
			return;
		}
		
		SharedPreferences sp = getSharedPreferences(MainActivity.SETTINGS_SP,
				Context.MODE_PRIVATE);
		isTemperatureC = sp.getBoolean(MainActivity.SETTINGS_TEMPERATURE_TYPE, 
				getResources().getBoolean(R.bool.config_default_temperature_c));
		temperatureType = isTemperatureC?getResources().getString(R.string.temperature_c):
			getResources().getString(R.string.temperature_f);

		String temperature = "";
		String tmpMin = "";
		String tmpMax = "";
		WeatherInfo info = null;

		defScreen = 0;
		String defWoeid = WeatherDataUtil.getInstance().getDefaultCityWoeid(
				MainActivity.this);
		Log.d(TAG, "defWoeid=" + defWoeid);

		weatherInfoMainContainer.removeAllViews();
		for (int i = 0; i < mWeatherInfoList.size(); i++) {
			info = mWeatherInfoList.get(i);

			if (defWoeid.equals(info.getWoeid())) {
				defScreen = i;
				Log.d(TAG, "defScreen=" + defScreen);
			}

			String date = info.getForecasts().get(0).getDate() + "("
					+ info.getForecasts().get(0).getDay() + ")";
			temperature = (isTemperatureC?info.getCondition().getTemp():info.getCondition().getTempF())
					+ temperatureType;
			tmpMin = (isTemperatureC?info.getForecasts().get(0).getLow():info.getForecasts().get(0).getLowF())
					+ temperatureType + "\n" + "MIN";
			tmpMax = (isTemperatureC?info.getForecasts().get(0).getHigh():info.getForecasts().get(0).getHighF())
					+ temperatureType + "\n" + "MAX";
			String text = info.getCondition().getText();
			int code = Integer.parseInt(info.getCondition().getCode());
			int resId;
			boolean isnight = WeatherDataUtil.getInstance().isNight();
			resId = WeatherDataUtil.getInstance()
					.getWeatherImageResourceByCode(code, isnight, WeatherDataUtil.NETHER_NOTIFY_WIDGET);
			if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE == resId) {
				resId = WeatherDataUtil.getInstance()
						.getWeatherImageResourceByText(
								info.getCondition().getText(), isnight, WeatherDataUtil.NETHER_NOTIFY_WIDGET);
			}

			weatherInfoMainView = new WeatherInfoMainView(MainActivity.this);
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date mDate = new Date(mWeatherInfoList.get(defScreen).getUpdateTime());
			String refreshTime = getResources().getString(R.string.refresh_time,
					format.format(mDate));
			
			weatherInfoMainView.bindView(date, info.getName(), resId, text,
					temperature, tmpMin, tmpMax, refreshTime, info.isGps());
			int[] temMaxPoints = new int[FORECAST_DAY - 1];
			int[] temMinPoints = new int[FORECAST_DAY - 1];
			
			for (int j = 1; j < FORECAST_DAY; j++) {
				weatherInfoMainView.updateForeCastItem(j, info.getForecasts()
						.get(j));
				temMaxPoints[j-1] = isTemperatureC ? Integer.valueOf(info.getForecasts().get(j).getHigh())
						: Integer.valueOf(info.getForecasts().get(j).getHighF());
				temMinPoints[j-1] = isTemperatureC ? Integer.valueOf(info.getForecasts().get(j).getLow())
						: Integer.valueOf(info.getForecasts().get(j).getLowF());

			}
			weatherInfoMainView.setLineChartData(temMaxPoints, temMinPoints);
			weatherInfoMainView.setListener(animateActionListener);
			weatherInfoMainContainer.addView(weatherInfoMainView);
		}

		weatherInfoMainContainer.setDefaultScreen(defScreen);

		
		updateIndicatorBar();
		/*
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date mDate = new Date(mWeatherInfoList.get(defScreen).getUpdateTime());
		String refreshTime = getResources().getString(R.string.refresh_time,
				format.format(mDate));
		refreshTimeText.setText(refreshTime);*/

	}

	
	
	AnimateEndListener animateActionListener = new AnimateEndListener() {
		@Override
		public void onAnimateStart() {
			refreshLayout.setEnable(false);
			weatherInfoMainContainer.setTouchMove(false);
		}
		
		public void onAnimateEnd() {
			int count = weatherInfoMainContainer.getChildCount();
			for (int i = 0; i < count; i++) {
				if (defScreen == i) {
					continue;
				}
				WeatherInfoMainView view = (WeatherInfoMainView)weatherInfoMainContainer.getChildAt(i);
				view.requestLayout();
			}
			refreshLayout.setEnable(true);
			weatherInfoMainContainer.setTouchMove(true);
		}


	};
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		if (WeatherDataUtil.getInstance()
				.getNeedUpdateMainUI(MainActivity.this)) {
			showLoadingProgress(true, R.string.progress_refresh);
			Log.d(TAG, "Load onResume");
			WeatherDataUtil.getInstance().setNeedUpdateMainUI(
					MainActivity.this, false);
			setWeatherFromBD();
			showLoadingProgress(false, R.string.progress_refresh);
		}
		
		if(null == mWeatherInfoList || mWeatherInfoList.isEmpty()) {
			Log.d(TAG, "NO info");
			refresh.setEnabled(false);
			finish();
		} else if(!refresh.isEnabled()) {
			Log.d(TAG, "Enable refresh");
			if (loadProgressView.getVisibility() != View.VISIBLE) {
				refresh.setEnabled(true);
			} else {
				Log.d(TAG, "loading");
			}
		}
		
	}

	private void updateIndicatorBar() {
		indicatorBar.removeAllViews();
		ImageView indicatorImage;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(5, 5, 5, 5);
		for (int i = 0; i < mWeatherInfoList.size(); i++) {
			indicatorImage = new ImageView(MainActivity.this);
			indicatorImage.setLayoutParams(lp);
			if (defScreen == i) {
				if (mWeatherInfoList.get(i).isGps()) {
					indicatorImage.setImageResource(R.drawable.point_gps_current);
				} else {
					indicatorImage.setImageResource(R.drawable.point_current);
				}
			} else {
				if (mWeatherInfoList.get(i).isGps()) {
					indicatorImage.setImageResource(R.drawable.point_gps);
				} else {
					indicatorImage.setImageResource(R.drawable.point);
				}
			}
			indicatorBar.addView(indicatorImage, i);
		}
	}
	
	private void setWeatherFromInternet() {
		Log.d(TAG, "setWeatherFromInternet");
		showLoadingProgress(true, R.string.progress_refresh);
		if (!WeatherApp.mModel.refreshWeather(mWeatherInfoList.get(defScreen))) {
			showLoadingProgress(false, R.string.progress_refresh);
		}
		
//		Intent intent = new Intent(MainActivity.this, UpdateWidgetService.class);
//		intent.setAction(WeatherAction.ACTION_WEATHER_REFRESH_CURRENT);
//		intent.putExtra("woeid", value);
//		startService(intent);
	}

	private void setWeatherFromBD() {
		Log.d(TAG, "setWeatherFromBD");
		Log.i("shiyang", "setWeatherFromBD");
		if (mSoftWeatherInfoList == null) {
			mSoftWeatherInfoList = 
					new WeakReference<List<WeatherInfo>>(WeatherApp.mModel.getWeatherInfos());
			mWeatherInfoList = mSoftWeatherInfoList.get();
		}
		//mWeatherInfoList = WeatherApp.mModel.getWeatherInfos();
		if (mWeatherInfoList.size() > 0
				&& mWeatherInfoList.get(0).getForecasts().size() >= FORECAST_DAY) {
			updateUI();
			startUpdateService(MainActivity.this, WeatherWidget.ACTION_UPDATE,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}
	}

	private void showLoadingProgress(boolean show, int textId) {
		Log.d(TAG, "showLoadingProgress:" + show);
		if (show) {
			loadProgressView.setVisibility(View.VISIBLE);
			progressText.setText(textId);
			refresh.setEnabled(false);
			setting.setEnabled(false);
			weatherInfoMainContainer.setEnabled(false);
			weatherInfoMainContainer.setTouchMove(false);
		} else {
			loadProgressView.setVisibility(View.GONE);
			refresh.setEnabled(true);
			setting.setEnabled(true);
			weatherInfoMainContainer.setEnabled(true);
			weatherInfoMainContainer.setTouchMove(true);
		}
	}

	private void startUpdateService(Context context, String action, int widgetId) {
		Log.d(TAG, "startUpdateService");
		Intent intent = new Intent(context, UpdateWidgetService.class);
		intent.setAction(action);
		intent.setData(Uri.parse(String.valueOf(widgetId)));
		context.startService(intent);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.refresh:
			SharedPreferences sp = getSharedPreferences(SETTINGS_SP,
					Context.MODE_PRIVATE);
			if (sp.getBoolean(SETTINGS_WIFI_ONLY, getResources()
					.getBoolean(R.bool.config_wifi_only_enable))) {
				if (Utils.isNetworkTypeWifi(MainActivity.this)) {
					setWeatherFromInternet();
				} else {
					Toast.makeText(MainActivity.this,
							R.string.toast_wifi_only_mode,
							Toast.LENGTH_SHORT).show();
					Log.d(TAG,
							"SETTINGS_WIFI_ONLY, network type NOT WIFI.");
				}
			} else {
				if (Utils.isNetworkAvailable(MainActivity.this)) {
					setWeatherFromInternet();
				} else {
					Toast.makeText(MainActivity.this,
							R.string.toast_net_inavailable,
							Toast.LENGTH_SHORT).show();
					Log.d(TAG, "Refresh BTN, network NOT available");
				}
			}
			break;
		case R.id.settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		default:
			break;
		}
		
		
	}
	//add shiyang start
	class MyRefreshHeader extends FrameLayout implements RefreshHeader{
		private Animation rotate_up;
	    private Animation rotate_down;
	    private Animation rotate_infinite;
	    private TextView textView;
	    private View arrowIcon;
	    private View successIcon;
	    private View loadingIcon;

	    public MyRefreshHeader(Context context) {
	        this(context, null);
	    }
	    public MyRefreshHeader(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        rotate_up = AnimationUtils.loadAnimation(context , R.layout.rotate_up);
	        rotate_down = AnimationUtils.loadAnimation(context , R.layout.rotate_down);
	        rotate_infinite = AnimationUtils.loadAnimation(context , R.layout.rotate_infinite);
	        inflate(context, R.layout.refresh_head, this);
	        textView = (TextView) findViewById(R.id.text);
	        arrowIcon = findViewById(R.id.arrowIcon);
	        successIcon = findViewById(R.id.successIcon);
	        loadingIcon = findViewById(R.id.loadingIcon);
	    }
	    @Override
	    public void reset() {
	        textView.setText(getResources().getText(R.string.header_reset));
	        successIcon.setVisibility(INVISIBLE);
	        arrowIcon.setVisibility(VISIBLE);
	        arrowIcon.clearAnimation();
	        loadingIcon.setVisibility(INVISIBLE);
	        loadingIcon.clearAnimation();
	    }
	    @Override
	    public void pull() {
	    }
	    @Override
	    public void refreshing() {
	        arrowIcon.setVisibility(INVISIBLE);
	        loadingIcon.setVisibility(VISIBLE);
	        textView.setText(getResources().getText(R.string.header_refreshing));
	        arrowIcon.clearAnimation();
	        loadingIcon.startAnimation(rotate_infinite);
	    }
	    @Override
	    public void onPositionChange(float currentPos, float lastPos, float refreshPos, boolean isTouch, State state) {
	        if (currentPos < refreshPos && lastPos >= refreshPos) {
	            if (isTouch && state == State.PULL) {
	                textView.setText(getResources().getText(R.string.header_pull));
	                arrowIcon.clearAnimation();
	                arrowIcon.startAnimation(rotate_down);
	            }
	        } else if (currentPos > refreshPos && lastPos <= refreshPos) {
	            if (isTouch && state == State.PULL) {
	                textView.setText(getResources().getText(R.string.header_pull_over));
	                arrowIcon.clearAnimation();
	                arrowIcon.startAnimation(rotate_up);
	            }
	        }
	    }
	    @Override
	    public void complete() {
	        loadingIcon.setVisibility(INVISIBLE);
	        loadingIcon.clearAnimation();
	        if ((Utils.isNetworkTypeWifi(MainActivity.this))||(Utils.isNetworkAvailable(MainActivity.this))){
	        	textView.setText(getResources().getText(R.string.header_completed));
	        	successIcon.setVisibility(VISIBLE);
	        }else{
	        	textView.setText(getResources().getText(R.string.header_fail));
	        }
	    }
	}
	@SuppressLint("ShowToast") @Override
	public void onRefresh() {
		SharedPreferences sp = getSharedPreferences(SETTINGS_SP,
				Context.MODE_PRIVATE);
		if (sp.getBoolean(SETTINGS_WIFI_ONLY, getResources()
				.getBoolean(R.bool.config_wifi_only_enable))) {
			if (Utils.isNetworkTypeWifi(MainActivity.this)) {
				WeatherModel.getInstance(getApplicationContext()).refreshAllWeather();
			} else {
				refreshLayout.refreshComplete();
				Toast.makeText(MainActivity.this,
						R.string.toast_wifi_only_mode,
						Toast.LENGTH_SHORT).show();
				Log.d(TAG,
						"SETTINGS_WIFI_ONLY, network type NOT WIFI.");
			}
		} else {
			if (Utils.isNetworkAvailable(MainActivity.this)) {
				WeatherModel.getInstance(getApplicationContext()).refreshAllWeather();
			} else {
				refreshLayout.refreshComplete();
				Toast.makeText(MainActivity.this,
						R.string.toast_net_inavailable,
						Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Refresh BTN, network NOT available");
			}
		}
	}
	//add shiyang end
}