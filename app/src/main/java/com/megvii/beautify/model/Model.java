package com.megvii.beautify.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by liyanshun on 2017/6/28.
 */

public class Model {
    public static int stickerPosition;
    public static int filterPosition;
    @SerializedName("title_chinese")
    public String titleChinese;
    public String zipName;
    public String filter;
    public String sample;
    public String quality;
    @SerializedName("title_english")
    public String titleEnglish;
    public String path;//path未使用 用zipname来算的
    public int imageId;
    public int type;
    public int status;//贴纸状态 0-未下载，1-下载中，2-下载完
}
