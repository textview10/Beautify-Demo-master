package com.megvii.beautify.login;

import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.megvii.beautify.R;
import com.megvii.beautify.app.Constant;
import com.megvii.beautify.app.MainApp;
import com.megvii.beautify.model.Model;
import com.megvii.beautify.model.ModelData;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.FileUtil;
import com.megvii.beautify.util.SharedUtil;
import com.megvii.beautify.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.AsyncOnSubscribe;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;

/**
 * Created by liyanshun on 2017/4/26.
 */

public class LoginPresenter {
    ILoginView mView;

    public LoginPresenter() {
    }

    public LoginPresenter(ILoginView mView) {
        this.mView = mView;
    }

    //保存资源的id 容易导致资源错序，升级版本避免
    public void init() {
        if (TextUtils.isEmpty(SharedUtil.getStringValueByKey(Util.KEYS[Util.TYPE_FILTER]))
                || (FileUtil.getVersionCode(MainApp.getContext()) != SharedUtil.getIntValueByKey(SharedUtil.VERSION_CODE))) {
            Observable.just("")
                    .map(new Func1<String, String>() {
                        @Override
                        public String call(String s) {
                            initStickerResource();
                            initFilterResource();
                            return null;
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            if(mView != null){
                                mView.initComplete();
                            }

                            SharedUtil.saveIntValue(SharedUtil.VERSION_CODE, FileUtil.getVersionCode(MainApp.getContext()));
                        }
                    });
        } else {
            if(mView!=null){
                mView.initComplete();
            }
        }
    }

    private void initStickerResource() {
        List<Model> list = getModels("stickerData.json");
        Class drawable = R.drawable.class;
        ModelData data = new ModelData(list);
        try {
            for (Model bean : list) {
                Field field = drawable.getField(bean.sample);
                bean.imageId = field.getInt(field.getName());
                String pathath = ConUtil.saveAssestsData(MainApp.getContext(), "sticker",
                        Constant.sStickerDownloadPath, bean.zipName);
                bean.type = Util.TYPE_STICKER;
            }
        } catch (Exception e) {

        }
        addFirstItem(list);
        SharedUtil.saveStringValue(Util.KEYS[Util.TYPE_STICKER], new Gson().toJson(data));
    }

    private void initFilterResource() {
        List<Model> list = getModels("filterData.json");
        Class drawable = R.drawable.class;
        ModelData data = new ModelData(list);
        try {
            for (Model bean : list) {
                Field field = drawable.getField(bean.sample);
                bean.imageId = field.getInt(field.getName());
                String pathath = ConUtil.saveAssestsData(MainApp.getContext(), "filter",
                        Constant.sFilterDownloadPath, bean.filter);
                bean.type = Util.TYPE_FILTER;
            }
        } catch (Exception e) {

        }
        addFirstItem(list);
        SharedUtil.saveStringValue(Util.KEYS[Util.TYPE_FILTER], new Gson().toJson(data));
    }

    private void addFirstItem(List<Model> list) {
        Model model = new Model();
        model.titleChinese = "原图";
        model.titleEnglish = "Origin";
        model.imageId = R.drawable.sticker_cancle;
        list.add(0, model);

    }


    private List<Model> getModels(String path) {
        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            inputStream = MainApp.getContext().getResources().getAssets().open(path);
            byte[] buffer = new byte[1024];
            baos = new ByteArrayOutputStream();

            int count = 0;
            while ((count = inputStream.read(buffer)) > 0) {
                baos.write(buffer, 0, count);
            }

            String filterStr = new String(baos.toByteArray());

            Type type = new TypeToken<List<Model>>() {
            }.getType();
            Object fromJson2 = new Gson().fromJson(filterStr, type);
            List<Model> list = (List<Model>) fromJson2;
            return list;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
