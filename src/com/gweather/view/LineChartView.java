package com.gweather.view;

import java.util.ArrayList;

import javax.security.auth.PrivateCredentialPermission;

import org.apache.http.params.CoreConnectionPNames;

import com.gweather.app.R;

import android.R.integer;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class LineChartView extends View {
    private static final String TAG = "LineChartView";

    private final static int HEIGHT = 200;
    private final static int WIDTH = 200;
    private static final int POINT_RADIUS = 4;

    private Paint mPointPaint;
    private Paint mMarkLinePaint;
    private Paint mLinePaint;
    private Paint mTextPaint;

    private ArrayList<ItemValue> mItemValues = new ArrayList<ItemValue>();
    private int mHeight;
    private int mWidth;
    private int maxHigh;
    private int minLow;
    private int gap;

    private ArrayList<Point> mHighPoints = new ArrayList<Point>();
    private ArrayList<Point> mLowPoints = new ArrayList<Point>();
    private Point currentHighPoint = new Point(0, 0);
    private Point currentLowPoint = new Point(0, 0);
    
    private int paddingHorizontal = 0;
    private int paddingVertical = 0;
    private int spacepaddig = 0;
    private String temperatureType;
    
    private ArrayList<Point> mHighMidPoints = new ArrayList<Point>();
    private ArrayList<Point> mHighMidMidPoints = new ArrayList<Point>();
    private ArrayList<Point> mHighControlPoints = new ArrayList<Point>();
    private ArrayList<Point> mLowMidPoints = new ArrayList<Point>();
    private ArrayList<Point> mLowMidMidPoints = new ArrayList<Point>();
    private ArrayList<Point> mLowControlPoints = new ArrayList<Point>();
    
    public LineChartView(Context context) {
        this(context, null);
    }

    public LineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPointPaint = new Paint();
        mPointPaint.setColor(getResources().getColor(R.color.white_secondary));

        mMarkLinePaint = new Paint();
        mMarkLinePaint.setColor(getResources().getColor(R.color.white_secondary));
        mMarkLinePaint.setStyle(Paint.Style.STROKE);
        mMarkLinePaint.setStrokeWidth(1);
        mMarkLinePaint.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));


        mLinePaint = new Paint();
        mLinePaint.setColor(getResources().getColor(R.color.white_secondary));
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(3);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);
        
        mTextPaint = new Paint();
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setColor(getResources().getColor(R.color.white));

    }

    public void setPadding(int h, int v, int space) {
    	paddingHorizontal = h;
    	paddingVertical = v;
    	spacepaddig = space;
    	mTextPaint.setTextSize(spacepaddig);
    }
    
    public void SetTemtype(String t) {
    	temperatureType = t;
    }
    
    
    public void initValues(int[] highs, int lows[]) {
        if (highs == null || lows == null) {
            Log.w(TAG, "param is NULL!");
            return;
        }

        if (highs.length == lows.length) {
            mItemValues.clear();
            maxHigh = highs[0];
            minLow = lows[0];
            for (int i=0; i<highs.length; i++) {
                if (highs[i] > maxHigh) {
                    maxHigh = highs[i];
                }

                if (lows[i] < minLow) {
                    minLow = lows[i];
                }

                ItemValue itemValue = new ItemValue(highs[i], lows[i]);
                mItemValues.add(itemValue);
            }
        } else {
            Log.w(TAG, "highs.length != lows.length");
        }
    }

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (MeasureSpec.EXACTLY == widthMode) {
            mWidth = widthSize;
        } else {
            mWidth = WIDTH;
        }

        if (MeasureSpec.EXACTLY == heightMode) {
            mHeight = heightSize;
        } else {
            mHeight = HEIGHT;
        }

        setMeasuredDimension(mWidth, mHeight);

        setXY(mWidth, mHeight);
        initPoints();
        initMidPoints();
        initMidMidPoints();
        initControlPoints();
    }

    private void setXY(int viewWidth, int viewHeight) {
        double scale = (viewHeight - paddingVertical - paddingVertical) / (maxHigh - minLow)  * 1.0d;
        if (mItemValues.size() == 1) {
            ItemValue itemValue = mItemValues.get(0);
            itemValue.setXY(paddingHorizontal, viewHeight, 0);
        } else if (mItemValues.size() > 1) {
            gap = (viewWidth - paddingHorizontal - paddingHorizontal) / (mItemValues.size() - 1);
            int x = paddingHorizontal;
            for(int i=0;i<mItemValues.size(); i++) {
                ItemValue itemValue = mItemValues.get(i);
                itemValue.setXY(x, (int) ((itemValue.high - minLow) * scale + paddingVertical) , (int) ((itemValue.low - minLow) * scale) + paddingVertical);
                x += gap;
            }
        }
    }

    private void initPoints() {
        mHighPoints.clear();
        mLowPoints.clear();
        for (ItemValue itemValue:mItemValues) {
            Point point = new Point(itemValue.x, itemValue.yHigh);
            mHighPoints.add(point);
            point = new Point(itemValue.x, itemValue.yLow);
            mLowPoints.add(point);
        }
    }
    
    private void initMidPoints() {
    	mHighMidPoints.clear();
    	mLowMidPoints.clear();
    	for (int i = 0; i < mHighPoints.size(); i++) {
			Point midHighPoint = null;
			Point minLowPoint = null;
			if (i == mHighPoints.size()-1) {
				return;
			} else {
				midHighPoint = new Point((mHighPoints.get(i).x + mHighPoints.get(i+1).x) / 2,
						(mHighPoints.get(i).y + mHighPoints.get(i+1).y) / 2);
				minLowPoint = new Point((mLowPoints.get(i).x + mLowPoints.get(i+1).x) / 2,
						(mLowPoints.get(i).y + mLowPoints.get(i+1).y) / 2);
			}
			mHighMidPoints.add(midHighPoint);
			mLowMidPoints.add(minLowPoint);
		}
    }
    
    private void initMidMidPoints() {
    	mHighMidMidPoints.clear();
    	mLowMidMidPoints.clear();
        for (int i = 0; i < mHighMidPoints.size(); i++) {
        	Point highMidMidPoint = null;
        	Point lowMidMidPoint = null;
        	if (i == mHighMidPoints.size() - 1) {
        		return;
        	} else {
        		highMidMidPoint = new Point((mHighMidPoints.get(i).x + mHighMidPoints.get(i+1).x) / 2,
        				(mHighMidPoints.get(i).y + mHighMidPoints.get(i+1).y) / 2);
        		
        		lowMidMidPoint = new Point((mLowMidPoints.get(i).x + mLowMidPoints.get(i+1).x) / 2,
        				(mLowMidPoints.get(i).y + mLowMidPoints.get(i+1).y) / 2);
			}
        	mHighMidMidPoints.add(highMidMidPoint);
        	mLowMidMidPoints.add(lowMidMidPoint);
        }
    }

    private void initControlPoints() {
    	mHighControlPoints.clear();
    	mLowControlPoints.clear();
        for (int i = 0; i < mHighPoints.size(); i++) {
			if (i == 0 || i == mHighPoints.size() - 1) {
				continue;
			} else {
				Point before = new Point();
				Point after = new Point();
				before.x = mHighPoints.get(i).x - mHighMidMidPoints.get(i - 1).x + mHighMidPoints.get(i - 1).x;
				before.y = mHighPoints.get(i).y - mHighMidMidPoints.get(i - 1).y + mHighMidPoints.get(i - 1).y;
				after.x = mHighPoints.get(i).x - mHighMidMidPoints.get(i - 1).x + mHighMidPoints.get(i).x;
				after.y = mHighPoints.get(i).y - mHighMidMidPoints.get(i - 1).y + mHighMidPoints.get(i).y;
				mHighControlPoints.add(before);
				mHighControlPoints.add(after);
			}
		}
        
        for (int i = 0; i < mLowPoints.size(); i++) {
			if (i == 0 || i == mLowPoints.size() - 1) {
				continue;
			} else {
				Point before = new Point();
				Point after = new Point();
				before.x = mLowPoints.get(i).x - mLowMidMidPoints.get(i - 1).x + mLowMidPoints.get(i - 1).x;
				before.y = mLowPoints.get(i).y - mLowMidMidPoints.get(i - 1).y + mLowMidPoints.get(i - 1).y;
				after.x = mLowPoints.get(i).x - mLowMidMidPoints.get(i - 1).x + mLowMidPoints.get(i).x;
				after.y = mLowPoints.get(i).y - mLowMidMidPoints.get(i - 1).y + mLowMidPoints.get(i).y;
				mLowControlPoints.add(before);
				mLowControlPoints.add(after);
			}
		}
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mItemValues.size() > 0) {
            Path path = new Path();
            for (ItemValue itemValue : mItemValues) {
                path.reset();
                canvas.drawCircle(itemValue.x, itemValue.yHigh, POINT_RADIUS, mPointPaint);
                canvas.drawCircle(itemValue.x, itemValue.yLow, POINT_RADIUS, mPointPaint);
                path.moveTo(itemValue.x, 0 +spacepaddig);
                path.lineTo(itemValue.x, mHeight-spacepaddig);
                canvas.drawPath(path, mMarkLinePaint);
                canvas.drawText(itemValue.high+temperatureType, itemValue.x, spacepaddig, mTextPaint);
                canvas.drawText(itemValue.low+temperatureType, itemValue.x, mHeight, mTextPaint);
            }
            
            
            path.reset();
            path.moveTo(mItemValues.get(0).x, mItemValues.get(0).yHigh);
            for (int i=1; i<mItemValues.size(); i++) {
                if (mItemValues.get(i).x < currentHighPoint.x) {
                    path.lineTo(mItemValues.get(i).x, mItemValues.get(i).yHigh);
                } else {
                    path.lineTo(currentHighPoint.x, currentHighPoint.y);
                    break;
                }
            }
            canvas.drawPath(path, mLinePaint);
            
            
            //drawMidPoints(canvas);
            //drawMidMidPoints(canvas);
            //drawControlPoints(canvas);
            
            /*
            path.reset();
            for (int i = 0; i < mHighPoints.size(); i++) {
				if (i == 0) {
					path.moveTo(mHighPoints.get(i).x, mHighPoints.get(i).y);
						path.quadTo(mHighControlPoints.get(i).x, mHighControlPoints.get(i).y,
							mHighPoints.get(i+1).x, mHighPoints.get(i+1).y);
				} else if (i < mHighPoints.size() - 2) {
					path.cubicTo(mHighControlPoints.get(2*i-1).x, mHighControlPoints.get(2*i-1).y,
							mHighControlPoints.get(2*i).x, mHighControlPoints.get(2*i).y, 
							mHighPoints.get(i+1).x, mHighPoints.get(i+1).y);

				} else if (i == mHighPoints.size() - 2) {
					path.moveTo(mHighPoints.get(i).x, mHighPoints.get(i).y);
					path.quadTo(mHighControlPoints.get(mHighControlPoints.size()-1).x, 
							mHighControlPoints.get(mHighControlPoints.size()-1).y, 
							mHighPoints.get(i+1).x, mHighPoints.get(i+1).y);
				}
			}
            canvas.drawPath(path, mLinePaint);
            
            path.reset();
            for (int i = 0; i < mLowPoints.size(); i++) {
				if (i == 0) {
					path.moveTo(mLowPoints.get(i).x, mLowPoints.get(i).y);
					path.quadTo(mLowControlPoints.get(i).x, mLowControlPoints.get(i).y,
							mLowPoints.get(i+1).x, mLowPoints.get(i+1).y);
				} else if (i < mLowPoints.size() - 2) {
					path.cubicTo(mLowControlPoints.get(2*i-1).x, mLowControlPoints.get(2*i-1).y,
							mLowControlPoints.get(2*i).x, mLowControlPoints.get(2*i).y, 
							mLowPoints.get(i+1).x, mLowPoints.get(i+1).y);
				} else if (i == mLowPoints.size() - 2) {
					path.moveTo(mLowPoints.get(i).x, mLowPoints.get(i).y);
					path.quadTo(mLowControlPoints.get(mLowControlPoints.size()-1).x, 
							mLowControlPoints.get(mLowControlPoints.size()-1).y, 
							mLowPoints.get(i+1).x, mLowPoints.get(i+1).y);
				}
			}
            canvas.drawPath(path, mLinePaint);
            */
            
            path.reset();
            path.moveTo(mItemValues.get(0).x, mItemValues.get(0).yLow);
            for (int i=1; i<mItemValues.size(); i++) {
                if (mItemValues.get(i).x < currentLowPoint.x) {
                    path.lineTo(mItemValues.get(i).x, mItemValues.get(i).yLow);
                } else {
                    path.lineTo(currentLowPoint.x, currentLowPoint.y);
                    break;
                }
            }
            canvas.drawPath(path, mLinePaint);
            
            
        }
    }
    

    private void drawMidPoints(Canvas canvas) {
    	mLinePaint.setColor(Color.BLUE);
        for (int i = 0; i < mHighMidPoints.size(); i++) {
            canvas.drawPoint(mHighMidPoints.get(i).x, mHighMidPoints.get(i).y, mLinePaint);
        }
    }


    private void drawMidMidPoints(Canvas canvas) {
    	mLinePaint.setColor(Color.YELLOW);
        for (int i = 0; i < mHighMidMidPoints.size(); i++) {
            canvas.drawPoint(mHighMidMidPoints.get(i).x, mHighMidMidPoints.get(i).y, mLinePaint);
        }

    }


    private void drawControlPoints(Canvas canvas) {
    	mLinePaint.setColor(Color.GRAY);
        // 画控制点
        for (int i = 0; i < mHighControlPoints.size(); i++) {
            canvas.drawPoint(mHighControlPoints.get(i).x, mHighControlPoints.get(i).y, mLinePaint);
        }
    }
    

    public void startAnimation() {
    	if (mHighPoints.size() == 0 || mLowPoints.size() == 0) {
    		return;
    	}
        ValueAnimator highAnimator = new ValueAnimator();
        highAnimator.setObjectValues(mHighPoints.toArray());
        highAnimator.setEvaluator(new LineEvaluator());
        highAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                if (currentHighPoint.x != point.x) {
                    currentHighPoint = point;
                    LineChartView.this.postInvalidate();
                }
            }
        });

        ValueAnimator lowAnimator = new ValueAnimator();
        lowAnimator.setObjectValues(mLowPoints.toArray());
        lowAnimator.setEvaluator(new LineEvaluator());
        lowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point point = (Point) animation.getAnimatedValue();
                if (currentLowPoint.x != point.x) {
                    currentLowPoint = point;
                }
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(highAnimator, lowAnimator);
        set.setDuration(1500);
        set.start();
    }

    class ItemValue {
        int high;
        int low;
        int x;
        int yHigh;
        int yLow;

        public ItemValue (int high, int low) {
            this.high = high;
            this.low = low;
        }

        public void setXY(int x, int yHigh, int yLow) {
            this.x = x;
            this.yHigh = mHeight - yHigh;
            this.yLow = mHeight - yLow;
        }
    }
}
