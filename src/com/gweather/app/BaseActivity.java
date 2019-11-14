package com.gweather.app;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public abstract class BaseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActionBar();
	}
	
	public void setCustomTitle(int resid){
		ActionBar actionBar = getActionBar();
		if (actionBar != null ) {
			((TextView)actionBar.getCustomView().findViewById(R.id.tv_title)).setText(resid);
		}
	}
	
	private void initActionBar() {
		ActionBar actionBar = getActionBar();
		if (actionBar != null ) {
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
			actionBar.setCustomView(R.layout.actionbar_view);
			actionBar.getCustomView().findViewById(R.id.img_back).setOnClickListener(
					new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							finish();
						}
					});
		}
	}
}
