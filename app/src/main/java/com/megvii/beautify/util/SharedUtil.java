package com.megvii.beautify.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.megvii.beautify.app.MainApp;

import java.util.Map;


/**
 * Save Data To SharePreference Or Get Data from SharePreference
 *
 * @author wanglx
 *         通过SharedPreferences来存储数据，自定义类型
 */
public class SharedUtil {
    private static final String FILE_NAME = "Megvii";

    public static final String VERSION_CODE="version_code";

    /**
     * Set int value into SharePreference
     *
     */
    //通过SharedPreferences来存储键值对
    public static void saveIntValue(String key, int value) {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    /**
     * Set int value into SharePreference
     *
     */
    //通过SharedPreferences来存储键值对
    public static void saveLongValue(String key, long value) {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static void writeDownStartApplicationTime() {
        SharedPreferences sp = MainApp.getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
//		Calendar calendar = Calendar.getInstance();
        //Date now = calendar.getTime();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd:hh-mm-ss");
        SharedPreferences.Editor editor = sp.edit();
        //editor.putString("启动时间", now.toString());
        editor.putLong("nowtimekey", now);
        editor.commit();

    }

    /**
     * Set Boolean value into SharePreference
     *
     * @param
     * @param key
     * @param value
     */
    public static void saveBooleanValue(String key, boolean value) {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    /**
     * Remove key from SharePreference
     *
     * @param key
     */
    public static void removeSharePreferences(String key) {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePre.edit();
        editor.remove(key);
        editor.commit();
    }

    /**
     * @param key
     * @return
     */
    public static boolean contains(String key) {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sharePre.contains(key);
    }

    /**
     * Get all value
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getAllMap() {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return (Map<String, Object>) sharePre.getAll();
    }

    /**
     * Get Integer Value
     *
     * @param key
     * @return
     */
    public static Integer getIntValueByKey(String key) {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sharePre.getInt(key, -1);
    }

    /**
     * Get Integer Value
     *
     * @param key
     * @return
     */
    public static Long getLongValueByKey(String key) {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sharePre.getLong(key, -1);
    }


    /**
     * Set String value into SharePreference
     *
     * @param key
     * @param value
     */
    public static void saveStringValue(String key, String value) {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Get String Value
     * 通过输入的key来获得userid
     *
     * @param key
     * @return
     */
    public static String getStringValueByKey(String key) {
        Context context = MainApp.getContext();
        SharedPreferences sharePre = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sharePre.getString(key, null);
    }

    public static Boolean getBooleanValueByKey(String key) {
        SharedPreferences sharePre = MainApp.getContext().getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sharePre.getBoolean(key, false);
    }

    /**
     * Get Value, Remove key
     *
     * @param key
     * @return
     */
    public static Integer getIntValueAndRemoveByKey(String key) {
        Integer value = getIntValueByKey(key);
        removeSharePreferences(key);
        return value;
    }

    /**
     * 设置userkey
     *
     * @param userkey
     */
    public static void setUserkey(String userkey) {
        saveStringValue("params_userkey", userkey);
    }

    /**
     * 获取userkey
     */
    public static String getUserkey() {
        return getStringValueByKey("params_userkey");
    }

}
