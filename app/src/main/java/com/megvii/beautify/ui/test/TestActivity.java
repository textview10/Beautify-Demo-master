package com.megvii.beautify.ui.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.megvii.beautify.R;
import com.megvii.beautify.app.Constant;
import com.megvii.beautify.app.MainApp;
import com.megvii.beautify.login.LoadingActivity;
import com.megvii.beautify.ui.test.ImageTest.ReadBitmapActivity;
import com.megvii.beautify.ui.test.Interfacetest.TestInitActivity;
import com.megvii.beautify.ui.test.VideoTest.TestVideoActivity;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.FileUtil;

import java.io.File;
import java.util.Observable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * Created by xiejiantao on 2017/8/29.
 */

public class TestActivity extends Activity {

    @BindView(R.id.tv_test_init)
    View tvTestInit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_layout);
        ButterKnife.bind(this);



        Constant.sTestVideoPath = FileUtil.getZipPath(this,"tesvideo");
        File file = new File( Constant.sTestVideoPath);
        if (!file.exists()) {
            file.mkdirs();
            rx.Observable.create(new rx.Observable.OnSubscribe<Object>() {
                @Override
                public void call(Subscriber<? super Object> subscriber) {
                    String pathath = ConUtil.saveAssestsData(MainApp.getContext(), "testvideo",
                            Constant.sTestVideoPath, "testvideo.mp4");
                }
            }).subscribe(new Action1<Object>() {
                @Override
                public void call(Object o) {
                    Toast.makeText(TestActivity.this,"视频初始化完成",Toast.LENGTH_SHORT);
                }
            });
        }




    }


    @OnClick({R.id.tv_test_init ,R.id.tv_beauty_camera,R.id.tv_beauty_img,R.id.tv_beauty_video})
    public void myclick(View view){
        switch (view.getId()){
            case R.id.tv_test_init:
                startActivity(new Intent(this, TestInitActivity.class));
                break;
            case R.id.tv_beauty_camera:
                startActivity(new Intent(this, LoadingActivity.class));
                break;
            case R.id.tv_beauty_img:
                startActivity(new Intent(this, ReadBitmapActivity.class));
                break;
            case R.id.tv_beauty_video:
                startActivity(new Intent(this, TestVideoActivity.class));
                break;
        }
    }
}
