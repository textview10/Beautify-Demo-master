package com.megvii.beautify.cameragl;

import android.util.Log;

import com.megvii.beautify.model.StaticsEvent;
import com.megvii.beautify.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by xiejiantao on 2017/8/10.
 */

/**
 * 统计时间面板
 */
public class RenderMessageBean {
    private  static volatile RenderMessageBean instance = new RenderMessageBean();
    public static RenderMessageBean   getInstance(){
        return instance;
    }

    private Executor singleThread = Executors.newFixedThreadPool(4);

    public long width;

    public long height;

    private HashMap<String, TraceInfo> mTraceTimeMap = new HashMap<>();

    private synchronized void sendMessage() {
        if (Util.isDebuging){
            StaticsEvent event = new StaticsEvent();
            event.info = getInfo();
            Log.e("RenderMessageBean", "tracerinfo is \n" + event.info);
            EventBus.getDefault().post(event);
        }
    }

    private synchronized String getInfo() {
        StringBuilder sb = new StringBuilder();
        String normalInfo =  "resolution=" + width + "*" + height + "\n";
        sb.append(normalInfo).append("\n");
        Set<Map.Entry<String,TraceInfo>> entryet = mTraceTimeMap.entrySet();
        for(Map.Entry<String,TraceInfo> entry: entryet){
            sb.append(entry.getValue().toString()).append("\n");
        }
        return sb.toString();
    }


    public void traceFps() {
        traceFps(getMethodName());
    }

    private String getMethodName() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[4];
        String methodName = e.getMethodName();
        return methodName;
    }

    public synchronized void traceFps(final String tag) {
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                TraceInfo traceInfo = mTraceTimeMap.get(tag + ".fps");
                if (traceInfo == null) {
                    traceInfo = new FPSTraceInfo();
                    mTraceTimeMap.put(tag + ".fps", traceInfo);
                }
                FPSTraceInfo FPSTraceInfo = (RenderMessageBean.FPSTraceInfo) traceInfo;
                FPSTraceInfo.tag = tag;
                FPSTraceInfo.update();
                sendMessage();
            }
        });
    }

    public synchronized void traceStart(final String tag){
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                TraceInfo traceInfo = mTraceTimeMap.get(tag + ".interval");
                if (traceInfo == null) {
                    traceInfo = new TimeTraceInfo();
                    mTraceTimeMap.put(tag + ".interval", traceInfo);
                }
                TimeTraceInfo info = (TimeTraceInfo) traceInfo;
                info.tag = tag;
                info.startTime = System.currentTimeMillis();
            }
        });
    }

    public synchronized void traceEnd(final String tag){
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                TraceInfo traceInfo = mTraceTimeMap.get(tag + ".interval");
                if (traceInfo == null) {
                    traceInfo = new TimeTraceInfo();
                    mTraceTimeMap.put(tag + ".interval", traceInfo);

                }
                TimeTraceInfo info = (TimeTraceInfo) traceInfo;
                info.tag = tag;
                info.spendTime = info.spendTime + (System.currentTimeMillis() - info.startTime);
                info.count ++;
                if(info.count == 20){
                    info.averageTime = info.spendTime/info.count;
                    info.spendTime = 0;
                    info.count = 0;
                }
                sendMessage();
            }
        });
    }


    private static class TraceInfo{

    }

    private static class FPSTraceInfo extends TraceInfo {
        String tag;
        long fps;
        long averageTime;
        long startTime = System.currentTimeMillis();
        long count;
        private void update() {
            count++;
            if (count == 20) {
                long oldTime = startTime;
                startTime = System.currentTimeMillis();
                long interval = startTime - oldTime;
                fps = count * 1000 / interval;
                averageTime = interval/count;
                count = 0;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(tag).append(".").append("fps :").append(fps);
            return sb.toString();
        }
    }

    private static class TimeTraceInfo extends TraceInfo {

        String tag;

        long startTime;

        long spendTime;
        long count;

        long averageTime;

        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append(tag).append(".").append("average :").append(averageTime);
            return sb.toString();
        }
    }
}