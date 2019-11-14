package com.gweather.view;

import com.gweather.app.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gweather.app.PresetCityInfo;

/**
 * Created by jingyi.liu on 2016/10/19.
 */

public class PinterestItemView extends FrameLayout {

    protected  CityClickListener mListener;
    private int mColumnCount;

    private TextView mCityName;
    PresetCityInfo mPresetCityInfo;
    private boolean isSelect = false;

    public PinterestItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCityName = (TextView)findViewById(R.id.pinterest_text);
        OnClickListener listener = createClickListener();
        setOnClickListener(listener);
    }

    public void loadData(PresetCityInfo presetCityInfo) {
        this.mPresetCityInfo = presetCityInfo;
        mCityName.setText(presetCityInfo.getCityName());
        isSelect = presetCityInfo.isSelect() > 0;
        setSelected(isSelect);
    }

    protected OnClickListener createClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null)
                    return;
                if (!isSelect) {
	                //isSelect = !isSelect;
	                //setSelected(isSelect);
	                //mPresetCityInfo.setSelect(!isSelect ? 1 : 0);
	                mListener.onCitySelected(mPresetCityInfo, !isSelect);
                }
            }
        };
    }

    public void setListener(CityClickListener listener){
        mListener = listener;
    }

    public interface Listener {
        void onCitySelected(PresetCityInfo ci);
    }

}
