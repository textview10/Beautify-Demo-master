package com.megvii.beautify.main.fragment;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.megvii.beautify.R;
import com.megvii.beautify.app.Constant;
import com.megvii.beautify.app.MainApp;
import com.megvii.beautify.component.DownLoaderManager;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.main.MainActivity;
import com.megvii.beautify.model.Model;
import com.megvii.beautify.model.ModelData;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.FileUtil;
import com.megvii.beautify.util.SharedUtil;
import com.megvii.beautify.util.Util;


import static com.megvii.beautify.model.ModelData.sFilterData;
import static com.megvii.beautify.model.ModelData.sStickData;

/**
 * Created by liyanshun on 2017/7/10.
 */

public class ListPresenter {
    private IView mView;
    private int mType;
    private Gson gson = new Gson();
    private ModelData data;


    public interface IView {
        void showLoading();

        void hideLoading();

        void onUpdateList(ModelData data);
    }

    public ListPresenter(IView view, int type) {
        mType = type;
        mView = view;
    }

    public void onDestory() {
        mView = null;
    }

    public void requestData() {
        if (mType == Util.TYPE_STICKER && sStickData != null) {
            data = sStickData;
        } else if (mType == Util.TYPE_FILTER && sFilterData != null) {
            data = sFilterData;
        }  else {
            String content = SharedUtil.getStringValueByKey(Util.KEYS[mType]);
            data = gson.fromJson(content, ModelData.class);
            if (mType == Util.TYPE_STICKER || mType == Util.TYPE_FILTER ) {
                initBeautyStatus(data);
            }
        }
        if (mView != null) {
            mView.onUpdateList(data);
        }
    }

    /**
     * 下载文件存在就认为下载了
     * 已经过滤第一个元素了
     */
    private void initBeautyStatus(ModelData data) {
        String parentPath;
        if (mType == Util.TYPE_STICKER) {
            sStickData = data;
            parentPath = Constant.sStickerDownloadPath;
        } else  {
            sFilterData = data;
            parentPath = Constant.sFilterDownloadPath;
        }

        for (Model model : data.modelList
                ) {
            String fileName = model.type == Util.TYPE_FILTER ? model.filter : model.zipName;
            if (!TextUtils.isEmpty(fileName) && FileUtil.isFileExist(parentPath + fileName)) {
                model.status = DownLoaderManager.STATE_DOWNLOADED;
            }
        }
    }

    /**
     * 只有下载完成的才会更新positon
     *
     * @param position
     */
    public void handleStickerClick(int position) {
        if (position == 0) {
//            surfaceviewQueueRun(new Runnable() {
//                @Override
//                public void run() {
//                    BeaurifyJniSdk.preViewInstance().nativeDisablePackage();
//                }
//            });
            Util.isStickerChanged = true;
            Util.sCurrentStickerPath = "";
        } else {
            final Model item = data.modelList.get(position);
            if (item.status == DownLoaderManager.STATE_NONE) {
                DownLoaderManager.getInstance().executeDownRunnable(item);
            } else if (item.status == DownLoaderManager.STATE_DOWNLOADED) {
                Util.sCurrentStickerPath = Constant.sStickerDownloadPath + item.zipName;
                Util.isStickerChanged = true;
//                surfaceviewQueueRun(new Runnable() {
//                    @Override
//                    public void run() {
//                        BeaurifyJniSdk.preViewInstance().nativeChangePackage(Constant.sStickerDownloadPath + item.zipName);
//                    }
//                });
            }
        }
    }

    /**
     * 有点黑
     *
     * @return
     */
    private GLSurfaceView getSurfaceview() {
        MainActivity activity = (MainActivity) ((ListFragment) mView).getActivity();
        return activity.getSurfaceview();
    }

    private void surfaceviewQueueRun(Runnable run) {
        GLSurfaceView glSurfacevoew = getSurfaceview();
        if (glSurfacevoew != null) {
            glSurfacevoew.queueEvent(run);
        }
    }

    /**
     * 只有下载完成的才会更新positon
     * 注释保留-注释的方法是错误的
     *
     * @param position
     */

    public void handleFilterClick(int position) {
        if (position == 0) {
            Util.isFilterChanged = true;
            Util.filterPath = "";
//            surfaceviewQueueRun(new Runnable() {
//                @Override
//                public void run() {
//                    BeaurifyJniSdk.nativeRemoveFilter();
//                }
//            });
        } else {
            final Model item = data.modelList.get(position);
            if (item.status == DownLoaderManager.STATE_NONE) {
                DownLoaderManager.getInstance().executeDownRunnable(item);
            } else if (item.status == DownLoaderManager.STATE_DOWNLOADED) {
                Util.isFilterChanged = true;
                Util.filterPath = Constant.sFilterDownloadPath + item.filter;
//                surfaceviewQueueRun(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.e("xie", "click filter run: " + Thread.currentThread().getId());
//                        BeaurifyJniSdk.nativeUseFastFilter(true);
//                        BeaurifyJniSdk.nativeSetFilter(Constant.sFilterDownloadPath + item.filter);
//                    }
//                });

            }
        }
    }





}
