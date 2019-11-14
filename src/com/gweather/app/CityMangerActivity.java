package com.gweather.app;

import java.lang.ref.WeakReference;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.gweather.utils.Utils;
import com.gweather.utils.WeatherDataUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class CityMangerActivity extends BaseActivity {
	private static final String TAG = "Gweather.CityManger";
	public static final int REQUEST_CODE_CITY_ADD = 1001;
	public static final int REQUEST_CODE_PERMISSION = 101;
	public static final int CITY_COUNT_MAX = 10;
	
	private static final String FRAGMENT_TAG_DELETE_CITY = "delete_city";
	private static final String FRAGMENT_TAG_LOADING = "loading";
	private static final String FRAGMENT_TAG_LOCATION_FIND = "location_find";
	
	private static final int CLICK_INDEX_DELETE = 1;

	private ImageView addCity;
	private ImageView location;
	private ImageView allRefresh;
	private TextView allRefreshTimeText;
	private ListView cityList;
	
	private LoadingProgressFragment mLoadingProgressFragment;

	private CityListAdapter mCityListAdapter;
	private CityListItem gpsItem;
	private List<CityListItem> items = new ArrayList<CityListItem>();
	private WeakReference<List<WeatherInfo>> mSoftWeatherInfoList;
	private List<WeatherInfo> mWeatherInfoList;
	private GeoHelper mGeoHelper;

	private int deletePosition;

	private static boolean isStoped = false;
	private boolean isAutoGps = false;
	private boolean isTemperatureC = true;
	private String temperatureType = "";

	private CityManagerReceiver mCityManagerReceiver;

	private class CityManagerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}

			String action = intent.getAction();
			Log.d(TAG, "WeatherRefreshedReceiver, " + action);
			if (WeatherAction.ACTION_WEATHER_REFRESHED_ALL.equals(action)) {
				refreshCityList();
				startUpdateService(CityMangerActivity.this,
						WeatherWidget.ACTION_UPDATE,
						AppWidgetManager.INVALID_APPWIDGET_ID);
				showLoadingProgress(false);
			} else if (WeatherAction.ACTION_QUERT_LOCATION_FINISH
					.equals(action)) {
				
			} else if (WeatherAction.ACTION_QUERT_GPS_WEATHER_FINISH
					.equals(action)) {
				refreshCityList();
				String defaultWoeid = WeatherDataUtil.getInstance()
						.getDefaultCityWoeid(CityMangerActivity.this);
				if (defaultWoeid.isEmpty() && !mWeatherInfoList.isEmpty()) {
					WeatherDataUtil
							.getInstance()
							.updateDefaultCityWoeid(
									CityMangerActivity.this,
									mWeatherInfoList.get(0).isGps() ? WeatherDataUtil.DEFAULT_WOEID_GPS
											: mWeatherInfoList.get(0)
													.getWoeid());

					startUpdateService(CityMangerActivity.this,
							WeatherWidget.ACTION_UPDATE,
							AppWidgetManager.INVALID_APPWIDGET_ID);
				}
				if (mWeatherInfoList.size() == 1) {
					WeatherDataUtil.getInstance()
							.setRefreshTime(CityMangerActivity.this,
									System.currentTimeMillis());
					setRefreshTime();
				}
				WeatherDataUtil.getInstance().setNeedUpdateMainUI(
						CityMangerActivity.this, true);

				showLoadingProgress(false);
			}

		}

	}

	private GeoHelper.OnQueryCityInfoFinishedListener onQueryCityInfoFinishedListener = new GeoHelper.OnQueryCityInfoFinishedListener() {

		@Override
		public void querySuccessed(CityInfo cityInfo) {
			isAutoGps = false;
			showLoadingProgress(false);
			if (isStoped) {
				Log.w(TAG, "City Activity has Stoped");
			} else {
				new LocationFindFragment(cityInfo).show(getFragmentManager(), FRAGMENT_TAG_LOCATION_FIND);
			}
		}

		@Override
		public void queryFailed(int reason) {
			Log.w(TAG, "queryFailed:"+reason);
			Toast.makeText(CityMangerActivity.this, R.string.toast_query_city_fail, Toast.LENGTH_SHORT).show();
			showLoadingProgress(false);
		}
	};

	Handler H = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CLICK_INDEX_DELETE:
				int position = msg.arg1;
				new DeleteCityFragment(items, position).show(getFragmentManager(), FRAGMENT_TAG_DELETE_CITY);
				break;

			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.city_manager);
		
		setCustomTitle(R.string.city_manager);
		
		WeatherApp.mModel = WeatherModel.getInstance(getApplicationContext());

		mGeoHelper = new GeoHelper(this);
		mGeoHelper.setOnQueryCityNameFinishedListener(onQueryCityInfoFinishedListener);
		
		mLoadingProgressFragment = new LoadingProgressFragment();
		
		initUI();
		refreshCityList();

		checkFirstRun();
		mCityManagerReceiver = new CityManagerReceiver();
		IntentFilter filter = new IntentFilter(
				WeatherAction.ACTION_WEATHER_REFRESHED_ALL);
		filter.addAction(WeatherAction.ACTION_QUERT_LOCATION_FINISH);
		filter.addAction(WeatherAction.ACTION_QUERT_GPS_WEATHER_FINISH);
		registerReceiver(mCityManagerReceiver, filter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		isStoped = false;
	}

	private void checkFirstRun() {
		Intent intent = getIntent();
		boolean isFirstRun = intent.getBooleanExtra("isFirstRun", false);
		Log.d(TAG, "isFirstRun=" + isFirstRun);
		if (isFirstRun) {
			boolean hasPermission = false;
			if (Build.VERSION.SDK_INT >= 23) {
				hasPermission = PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
				Log.d(TAG, "hasPermission=" + hasPermission);
				if (!hasPermission) {
					return;
				}
			}

			boolean agpsOpen = Utils.isAPGSOpen(CityMangerActivity.this);

			Log.d(TAG, "agpsOpen=" + agpsOpen);
			if (agpsOpen) {
				if (!mGeoHelper.isWorking()) {
					showLoadingProgress(true);
					mGeoHelper.queryLocationCityInfo();
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult, requestCode: " + requestCode
				+ ", resultCode: " + resultCode);
		switch (requestCode) {
		case REQUEST_CODE_CITY_ADD:
			refreshCityList();
			String defaultWoeid = WeatherDataUtil.getInstance()
					.getDefaultCityWoeid(CityMangerActivity.this);
			if (defaultWoeid.isEmpty() && !mWeatherInfoList.isEmpty()) {
				WeatherDataUtil
						.getInstance()
						.updateDefaultCityWoeid(
								CityMangerActivity.this,
								mWeatherInfoList.get(0).isGps() ? WeatherDataUtil.DEFAULT_WOEID_GPS
										: mWeatherInfoList.get(0).getWoeid());
				startUpdateService(CityMangerActivity.this,
						WeatherWidget.ACTION_UPDATE,
						AppWidgetManager.INVALID_APPWIDGET_ID);
			}
			if (mWeatherInfoList.size() == 1) {
				WeatherDataUtil.getInstance().setRefreshTime(
						CityMangerActivity.this, System.currentTimeMillis());
				setRefreshTime();
			}
			WeatherDataUtil.getInstance().setNeedUpdateMainUI(
					CityMangerActivity.this, true);

			break;

		default:
			break;
		}

	}

	private void initUI() {
		addCity = (ImageView) findViewById(R.id.add_city);
		addCity.setOnClickListener(onClickListener);

		location = (ImageView) findViewById(R.id.location);
		location.setOnClickListener(onClickListener);

		allRefresh = (ImageView) findViewById(R.id.all_refresh);
		allRefresh.setOnClickListener(onClickListener);
		allRefreshTimeText = (TextView) findViewById(R.id.latest_all_refresh_time);
		cityList = (ListView) findViewById(R.id.city_list);
	}

	View.OnClickListener onClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.all_refresh:
				SharedPreferences sp = getSharedPreferences(
						MainActivity.SETTINGS_SP, Context.MODE_PRIVATE);
				if (sp.getBoolean(
						MainActivity.SETTINGS_WIFI_ONLY,
						getResources().getBoolean(
								R.bool.config_wifi_only_enable))) {
					if (Utils.isNetworkTypeWifi(CityMangerActivity.this)) {
						showLoadingProgress(true);
						if (!WeatherApp.mModel.refreshAllWeather()) {
							showLoadingProgress(false);
						}
					} else {
						Toast.makeText(CityMangerActivity.this,
								R.string.toast_wifi_only_mode,
								Toast.LENGTH_SHORT).show();
						Log.d(TAG,
								"CityManager-SETTINGS_WIFI_ONLY, network type NOT WIFI.");
					}
				} else {
					if (Utils.isNetworkAvailable(CityMangerActivity.this)) {
						showLoadingProgress(true);
						if (!WeatherApp.mModel.refreshAllWeather()) {
							showLoadingProgress(false);
						}
					} else {
						Toast.makeText(CityMangerActivity.this,
								R.string.toast_net_inavailable,
								Toast.LENGTH_SHORT).show();
						Log.d(TAG,
								"CityManager-Refresh BTN, network NOT available");
					}
				}

				break;
			case R.id.add_city:
				if (items.size() < CITY_COUNT_MAX) {
					Intent intent = new Intent(CityMangerActivity.this,
							AddCityActivity.class);
					intent.putExtra("from_manager", true);
					startActivityForResult(intent, REQUEST_CODE_CITY_ADD);
				} else {
					Toast.makeText(
							CityMangerActivity.this,
							getResources().getString(R.string.city_max_toast,
									CITY_COUNT_MAX), Toast.LENGTH_SHORT).show();
				}
				break;
			case R.id.location:
				if (!Utils.isNetworkAvailable(CityMangerActivity.this)) {
					Toast.makeText(CityMangerActivity.this,
							R.string.toast_net_inavailable, Toast.LENGTH_SHORT)
							.show();
					Log.d(TAG, "Location BTN, network NOT available");
					return;
				}

				if (Build.VERSION.SDK_INT >= 23) {
					boolean hasPermission = PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
					Log.d(TAG, "ACCESS_COARSE_LOCATION, hasPermission="
							+ hasPermission);
					if (!hasPermission) {
						requestPermissions(
								new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
								REQUEST_CODE_PERMISSION);
						return;
					}

				}

				if (Utils.isAPGSOpen(CityMangerActivity.this)) {
					Log.d(TAG, "CityManager - AGPS Opened");
					if (!mGeoHelper.isWorking()) {
						showLoadingProgress(true);
						mGeoHelper.queryLocationCityInfo();
					}
				} else {
					Log.d(TAG, "CityManager - AGPS Not Open");
					Intent intent = new Intent(
							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(intent);
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onStop() {
		super.onStop();
		isStoped = true;
		WeatherApp.mModel.stopQueryCity();

		mGeoHelper.stopQuery();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mCityManagerReceiver);
		mCityManagerReceiver = null;
		mGeoHelper.onDestory();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && isAutoGps) {
			isAutoGps = false;
			WeatherApp.mModel.stopQueryCity();
			mGeoHelper.stopQuery();
			showLoadingProgress(false);
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void refreshCityList() {
		Log.d(TAG, "refreshCityList");
		SharedPreferences sp = getSharedPreferences(MainActivity.SETTINGS_SP,
				Context.MODE_PRIVATE);
		isTemperatureC = sp.getBoolean(MainActivity.SETTINGS_TEMPERATURE_TYPE,
				getResources().getBoolean(R.bool.config_default_temperature_c));
		temperatureType = isTemperatureC ? getResources().getString(
				R.string.temperature_c) : getResources().getString(
				R.string.temperature_f);
		gpsItem = null;
		items.clear();
		CityListItem item;
		if (mSoftWeatherInfoList == null) {
			mSoftWeatherInfoList = new WeakReference<List<WeatherInfo>>(
					WeatherApp.mModel.getWeatherInfos());
			mWeatherInfoList = mSoftWeatherInfoList.get();
		}
		// mWeatherInfoList = WeatherApp.mModel.getWeatherInfos();

		for (WeatherInfo weatherInfo : mWeatherInfoList) {
			item = new CityListItem(weatherInfo.getWoeid(),
					weatherInfo.getName());
			item.setText(weatherInfo.getCondition().getText());
			item.setWeather(isTemperatureC ? weatherInfo.getCondition()
					.getTemp() : weatherInfo.getCondition().getTempF());
			item.setGPs(weatherInfo.isGps());
			if (weatherInfo.isGps()) {
				gpsItem = item;
			} else {
				items.add(item);
			}
		}
		if (null != gpsItem) {
			items.add(0, gpsItem);
		}

		if (items.size() < CITY_COUNT_MAX) {
			addCity.setImageResource(R.drawable.add_city);
		} else {
			addCity.setImageResource(R.drawable.add_city_disable);
		}

		mCityListAdapter = new CityListAdapter(CityMangerActivity.this, items);
		cityList.setAdapter(mCityListAdapter);
		cityList.setOnItemLongClickListener(longClickListener);

		setRefreshTime();
	}

	private void setRefreshTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date mDate = new Date(WeatherDataUtil.getInstance().getRefreshTime(
				CityMangerActivity.this));
		String refreshTime = getResources().getString(R.string.refresh_time,
				format.format(mDate));
		allRefreshTimeText.setText(refreshTime);
	}

	AdapterView.OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			Log.d(TAG, "List-onItemLongClick:" + position);
			new DeleteCityFragment(items, position).show(getFragmentManager(), FRAGMENT_TAG_DELETE_CITY);
			return true;
		}
	};

	private void startUpdateService(Context context, String action, int widgetId) {
		Log.d(TAG, "CityManager - startUpdateService");
		Intent intent = new Intent(context, UpdateWidgetService.class);
		intent.setAction(action);
		intent.setData(Uri.parse(String.valueOf(widgetId)));
		context.startService(intent);
	}

	private void showLoadingProgress(boolean show) {
		Log.d(TAG, "showLoadingProgress:" + show);
		if (show) {
			mLoadingProgressFragment.show(getFragmentManager(), FRAGMENT_TAG_LOADING);
			location.setEnabled(false);
			addCity.setEnabled(false);
			allRefresh.setEnabled(false);
		} else {
			if (mLoadingProgressFragment != null 
					&& mLoadingProgressFragment.getDialog() != null) {
				if (mLoadingProgressFragment.getDialog().isShowing()) {
					mLoadingProgressFragment.dismiss();
				}
			}
			location.setEnabled(true);
			addCity.setEnabled(true);
			allRefresh.setEnabled(true);
		}
	}
	
	class ItemChildClickListener implements View.OnClickListener {

		private int index;
		private int position;
		
		public ItemChildClickListener(int index, int position) {
			this.index = index;
			this.position = position;
		}
		
		@Override
		public void onClick(View v) {
			Message msg = new Message();
			msg.what = index;
			msg.arg1 = position;
			H.sendMessage(msg);
		}
		
	}

	class CityListAdapter extends BaseAdapter {
		private List<CityListItem> mList;
		private LayoutInflater mInflater;

		public CityListAdapter(Context context, List<CityListItem> mList) {
			super();
			this.mList = mList;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder mHolder;

			if (convertView == null || convertView.getTag() == null) {
				// Time consuming 1 -- inflate
				convertView = mInflater.inflate(R.layout.city_list_item_1, null);
				mHolder = new ViewHolder();
				// Time consuming 2 -- findViewById
				mHolder.name = (TextView) convertView.findViewById(R.id.name);
				mHolder.gpsIcon = (ImageView) convertView
						.findViewById(R.id.ic_gps);
				mHolder.delete = (ImageView) convertView
						.findViewById(R.id.iv_delete);
				convertView.setTag(mHolder);
			} else {
				mHolder = (ViewHolder) convertView.getTag();
			}
			CityListItem bean = mList.get(position);
			mHolder.name.setText(bean.name);
			mHolder.gpsIcon
					.setVisibility(bean.isGps ? View.VISIBLE : View.GONE);
			mHolder.delete.setOnClickListener(new ItemChildClickListener(CLICK_INDEX_DELETE, position));
			return convertView;
		}

		// Google I/O
		class ViewHolder {
			public TextView name;
			public ImageView gpsIcon;
			public ImageView delete;
		}
	}

	class CityListItem {
		public String woeid;
		public String name;
		public int imgRes;
		public String text;
		public String weather;
		public boolean isGps;

		public CityListItem(String woeid, String name) {
			super();
			this.woeid = woeid;
			this.name = name;
		}

		public void setImgRes(int imgRes) {
			this.imgRes = imgRes;
		}

		public void setWeather(String weather) {
			this.weather = weather;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setGPs(boolean isGps) {
			this.isGps = isGps;
		}
	}

	class LocationFindFragment extends DialogFragment {
		private CityInfo cityInfo;
		
		public LocationFindFragment(CityInfo cityInfo) {
			this.cityInfo = cityInfo;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			String title = getResources().getString(R.string.update_location_city,
					cityInfo.getName());
			builder.setTitle(title);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					showLoadingProgress(true);
					if (!WeatherApp.mModel.addWeatherByCity(
							cityInfo, true)) {
						showLoadingProgress(false);
					}
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}
	}
	
	class DeleteCityFragment extends DialogFragment {
		
		private List<CityListItem> items;
		private int position;
		
		public DeleteCityFragment(List<CityListItem> items, int position) {
			this.items = items;
			this.position = position;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			String title = getResources().getString(R.string.delete_city,
					items.get(position).name);
			builder.setTitle(title);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					CityListItem item = items
							.get(position);
					for (WeatherInfo info : mWeatherInfoList) {
						if (item.isGps && info.isGps()) {
							WeatherApp.mModel
									.deleteWeatherInfo(info);
							break;
						} else if (!item.isGps
								&& !info.isGps()
								&& (info.getWoeid()
										.equals(item.woeid))) {
							WeatherApp.mModel
									.deleteWeatherInfo(info);
							break;
						}
					}

					String defaultWoeid = WeatherDataUtil
							.getInstance().getDefaultCityWoeid(
									CityMangerActivity.this);

					if ((!item.isGps
							&& defaultWoeid.equals(items
									.get(deletePosition).woeid) && !defaultWoeid
								.equals(WeatherDataUtil.DEFAULT_WOEID_GPS))
							|| (item.isGps && defaultWoeid
									.equals(WeatherDataUtil.DEFAULT_WOEID_GPS))) {
						WeatherDataUtil
								.getInstance()
								.updateDefaultCityWoeid(
										CityMangerActivity.this,
										"");

						String firstWoeid = WeatherApp.mModel
								.getFirstWeatherFromDB();
						if (null != firstWoeid) {
							WeatherDataUtil
									.getInstance()
									.updateDefaultCityWoeid(
											CityMangerActivity.this,
											firstWoeid);
							//liuxiaochen start
							if(WeatherApp.mModel.notifyIsExist()){
								defaultWoeid = firstWoeid;
								for (WeatherInfo info:mWeatherInfoList) {
									if (!info.isGps()
											&& defaultWoeid.equals(info.getWoeid()) && !defaultWoeid
												.equals(WeatherDataUtil.DEFAULT_WOEID_GPS)) {
										WeatherApp.mModel.refreshNotification(info);
										break;
									}else if(info.isGps() && defaultWoeid
											.equals(WeatherDataUtil.DEFAULT_WOEID_GPS)){
										WeatherApp.mModel.refreshNotification(info);
										break;
									}
								}
							} else{
								if(WeatherApp.mModel.notifyIsExist()){
									if(mWeatherInfoList.size() == 0){
										WeatherApp.mModel.setDefaultNotification();
									}
								}
							}
								
							//liuxiaochen  end 
						}

						startUpdateService(
								CityMangerActivity.this,
								WeatherWidget.ACTION_UPDATE,
								AppWidgetManager.INVALID_APPWIDGET_ID);
					}

					refreshCityList();
					WeatherDataUtil.getInstance()
							.setNeedUpdateMainUI(
									CityMangerActivity.this,
									true);
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}
	}
}
