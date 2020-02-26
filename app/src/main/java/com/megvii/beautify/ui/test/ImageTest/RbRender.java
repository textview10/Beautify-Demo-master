package com.megvii.beautify.ui.test.ImageTest;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.megvii.beautify.R;
import com.megvii.beautify.app.Constant;
import com.megvii.beautify.app.MainApp;
import com.megvii.beautify.cameragl.ImageMatrix;
import com.megvii.beautify.cameragl.OpenglUtil;
import com.megvii.beautify.cameragl.TexureToBufferHelper;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.MLog;
import com.megvii.beautify.util.Util;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by xiejiantao on 2017/8/11.
 */

public class RbRender implements GLSurfaceView.Renderer {
    Context mcontext;
    Bitmap bmp;

    private final FloatBuffer mVertexBuffer;
    private final FloatBuffer mTextureBuffer;
    private ImageMatrix mImageMatrix;

    int screenW;
    int screenH;

    public RbRender(Context context) {
        mcontext = context;
        mImageMatrix = new ImageMatrix(context);
        mVertexBuffer = ByteBuffer.allocateDirect(OpenglUtil.CUBE.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mVertexBuffer.put(OpenglUtil.CUBE).position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(OpenglUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureBuffer.put(OpenglUtil.TEXTURE_NO_ROTATION).position(0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        screenW=width;
        screenH=height;
        bmp = getBitmap();
        BeaurifyJniSdk.preViewInstance().nativeCreateBeautyHandle(mcontext, bmp.getWidth(),
                bmp.getHeight(), 0,Util.MG_FPP_DENSEDETECTIONMODE_PREVIEW,
                ConUtil.getFileContent(mcontext, R.raw.mgbeautify_1_2_4_model), ConUtil.getFileContent(mcontext, R.raw.detect_model),ConUtil.getFileContent(mcontext, R.raw.dense_model));


        initBeautyParam();


        mImageMatrix.init();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        byte[] bmpByte = ConUtil.getPixelsRGBA(bmp);

        BeaurifyJniSdk.preViewInstance().nativeDetectFace(bmpByte, bmp.getWidth(), bmp.getHeight(),Util.MG_IMAGEMODE_RGBA);

        int srcText = genTexture(bmp.getWidth(), bmp.getHeight(), bmpByte);
        int dstText = genTexture(bmp.getWidth(), bmp.getHeight(), null);

//        String path = Constant.sStickerDownloadPath + "airlineStewardess.zip";
//        File file = new File(path);
//        if (file.exists()) {
//            BeaurifyJniSdk.nativeChangePackage(path);
//        }
        BeaurifyJniSdk.preViewInstance().nativeProcessTexture(srcText, dstText,1);

        MLog.e("onDrawFrame: image");
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


        GLES20.glViewport(0, 0, screenW, screenH);

        mImageMatrix.onDrawFrame(dstText,mVertexBuffer,mTextureBuffer);

        //  GLES20.glViewport(0, 0, bmp.getWidth(), bmp.getHeight());

        //展示如何获取data和bitmao 按照自己的需要去实现
//        byte[] buffer = new byte[bmp.getWidth() * bmp.getHeight() * 4];
//        ByteBuffer bbufer = ByteBuffer.wrap(buffer);
//        GLES20.glReadPixels(0, 0, bmp.getWidth(), bmp.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bbufer);
//        final Bitmap map = TexureToBufferHelper.getTextureTestBitmap(bbufer, bmp.getWidth(), bmp.getHeight());
    }


    //为啥选这张图，百度一圈就这个穿的最多。。。。这个年代啊
    public Bitmap getBitmap() {
        Resources res = MainApp.getContext().getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.das);
        return bmp;
    }


    public int genTexture(int width, int height, byte[] pixels) {
        int[] mTextureOutID = new int[1];
        ByteBuffer byteBuffer = null;
        if (pixels != null) {
            byteBuffer = ByteBuffer.wrap(pixels);
        }

        GLES20.glGenTextures(1, mTextureOutID, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureOutID[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, (pixels == null) ? null : byteBuffer);
        return mTextureOutID[0];
    }


    private void initBeautyParam() {
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ENLARGE_EYE, 3);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_SHRINK_FACE, 3);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_DENOISE, 3);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS, 3);
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK, 3);
    }

    public void destroy() {
        BeaurifyJniSdk.preViewInstance().nativeReleaseResources();
    }
}
