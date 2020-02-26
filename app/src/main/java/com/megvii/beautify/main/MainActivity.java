package com.megvii.beautify.main;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.megvii.beautify.R;
import com.megvii.beautify.app.MainApp;
import com.megvii.beautify.cameragl.CameraManager;
import com.megvii.beautify.cameragl.CameraSurfaceView;
import com.megvii.beautify.cameragl.LandMarkView;
import com.megvii.beautify.component.DownLoaderManager;
import com.megvii.beautify.component.SensorEventUtil;
import com.megvii.beautify.main.fragment.BeautyFragmentPreView;
import com.megvii.beautify.main.fragment.BeautyMakeupFragment;
import com.megvii.beautify.main.fragment.BeautyShapeFragment;
import com.megvii.beautify.main.fragment.ListFragment;
import com.megvii.beautify.model.BeautyDownEvent;
import com.megvii.beautify.model.StaticsEvent;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.MLog;
import com.megvii.beautify.util.NoDoubleClickUtil;
import com.megvii.beautify.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends Activity implements View.OnClickListener {
    @BindView(R.id.main_radio_group)
    RadioGroup radioGroup;

    @BindView(R.id.none)
    RadioButton radioButtonNone;

    @BindView(R.id.iv_test)
    ImageView ivTest;

    @BindView(R.id.fragment_content)
    ViewGroup fragmentContent;

    @BindView(R.id.facepp_layout_surfaceview)
    public CameraSurfaceView glSurfaceView;
    @BindView(R.id.main_cameraSwitch)
    Button mSwitchCameraButton;

    @BindView(R.id.landmark_switch)
    Button mSwitchLandMark;

    @BindView(R.id.statics_info)
    TextView tvStatics;

    @BindView(R.id.facepp_layout_debugSwitch)
    Button btnDebugSwitch;

    @BindView(R.id.takepicture)
    Button btnTakePicture;

    private CameraManager mCameraManager;
    private Fragment mCurrentFragment;

    private SensorEventUtil sensorUtil;


    //    test 测试方法保留
    public Handler bitmapTestHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ivTest.setImageBitmap((Bitmap) msg.obj);
        }
    };
//    public static TestVideoActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MLog.i("xie mainactivity create");
        Util.isPreView = true;
        super.onCreate(savedInstanceState);
        //activity = this;
        toggleHideyBar(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ConUtil.toggleHideyBar(this);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                MLog.i("radioGroup:" + checkedId);
                if (NoDoubleClickUtil.isFastDoubleClick()){
                    return;
                }
                showFragment(checkedId);
                radioButtonNone.setChecked(true);

            }
        });

        //根据能力，是否隐藏功能
        if(Util.needGoneBeautify()){
            View beautifyButton = findViewById(R.id.beauty);
            beautifyButton.setVisibility(View.GONE);
        }
        if(Util.needGoneBeautifyShape()){
            View beautifyShapeButton = findViewById(R.id.beautity);
            beautifyShapeButton.setVisibility(View.GONE);
        }
        if(Util.needGoneMakeup()){
            View MakeupButton = findViewById(R.id.makeup);
            MakeupButton.setVisibility(View.GONE);
        }

        glSurfaceView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    if (radioGroup.getVisibility() == View.INVISIBLE) {
                        radioGroup.setVisibility(View.VISIBLE);
                        fragmentContent.setVisibility(View.INVISIBLE);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.remove(mCurrentFragment);
                        fragmentTransaction.commit();
                    } else {
                        if (!mCameraManager.getIsFront()) {
                            mCameraManager.autoFocus();
                        }
                    }
                    Util.isShowOriPic = true;
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Util.isShowOriPic = false;
                    return true;
                }
                return false;
            }
        });
        mCameraManager = new CameraManager(this);
        sensorUtil=new SensorEventUtil(this);
        glSurfaceView.setCameraManager(mCameraManager,sensorUtil);
        mSwitchCameraButton.setOnClickListener(this);
        btnDebugSwitch.setOnClickListener(this);
        mSwitchLandMark.setOnClickListener(this);
        btnTakePicture.setOnClickListener(this);
        EventBus.getDefault().register(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStaticsEvent(StaticsEvent event) {
        tvStatics.setText(event.info);
    }

    private void showFragment(int checkedId) {
        if (checkedId == R.id.none) {
            return;
        }
        radioGroup.setVisibility(View.INVISIBLE);
        fragmentContent.setVisibility(View.VISIBLE);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Bundle data = new Bundle();
        switch (checkedId) {
            case R.id.beauty:
                mCurrentFragment = new BeautyFragmentPreView();
                break;
            case R.id.beautity:
                mCurrentFragment = new BeautyShapeFragment();
                break;
            case R.id.makeup:
                mCurrentFragment = new BeautyMakeupFragment();
                break;
            case R.id.sticker:
                data.putInt(ListFragment.ARGUMENTS, Util.TYPE_STICKER);
                break;
            case R.id.filter:
                data.putInt(ListFragment.ARGUMENTS, Util.TYPE_FILTER);
                break;

        }

        if (!data.isEmpty()) {
            mCurrentFragment = new ListFragment();
            mCurrentFragment.setArguments(data);
        }
        fragmentTransaction.add(R.id.fragment_content, mCurrentFragment);
        fragmentTransaction.commit();

    }


    @Override
    public void onBackPressed() {
        if (radioGroup.getVisibility() == View.INVISIBLE) {
            radioGroup.setVisibility(View.VISIBLE);
            toggleHideyBar(this);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.remove(mCurrentFragment);
            fragmentTransaction.commit();
            fragmentContent.setVisibility(View.INVISIBLE);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        sensorUtil.unRegister();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        MLog.i("xie mainactivity destroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConUtil.toggleHideyBar(this);
        mCameraManager.openCamera();
        MLog.i("xie mainactivity onresume");
        ConUtil.acquireWakeLock(this);
        glSurfaceView.onResume();
        glSurfaceView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MLog.i("xie mainactivity onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        MLog.i("xie mainactivity pause");
        mCameraManager.closeCamera();
        ConUtil.releaseWakeLock();
        glSurfaceView.onPause();
        glSurfaceView.setVisibility(View.INVISIBLE);
    }

    public static void toggleHideyBar(Activity activity) {
        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;


        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

//        if (Build.VERSION.SDK_INT >= 16) {
//            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
//        }
//
        if (Build.VERSION.SDK_INT >= 19) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.landmark_switch:
                Util.isDebugingLandMark = !Util.isDebugingLandMark;
                break;
            case R.id.main_cameraSwitch:
                if (!NoDoubleClickUtil.isDoubleClick()) {
                    mCameraManager.switchCamera(glSurfaceView.getCameraRender());
                    Util.switchcamera = true;
                }
                break;
            case R.id.takepicture:
                if(glSurfaceView!= null) {
                    glSurfaceView.getCameraRender().takePicture(mCameraManager.isFrontCam(), new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, R.string.text_saved, Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    });
                }
                break;
            case R.id.facepp_layout_debugSwitch:
                Util.isDebuging = !Util.isDebuging;
                if (Util.isDebuging) {
                    tvStatics.setVisibility(View.VISIBLE);
                    btnDebugSwitch.setBackgroundResource(R.drawable.debug_open);
                } else {
                    tvStatics.setVisibility(View.INVISIBLE);
                    btnDebugSwitch.setBackgroundResource(R.drawable.debug_close);
                }
                break;
        }
    }

    public GLSurfaceView getSurfaceview() {
        return glSurfaceView;
    }
}
