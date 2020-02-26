package com.megvii.beautify.util;

import android.util.Log;

/**
 * Created by liyanshun on 2017/4/26.
 */

public class MLog {
    private static final String CLASS_NAME = MLog.class.getName();
    public static boolean SHOW_LOG = true;

    private MLog() {
    }

    private static String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(CLASS_NAME)) {
                continue;
            }
            return st.getFileName() + "[Line: " + st.getLineNumber() + "] ";
        }
        return null;
    }

    public static void v(String message) {
        if (SHOW_LOG) {
            Log.v(getFunctionName(), message);
        }
    }

    public static void d(String message) {
        if (SHOW_LOG) {
            Log.d(getFunctionName(), message);
        }
    }

    public static void i(String message) {
        if (SHOW_LOG) {
            Log.i(getFunctionName(), message);
        }
    }

    public static void w(String message) {
        if (SHOW_LOG) {
            Log.w(getFunctionName(), message);
        }
    }

    public static void e(Object message) {
        if (SHOW_LOG) {
            Log.e(getFunctionName(), message.toString());
        }
    }

    public static void e(String tag, String message, Throwable r) {
        if (SHOW_LOG) {
            Log.e(tag, message, r);
        }
    }

    public static void v(String tag, String message, Throwable r) {
        if (SHOW_LOG) {
            Log.v(tag, message, r);
        }
    }
}
