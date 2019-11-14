package com.gweather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

public class MainMenuLayout extends LinearLayout {
	private static final String TAG = "Gweather.MainMenuLayout";
	
	private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_MOVE = 1;
    private static final int SNAP_VELOCITY = 600;

    private int mTouchState = TOUCH_STATE_REST;
    
	private int mTouchSlop;
    private float mLastMotionX;
    private int mViewWidth;
    
    private VelocityTracker mVelocityTracker;
    
    private OnMoveEnoughListener mOnMoveEnoughListener;
    
    public interface OnMoveEnoughListener {
    	void moveEnough();
    };

    public void setOnMoveEnoughListener(OnMoveEnoughListener listener) {
    	mOnMoveEnoughListener = listener;
    }
    
	public MainMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public MainMenuLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MainMenuLayout(Context context) {
		this(context, null);
	}
	

	private void init() {
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
		mVelocityTracker.addMovement(event);
		
		final int action = event.getAction();
        final float x = event.getX();
        
        switch (action) {
        case MotionEvent.ACTION_DOWN:
        	mViewWidth = getWidth();
        	break;
        case MotionEvent.ACTION_MOVE:
        	if (mLastMotionX - x > mViewWidth / 3) {
        		/*if (mOnMoveEnoughListener != null) {
            		mOnMoveEnoughListener.moveEnough();
            	}*/
        	} else if (mLastMotionX - x < -mViewWidth / 3) {
        		if (mOnMoveEnoughListener != null) {
            		mOnMoveEnoughListener.moveEnough();
            	}
        	}
        	break;
        case MotionEvent.ACTION_UP:
        	final VelocityTracker velocityTrackers = mVelocityTracker;
            mVelocityTracker.computeCurrentVelocity(1000);
            int velocityX = (int) velocityTrackers.getXVelocity();
            if (velocityX > SNAP_VELOCITY) {
            	// Fling enough to move left
            	if (mOnMoveEnoughListener != null) {
            		mOnMoveEnoughListener.moveEnough();
            	}
            } else if (velocityX < -SNAP_VELOCITY) {
            	// Fling enough to move right
            	/*if (mOnMoveEnoughListener != null) {
            		mOnMoveEnoughListener.moveEnough();
            	}*/
            }
        	if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        	mTouchState = TOUCH_STATE_REST;
        	break;
        case MotionEvent.ACTION_CANCEL:
        	if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            mTouchState = TOUCH_STATE_REST;
            break;
        }
		return true;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getActionMasked();
		final float x = ev.getX();
		switch (action) {
        case MotionEvent.ACTION_DOWN:
        	Log.d(TAG, "Intercept-ACTION_DOWN");
        	mTouchState = TOUCH_STATE_REST;
        	mLastMotionX = x;
        	break;
        case MotionEvent.ACTION_MOVE:
        	Log.d(TAG, "Intercept-ACTION_MOVE");
			final int xDiff = (int) Math.abs(mLastMotionX - x);
			Log.d(TAG, "xDiff="+xDiff);
			Log.d(TAG, "mTouchSlop="+mTouchSlop);
        	if (xDiff > mTouchSlop) {
        		mTouchState = TOUCH_STATE_MOVE;
        	}
        	break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
        	Log.d(TAG, "Intercept-ACTION_UP");
        	mTouchState = TOUCH_STATE_REST;
        	break;
		}
		return mTouchState != TOUCH_STATE_REST;
	}

}
