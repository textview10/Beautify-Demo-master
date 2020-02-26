package com.megvii.beautify.cameragl;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.megvii.beautify.R;
import com.megvii.beautify.component.SensorEventUtil;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.util.CaptureUtil;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.KeyPoints;
import com.megvii.beautify.util.LandMarkMatrix;
import com.megvii.beautify.util.MLog;
import com.megvii.beautify.util.NV21Matrix;
import com.megvii.beautify.util.NoDoubleClickUtil;
import com.megvii.beautify.util.ScriptInsics;
import com.megvii.beautify.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES30.*;
import static org.greenrobot.eventbus.EventBus.TAG;

/**
 * Created by liyanshun on 2017/7/3.
 */

public class CameraRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {
    private static boolean USE_OES_TEXTURE = false;
    private Context mContext;
    private int mWidth, mHeight;
    private SurfaceTexture mSurfaceTexture;
    private CameraManager mCameraManager;
    private ICameraMatrix mCameraMatrix;
    private ImageMatrix mImageMatrix;
    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mTextureBuffer;
    private int current_out_tex_idx = 0;
    //the pay load of min face, modify this parameter to larger values will cause the
    //detect face faster.
    private final static int FACE_DETECT_BALANCE_PARAM = 8;
    public int mRenderW = 1920;
    public int mRenderH = 1080;

    public boolean mCameraChange = false;
    /**
     * OpenGL params
     */
//    private ByteBuffer mFullQuadVertices;
    private CameraSurfaceView.RequestRenderListener requestRenderListener;
    private boolean drawCamera = false;
    private int[] mOutTextureId;
    private int mFrontTexture;
    private int mOES_Texture;
    float[] textureCords;
    private ICameraOESMatrix mCameraOESMatrix;
    private LandMarkMatrix mLandMarkMatrix = new LandMarkMatrix();


    private volatile RenderMessageBean messageBean = RenderMessageBean.getInstance();

    /**
     * 解决红米米黑屏的问题  update完了之后直接切换到available 变量就重置不成功了
     * 后期尝试去掉变量
     * update已经去掉变量
     */
    private NV21Matrix nv21Matrix = new NV21Matrix();

    //texture转为buffer
    private TexureToBufferHelper mBufferhelper;
    private SensorEventUtil sensorUtil;
    private CaptureUtil mCaptureUtil;

    public CameraRender(Context context, CameraManager cameraManager, SensorEventUtil sensorUtil) {
        mContext = context;
        mCameraManager = cameraManager;
        mCameraMatrix = new ICameraMatrix(context);
        mImageMatrix = new ImageMatrix(context);
        mCameraOESMatrix = new ICameraOESMatrix(context);

        this.sensorUtil = sensorUtil;
        mCaptureUtil = new CaptureUtil(context, new CaptureUtil.GLExecutor() {
            @Override
            public void runOnRenderThread(Runnable runnable) {
                if (requestRenderListener != null) {
                    requestRenderListener.runOnRenderThread(runnable);
                }
            }
        });
        mVertexBuffer = ByteBuffer.allocateDirect(OpenglUtil.CUBE.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        System.out.println("native order is " + ByteOrder.nativeOrder());

        mVertexBuffer.put(OpenglUtil.CUBE).position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(OpenglUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureBuffer.put(OpenglUtil.TEXTURE_NO_ROTATION).position(0);

        mBufferhelper = new TexureToBufferHelper();
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        MLog.e("surface create ");

        //加载美瞳模板资源
        Util.initTemplates(mContext);
    }

    private boolean isOESMode(){
        boolean oesMode =  USE_OES_TEXTURE && !Util.isTestHAL;
        //Log.e(TAG, "isOESMode " + oesMode);
        return oesMode;
    }


    private void swapIndex(){
        if (current_out_tex_idx == 0){
            current_out_tex_idx = 1;
        } else {
            current_out_tex_idx = 0;
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mCameraManager.mCamera == null) {
            MLog.e("camera open fail");
            return;
        }
        if (NoDoubleClickUtil.isDoubleChanged()) {
            //防止异常
            MLog.e("surface changed twice");
            return;
        }
        MLog.e("surface changed width:" + width + " height:" + height);

        if(mOutTextureId != null) {
            if(mWidth == width && mHeight == height && !mCameraChange){
                return;
            }
            else {
                onDestroy();
                if(mCameraChange){
                    mCameraChange = false;
                }
            }
        }


        mWidth = width;
        mHeight = height;
        messageBean.width = mCameraManager.cameraWidth;
        messageBean.height = mCameraManager.cameraHeight;

        GLES20.glClearColor(0, 0, 0, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        if (mCameraManager.getIsFront()) {
            textureCords = OpenglUtil.TEXTURE_ROTATED_FRONT;
        } else {
            textureCords = OpenglUtil.TEXTURE_ROTATED_BACK;
        }

        mTextureBuffer.clear();
        mTextureBuffer.put(textureCords).position(0);

        //set up surfacetexture------------------

        if (isOESMode()){
            mOES_Texture = mCameraMatrix.getOESTexture();
            //mCameraManager.setViewPort(width, height);
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            mSurfaceTexture = new SurfaceTexture(mOES_Texture);
        }else {
            mFrontTexture = mCameraMatrix.get2DTextureID();
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrontTexture);
            glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    mRenderW, mRenderH, 0,
                    GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, null);
            //mCameraManager.setViewPort(width, height);
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            mSurfaceTexture = new SurfaceTexture(10);
        }
        mCameraManager.actionDetect(this);
        mCameraManager.startPreview(mSurfaceTexture);
        Matrix.setIdentityM(mtx, 0);
        if (isOESMode()) {
            mSurfaceTexture.updateTexImage();
            mCameraOESMatrix.init(mCameraManager.getIsFront());
            mCameraOESMatrix.initCameraFrameBuffer(mRenderW, mRenderH);
            mCameraOESMatrix.onOutputSizeChanged(mRenderW, mRenderH);
            mCameraOESMatrix.setTextureTransformMatrix(mtx);
        }else {
            //双缓冲
            mCameraMatrix.init(mCameraManager.getIsFront());
            mCameraMatrix.initCameraFrameBuffer(mRenderW, mRenderH);
            mCameraMatrix.onOutputSizeChanged(mRenderW, mRenderH);
            mCameraMatrix.setTextureTransformMatrix(mtx);
        }

        mImageMatrix.init();
        mOutTextureId = OpenglUtil.initTextureID(mRenderW, mRenderH);
        MLog.e("onSurfaceChanged: " + Thread.currentThread().getId() + "angle" + mCameraManager.Angle);
        BeaurifyJniSdk.preViewInstance().nativeSetLogLevel(BeaurifyJniSdk.MG_LOG_LEVEL_DEBUG);
        BeaurifyJniSdk.preViewInstance().nativeCreateBeautyHandle(mContext,
                mRenderW,
                mRenderH,
                mCameraManager.Angle
                , Util.MG_FPP_DENSEDETECTIONMODE_PREVIEW,
                ConUtil.getFileContent(mContext, R.raw.mgbeautify_1_2_4_model),
                ConUtil.getFileContent(mContext, R.raw.detect_model),
                ConUtil.getFileContent(mContext,R.raw.dense_model)
        );

        BeaurifyJniSdk.preViewInstance().nativeUseFastFilter(false);

        GLES20.glViewport(0, 0, width, height);

        // changeImageDisplaySize(width, height);
        sceenAutoFit(mWidth, mHeight, mCameraManager.cameraWidth, mCameraManager.cameraHeight, mCameraManager.Angle);

        MLog.e("onSurfaceChanged: " + Thread.currentThread().getId() + "sw" + width + "sh" + height + "cw" + mCameraManager.cameraWidth + "ch" + mCameraManager.cameraHeight + "angle" + mCameraManager.Angle);

        mBufferhelper.onOutputSizeChanged(mRenderW, mRenderH);
        initBeautyParam();
    }
    float[] mtx = new float[16];
    @Override
    public void onDrawFrame(GL10 gl) {

        if(mImageMatrix == null)
            return;
        if(Util.isDebuging){
            messageBean.traceFps("onDrawFrame");
            messageBean.traceStart("onDrawFrame_All");
        }
        if (Util.isTestHAL){
            drawTestHALFrame(gl);
        }else {
            drawNormalFrame(gl);
        }
        if(Util.isDebuging){
            messageBean.traceEnd("onDrawFrame_All");
        }

    }
    public void drawNormalFrame(GL10 gl) {
        //messageBean.traceStart("drawNormalFrame_All");
        //messageBean.traceStart("drawNormalFrame1");

        if (Util.isFilterChanged || !Util.isFilterParamInited) {   //滤镜有变更或者未初始化
            if (TextUtils.isEmpty(Util.filterPath)) {
                BeaurifyJniSdk.preViewInstance().nativeRemoveFilter();
            } else {
                BeaurifyJniSdk.preViewInstance().nativeSetFilter(Util.filterPath);
            }
            Util.isFilterParamInited = true;
            Util.isFilterChanged = false;

        }

        if (Util.isStickerChanged || !Util.isStickerParamInited) {   //滤镜有变更或者未初始化
            if (TextUtils.isEmpty(Util.sCurrentStickerPath)) {
                BeaurifyJniSdk.preViewInstance().nativeDisablePackage();
            } else {
                BeaurifyJniSdk.preViewInstance().nativeChangePackage(Util.sCurrentStickerPath);
            }
            Util.isStickerParamInited = true;
            Util.isStickerChanged = false;
        }

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        int textureID;
        if(isOESMode()) {
            textureID = mCameraOESMatrix.onDrawToTexture(mOES_Texture);
        }else {
            textureID = mCameraMatrix.onDrawToTexture(mFrontTexture);
        }

        int frameTexture = 0;
        if(!Util.isShowOriPic){
            mCameraManager.lock.lock();
            //BeaurifyJniSdk.preViewInstance().nativeProcessTexture(textureID, mOutTextureId[current_out_tex_idx],1);
            BeaurifyJniSdk.preViewInstance().nativeProcessImageInTextureOut(mCameraManager.mDataCache,mCameraManager.cameraWidth,mCameraManager.cameraHeight,mOutTextureId[current_out_tex_idx]);
//            byte[] outImage = new byte[mCameraManager.cameraWidth*mCameraManager.cameraHeight*3/2];
//            BeaurifyJniSdk.preViewInstance().nativeProcessImageInImageOutNV21(mCameraManager.mDataCache,outImage,mCameraManager.cameraWidth,mCameraManager.cameraHeight);
            mCameraManager.lock.unlock();
            swapIndex();

            frameTexture = mOutTextureId[current_out_tex_idx];
        }else{
            frameTexture = textureID;
        }

        if (Util.isDebugingLandMark) {
            mLandMarkMatrix.drawLandMark(frameTexture
                    , mRenderW
                    , mRenderH);
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, mWidth, mHeight);

        mImageMatrix.onDrawFrame(frameTexture, mVertexBuffer, mTextureBuffer);


        if(isOESMode()){
            mSurfaceTexture.updateTexImage();
        }

        //messageBean.traceEnd("drawNormalFrame2");
       // messageBean.traceEnd("drawNormalFrame_All");
    }

    public void drawTestHALFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setIdentityM(mtx, 0);
        //mSurfaceTexture.getTransformMatrix(mtx);
        mCameraMatrix.setTextureTransformMatrix(mtx);

        int textureID = mCameraMatrix.onDrawToTexture(mFrontTexture);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, mWidth, mHeight);

        mImageMatrix.onDrawFrame(textureID, mVertexBuffer, mTextureBuffer);

    }
    private void updateTexture(final byte[] data) {
        if(isOESMode()){
            //do nothing
            //mSurfaceTexture.updateTexImage();
        } else {
            nv21Matrix.setOutputSize(mRenderW, mRenderH);
            nv21Matrix.renderNv21(data
                    , mFrontTexture
                    , mCameraManager.cameraWidth
                    , mCameraManager.cameraHeight
                    , mCameraManager.getIsFront());
        }
    }

    /**
     * 主线程
     *
     * @param surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        MLog.e(" onFrameAvailable startRequestRender" + Thread.currentThread().getId());
    }


    public void setRequestRenderListener(CameraSurfaceView.RequestRenderListener requestRenderListener) {
        this.requestRenderListener = requestRenderListener;
    }

    private void changeImageDisplaySize(int width, int height) {
        int outputWidth = width;
        int outputHeight = height;
        if (mCameraManager.Angle == 270 || mCameraManager.Angle == 90) {
            outputWidth = height;
            outputHeight = width;
        }
        float ratio1 = (float) outputWidth / mCameraManager.cameraWidth;
        float ratio2 = (float) outputHeight / mCameraManager.cameraHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(mCameraManager.cameraWidth * ratioMax);
        int imageHeightNew = Math.round(mCameraManager.cameraHeight * ratioMax);

        float ratioWidth = imageWidthNew / (float) outputWidth;
        float ratioHeight = imageHeightNew / (float) outputHeight;

        if (mCameraManager.Angle == 270 || mCameraManager.Angle == 90) {
            ratioWidth = imageHeightNew / (float) outputHeight;
            ratioHeight = imageWidthNew / (float) outputWidth;
        }

        //从手机顶端开始绘制
        float offset_width = (float) (1.0f - 1.0 / ratioWidth);
        float offset_height = (float) (1.0f - 1.0 / ratioHeight);
        float[] cube = new float[]{
                OpenglUtil.CUBE[0] / ratioHeight + offset_height, OpenglUtil.CUBE[1] / ratioWidth + offset_width,
                OpenglUtil.CUBE[2] / ratioHeight + offset_height, OpenglUtil.CUBE[3] / ratioWidth + offset_width,
                OpenglUtil.CUBE[4] / ratioHeight + offset_height, OpenglUtil.CUBE[5] / ratioWidth + offset_width,
                OpenglUtil.CUBE[6] / ratioHeight + offset_height, OpenglUtil.CUBE[7] / ratioWidth + offset_width};
        mVertexBuffer.clear();
        mVertexBuffer.put(cube).position(0);
    }


    /**
     * 按照centercrop的源码修改，这里viewport相当于已经做过scale了，
     * 所以不需要额外scale，但是dx不需要除以2还不理解，centercrop 是要除的。
     *
     * @param screenW
     * @param screenH
     * @param cameraW
     * @param cameraH
     * @param angle
     */
    public void sceenAutoFit(int screenW, int screenH, int cameraW, int cameraH, int angle) {
        if (angle == 90 || angle == 270) {
            int temp = cameraW;
            cameraW = cameraH;
            cameraH = temp;
        }
        float scale;
        float dx = 0, dy = 0;
        float dxRatio = 0f;
        float dyRatio = 0f;


        if (cameraW * screenH > screenW * cameraH) {
            scale = (float) screenH / (float) cameraH;
            dx = (screenW - cameraW * scale);
            dxRatio = dx / screenW;
        } else {
            scale = (float) screenW / (float) cameraW;
            dy = (screenH - cameraH * scale);
            dyRatio = dy / screenH;
        }
        float[] cube = new float[]{
                OpenglUtil.CUBE[0] + dxRatio, OpenglUtil.CUBE[1] + dyRatio,
                OpenglUtil.CUBE[2] - dxRatio, OpenglUtil.CUBE[3] + dyRatio,
                OpenglUtil.CUBE[4] + dxRatio, OpenglUtil.CUBE[5] - dyRatio,
                OpenglUtil.CUBE[6] - dxRatio, OpenglUtil.CUBE[7] - dyRatio};
        mVertexBuffer.clear();
        mVertexBuffer.put(cube).position(0);
    }

    private byte[] yuv;

    public byte[] scaleUV420(byte[] data, int width, int height, int scale, int[] sz) {
        Log.e("wangshuai", "data length is " + data.length);
        int w = width / scale;
        int h = height / scale;

        if (w % 2 == 1) {
            w += 1;
        }
        if (h % 2 == 1) {
            h += 1;
        }
        sz[0] = w;
        sz[1] = h;
        if (yuv == null || yuv.length!= w * h + w * h / 2) {
            yuv = new byte[w * h + w * h / 2];
        }
        int i = 0;
        for (int k = 0; k < height; k += scale) {
            for (int m = 0; m < width; m += scale) {
                yuv[i] = data[k * width + m];
                i++;
            }
        }
        int size = width * height;
        i = w * h;
        for (int k = 0; k < height / 2; k += scale) {
            for (int m = 0; m < width; m += (2 * scale)) {
                if (i < yuv.length) {
                    yuv[i] = data[size + (k * width) + m];
                    i++;
                }
                if (i < yuv.length) {
                    yuv[i] = data[size + (k * width) + m + 1];
                    i++;
                }
            }
        }
        assert (i == size + size / 2);
        return yuv;
    }

    private ExecutorService threadPoolExecutor = (ExecutorService) Executors.newSingleThreadExecutor();

    private int[] frontPoints = new int[Util.LandMarkPointSize];

    public static interface TakePictureCallBack{
        void onTakPicture(Buffer byteBuffer);
    }

    public void takePicture(final boolean isFrontCam, final Runnable callback){
        int frameTexture = mOutTextureId[0];
        mCaptureUtil.takePicture(frameTexture
                , mRenderW
                , mRenderH
                , mCameraManager.cameraHeight
                , mCameraManager.cameraWidth
                , isFrontCam
                , callback);
    }

    private void reverseBuf(ByteBuffer buf, int width, int height)
    {
        long ts = System.currentTimeMillis();
        int i = 0;
        byte[] tmp = new byte[width * 4];
        while (i++ < height / 2)
        {
            buf.get(tmp);
            System.arraycopy(buf.array(), buf.limit() - buf.position(), buf.array(), buf.position() - width * 4, width * 4);
            System.arraycopy(tmp, 0, buf.array(), buf.limit() - buf.position(), width * 4);
        }
        buf.rewind();
        Log.d(TAG, "reverseBuf took " + (System.currentTimeMillis() - ts) + "ms");
    }

    public Buffer readToBuffer(){

        ByteBuffer buf = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        reverseBuf(buf, mWidth, mHeight);
        return buf;
    }

    public Bitmap getBitmap(Buffer buf) {
        Bitmap bmp = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buf);
        return bmp;
    }

    public void save(Bitmap bmp, String filename){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    byte[] halTestData;
    private boolean isProcessing;
    private  void dealwithFrameTestHAL(final byte[] data, final Camera camera) {
        if(halTestData == null || halTestData.length!= data.length){
            halTestData = new byte[data.length];
        }
        if(isProcessing){
            return;
        }
        isProcessing = true;
        Future<Integer> future =   threadPoolExecutor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                if(isReleased){
                    return 0;
                }
                Util.testHALProcessNV21Video(mContext, data, halTestData, mCameraManager.cameraWidth, mCameraManager.cameraHeight);

                if (requestRenderListener != null) {
                    requestRenderListener.runOnRenderThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTexture(halTestData);
                        }
                    });
                }
                requestRenderListener.startRequestRender();
                isProcessing = false;
                return 0;
            }
        });

    }


    private  void dealwithFrame(final byte[] data, final Camera camera) {
        if (requestRenderListener != null) {
            requestRenderListener.runOnRenderThread(new Runnable() {
                @Override
                public void run() {
                    updateTexture(data);
                }
            });
        }
        double xScale = mRenderW/(double)mCameraManager.cameraWidth;
        double yScale = mRenderH/(double)mCameraManager.cameraHeight;

        onDectBeauty(data, xScale, yScale, mCameraManager.cameraWidth
                , mCameraManager.cameraHeight);
        if (Util.isDebugingLandMark) {
            BeaurifyJniSdk.preViewInstance().nativeGetDenseLMPoints(frontPoints);
            //BeaurifyJniSdk.preViewInstance().nativeGetPoints(frontPoints);
            mLandMarkMatrix.setPoints(frontPoints);
        }

    }

    //byte[] dataCache;
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if(Util.isDebuging){
            messageBean.traceFps("onPreviewFrame");
            messageBean.traceStart("onPreviewFrame");
        }

        if (Util.isTestHAL){
            dealwithFrameTestHAL(data, camera);
        }else {
            dealwithFrame(data, camera);
        }
        requestRenderListener.startRequestRender();
        if(Util.isDebuging){
            messageBean.traceEnd("onPreviewFrame");
        }

    }

    int rotation = 0;
    private void onDectBeauty(final byte[] data, double xScale, double yScale,  final int cameraWidth, final int cameraHeight) {
        //long faceTime = System.currentTimeMillis();
        final int orientation = sensorUtil.orientation;
        if (orientation == 0) 
            rotation = mCameraManager.Angle;
        else if (orientation == 1)
            rotation = 0;
        else if (orientation == 2)
            rotation = 180;
        else if (orientation == 3)
            rotation = 360 - mCameraManager.Angle;
        BeaurifyJniSdk.preViewInstance().nativeDetectFaceOrientation(data, xScale, yScale, cameraWidth, cameraHeight, Util.MG_IMAGEMODE_GRAY, rotation);
    }


    public void deleteTextures(GLSurfaceView mGlSurfaceView) {
        mGlSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                MLog.e("onSurfaceChanged: queueEvent" + Thread.currentThread().getId());
                if(mCameraMatrix != null){
                    mCameraMatrix.destroyFramebuffers();
                    mCameraMatrix.destroy();
                }
                if(mImageMatrix != null){
                   mImageMatrix.destroy();
                }
                if(mBufferhelper != null){
                    mBufferhelper.destroy();
                }
                // GLES20.glDeleteTextures(1, mTextureIds, 0);
                if (mOutTextureId != null) {
                    GLES20.glDeleteTextures(2, mOutTextureId, 0);
                    mOutTextureId = null;
                }
        }
        });
    }

    private boolean isReleased = false;
    public void onDestroy() {
        if(null != mSurfaceTexture)
        {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

        BeaurifyJniSdk.preViewInstance().nativeReleaseResources();
        Util.isFilterParamInited = false;
        Util.isStickerParamInited = false;
        Future<Integer> future =   threadPoolExecutor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Util.testHALReleaseNV21Video();
                isReleased = true;
                return 0;
            }
        });
        try {
            future.get();
        }catch (Throwable throwable){

        }

    }

    private void initBeautyParam() {
        if (mCameraManager.getIsFront()) {
            BeaurifyJniSdk.preViewInstance().nativeSetStickerParam(1.0f);
        } else {
            BeaurifyJniSdk.preViewInstance().nativeSetStickerParam(0.0f);
        }

        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_DENOISE, Util.CURRENT_MG_BEAUTIFY_DENOISE);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS, Util.CURRENT_MG_BEAUTIFY_BRIGHTNESS);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTEN_EYE,Util.CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_TOOTH,Util.CURRENT_MG_BEAUTIFY_TOOTH);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK, Util.CURRENT_MG_BEAUTIFY_ADD_PINK);

        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_SHRINK_FACE, Util.CURRENT_MG_BEAUTIFY_SHRINK_FACE);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ENLARGE_EYE, Util.CURRENT_MG_BEAUTIFY_ENLARGE_EYE);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_THIN_FACE,Util.CURRENT_MG_BEAUTIFY_THIN_FACE);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_EYEBROW,Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBROW);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_EYEBROW,Util.CURRENT_MG_BEAUTIFY_EYEBROW,Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_R,Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_G,Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_B,
                Util.DEFAULT_EYEBROW_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX],null,
                Util.DEFAULT_EYEBROW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX].mKeyPoints,
                Util.DEFAULT_EYEBROW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX].mKeyPointsSize);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_CONTACT_LENS,Util.CURRENT_MG_BEAUTIFY_CONTACTLENS,0,0,0,
                Util.DEFAULT_CONTACT_LENS_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_CONTACT_LENS_TEMPLATE_INDEX],null,null,0);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_LIP,Util.CURRENT_MG_BEAUTIFY_LIP,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_R,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_G,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_B,
                null,null,null,0);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_ADD_BLUSH,Util.CURRENT_MG_BEAUTIFY_BLUSH,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_R,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_G,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_B,
                Util.DEFAULT_BLUSH_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX],null,
                Util.DEFAULT_BLUSH_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX].mKeyPoints,
                Util.DEFAULT_BLUSH_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX].mKeyPointsSize);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_EYESHADOW,Util.CURRENT_MG_BEAUTIFY_EYESHADOW,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_R,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_G,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_B,
                Util.DEFAULT_EYESHADOW_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX],null,
                Util.DEFAULT_EYESHADOW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX].mKeyPoints,
                Util.DEFAULT_EYESHADOW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX].mKeyPointsSize);

        Pair<Bitmap[],Bitmap[]> shadingTemplate = Util.DEFAULT_SHADING_TEMPLATE.get(Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_SHADING,Util.CURRENT_MG_BEAUTIFY_SHADING,0,0,0,
                shadingTemplate.first, shadingTemplate.second,
                Util.DEFAULT_SHADING_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX].mKeyPoints,
                Util.DEFAULT_SHADING_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX].mKeyPointsSize);






    }

}
