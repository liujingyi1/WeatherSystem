package com.gweather.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gweather.utils.Utils;
import com.gweather.view.CityClickListener;
import com.gweather.view.MySectionIndexer;
import com.gweather.view.PinnedHeaderListView;
import com.gweather.view.PinterestAdapter;
import com.gweather.view.PresetCityListAdapter;
import com.gweather.view.SideBar;
import com.gweather.app.R;

public class AddCityActivity extends AddCityEntryActivity {
	private static final String TAG = "Gweather.AddCityActivity";

	private EditText cityName;
	private ImageButton searchCity;
	private ListView cityList;
	private View loadProgressView;

	private List<CityInfo> mCityInfos;
	private ArrayAdapter<String> cityInfoAdapter;
	private WeatherRefreshedReceiver mWeatherRefreshedReceiver;
	
	private Context mContext;
	private PinnedHeaderListView mPresetListView;
	private PresetCityListAdapter mPresetCityAdapter;
	
    private ListView mPinterestListView;
    private PinterestAdapter mPinterestAdapter;
    
	List<PresetCityInfo> mPresetCity = new ArrayList<PresetCityInfo>();
	private boolean isSearchModel = false;
	private View mPresetRootView;
	private Drawable mDrawableRight;
	
	private class WeatherRefreshedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (null == intent) {
				return;
			}
			String action = intent.getAction();
			Log.d(TAG, "WeatherRefreshedReceiver, " + action);
			if (WeatherAction.ACTION_ADD_WEATHER_FINISH.equals(action)) {
				
				AddCityActivity.this.setResult(
						CityMangerActivity.REQUEST_CODE_CITY_ADD, null);
				
				AddCityActivity.this.finish();
				showLoadingProgress(false);
			}
		}

	}
	
	WeatherModel.OnCityInfoUpdatedListener onCityInfoUpdatedListener = new WeatherModel.OnCityInfoUpdatedListener() {

		@Override
		public void updated() {
			if(!mCityInfos.isEmpty()) {
				final int count = mCityInfos.size();
				String[] cityInfosStrings = new String[count];
				for (int i = 0; i < count; i++) {
					cityInfosStrings[i] = mCityInfos.get(i).toString();
					Log.d(TAG, "[" + i + "] CityInfo="
							+ mCityInfos.get(i).getWoeid() + ": "
							+ mCityInfos.get(i).toString());
				}
				cityInfoAdapter = new ArrayAdapter<String>(
						AddCityActivity.this,
						R.layout.simple_list_item, cityInfosStrings);
				cityList.setAdapter(cityInfoAdapter);
				cityList.setVisibility(View.VISIBLE);
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.city_not_found),
						Toast.LENGTH_SHORT).show();
			}

			showLoadingProgress(false);
			
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_international_city);
		
		setCustomTitle(R.string.city_manager);
		mContext = getApplicationContext();
		
		mCityInfos = WeatherApp.mModel.getCityInfos();
		
		cityName = (EditText) findViewById(R.id.city_name);
		cityName.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mDrawableRight = cityName.getCompoundDrawables()[2];
		cityName.setCompoundDrawables(cityName.getCompoundDrawables()[0], null, null, null);
		cityName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					if (Utils.isNetworkAvailable(AddCityActivity.this)) {
						searchCity();
					} else {
						Toast.makeText(AddCityActivity.this,
								R.string.toast_net_inavailable, Toast.LENGTH_SHORT)
								.show();
						Log.d(TAG, "Search city BTN, network NOT available");
						return false;
					}
				}
				return true;
			}
		});
		cityName.addTextChangedListener(new cityNameWatcher());
		cityName.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				Drawable drawable = cityName.getCompoundDrawables()[2];
				if (drawable == null)
					return false;
				
				if (event.getAction() != MotionEvent.ACTION_UP)
					return false;
				if (event.getX() > cityName.getWidth()
						           - cityName.getPaddingRight()
						           - drawable.getIntrinsicWidth()
						           - cityName.getCompoundDrawablePadding()) {
					cityName.setText("");
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromInputMethod(cityName.getWindowToken(), 0);
					//cityName.clearFocus();
				}
				
				return false;
			}
		});
		searchCity = (ImageButton) findViewById(R.id.search_city);
		searchCity.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Utils.isNetworkAvailable(AddCityActivity.this)) {
					searchCity();
				} else {
					Toast.makeText(AddCityActivity.this,
							R.string.toast_net_inavailable, Toast.LENGTH_SHORT)
							.show();
					Log.d(TAG, "Search city BTN, network NOT available");
					return;
				}

			}
		});

		cityList = (ListView) findViewById(R.id.city_list);
		cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "position = " + position + ", id = " + id);
				int length = mCityInfos.size();
				if (position < length) {
					addCity(mCityInfos.get(position));
				}
			}
		});
		
		loadProgressView = findViewById(R.id.loading_progress_view);
	
		mWeatherRefreshedReceiver = new WeatherRefreshedReceiver();
		IntentFilter filter = new IntentFilter(
				WeatherAction.ACTION_ADD_WEATHER_FINISH);
		registerReceiver(mWeatherRefreshedReceiver, filter);
		
		initPresetCityListView();
		
		String[] hotCityWoeid = mContext.getResources().getStringArray(R.array.hot_citys_woeid);
		String[] cityWoeid = mContext.getResources().getStringArray(R.array.citys_woeid);
		if (hotCityWoeid.length > 0 || cityWoeid.length > 0) {
			getLoaderManager().initLoader(0, null, this);
		}
		
	}
	
	class cityNameWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void afterTextChanged(Editable s) {
			setSearchMode(s.length() > 0);
			
		}
		
	}
	
	@Override
	public View onCreateView(String name, Context context, AttributeSet attrs) {
		// TODO Auto-generated method stub
		
		if (cityName != null) {
			cityName.requestFocus();
		}
		
		return super.onCreateView(name, context, attrs);
	}
	
	@Override
	public void onBackPressed() {
		if (isSearchModel) {
			cityName.setText("");
		} else {
			super.onBackPressed();
		}
	}
	
	private void setSearchMode(boolean searchMode) {
		if (isSearchModel != searchMode) {
			isSearchModel = searchMode;
			if (isSearchModel) {
				mPresetRootView.setVisibility(View.GONE);
				
				int padding = mContext.getResources().getDimensionPixelOffset(R.dimen.edittext_drawable_padding);
				cityName.setCompoundDrawables(cityName.getCompoundDrawables()[0], null, mDrawableRight, null);
			} else {
				cityList.setAdapter(null);
				cityList.setVisibility(View.GONE);
				mPresetRootView.setVisibility(View.VISIBLE);
				
				int padding = mContext.getResources().getDimensionPixelOffset(R.dimen.edittext_drawable_padding);
				cityName.setCompoundDrawables(cityName.getCompoundDrawables()[0], null, null, null);
			}
			
		}
	}
	
	
	private boolean initPresetCityListView() {
		mPresetRootView = findViewById(R.id.preset_city);
		
		mPresetListView = (PinnedHeaderListView)findViewById(R.id.preset_city_list);

    	mPresetCityAdapter = new PresetCityListAdapter(mContext, mPresetListView);
    	mPresetCityAdapter.setListener(mAdapterListener);
    	
    	mPresetListView.setAdapter(mPresetCityAdapter);
    	mPresetListView.setOnScrollListener(mPresetCityAdapter);
    	mPresetListView.setPinnedHeaderView(LayoutInflater.from(mContext).inflate(R.layout.pinned_header_view, mPresetListView, false));
    	
    	SideBar sidebar = (SideBar)findViewById(R.id.sidebar);
    	sidebar.setOnItemClickListener(new SideBar.OnItemClickListener() {
			
            @Override
            public void OnItemClick(String s) {
                if (s != null) {
                    if (s.equals("#")) {
                    	mPresetListView.setSelection(0);
                    } else {
                    	MySectionIndexer sectionIndexer = mPresetCityAdapter.getSectionIndexer();
                        int section = sectionIndexer.getSectionForLetter(s.charAt(0));
                        int position = sectionIndexer.getPositionForSection(section);
                        if (position != -1) {
                        	mPresetListView.setSelection(position + mPresetListView.getHeaderViewsCount());
                        }
                    }

                }
            }
		});
    	
    	addHeaderView(getLayoutInflater());
		
		return true;
	}
	
    private void addHeaderView(LayoutInflater inflater) {
    	
    	View rootView = inflater.inflate(R.layout.preset_city_header_view, null);
        mPinterestListView = (ListView)rootView.findViewById(R.id.pinterest_list);

        mPinterestAdapter = new PinterestAdapter(getApplicationContext(), mAdapterListener, mPinterestListView);

        ViewTreeObserver vto = mPinterestListView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPinterestListView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mPinterestAdapter.initRowCount();
            }
        });

        mPinterestListView.setAdapter(mPinterestAdapter);
        
        mPresetListView.addHeaderView(rootView);
    }
    
    private CityClickListener mAdapterListener = new CityClickListener() {
		
		@Override
		public void onCitySelected(PresetCityInfo ci, boolean add) {
			if (Utils.isNetworkAvailable(AddCityActivity.this)) {
				
				ci.setSelect(add ? 1 : 0);
				if (ci.isHotCity() > 0) {
					mPinterestAdapter.notifyDataSetChanged();
				} else {
					mPresetCityAdapter.notifyDataSetChanged();
				}
			
				CityInfo info = new CityInfo();
				info.setWoeid(ci.getWoeId());
				info.setName(ci.getCityName());
				addCity(info);
			} else {
				
				Toast.makeText(AddCityActivity.this,
						R.string.toast_net_inavailable, Toast.LENGTH_SHORT)
						.show();
				Log.d(TAG, "Search city BTN, network NOT available");
				return;
			}
		}
	};
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor dataCursor) {
		super.onLoadFinished(loader, dataCursor);
		int count = dataCursor.getCount();
		
		if (mPinterestAdapter != null && mPresetCityAdapter != null 
								      && dataCursor != null) {
			ArrayList<PresetCityInfo> presetHotCity = new ArrayList<PresetCityInfo>();
			ArrayList<PresetCityInfo> presetCity = new ArrayList<PresetCityInfo>();
			
			while (dataCursor.moveToNext()) {
			    PresetCityInfo cityInfo = new PresetCityInfo();
			    cityInfo.setID(dataCursor.getInt(COLUMN_ID));
			    cityInfo.setWoeId(dataCursor.getString(COLUMN_WOEID));
			    cityInfo.setCityName(dataCursor.getString(COLUMN_NAME));
			    cityInfo.setSortKey(dataCursor.getString(COLUMN_SORY_KEY));
			    int isHotCity = dataCursor.getInt(COLUMN_IS_HOT_CITY);
			    cityInfo.setHotCity(isHotCity);
			    cityInfo.setSelect(dataCursor.getInt(COLUMN_IS_SELECT));
				if (isHotCity > 0) {
					presetHotCity.add(cityInfo);
				} else {
					presetCity.add(cityInfo);
				}
			}
			
			mPinterestAdapter.setDate(presetHotCity);
			mPresetCityAdapter.setDate(presetCity);
			
			SideBar sidebar = (SideBar)findViewById(R.id.sidebar);
			if (presetCity.size() == 0) {
				sidebar.setVisibility(View.INVISIBLE);
			} else {
				sidebar.setVisibility(View.VISIBLE);
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		cityName.addTextChangedListener(null);
		unregisterReceiver(mWeatherRefreshedReceiver);
		mWeatherRefreshedReceiver = null;
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		WeatherApp.mModel.stopQueryCity();
	}

	private void searchCity() {
		String name = cityName.getText().toString();
		if (name.isEmpty()) {
			// Any toast?
		} else {
			showLoadingProgress(true);
			WeatherApp.mModel.setOnCityInfoUpdatedListener(onCityInfoUpdatedListener);
			if (!WeatherApp.mModel.getCityInfosByNameFromInternet(name)) {
				showLoadingProgress(false);
			}
		}
	}

	private void addCity(CityInfo info) {
		Log.d(TAG, "addCity, woeid:" + info.getWoeid());
		WeatherApp.mModel.stopQueryCity();
		cityList.setVisibility(View.GONE);
		mPresetRootView.setVisibility(View.GONE);
		
		showLoadingProgress(true);
		
		if(!WeatherApp.mModel.addWeatherByCity(info, false)) {
			showLoadingProgress(false);
		}
	}

	private void showLoadingProgress(boolean show) {
		Log.d(TAG, "showLoadingProgress:" + show);
		if (show) {
			loadProgressView.setVisibility(View.VISIBLE);
			searchCity.setEnabled(false);
			cityName.setEnabled(false);
		} else {
			loadProgressView.setVisibility(View.GONE);
			searchCity.setEnabled(true);
			cityName.setEnabled(true);
		}
	}

}
