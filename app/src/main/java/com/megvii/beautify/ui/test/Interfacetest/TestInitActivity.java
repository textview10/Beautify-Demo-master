package com.megvii.beautify.ui.test.Interfacetest;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.megvii.beautify.R;
import com.megvii.beautify.cameragl.OpenglUtil;
import com.megvii.beautify.component.TexturePbufferRenderer;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.SysUtil;
import com.megvii.beautify.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xiejiantao on 2017/8/30.
 * 垃圾代码maker
 */

public class TestInitActivity extends Activity {

    @BindView(R.id.et_repeat_count)
    EditText etRepeatCount;
    @BindView(R.id.et_init_repeat_count)
    EditText etInitRepeatCount;
    @BindView(R.id.et_process_repeat_count)
    EditText etProcessRepeatCount;
    @BindView(R.id.et_release_repeat_count)
    EditText etReleaseRepeatCount;
    @BindView(R.id.tv_result)
    TextView tvResult;

    int mRepeatCount;
    int mInitCount;
    int mProcessCount;
    int mReleaseCount;
    String result = "";
    int resCode;
    long time;
    TexturePbufferRenderer surfaceRun;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvResult.setText(result);
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_init_layout);
        ButterKnife.bind(this);


    }


    @OnClick(R.id.tv_exec)
    public void wodeClick(View view) {
        switch (view.getId()) {
            case R.id.tv_exec:
                result ="";
                try {
                    mRepeatCount = Integer.valueOf(etRepeatCount.getText().toString());
                    mInitCount = Integer.valueOf(etInitRepeatCount.getText().toString());
                    mProcessCount = Integer.valueOf(etProcessRepeatCount.getText().toString());
                    mReleaseCount = Integer.valueOf(etReleaseRepeatCount.getText().toString());
                    result += "各个循环次数" + mRepeatCount + " " + mInitCount + " " + mProcessCount + " " + mReleaseCount + "\n";
                }catch (Exception e){
                    Toast.makeText(this,"别拿非法数据糊弄我",Toast.LENGTH_LONG);
                }
                startEXe();
                break;
        }
    }


    public void  startEXe(){

        surfaceRun  = new TexturePbufferRenderer() {

            private int[] mOutTextureId;
            private int[] mInTextureId;

            @Override
            protected boolean draw() {
                time = System.currentTimeMillis();
                for (int i = 0; i < mProcessCount; i++) {
                    resCode |= BeaurifyJniSdk.preViewInstance().nativeProcessTexture(mInTextureId[0], mOutTextureId[0],1);
                }
                time = mProcessCount == 0 ? (long) ((System.currentTimeMillis() - time) * 1.0f / mInitCount) : 0;
                result += "   process完mem" + SysUtil.getNativeMemoryInfo() + "   code" + resCode + "    time" + time + "\n";
                resCode = 0;
                return false;

            }

            @Override
            protected void initGLComponents() {
                //确保只一次
                if (mOutTextureId == null || mOutTextureId.length < 1) {
                    mOutTextureId = OpenglUtil.initTextureID(1280, 720);
                    mInTextureId = OpenglUtil.initTextureID(1280, 720);
                }

                time = System.currentTimeMillis();
                for (int i = 0; i < mInitCount; i++) {
                    //既没有崩溃也没有出错，然后从这里中断了。错误信息没打印出来，单元测试还通过了
                    resCode |= BeaurifyJniSdk.preViewInstance().nativeCreateBeautyHandle(TestInitActivity.this, 1280,
                            720, 90, Util.MG_FPP_DENSEDETECTIONMODE_PREVIEW,
                            ConUtil.getFileContent(TestInitActivity.this, R.raw.mgbeautify_1_2_4_model),
                            ConUtil.getFileContent(TestInitActivity.this, R.raw.detect_model),
                            ConUtil.getFileContent(TestInitActivity.this, R.raw.dense_model)
                    );

                    System.out.println("OpenGL init OK. start draw222...");
                }
                time = (long) ((System.currentTimeMillis() - time) * 1.0f / mInitCount);
                result += "   init完mem" + SysUtil.getNativeMemoryInfo() + "   code" + resCode + "    time" + time + "\n";
                resCode = 0;
                System.out.println("OpenGL init OK. start draw44..." + resCode);
            }

            @Override
            protected void deinitGLComponents() {

                time = System.currentTimeMillis();
                for (int i = 0; i < mReleaseCount; i++) {
                    resCode |= BeaurifyJniSdk.preViewInstance().nativeReleaseResources();
                }
                time = mReleaseCount == 0 ? (long) ((System.currentTimeMillis() - time) * 1.0f / mReleaseCount) : 0;
                result += "   release完mem" + SysUtil.getNativeMemoryInfo() + "   code" + resCode + "    time" + time + "\n";
                resCode = 0;
                if (mOutTextureId != null || mOutTextureId.length > 1) {
                    GLES20.glDeleteTextures(1, mOutTextureId, 0);
                    GLES20.glDeleteTextures(1, mInTextureId, 0);

                }

            }

            @Override
            public SurfaceTexture getSurfaceTexture() {
                return null;
            }

            @Override
            public void run() {
                System.out.println("OpenGL init OK. start draw1...");
                initEGL();
                result += "初始内存" + SysUtil.getNativeMemoryInfo();
                for (int i = 0; i < mRepeatCount; i++) {
                    initGLComponents();
                    Log.d(LOG_TAG, "OpenGL init OK. start draw3...");
                    draw();
                    deinitGLComponents();
                }


                deinitEGL();
                result += "   release完mem" + SysUtil.getNativeMemoryInfo() ;
                handler.sendEmptyMessage(0);
            }
        };
    }


}
