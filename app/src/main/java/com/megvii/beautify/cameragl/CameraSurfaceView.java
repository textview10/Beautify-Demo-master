package com.megvii.beautify.cameragl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.megvii.beautify.component.SensorEventUtil;
import com.megvii.beautify.util.MLog;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;


public class CameraSurfaceView extends GLSurfaceView {
    private Context mContext;
    private CameraManager mCameraManager;
    private SensorEventUtil sensorUtil;
    private RequestRenderListener requestRenderListener = new RequestRenderListener() {
        @Override
        public void startRequestRender() {
            requestRender();
        }

        @Override
        public void runOnRenderThread(Runnable runnable) {
            queueEvent(runnable);
        }
    };
    /**
     * Camera and SurfaceTexture
     */
    private CameraRender render;

    public CameraSurfaceView(Context context) {
        super(context);
        mContext = context;
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }


    public CameraRender getCameraRender(){
        return render;
    }


    public void setCameraManager(CameraManager mCameraManager, SensorEventUtil sensorUtil) {
        this.mCameraManager = mCameraManager;
        this.sensorUtil=sensorUtil;
        init();
    }

    private void init() {
        MLog.i("xie mainactivity surface init");
        render = new CameraRender(mContext, mCameraManager,sensorUtil);
        render.setRequestRenderListener(requestRenderListener);
        setPreserveEGLContextOnPause(true);
        setEGLContextClientVersion(3);
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override

    protected void onDetachedFromWindow() {
        MLog.i("xie mainactivity onDetachedFromWindow");
        super.onDetachedFromWindow();
        destroyRender();
    }

    private void destroyRender(){
        if(render != null){
            render.deleteTextures(this);
            render.onDestroy();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        destroyRender();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public interface RequestRenderListener {
        void startRequestRender();
        void runOnRenderThread(Runnable runnable);
    }

}
