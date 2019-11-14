package com.gweather.app;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/*import com.amap.api.location.AMapLocation;
 import com.amap.api.location.AMapLocationClient;
 import com.amap.api.location.AMapLocationClientOption;
 import com.amap.api.location.AMapLocationListener;
 import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;*/
import com.gweather.utils.CityNameXMLParser;
import com.gweather.utils.Utils;
import com.gweather.utils.WebAccessTools;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class GeoHelper /* implements AMapLocationListener */{
	private static final String TAG = "Gweather.GeoHelper";

	private static final String URL_QUERY_LOCATION_PART1 = "https://query.yahooapis.com/v1/public/yql?q=select+*+from+geo.places+where+woeid+in+(select+place.woeid+from+flickr.places+where+api_key="
			+ Utils.KEY_PUBLIC + "and+lat='";
	private static final String URL_QUERY_LOCATION_PART2 = "'+and+lon='";
	private static final String URL_QUERY_LOCATION_PART3 = "')+and+lang='en-US'";;

	private static final String URL_QUERY_CITY_PART1 = "http://query.yahooapis.com/v1/public/yql?q=select+*+from+geo.places+where+text='";
	private static final String URL_QUERY_CITY_PART2 = "*'+and+lang='en-US'";

	private static final int FAIL_GET_LOCATION = 0;
	private static final int FAIL_GET_CITYINFO = 1;

	private static final int QUERY_ADDRESS_NAME_LV_INVALID = -1;
	private static final int QUERY_ADDRESS_NAME_LV_LOCALITY = 1;
	private static final int QUERY_ADDRESS_NAME_LV_SUBADMIN = 2;
	private static final int QUERY_ADDRESS_NAME_LV_ADMIN = 3;
	private static final int QUERY_ADDRESS_NAME_LV_MAX = QUERY_ADDRESS_NAME_LV_ADMIN;

	private static final int MSG_QUERY_ADDRESS = 101;
	private static final int MSG_CITY_IN_DB = 102;

	private boolean TEST = false;

	private Context context;
	/*
	 * private AMapLocationClient locationClient = null; private
	 * AMapLocationClientOption locationOption = null;
	 */
	private LocationManager mLocationManager;
	private Thread mGetLocationThread = null;
	private QueryLocationTask mQueryLocationTask;
	private Geocoder mGeocoder;
	private ReverseGeocoderTask mReverseGeocoderTask;
	private QueryCityTask mQueryCityTask;

	private GPSCityInfoModel gpsModel;

	private CityInfo gpsCityInfo;
	private ArrayList<CityInfo> cityInfos = new ArrayList<CityInfo>();
	private List<CityInfo> mGpsCityInfoList = new ArrayList<CityInfo>();

	private OnQueryCityInfoFinishedListener onQueryCityInfoFinishedListener;

	private boolean isWorking = false;
	private int queryAddressNameLevel = QUERY_ADDRESS_NAME_LV_INVALID;

	private Location location;
	private Address address;

	public void setQueryAddressArg(Location location, Address address) {
		this.location = location;
		this.address = address;
	}

	private Handler H = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_QUERY_ADDRESS:
				queryAndCheckCity(location, address);
				break;
			case MSG_CITY_IN_DB:
				if (null != onQueryCityInfoFinishedListener) {
					onQueryCityInfoFinishedListener.querySuccessed(gpsCityInfo);
					isWorking = false;
				}
				break;
			default:
				break;
			}
		};
	};

	public interface OnQueryCityInfoFinishedListener {
		void querySuccessed(CityInfo cityInfo);

		void queryFailed(int reason);
	}

	public void setOnQueryCityNameFinishedListener(
			OnQueryCityInfoFinishedListener listener) {
		onQueryCityInfoFinishedListener = listener;
	}

	public GeoHelper(Context context) {
		this.context = context;
		mGeocoder = new Geocoder(context, Locale.ENGLISH);
		gpsModel = new GPSCityInfoModel(context);
		// AMapInit(context);
	}

	public void queryLocationCityInfo() {
		gpsCityInfo = null;
		isWorking = true;
		getLocation();
	}

	public void stopQuery() {
		clearLocationThing();

		if (null != mReverseGeocoderTask
				&& mReverseGeocoderTask.getStatus() == AsyncTask.Status.RUNNING) {
			mReverseGeocoderTask.cancel(true);
		}

		if (null != mQueryLocationTask
				&& mQueryLocationTask.getStatus() == AsyncTask.Status.RUNNING) {
			mQueryLocationTask.cancel(true);
		}

		if (null != mQueryCityTask
				&& mQueryCityTask.getStatus() == AsyncTask.Status.RUNNING) {
			mQueryCityTask.cancel(true);
		}

	}

	public void onDestory() {
		// AMapDestroy();
	}

	public boolean isWorking() {
		return isWorking;
	}

	private void getLocation() {
		Log.i(TAG, "getLocation");
		if (mGetLocationThread == null) {
			mGetLocationThread = new Thread(new GetLocationRunnable());
			mGetLocationThread.start();
			/*
			 * AMapInit(context); AMapStart();
			 */
		} else {
			Log.w(TAG, "LocationThread is exit");
			clearLocationThing();
			if (onQueryCityInfoFinishedListener != null) {
				onQueryCityInfoFinishedListener.queryFailed(FAIL_GET_LOCATION);
				isWorking = false;
			}
		}
	}

	class GetLocationRunnable implements Runnable {
		@Override
		public void run() {
			String contextService = Context.LOCATION_SERVICE;
			mLocationManager = (LocationManager) context
					.getSystemService(contextService);
			if (mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER) == null) {
				Log.d(TAG, "NETWORK_PROVIDER is NULL");
			} else {
				Log.d(TAG, "NETWORK_PROVIDER is OK");
				
                List<String> providers = mLocationManager.getAllProviders();
                boolean hasGPS = providers.contains(LocationManager.GPS_PROVIDER);
				
				Looper.prepare();
				String provider = LocationManager.NETWORK_PROVIDER;
				mLocationManager.requestLocationUpdates(provider, 5000, 1,
						netLocationListener, Looper.myLooper());

				if (hasGPS) {
					provider = LocationManager.GPS_PROVIDER;
				    mLocationManager.requestLocationUpdates(provider, 5000, 1,
				    		gpsLocationListener, Looper.myLooper());
				    mLocationManager.addGpsStatusListener(gpsStatusListener);
				}
				Looper.loop();
			}
		}

	}

	private final LocationListener netLocationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(TAG, "netLocationListener - onStatusChanged");
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "netLocationListener - onProviderEnabled");
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "netLocationListener - onProviderDisabled");
		}

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "netLocationListener - onLocationChanged");
			updateLocation(location);
		}
	};

	private final LocationListener gpsLocationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(TAG, "gpsLocationListener - onStatusChanged");
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "gpsLocationListener - onProviderEnabled");
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "gpsLocationListener - onProviderDisabled");
		}

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "gpsLocationListener - onLocationChanged");
			updateLocation(location);
		}
	};

	private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {

		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
			case GpsStatus.GPS_EVENT_STARTED:
				Log.d(TAG, "onGpsStatusChanged - GPS_EVENT_STARTED");
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.d(TAG, "onGpsStatusChanged - GPS_EVENT_STOPPED");
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Log.d(TAG, "onGpsStatusChanged - GPS_EVENT_FIRST_FIX");
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.d(TAG, "onGpsStatusChanged - GPS_EVENT_SATELLITE_STATUS");
				break;

			default:
				break;
			}

		}
	};

	private void updateLocation(double latitude, double longitude) {
		Location location = new Location(LocationManager.NETWORK_PROVIDER);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		updateLocation(location);
	}

	private void updateLocation(Location location) {
		if (location != null) {
//			if (TEST) {
//				location.setLatitude(33.718151);
//				location.setLongitude(73.060547);
//			}
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			Log.d(TAG, "updateLocation, lat=" + lat + ", lng=" + lng);

			gpsCityInfo = hasLocated(lat, lng);
			if (null != gpsCityInfo) {
				// Find city info in DB
				Log.d(TAG, "Location not changed");
				H.sendEmptyMessage(MSG_CITY_IN_DB);
			} else {
				queryCityByLocation(location, cityInfos);
			}

			clearLocationThing();
			/*
			 * AMapStop(); AMapDestroy();
			 */
		}
	}

	private CityInfo hasLocated(double lat, double lng) {
		Log.d(TAG, "hasLocated, lat=" + lat + ", lng=" + lng);

		if (mGpsCityInfoList.isEmpty()) {
			gpsModel.loadGpsCityInfos(mGpsCityInfoList);
		}

		Log.d(TAG, "gps cityInfo count:" + mGpsCityInfoList.size());
		if (mGpsCityInfoList.isEmpty()) {
			return null;
		} else {
			for (CityInfo cityInfo : mGpsCityInfoList) {
				Log.d(TAG, "woeid:" + cityInfo.getWoeid() + ", lat="
						+ cityInfo.getLocationInfo().getLat() + ", lon="
						+ cityInfo.getLocationInfo().getLon());
				if (cityInfo.getWoeid() != null
						&& !cityInfo.getWoeid().isEmpty()
						&& ((cityInfo.getLocationInfo().getLat() == lat && cityInfo
								.getLocationInfo().getLon() == lng) || isPointInLocationInfo(
								lat, lng, cityInfo.getLocationInfo()))) {

					return cityInfo;

				}
			}
			return null;
		}
	}

	private void clearLocationThing() {
		if (mGetLocationThread != null) {
			mGetLocationThread.interrupt();
			mGetLocationThread = null;
		}

		if (mLocationManager != null) {
			mLocationManager.removeUpdates(netLocationListener);
			mLocationManager.removeUpdates(gpsLocationListener);
			mLocationManager.removeGpsStatusListener(gpsStatusListener);
			mLocationManager = null;
		}

		/*
		 * AMapStop(); AMapDestroy();
		 */
	}

	private void queryCityByLocation(Location location,
			ArrayList<CityInfo> cityInfos) {
		cityInfos.clear();

		if (null != mQueryLocationTask
				&& mQueryLocationTask.getStatus() == AsyncTask.Status.RUNNING) {
			mQueryLocationTask.cancel(true);
		}

		mQueryLocationTask = new QueryLocationTask(location, cityInfos);
		mQueryLocationTask.execute();
	}

	class QueryLocationTask extends AsyncTask<Void, Void, Void> {
		private Location location;
		private ArrayList<CityInfo> mCityInfos;

		public QueryLocationTask(Location location,
				ArrayList<CityInfo> mCityInfos) {
			this.location = location;
			this.mCityInfos = mCityInfos;
		}

		@Override
		protected Void doInBackground(Void... params) {
			String url = URL_QUERY_LOCATION_PART1 + location.getLatitude()
					+ URL_QUERY_LOCATION_PART2 + location.getLongitude()
					+ URL_QUERY_LOCATION_PART3;
			if (TEST) {
				mCityInfos.clear();
			} else {
				String content = new WebAccessTools(context).getWebContent(url);
				parseCity(content, mCityInfos);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mCityInfos.size() > 0) {
				Log.d(TAG, "Get gps city info Success");
				gpsCityInfo = mCityInfos.get(0);
				gpsCityInfo.getLocationInfo().setLat(location.getLatitude());
				gpsCityInfo.getLocationInfo().setLon(location.getLongitude());

				gpsModel.saveGpsCityInfoToDB(gpsCityInfo);

				if (null != onQueryCityInfoFinishedListener) {
					onQueryCityInfoFinishedListener.querySuccessed(gpsCityInfo);
					mGpsCityInfoList.add(gpsCityInfo);
					isWorking = false;
				}
			} else {
				Log.w(TAG, "Get gps city info Fail");
				if (null != onQueryCityInfoFinishedListener) {
					// onQueryCityInfoFinishedListener.queryFailed(FAIL_GET_CITYINFO);
					// isWorking = false;
					mReverseGeocoderTask = new ReverseGeocoderTask(location);
					if (null != mReverseGeocoderTask
							&& mReverseGeocoderTask.getStatus() == AsyncTask.Status.RUNNING) {
						mReverseGeocoderTask.cancel(true);
					}
					mReverseGeocoderTask.execute();
				}
			}
		}
	}

	private void parseCity(String content, List<CityInfo> mCityInfos) {
		mCityInfos.clear();
		if (null == content || content.isEmpty()) {
			Log.w(TAG, "parseCity content is Empty");
			return;
		}

		SAXParserFactory mSAXParserFactory = SAXParserFactory.newInstance();
		try {
			SAXParser mSAXParser = mSAXParserFactory.newSAXParser();
			XMLReader mXmlReader = mSAXParser.getXMLReader();
			CityNameXMLParser handler = new CityNameXMLParser(mCityInfos);
			mXmlReader.setContentHandler(handler);
			StringReader stringReader = new StringReader(content);
			InputSource inputSource = new InputSource(stringReader);
			mXmlReader.parse(inputSource);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ReverseGeocoderTask extends AsyncTask<Void, Void, Address> {

		private Location location;

		public ReverseGeocoderTask(Location location) {
			this.location = location;
		}

		@Override
		protected Address doInBackground(Void... params) {
			try {
				// location.setLatitude(33.718151);
				// location.setLongitude(73.060547);
				Log.i(TAG, "Try to get address");
				List<Address> addresses = mGeocoder.getFromLocation(
						location.getLatitude(), location.getLongitude(), 1);
				if (addresses.size() > 0) {
					Address address = addresses.get(0);
					Log.i(TAG, "address:" + address);
					return address;
				} else {
					Log.w(TAG, "addresses is Empty");
					return null;
				}
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "IOException, " + e);
				return null;
			}
		}

		@Override
		protected void onPostExecute(Address result) {
			super.onPostExecute(result);
			if (result == null) {
				onQueryCityInfoFinishedListener.queryFailed(FAIL_GET_CITYINFO);
				isWorking = false;
			} else {
				queryAddressNameLevel = QUERY_ADDRESS_NAME_LV_INVALID;
				queryAndCheckCity(location, result);
			}
		}
	}

	public void queryAndCheckCity(Location location, Address address) {
		if (null != mQueryCityTask
				&& mQueryCityTask.getStatus() == AsyncTask.Status.RUNNING) {
			mQueryCityTask.cancel(true);
		}
		mQueryCityTask = new QueryCityTask(cityInfos, location, address);
		mQueryCityTask.execute();
	}

	class QueryCityTask extends AsyncTask<String, Void, Void> {
		private List<CityInfo> mCityInfos;
		private Location location;
		private Address address;
		private String name;
		private double lat;
		private double lng;

		public QueryCityTask(List<CityInfo> cityInfos, Location location,
				Address address) {
			mCityInfos = cityInfos;
			this.location = location;
			this.address = address;
			if (address.getLocality() != null
					&& queryAddressNameLevel < QUERY_ADDRESS_NAME_LV_LOCALITY) {
				queryAddressNameLevel = QUERY_ADDRESS_NAME_LV_LOCALITY;
				name = address.getLocality();
			} else if (address.getSubAdminArea() != null
					&& queryAddressNameLevel < QUERY_ADDRESS_NAME_LV_SUBADMIN) {
				queryAddressNameLevel = QUERY_ADDRESS_NAME_LV_SUBADMIN;
				name = address.getSubAdminArea();
			} else if (address.getAdminArea() != null
					&& queryAddressNameLevel < QUERY_ADDRESS_NAME_LV_ADMIN) {
				queryAddressNameLevel = QUERY_ADDRESS_NAME_LV_ADMIN;
				name = address.getAdminArea();
			}
			lat = location.getLatitude();
			lng = location.getLongitude();
			Log.d(TAG, "QueryCityTask, lat:" + lat + ", lng:" + lng);
		}

		@Override
		protected Void doInBackground(String... params) {

			if (name != null && !name.isEmpty()) {
				Log.i(TAG, "Try to query-" + name);
				String url = URL_QUERY_CITY_PART1 + name + URL_QUERY_CITY_PART2;
				String content = new WebAccessTools(context).getWebContent(url);
				parseCity(content, mCityInfos);
			} else {
				mCityInfos.clear();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mCityInfos.isEmpty()) {
				onQueryCityInfoFinishedListener.queryFailed(FAIL_GET_CITYINFO);
				isWorking = false;
			} else {
				for (CityInfo info : mCityInfos) {
					Log.i(TAG, "checking-" + info.toString());
					if (!info.getCountry().equals(address.getCountryName())) {
						continue;
					}
	
					Log.d(TAG, "LocationInfo:" + info.getLocationInfo().toString());
					if (isPointInLocationInfo(lat, lng, info.getLocationInfo())) {
						Log.i(TAG, "Find Location");
						String cityInfoName = info.getAdmin3() != null ? info
								.getAdmin3() : (info.getAdmin2() != null ? info
								.getAdmin2() : info.getAdmin1());
						Log.i(TAG, "cityInfoName:" + cityInfoName);
						if (!cityInfoName.isEmpty()
								&& (cityInfoName.contains(name) || name
										.contains(cityInfoName))) {
							gpsCityInfo = mCityInfos.get(0);
							gpsCityInfo.getLocationInfo().setLat(
									location.getLatitude());
							gpsCityInfo.getLocationInfo().setLon(
									location.getLongitude());
	
							if (null != onQueryCityInfoFinishedListener) {
								onQueryCityInfoFinishedListener
										.querySuccessed(gpsCityInfo);
								mGpsCityInfoList.add(gpsCityInfo);
								isWorking = false;
							}
							break;
						}
						
					}
				}
	
				if (gpsCityInfo == null) {
					Log.i(TAG, "Try failed");
					if (queryAddressNameLevel < QUERY_ADDRESS_NAME_LV_MAX) {
						queryAndCheckCity(location, address);
						setQueryAddressArg(location, address);
						H.sendEmptyMessageDelayed(MSG_QUERY_ADDRESS, 100);
					} else {
						onQueryCityInfoFinishedListener
								.queryFailed(FAIL_GET_CITYINFO);
						isWorking = false;
					}
				}
			}
		}
	}

	private boolean isPointInLocationInfo(double lat, double lng,
			LocationInfo locationInfo) {
		boolean latOK = false;
		boolean lngOK = false;

		if (locationInfo.getNorthEastLat() > locationInfo.getSouthWestLat()) {
			if (lat <= locationInfo.getNorthEastLat()
					&& lat >= locationInfo.getSouthWestLat()) {
				latOK = true;
			}
		} else {
			if (lat >= locationInfo.getNorthEastLat()
					&& lat <= locationInfo.getSouthWestLat()) {
				latOK = true;
			}
		}

		if (locationInfo.getNorthEastLon() > locationInfo.getSouthWestLon()) {
			if (lng <= locationInfo.getNorthEastLon()
					&& lng >= locationInfo.getSouthWestLon()) {
				lngOK = true;
			}
		} else {
			if (lng >= locationInfo.getNorthEastLon()
					&& lng <= locationInfo.getSouthWestLon()) {
				lngOK = true;
			}
		}

		return (latOK && lngOK);
	}

	// AMap Start
	/*
	 * boolean supportAmap = false;
	 * 
	 * private void AMapInit(Context context) { supportAmap =
	 * context.getResources().getBoolean(R.bool.config_use_amap); if
	 * (supportAmap) { AMapDestroy();
	 * 
	 * locationClient = new AMapLocationClient(context); locationOption = new
	 * AMapLocationClientOption();
	 * locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
	 * locationClient.setLocationListener(this); } }
	 * 
	 * private void AMapStart() { if (supportAmap) {
	 * locationClient.setLocationOption(locationOption);
	 * locationClient.startLocation(); } }
	 * 
	 * private void AMapStop() { if (supportAmap) { if (null != locationClient)
	 * { locationClient.stopLocation(); } } }
	 * 
	 * private void AMapDestroy() { if (supportAmap) { if (null !=
	 * locationClient) { locationClient.onDestroy(); locationClient = null;
	 * locationOption = null; } } }
	 * 
	 * @Override public void onLocationChanged(AMapLocation location) { double
	 * lat = location.getLatitude(); double lon = location.getLongitude();
	 * Log.d(TAG, "AMap - lat=" + lat + ", lon=" + lon); if (lat == 0.0 && lon
	 * == 0.0) { Log.d(TAG, "AMap - invalid value"); } else {
	 * updateLocation(lat, lon); } }
	 */
	// AMap End
}
