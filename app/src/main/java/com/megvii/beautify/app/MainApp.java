package com.megvii.beautify.app;

import android.app.Application;
import android.content.Context;

import com.megvii.beautify.util.FileUtil;
import com.megvii.beautify.util.Util;

import java.io.File;


/**
 * Created by liyanshun on 2017/4/24.
 */

public class MainApp extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
        initFiles();

    }


    private void initFiles() {

        File cacheDir;

        Constant.sStickerDownloadPath = FileUtil.getZipPath(mContext,"sticker");
         cacheDir = new File( Constant.sStickerDownloadPath);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        Constant.sFilterDownloadPath = FileUtil.getZipPath(mContext,"filter");
         cacheDir = new File( Constant.sFilterDownloadPath);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        Util.mRandomFrestModelPath = FileUtil.getDiskCachePath(mContext) + "/random_forest_model/";
        cacheDir = new File( Util.mRandomFrestModelPath);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

    }

}
