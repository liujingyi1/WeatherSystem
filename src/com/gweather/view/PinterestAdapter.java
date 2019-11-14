package com.gweather.view;

import java.util.ArrayList;

import javax.security.auth.PrivateCredentialPermission;

import com.gweather.app.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gweather.app.PresetCityInfo;
/**
 * Created by jingyi.liu on 2016/10/19.
 */

public class PinterestAdapter extends BaseAdapter {

    private CityClickListener mListener;
    private Context mContext;
    private Resources mResources;
    private ArrayList<PresetCityInfo> mCityList;
    private ArrayList<RowConfig> mRowCountList;
    ListView mListView;
    private LayoutInflater mInflater;
    private int mItemSpace;
    private int mRootWidth;

    public PinterestAdapter(Context context, CityClickListener listener, ListView listView) {
        mContext = context;
        mListener = listener;
        mListView = listView;
        mResources = mContext.getResources();
        mInflater = LayoutInflater.from(context);
    }

    public void setDate(ArrayList<PresetCityInfo> list) {
    	if (mCityList != null) {
    		mCityList.clear();
    	}
    	mCityList = list;
    	initRowCount();
    	notifyDataSetChanged();
    }

    private class RowConfig {
        public RowConfig(int count, int totalWidth) {
            this.count = count;
            this.totalWidth = totalWidth;
        }
        public int count;
        public int totalWidth;
    }
    
    public void initRowCount() {
        mRowCountList = new ArrayList<RowConfig>();

        if (mCityList != null) {
	        mRootWidth = mListView.getWidth();
	        mItemSpace = mResources.getDimensionPixelOffset(R.dimen.pinterest_item_space);
	        int itemPadding = mResources.getDimensionPixelOffset(R.dimen.pinterest_item_padding);
	        final ViewGroup itemView = (ViewGroup)mInflater.inflate(R.layout.pinterest_item_view, null);
	        Rect bounds = new Rect();
	        TextPaint paint;
	        paint = ((TextView)itemView.findViewById(R.id.pinterest_text)).getPaint();
	
	        int rowIndex = 0;
	        int totalWidth = 0;
	        Integer count = 0;
	
	        for (int i = 0; i < mCityList.size(); i++) {
	            if (totalWidth >= mRootWidth && i != 0) {
	                RowConfig item = new RowConfig(count, mRootWidth);
	                mRowCountList.add(rowIndex, item);
	                rowIndex++;
	                count = 0;
	                totalWidth = 0;
	            }
	
	            String text = mCityList.get(i).getCityName();
	            paint.getTextBounds(text, 0, text.length(), bounds);
	            int width = bounds.width();
	            int currentWidth = count == 0 ? width + 2*itemPadding : width + mItemSpace + 2*itemPadding;
	            totalWidth += currentWidth;
	
	            if (totalWidth < mRootWidth) {
	                count++;
	            } else {
	                if (count == 0) {
	                    RowConfig item = new RowConfig(1, mRootWidth);
	                    mRowCountList.add(rowIndex, item);
	                    rowIndex++;
	                    count = 0;
	                    totalWidth = 0;
	                } else {
	                    RowConfig item = new RowConfig(count, totalWidth - currentWidth);
	                    mRowCountList.add(rowIndex, item);
	                    rowIndex++;
	                    count = 1;
	                    totalWidth = width + 2 * itemPadding;
	                }
	            }
	        }
	
	        if (count > 0) {
	            mRowCountList.add(rowIndex, new RowConfig(count, totalWidth));
	        }
	        
	        Bitmap image = BitmapFactory.decodeResource(mResources, R.drawable.citi_background_normal);
	        if (image != null) {
	        	ViewGroup.LayoutParams lp = mListView.getLayoutParams();
	        	lp.height = (image.getHeight() + mItemSpace) * mRowCountList.size();
	            mListView.setLayoutParams(lp);
	        }
        }
        
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        return mRowCountList == null ? 0 : mRowCountList.size();

    }

    @Override
    public ArrayList<PresetCityInfo> getItem(int position) {
        if (mRowCountList != null) {
            int columnCount = mRowCountList.get(position).count;
            ArrayList<PresetCityInfo> resultList = new ArrayList<PresetCityInfo>(columnCount);
            int count = 0;
            for (int i = 0; i < position; i++) {
                count += mRowCountList.get(i).count;
            }
            for (int i = 0; i < columnCount; i++) {
                resultList.add(mCityList.get(count));
                count++;
            }
            return resultList;
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        PinterestRow pinterestRow = (PinterestRow)convertView;
        ArrayList<PresetCityInfo> cityInfos = getItem(position);

        if (pinterestRow == null) {
            pinterestRow = new PinterestRow(mContext, position);
        }
        pinterestRow.configureRow(cityInfos, position == getCount() - 1);

        return pinterestRow;
    }

    private class PinterestRow extends FrameLayout {

        int position;

        public PinterestRow(Context context, int position) {
            super(context);
            setFocusable(false);
            this.position = position;
        }

        public void configureRow(ArrayList<PresetCityInfo> list, boolean isLastRow) {
            int columnCount = list.size();
            for (int columnCounter = 0; columnCounter < columnCount; columnCounter++) {
                PresetCityInfo data = list.get(columnCounter);
                addItem(data, columnCounter, isLastRow);
            }
        }

        private void addItem(PresetCityInfo cityInfo, int childIndex, boolean islastRow) {
            final PinterestItemView itemview;
            if (getChildCount() <= childIndex) {
                itemview = (PinterestItemView) inflate(mContext, R.layout.pinterest_item_view, null);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(
                        0,
                        mItemSpace / 2,
                        0,
                        mItemSpace / 2);
                itemview.setLayoutParams(params);
                itemview.setListener(mListener);
                addView(itemview);
            } else {
                itemview = (PinterestItemView)getChildAt(childIndex);
            }
            itemview.loadData(cityInfo);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            //super.onLayout(changed, left, top, right, bottom);
            final int count = getChildCount();
            int childLeft = left + (mRootWidth - mRowCountList.get(position).totalWidth) / 2;
            View child = null;
            
            for (int i = 0; i < count; i++) {
                child = getChildAt(i);
                int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, mItemSpace / 2, childLeft + childWidth, mItemSpace / 2 + child.getMeasuredHeight());
                childLeft += mItemSpace + childWidth;
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        }
    }
}
