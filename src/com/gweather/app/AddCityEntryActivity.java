package com.gweather.app;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.content.CursorLoader;

public class AddCityEntryActivity extends BaseActivity 
				implements LoaderCallbacks<Cursor>{
	
	private static final String URI_CITY = "content://com.gweather.app.weather/gpresetcity";

	protected int COLUMN_ID = 0;
	protected int COLUMN_WOEID = 1;
	protected int COLUMN_NAME = 2;
	protected int COLUMN_SORY_KEY = 3;
	protected int COLUMN_IS_HOT_CITY = 4;
	protected int COLUMN_IS_SELECT = 5;
	
	public AddCityEntryActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri baseUri;
		baseUri = Uri.parse(URI_CITY);
		
		String[] projection = {
				WeatherProvider._ID,
				WeatherProvider.WOEID,
				WeatherProvider.NAME,
				WeatherProvider.SORY_KEY,
				WeatherProvider.IS_HOT_CITY,
				WeatherProvider.IS_SELECT,
		};
		
		String orderBy = WeatherProvider.NAME + " COLLATE LOCALIZED ASC";
		
		Loader<Cursor> loader = new CursorLoader(getApplicationContext(), 
				baseUri, 
				projection, 
				null, 
				null, orderBy);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.i("jingyi", "onLoadFinished------------");
		Log.i("jingyi", "data.getCount()="+data.getCount());
		if (data != null && data.getCount() == 0) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		
	}

}
