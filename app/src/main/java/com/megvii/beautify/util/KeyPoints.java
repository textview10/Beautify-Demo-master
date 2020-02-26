package com.megvii.beautify.util;

import android.graphics.PointF;

public class KeyPoints{
    KeyPoints(int PointSize){
        mKeyPointsSize = PointSize;
        mKeyPoints = new float[PointSize*2];
    }
    public float[] mKeyPoints = null;
    public int mKeyPointsSize = 0;
}

