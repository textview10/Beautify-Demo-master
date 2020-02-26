package com.megvii.beautify.ui.test.VideoTest;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.megvii.beautify.R;
import com.megvii.beautify.app.Constant;
import com.megvii.beautify.app.MainApp;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.util.ConUtil;

public class TestVideoActivity extends Activity {

    private GLSurfaceView glSurfaceView;
    private GLRenderer glRenderer;

    int changeSticher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_video);
        glSurfaceView= (GLSurfaceView) findViewById(R.id.surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glRenderer=new GLRenderer(this,  Constant.sTestVideoPath+"testvideo.mp4");
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);


        findViewById(R.id.tv_change_sticker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                glSurfaceView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        //这里只是简单的展示如何使用贴纸
                        changeSticher = (changeSticher+1) % 2;
                        if (changeSticher == 0) {
                            BeaurifyJniSdk.preViewInstance().nativeChangePackage(Constant.sStickerDownloadPath + "bear.zip");

                        } else {
                            BeaurifyJniSdk.preViewInstance().nativeChangePackage(Constant.sStickerDownloadPath + "airlineStewardess.zip");

                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        glRenderer.destroy();
        glRenderer.getMediaPlayer().release();
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
        glRenderer.getMediaPlayer().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
}