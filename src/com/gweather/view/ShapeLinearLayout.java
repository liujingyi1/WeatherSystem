package com.gweather.view;

import com.gweather.app.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public class ShapeLinearLayout extends LinearLayout {

	private static final String TAG = "ShapeLinearLayout";
	
	private int width = 0;
	private int height = 0;
	private float proportion = 1;

	
	public ShapeLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ShapeLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ShapeLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public ShapeLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}
	
	public void setProportion(float proportion) {
		this.proportion = proportion;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
		Log.i("TAG", "widthSize="+width+"  heightSize="+height);
	}
	
	@Override
	public void draw(Canvas canvas) {
		int layerId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(),
				null, Canvas.ALL_SAVE_FLAG);
		canvas.drawColor(getResources().getColor(R.color.main_white_background));
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.TRANSPARENT);
		paint.setStyle(Paint.Style.FILL);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

		Path path = new Path();
		path.reset();
		path.moveTo(0, 0);
		path.quadTo(width / 2, (int)(canvas.getWidth()*0.13*proportion), width, 0);
		canvas.drawPath(path, paint);
		canvas.restoreToCount(layerId);

		
		super.draw(canvas);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	

}
