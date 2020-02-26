package com.megvii.beautify.util;

import android.os.SystemClock;

/**
 * Created by wangshuai on 2018/2/23.
 */

public class TimeTracer {
    private long mStartTime;
    private String mName;
    public void startTrace(String name){
        if(mName!= null){
            stopTrace();
        }
        MLog.e("TimeTracer_"+ mName, "start", null);
        mName = name;
        mStartTime = System.currentTimeMillis();
    }
    public void stopTrace(){
        long time = System.currentTimeMillis()- mStartTime;
        MLog.e("TimeTracer_"+ mName, "end time is " + time, null);
        mName = null;
    }
}
