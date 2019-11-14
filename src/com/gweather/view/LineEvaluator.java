package com.gweather.view;

import android.animation.TypeEvaluator;
import android.graphics.Point;

public class LineEvaluator implements TypeEvaluator{

    private int x;
    private int y;

    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        Point pointStart = (Point) startValue;
        Point pointEnd = (Point) endValue;

        x = (int) ((pointEnd.x - pointStart.x) * fraction + pointStart.x);
        y = (int) ((pointEnd.y - pointStart.y) * fraction + pointStart.y);

        return new Point(x, y);
    }
}
