package com.gweather.view;

import java.util.Arrays;

import android.widget.SectionIndexer;

public class MySectionIndexer implements SectionIndexer {

    private final String[] mSections;
    private final int[] mPositions;
    private final int mCount;

    public MySectionIndexer (String[] sections, int[] counts) {
        if (sections == null || counts == null) {
            throw new NullPointerException();
        }
        if (sections.length != counts.length) {
            throw new IllegalArgumentException(
                    "The section and counts arrays must have the same length");
        }
        this.mSections = sections;
        mPositions = new int[counts.length];
        int positon = 0;
        for (int i = 0; i < counts.length; i++) {
            if (mSections[i] == null) {
                mSections[i] = "";
            } else {
                mSections[i] = mSections[i].trim();
            }

            mPositions[i] = positon;
            positon += counts[i];
            
        }
        mCount = positon;
    }

    @Override
    public Object[] getSections() {
        return mSections;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= mCount) {
            return -1;
        }

        int index = Arrays.binarySearch(mPositions, position);
        return index >= 0 ? index : -index - 2;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= mSections.length) {
            return -1;
        }
        return mPositions[sectionIndex];
    }

    public int getSectionForLetter(int letter) {
    	if (mSections == null) {
    		return -1;
    	}
    	
        if (letter < 0) {
            return -1;
        }

        for (int i = 0; i < mSections.length; i++) {
        	if (mSections[i].charAt(0) == letter) {
        		return i;
        	}
        }

        return -1;
    }
    
}
