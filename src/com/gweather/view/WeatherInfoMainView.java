package com.gweather.view;

import com.gweather.app.R;

import android.R.integer;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gweather.app.MainActivity;
import com.gweather.app.WeatherInfo;
import com.gweather.utils.WeatherDataUtil;

public class WeatherInfoMainView extends LinearLayout {
	private static final String TAG = "Gweather.WeatherInfoMainView";
	private Context mContext;
	
	private boolean isTemperatureC = true;
	private String temperatureType = "";
	
	private TextView date;
	private TextView cityName;
	private ImageView weatherIcon;
	private TextView weatherText;
	private TextView currentTemp;
	private TextView tempMin;
	private TextView tempMax;
	private TextView refreshView;

	private LinearLayout forecastItem1;
	private LinearLayout forecastItem2;
	private LinearLayout forecastItem3;
	private LinearLayout forecastItem4;

	private ForeCastItemViews foreCastItemViews1 = new ForeCastItemViews();
	private ForeCastItemViews foreCastItemViews2 = new ForeCastItemViews();
	private ForeCastItemViews foreCastItemViews3 = new ForeCastItemViews();
	private ForeCastItemViews foreCastItemViews4 = new ForeCastItemViews();
	
	private View mLineChartContainer;
	private LineChartView mLineChart;
	private TextView mChartItemText1;
	private TextView mChartItemText2;
	private TextView mChartItemText3;
	private TextView mChartItemText4;
	
	private View mForecastsContainer;
	private View mSwipButtonContainer;
	private View mForecastsRoot;
	private View refreshContainer;
	
	private String IS_LINECHART_MODE_KEY = "is_Linechart_mode";

	private int lineChartContainerHeight;
	private int forecastContainerHeight;
	private int swipButtonContainerHeight;
	private int forecastRootHeight;
	private boolean isAniamtorRunning;
	private AnimateEndListener listener;
	private ImageView swipeButton;

	public WeatherInfoMainView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		mContext = context;
		SharedPreferences sp = mContext.getSharedPreferences(MainActivity.SETTINGS_SP,
				Context.MODE_PRIVATE);
		isTemperatureC = sp.getBoolean(MainActivity.SETTINGS_TEMPERATURE_TYPE, 
				mContext.getResources().getBoolean(R.bool.config_default_temperature_c));
		temperatureType = isTemperatureC?mContext.getResources().getString(R.string.temperature_c):
			mContext.getResources().getString(R.string.temperature_f);
				
		LayoutInflater.from(context).inflate(R.layout.weather_info_main, this);

		date = (TextView) findViewById(R.id.date_y);
		cityName = (TextView) findViewById(R.id.city_name);
		weatherIcon = (ImageView) findViewById(R.id.weather_icon);
		weatherText = (TextView) findViewById(R.id.weather_text);
		currentTemp = (TextView) findViewById(R.id.currentTemp);
		tempMin = (TextView) findViewById(R.id.temp_min);
		tempMax = (TextView) findViewById(R.id.temp_max);
		refreshView = (TextView) findViewById(R.id.refresh_text);
		
		forecastItem1 = (LinearLayout) findViewById(R.id.forecast_item_1);
		forecastItem2 = (LinearLayout) findViewById(R.id.forecast_item_2);
		forecastItem3 = (LinearLayout) findViewById(R.id.forecast_item_3);
		forecastItem4 = (LinearLayout) findViewById(R.id.forecast_item_4);
		
		
		initForecastItemView(forecastItem1, foreCastItemViews1);
		initForecastItemView(forecastItem2, foreCastItemViews2);
		initForecastItemView(forecastItem3, foreCastItemViews3);
		initForecastItemView(forecastItem4, foreCastItemViews4);
		
		mForecastsRoot = findViewById(R.id.forecast_root);
		
		mForecastsContainer =  findViewById(R.id.forecast_item_container);
		
		mLineChartContainer = findViewById(R.id.line_chart_container);
		mLineChart = (LineChartView)findViewById(R.id.line_chart);
		mLineChart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
            	mLineChart.requestLayout();
            	mLineChart.startAnimation();
            	mLineChart.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
		mChartItemText1 = (TextView) findViewById(R.id.chart_forecast_item1);
		mChartItemText2 = (TextView) findViewById(R.id.chart_forecast_item2);
		mChartItemText3 = (TextView) findViewById(R.id.chart_forecast_item3);
		mChartItemText4 = (TextView) findViewById(R.id.chart_forecast_item4);

		mSwipButtonContainer = findViewById(R.id.swip_button_container);
		refreshContainer = findViewById(R.id.refresh_text_container);

		swipeButton = (ImageView)findViewById(R.id.swip_button);
		swipeButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startAnimation();
				
			}
		});

		/*int colors[] = {0xff4FB1FF, 0xff028efd};
		GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
		mLineChartContainer.setBackground(gd);*/
		/*
		int colors[] = {0xffff0000, 0xff00ff00, 0xff0000ff};
		GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
		
		forecastItem1.setBackground(gd);
		*/
	}
	
	private boolean getForecastMode() {
		SharedPreferences sp = mContext.getSharedPreferences(MainActivity.SETTINGS_SP,
				Context.MODE_PRIVATE);
		
		return sp.getBoolean(IS_LINECHART_MODE_KEY, false);
	}
	
	private void updateForecastMode() {
		SharedPreferences sp = mContext.getSharedPreferences(MainActivity.SETTINGS_SP,
				Context.MODE_PRIVATE);

		boolean mode = sp.getBoolean(IS_LINECHART_MODE_KEY, false);
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(IS_LINECHART_MODE_KEY, !mode);
		editor.commit();
		
	}
	
	public void setListener(AnimateEndListener listener) {
		this.listener = listener;
	}
	
	public interface AnimateEndListener {
		public void onAnimateStart();
		public void onAnimateEnd();
	}
	
	public boolean getIsAnimateRunning() {
		return isAniamtorRunning;
	}
	
	
	public void startAnimation() {
		boolean isLinechartMode = getForecastMode();

		ObjectAnimator animator1;
		ObjectAnimator animator2;
		ObjectAnimator animator3;
		ObjectAnimator animator4;
		
		int dev = getResources().getDimensionPixelOffset(R.dimen.swipe_button_deviation);
		
		if (isLinechartMode) {
			animator1 = ObjectAnimator.ofFloat(mForecastsContainer, "translationY", 
					-forecastContainerHeight);
			animator2 = ObjectAnimator.ofFloat(mSwipButtonContainer, "translationY", 
					-forecastContainerHeight+swipButtonContainerHeight+dev);
			animator3 = ObjectAnimator.ofFloat(mLineChartContainer, "alpha", 1,0);
			animator4 = ObjectAnimator.ofFloat(refreshContainer, "alpha", 1);

		} else {
			animator1 = ObjectAnimator.ofFloat(mForecastsContainer, "translationY", 
					forecastContainerHeight);
			animator2 = ObjectAnimator.ofFloat(mSwipButtonContainer, "translationY", 
					forecastContainerHeight-swipButtonContainerHeight-dev);
			animator3 = ObjectAnimator.ofFloat(mLineChartContainer, "alpha", 1);
			animator4 = ObjectAnimator.ofFloat(refreshContainer, "alpha", 1,0);
		}
		
		AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				isAniamtorRunning = true;
				swipeButton.setEnabled(false);
				listener.onAnimateStart();
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				isAniamtorRunning = true;
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				updateForecastMode();
				requestLayout();
				//invalidate();
				isAniamtorRunning = false;
				listener.onAnimateEnd();
				swipeButton.setEnabled(true);
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				isAniamtorRunning = false;
				
			}
		});
        set.play(animator1).with(animator2).with(animator3).with(animator4);
        set.setDuration(400);
        set.start();
	}
	
	/*
    public void startAnimation() {
    	ArrayList<Point> pathArray = new ArrayList<Point>();
    	boolean isLinechartMode = getForecastMode();
    	if (isLinechartMode) {
    		pathArray.clear();
    		pathArray.add(new Point(0,0));
    		pathArray.add(new Point(0,forecastContainerHeight));
    	} else {
    		pathArray.clear();
    		pathArray.add(new Point(0,forecastContainerHeight));
    		pathArray.add(new Point(0,0));
    	}
        ValueAnimator highAnimator = new ValueAnimator();
        highAnimator.setObjectValues(pathArray.toArray());
        highAnimator.setEvaluator(new LineEvaluator());
        highAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
            	Point point = (Point) animation.getAnimatedValue();
            	animateLayout(point.y);
            }
        });
        
        AnimatorSet set = new AnimatorSet();
        set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				updateForecastMode();
				requestLayout();
				//invalidate();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
        set.play(highAnimator);
        set.setDuration(1000);
        set.start();
        
    }
    
    private void animateLayout(int progress) {
    	boolean isLinechartMode = getForecastMode();
    	
    	mForecastsContainer.setTranslationY(isLinechartMode ? -progress : forecastContainerHeight - progress);
    	//mForecastsContainer.setTranslationY(-progress);
    	//mForecastsContainer.setProportion(((float)progress/(float)forecastContainerHeight));
    	//mForecastsContainer.invalidate();
    	mLineChartContainer.setAlpha((float)(forecastContainerHeight - progress)  / (float)255);
    	refreshContainer.setAlpha((float)progress / (float)255);
		if (progress >= swipButtonContainerHeight) {
			mSwipButtonContainer.setTranslationY(isLinechartMode ? -progress+swipButtonContainerHeight 
					: forecastContainerHeight-progress);
		}
    }*/
    
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
		boolean isLinechartMode = getForecastMode();
		
		if (mForecastsContainer != null && mLineChartContainer != null) {
			forecastRootHeight = mForecastsRoot.getMeasuredHeight();
			forecastContainerHeight = mForecastsContainer.getMeasuredHeight();
			lineChartContainerHeight = mLineChartContainer.getMeasuredHeight();
			swipButtonContainerHeight = mSwipButtonContainer.getMeasuredHeight();
			
			int width = getMeasuredWidth();

			//float scale = mContext.getResources().getDisplayMetrics().density;
			//Log.i("jingyi", "width="+(int)(width / scale + 0.5f));
			
			mForecastsContainer.setTranslationY(0);
			mSwipButtonContainer.setTranslationY(0);
			
			int dev = getResources().getDimensionPixelOffset(R.dimen.swipe_button_deviation);

			if (isLinechartMode) {
				mForecastsContainer.layout(0, forecastRootHeight, 
						width, forecastRootHeight + forecastContainerHeight);
				refreshContainer.setAlpha(0);
				mLineChartContainer.setAlpha(255);
				mSwipButtonContainer.layout(0, forecastRootHeight-swipButtonContainerHeight, 
						width, forecastRootHeight);
				swipeButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.main_swip_button));
			} else {
				mForecastsContainer.layout(0, forecastRootHeight-forecastContainerHeight, 
						width, forecastRootHeight);
				refreshContainer.setAlpha(255);
				mLineChartContainer.setAlpha(0);
			    mSwipButtonContainer.layout(0, forecastRootHeight-forecastContainerHeight+dev, 
			    		width, forecastRootHeight-forecastContainerHeight+swipButtonContainerHeight+dev);
			    swipeButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.main_swip_button_down));
			}
			refreshContainer.layout(0, forecastRootHeight-forecastContainerHeight-refreshContainer.getMeasuredHeight(), 
					width, forecastRootHeight-forecastContainerHeight);
		}
	}
	
	public WeatherInfoMainView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WeatherInfoMainView(Context context) {
		this(context, null);
	}
	
	private void initForecastItemView(LinearLayout parent,
			ForeCastItemViews views) {
		views.dayText = (TextView) parent.findViewById(R.id.forecast_day);
		views.dateText = (TextView) parent.findViewById(R.id.forecast_date);
		views.iconImage = (ImageView) parent.findViewById(R.id.forecast_icon);
		views.textText = (TextView) parent.findViewById(R.id.forecast_text);
		views.weatherText = (TextView) parent
				.findViewById(R.id.forecast_weather);
	}

	public void setLineChartData(int[] temMaxPoints, int[] temMinPoints) {
		mLineChart.initValues(temMaxPoints, temMinPoints);
		int h = getResources().getDimensionPixelOffset(R.dimen.chart_item_width) / 2;
		int v = getResources().getDimensionPixelOffset(R.dimen.line_chart_vertical_padding);
		int s = getResources().getDimensionPixelOffset(R.dimen.line_chart_text_size);

		mLineChart.setPadding(h, v, s);
		mLineChart.SetTemtype(temperatureType);
	}
	
	public void bindView(String dateS, String nameS, int imgRes, String textS,
			String currentTempS, String tempMinS, String tempMaxS, String refresh, boolean isGps) {
		date.setText(dateS);
		cityName.setText(nameS);
		weatherIcon.setImageResource(imgRes);
		weatherText.setText(textS);
		currentTemp.setText(currentTempS);
		tempMin.setText(tempMinS);
		tempMax.setText(tempMaxS);
		refreshView.setText(refresh);
	}

	public void updateForeCastItem(int index,
			WeatherInfo.Forecast forecast) {
		
		ForeCastItemViews views = null;
		TextView chartTextView = null;
		switch (index) {
		case 1:
			views = foreCastItemViews1;
			chartTextView = mChartItemText1;
			break;
		case 2:
			views = foreCastItemViews2;
			chartTextView = mChartItemText2;
			break;
		case 3:
			views = foreCastItemViews3;
			chartTextView = mChartItemText3;
			break;
		case 4:
			views = foreCastItemViews4;
			chartTextView = mChartItemText4;
			break;
		default:
			break;
		}
		if (views == null) {
			return;
		}
		views.dayText.setText(forecast.getDay());
		//views.dateText.setText(forecast.getDateShort() + "("+forecast.getDay()+")");
		views.dateText.setText(forecast.getDateShort() + forecast.getDay());
		views.textText.setText(forecast.getText());
		views.weatherText.setText((isTemperatureC?forecast.getLow():forecast.getLowF()) + temperatureType +"-"
				+ (isTemperatureC?forecast.getHigh():forecast.getHighF()) + temperatureType);
		int code = Integer.parseInt(forecast.getCode());
		int resId;
		//resId = WeatherDataUtil.getInstance().getWeatherImageResourceByCode(
		//		code, false, WeatherDataUtil.NETHER_NOTIFY_WIDGET);
		resId = WeatherDataUtil.getInstance().getWeatherImageResourceByCode(
				code, false, WeatherDataUtil.SMALL_DARK);

		if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE == resId) {
			resId = WeatherDataUtil.getInstance()
					.getWeatherImageResourceByText(forecast.getText(), false,
							WeatherDataUtil.SMALL_DARK);
		}
		views.iconImage.setImageResource(resId);
		
		
		chartTextView.setText(forecast.getDateShort() + forecast.getDay());
		
		int resId1;
		resId1 = WeatherDataUtil.getInstance().getWeatherImageResourceByCode(
				code, false, WeatherDataUtil.SMALL_WHITE);

		if (WeatherDataUtil.INVALID_WEAHTER_RESOURCE == resId1) {
			resId1 = WeatherDataUtil.getInstance()
					.getWeatherImageResourceByText(forecast.getText(), false,
							WeatherDataUtil.SMALL_WHITE);
		}
		int padding = getResources().getDimensionPixelOffset(R.dimen.chart_item_drawable_padding);
		int size = getResources().getDimensionPixelOffset(R.dimen.chart_item_drawable_size);
		Drawable drawable = getResources().getDrawable(resId1);
		drawable.setBounds(0, 0, size, size);
		chartTextView.setCompoundDrawables(null, drawable, null, null);
		chartTextView.setCompoundDrawablePadding(padding);
	}
	
	class ForeCastItemViews {
		public TextView dayText;
		public TextView dateText;
		public ImageView iconImage;
		public TextView textText;
		public TextView weatherText;
	}
	
}
