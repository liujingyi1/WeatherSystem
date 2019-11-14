package com.gweather.view;

import java.util.ArrayList;
import java.util.List;

import com.gweather.app.R;

import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.gweather.app.PresetCityInfo;
import com.gweather.view.PinterestItemView.Listener;

public class PresetCityListAdapter extends BaseAdapter implements 
        PinnedHeaderListView.PinnedHeaderAdapter, AbsListView.OnScrollListener{

    private List<PresetCityInfo> mList;
    private MySectionIndexer mSectionIndexer;
    private Context mContext;
    private int mLocationPosition = -1;
    private LayoutInflater mInflater;
    private CityClickListener listener;
    private PinnedHeaderListView mListView;
    
	private static final String ALL_CHARACTER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String[] sections = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z" };
	
    public PresetCityListAdapter (Context context, PinnedHeaderListView listView) {
        this.mContext = context;
        this.mListView = listView;
        
		int[] counts = new int[sections.length];
		mSectionIndexer = new MySectionIndexer(sections, counts);
		
        mInflater = LayoutInflater.from(context);
    }
    
    public void setListener(CityClickListener listener) {
    	this.listener = listener;
    }
    
    public void setDate(ArrayList<PresetCityInfo> list) {
    	
    	if (mList != null) {
    		mList.clear();
    	}
    	mList = list;
    	
    	int[] counts = new int[sections.length];
    	for (PresetCityInfo cityString : list) {
    		counts[ALL_CHARACTER.indexOf(cityString.getSoryKey())]++;
    	}
    	
    	mSectionIndexer = new MySectionIndexer(sections, counts);
    	
    	notifyDataSetChanged();
    }
    
    public MySectionIndexer getSectionIndexer() {
    	return mSectionIndexer;
    }
    
    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemViewHolder viewHolder;
        PresetCityItemView view;

        if (convertView == null) {
            view = (PresetCityItemView)mInflater.inflate(R.layout.preset_city_item_view, null);

            final TextView textView = (TextView) view.findViewById(R.id.cityname);
            final TextView sectionView = (TextView) view.findViewById(R.id.section_header);
            final ViewGroup section = (ViewGroup)view.findViewById(R.id.section_header_container);
            viewHolder = new ItemViewHolder(sectionView, textView, section);

            view.setTag(viewHolder);
        } else {
            view = (PresetCityItemView)convertView;
            viewHolder = (ItemViewHolder)view.getTag();
        }

        int section = mSectionIndexer.getSectionForPosition(position);
        
        if (mSectionIndexer.getPositionForSection(section) == position) {
            viewHolder.section.setVisibility(View.VISIBLE);
            viewHolder.sectionView.setText(mList.get(position).getSoryKey());
        } else {
            viewHolder.section.setVisibility(View.GONE);
        }
        
        view.setListener(listener);
        view.loadData(mList.get(position));

        return view;
    }
    

    private class ItemViewHolder {
        protected TextView textView;
        protected TextView sectionView;
        protected ViewGroup section;

        public ItemViewHolder(TextView sectionView, TextView view, ViewGroup section) {
            this.sectionView = sectionView;
            this.textView = view;
            this.section = section;
        }
    }

    @Override
    public int getPinnedHeaderState(int position) {
        int realPosition = position;
        if (realPosition < 0 ||
                (mLocationPosition != -1 && mLocationPosition == realPosition)) {
            return PINNED_HEADER_GONE;
        }
        mLocationPosition = -1;
        int section = mSectionIndexer.getSectionForPosition(realPosition);
        int nextSectionPosition = mSectionIndexer.getPositionForSection(section+1);
        if (nextSectionPosition != -1
                && realPosition == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }
        return PINNED_HEADER_VISIBLE;
    }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) {
    	//alpha 0 ~ 255
        if (position >= 0) {
        	TextView textView = (TextView) header.findViewById(R.id.section_header);
        	textView.setText(mList.get(position).getSoryKey());
        	textView.setTextColor(textView.getTextColors().withAlpha(alpha));
        	textView.invalidate();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (view instanceof PinnedHeaderListView) {
            ((PinnedHeaderListView)view).configureHeaderView(firstVisibleItem);
        }
    }

}
