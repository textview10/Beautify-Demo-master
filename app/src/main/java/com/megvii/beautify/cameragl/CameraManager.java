package com.megvii.beautify.cameragl;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.megvii.beautify.main.MainActivity;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by liyanshun on 2017/7/3.
 */
public class CameraManager implements Camera.PreviewCallback {
    public Camera mCamera;
    private int mCamraId = 1;  //1是前置摄像头   0是后置摄像头
    public int cameraWidth = 1280;
    public int cameraHeight = 720;
    public int Angle;
    private final static String PHONES_HONGMI = "2014813";
    private final static String PHONES_HUAWEI_P7 = "HUAWEI P7-L09";
    public static final int CAMERA_HAL_API_VERSION_1_0 = 0x100;
    private boolean mUsHAL1 = false;
    private WeakReference<Activity> mActivity;
    private HandlerThread mWorkerThread;
    private Handler mHandler;
    private Object mCameraOpenLock = new Object();

    Lock lock= new ReentrantLock();  // 主要保护mDataCache

    public byte[] mDataCache;
    public CameraManager(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public synchronized void switchCamera(CameraRender render) {
        //render.callFackDataDetect();
        closeCamera();
        mCamraId = (mCamraId + 1) % 2;
        render.mCameraChange = true;
        openCamera();
        ((MainActivity)mActivity.get()).glSurfaceView.setVisibility(View.GONE);
        ((MainActivity)mActivity.get()).glSurfaceView.setVisibility(View.VISIBLE);
        if (!getIsFront()){
            autoFocus();
        }
    }

    public boolean isFrontCam() {
        return mCamraId == 1;
    }

    public synchronized void openCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if(mWorkerThread == null){
            mWorkerThread = new HandlerThread("open_camera_thread");
            mWorkerThread.start();
            mHandler = new Handler(mWorkerThread.getLooper());
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mCameraOpenLock) {
                    if (mUsHAL1) {
                        try {
                            Method method = Camera.class.getMethod("openLegacy", Integer.TYPE, Integer.TYPE);
                            mCamera = (Camera) method.invoke(null, mCamraId, CAMERA_HAL_API_VERSION_1_0);
                        } catch (Throwable throwable) {
                        }
                    } else {
                        mCamera = Camera.open(mCamraId);
                    }
                    mCameraOpenLock.notifyAll();
                }
            }
        });

        synchronized (mCameraOpenLock) {
            while (mCamera == null){
                try {
                    mCameraOpenLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Camera.Parameters param = mCamera.getParameters();

        int width = 1920;
        int height = 1080;
        //Log.w("ceshi", "android.os.Build.MODEL: " + android.os.Build.MODEL);
        if (PHONES_HONGMI.equals(android.os.Build.MODEL)) {
            width = 768;
            height = 432;
        }
        if (PHONES_HUAWEI_P7.equals(android.os.Build.MODEL)) {
            width = 960;
            height = 540;
        }

        Camera.Size bestPreviewSize = calBestPreviewSize(mCamera.getParameters(), width, height);

        cameraWidth = bestPreviewSize.width;
        cameraHeight = bestPreviewSize.height;

        Angle = getAngle();
        //MLog.i("cameraWidth:" + cameraWidth + ", cameraHeight:" + cameraHeight);
        param.setPreviewSize(cameraWidth, cameraHeight);

        mCamera.setParameters(param);
    }

    /**
     * 不是很正规
     * @return
     */
    public boolean getIsFront(){
        if (mCamraId==1){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 通过传入的宽高算出最接近于宽高值的相机大小
     */
    private Camera.Size calBestPreviewSize(Camera.Parameters camPara, final int width, final int height) {
        List<Camera.Size> allSupportedSize = camPara.getSupportedPreviewSizes();
        ArrayList<Camera.Size> widthLargerSize = new ArrayList<Camera.Size>();
        for (Camera.Size tmpSize : allSupportedSize) {
            if (tmpSize.width > tmpSize.height) {
                widthLargerSize.add(tmpSize);
            }
            Log.d("CameraManager", "calBestPreviewSize " + tmpSize.width + ", " + tmpSize.height);
        }

        Collections.sort(widthLargerSize, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int off_one = Math.abs(lhs.width * lhs.height - width * height);
                int off_two = Math.abs(rhs.width * rhs.height - width * height);
                return off_one - off_two;
            }
        });

        return widthLargerSize.get(0);
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        try {
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.startPreview();
            onPreviewStarted(mCamera);
            mCamera.setPreviewCallbackWithBuffer(this);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }

    private Camera.PreviewCallback mCallBack;

    private void onPreviewStarted(Camera camera) {
        Camera.Size s = camera.getParameters().getPreviewSize();
        int wishedBufferSize = s.height * s.width * 3 / 2;
        camera.addCallbackBuffer(new byte[wishedBufferSize]);
        camera.addCallbackBuffer(new byte[wishedBufferSize]);
        camera.addCallbackBuffer(new byte[wishedBufferSize]);
        //camera.addCallbackBuffer(new byte[wishedBufferSize]);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        lock.lock();
        if (mDataCache == null || mDataCache.length!= data.length){
            mDataCache = new byte[data.length];
        }
        System.arraycopy(data, 0, mDataCache, 0, data.length);
        camera.addCallbackBuffer(data);
        if (mCallBack!= null){
            mCallBack.onPreviewFrame(mDataCache, camera);
        }
        lock.unlock();

    }

    public synchronized void autoFocus() {
        if (mCamera != null) {
            try {
                mCamera.cancelAutoFocus();
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.setParameters(parameters);
                mCamera.autoFocus(null);
            }catch (Exception e){

            }

        }
    }

    /**
     * 开始检测脸
     */
    public boolean actionDetect(Camera.PreviewCallback mActivity) {
        mCallBack = mActivity;
        return true;
    }

    public synchronized void closeCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
        if(mWorkerThread!= null){
            mWorkerThread.quit();
            mWorkerThread = null;
        }
    }

    /**
     * 对下面官方的计算一般是计算display 放在这里并不合适
     * 前置270 后置90
     *
     *
     * 前置摄像头

     1.camera onPreviewFrame     和 。    surfacetexture呈现的camera 是镜像的关系
     2.onPreviewFrame逆时针旋转270度和真人呈镜像关系。
     3.surfacetexture 。   旋转90度和真人一样，所以setDisplayOrientation应该是操作的这个图像
     https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
     这是orient的链接项目也用到了 前置摄像头的确Camera.CameraInfo()是270   但是算出来使用的结果是90
     4.项目是先横着镜像，然后处理 （全部传入270度---角度是参照正常物体需要逆时针多少度和图像对应起来）。 然后 旋转270+镜像


     后置摄像头

     1.camera onPreviewFrame     和 。    surfacetexture呈现的camera 是一样的
     2.onPreviewFrame逆时针旋转90度和真人一样。
     3.surfacetexture 。   旋转90度和真人一样，所以setDisplayOrientation应该是操作的这个图像
     https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
     这是orient的链接项目也用到了 后置摄像头的确Camera.CameraInfo()是90   算出来使用的结果也是90
     4.项目是不旋转操作，然后处理 （全部传入90度）。 然后 旋转90
     */

    private int getAngle() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCamraId, info);
        return info.orientation;
    }

    private int getAngleGoogle() {
        int rotateAngle = 90;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCamraId, info);
        if (mActivity.get() == null) {
            return rotateAngle;
        }
        int rotation = mActivity.get().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        Log.e("xie", "xie getAngle: origin onPreviewFrame"+degrees+"orient"+info.orientation);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotateAngle = (info.orientation + degrees) % 360;
            rotateAngle = (360 - rotateAngle) % 360; // compensate the mirror
        } else { // back-facing
            rotateAngle = (info.orientation - degrees + 360) % 360;
        }
        Log.e("xie", "xie getAngle: process"+rotateAngle);
        return rotateAngle;
    }


}
