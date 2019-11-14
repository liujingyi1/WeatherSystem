package com.gweather.view;

import com.gweather.app.PresetCityInfo;
import com.gweather.view.PinterestItemView.Listener;
import com.gweather.app.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PresetCityItemView extends LinearLayout {

    protected  CityClickListener mListener;

    private TextView mCityName;
    private ViewGroup mCityNameContainer;
    PresetCityInfo mPresetCityInfo;
    boolean isSelect = false;
	
	public PresetCityItemView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public PresetCityItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PresetCityItemView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public PresetCityItemView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCityName = (TextView)findViewById(R.id.cityname);
        mCityNameContainer = (ViewGroup)findViewById(R.id.cityname_container);
        OnClickListener listener = createClickListener();
        setOnClickListener(listener);
    }

    public void loadData(PresetCityInfo presetCityInfo) {
        this.mPresetCityInfo = presetCityInfo;
        mCityName.setText(presetCityInfo.getCityName());
        isSelect = presetCityInfo.isSelect() > 0;
        mCityNameContainer.setSelected(isSelect);
    }

    protected OnClickListener createClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null)
                    return;
                if (!isSelect) {
	                //isSelect = !isSelect;
	                //mCityNameContainer.setSelected(isSelect);
	                //mPresetCityInfo.setSelect(!isSelect ? 1 : 0);
	                mListener.onCitySelected(mPresetCityInfo, !isSelect);
                }
            }
        };
    }

    public void setListener(CityClickListener listener){
        mListener = listener;
    }
}
