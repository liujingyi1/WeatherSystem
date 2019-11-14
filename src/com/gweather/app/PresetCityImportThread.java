package com.gweather.app;

import com.gweather.app.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class PresetCityImportThread extends Thread {
	
	private static boolean isRunningCheck = false;
	
	private static final String URI_CITY = "content://com.gweather.app.weather/gpresetcity";
	private static final String URI_WEATHRE = "content://com.gweather.app.weather/gweather";
	
	private Context mContext;
	
	private String mInsertHotCityWoeid[] = null;
	private String mInsertHotCity[] = null;
	private String mInsertCityWoeid[] = null;
	private String mInsertCity[] = null;
	
	private int hotCityCount = 0;
	private int cityCount = 0;
	

	public PresetCityImportThread(Context context) {
		mContext = context;
		
		mInsertHotCityWoeid = mContext.getResources().getStringArray(R.array.hot_citys_woeid);
		mInsertHotCity = mContext.getResources().getStringArray(R.array.hot_citys);
		mInsertCityWoeid = mContext.getResources().getStringArray(R.array.citys_woeid);
		mInsertCity = mContext.getResources().getStringArray(R.array.citys);
		
		if (mInsertHotCityWoeid != null && mInsertHotCity != null
				&& mInsertHotCityWoeid.length == mInsertHotCity.length) {
			hotCityCount = mInsertHotCityWoeid.length;
		}
		if (mInsertCityWoeid != null && mInsertCity != null
				&& mInsertCityWoeid.length == mInsertCity.length) {
			cityCount = mInsertCityWoeid.length;
		}
	}

	

    @Override
    public void run() {
    	if (hotCityCount != 0 || cityCount != 0) {
    		importPresetCity();
    	}
    }
    
    private void importPresetCity() {
    	if (isRunningCheck) {
    		Log.i("jingyi", "isRunningCheck="+isRunningCheck);
    		return;
    	}
    	isRunningCheck = true;
    	
		String where = WeatherProvider.WOEID+"=" + 
						(hotCityCount != 0 ? mInsertHotCityWoeid[0] : mInsertCityWoeid[0]);
        Cursor cityCursor = mContext.getContentResolver().query(Uri.parse(URI_CITY), new String[] {
        		WeatherProvider.WOEID, WeatherProvider.NAME
        }, where, null, null);
        
		try {
			if (cityCursor != null && cityCursor.getCount() > 0) {
				return;
			} else {
		    	for (int i = 0; i < hotCityCount; i++) {
		    		Log.i("jingyi", "i="+i+" mInsertHotCityWoeid[i]="+mInsertHotCityWoeid[i]);

    				int select = getSelect(mInsertHotCityWoeid[i]);
    				
    				ContentValues values = new ContentValues();
    				values.put(WeatherProvider.WOEID, mInsertHotCityWoeid[i]);
    				values.put(WeatherProvider.NAME, mInsertHotCity[i]);
    				values.put(WeatherProvider.IS_HOT_CITY, 1);
    				values.put(WeatherProvider.IS_SELECT, select);
    				mContext.getContentResolver().insert(Uri.parse(URI_CITY), values);

		    	}
		    	
		    	for (int i = 0; i < cityCount; i++) {
	
					int select = getSelect(mInsertCityWoeid[i]);
					
					ContentValues values = new ContentValues();
					values.put(WeatherProvider.WOEID, mInsertCityWoeid[i]);
					values.put(WeatherProvider.NAME, mInsertCity[i]);
					values.put(WeatherProvider.IS_HOT_CITY, 0);
					values.put(WeatherProvider.IS_SELECT, select);
					values.put(WeatherProvider.SORY_KEY, (String.valueOf(mInsertCity[i].charAt(0)).toUpperCase()));
					mContext.getContentResolver().insert(Uri.parse(URI_CITY), values);
		    	}
	
			}
		} finally {
			if (cityCursor != null) {
				cityCursor.close();
			}
		}

    	
    	isRunningCheck = false;
    }

    private int getSelect(String woeid) {
		int select = 0;
		String defaultWoeid = mContext.getResources().getString(R.string.default_woeid);
		if (defaultWoeid.compareTo(woeid) == 0) {
    		String where1 = WeatherProvider.WOEID + "=" + woeid;
            Cursor cur = mContext.getContentResolver().query(Uri.parse(URI_WEATHRE), new String[] {
            		WeatherProvider.WOEID
            }, where1, null, null);
            
            if(cur != null) {
            	if (cur.getCount() > 0) {
            		select = 1;
            	}
            	cur.close();
            }
		}
		return select;
    }
    
}
