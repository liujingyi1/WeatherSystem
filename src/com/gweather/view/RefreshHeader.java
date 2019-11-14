package com.gweather.view;


public interface RefreshHeader {

    void reset();

    void pull();

    void refreshing();

    void onPositionChange(float currentPos, float lastPos, float refreshPos, boolean isTouch, State state);
   
    void complete();
    
}
