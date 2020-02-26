package com.megvii.beautify.ui.test.ImageTest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import com.megvii.beautify.R;
import com.megvii.beautify.app.Constant;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.util.Util;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xiejiantao on 2017/8/11.
 */

public class ReadBitmapActivity extends Activity {


    @BindView(R.id.gfv_image)
    RbGlsurfaceview gfvImage;

    public int changeSticher;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readbitmap);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.tv_change_sticker)
    public void wodeClick(View view) {
        switch (view.getId()) {
            case R.id.tv_change_sticker:
                gfvImage.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        //这里只是简单的展示如何使用贴纸
                        changeSticher = (changeSticher+1) % 2;
                        if (changeSticher == 0) {
                            BeaurifyJniSdk.preViewInstance().nativeChangePackage(Constant.sStickerDownloadPath + "airlineStewardess.zip");
                            gfvImage.requestRender();
                        } else {
                            BeaurifyJniSdk.preViewInstance().nativeChangePackage(Constant.sStickerDownloadPath + "bear.zip");
                            gfvImage.requestRender();
                        }
                    }
                });
                break;
        }
    }


}
