package com.megvii.beautify.ui.test.VideoTest;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.megvii.beautify.R;
import com.megvii.beautify.cameragl.ICameraMatrix;
import com.megvii.beautify.cameragl.ImageMatrix;
import com.megvii.beautify.cameragl.OpenglUtil;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by xiejiantao on 2017/8/11.
 * http://blog.csdn.net/martin20150405/article/details/53316174
 * 可以参考这个不过还是重写了
 */
public class GLRenderer implements GLSurfaceView.Renderer
        , SurfaceTexture.OnFrameAvailableListener, MediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = "GLRenderer";
    private Context context;
    private int aPositionHandle;

    private FloatBuffer vertexBuffer;
    private final float[] vertexData = {
            -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
    };

    private final float[] projectionMatrix = new float[16];


    private ICameraMatrix mCameraMatrix;
    private ImageMatrix mImageMatrix;

    private final float[] textureVertexData = {
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
    };
    private FloatBuffer textureVertexBuffer;

    private int textureId;

    private SurfaceTexture surfaceTexture;
    private MediaPlayer mediaPlayer;
    private float[] mSTMatrix = new float[16];


    private boolean updateSurface;
    private boolean playerPrepared;
    private int screenWidth, screenHeight;
    private int mVideoWidth, mVideoHeight;

    byte readOriginArray[];
    ByteBuffer readOriginBuffer;

    int dstText;

    int srcText;
    boolean isChanged;

    public GLRenderer(Context context, String videoPath) {
        this.context = context;
        mCameraMatrix = new ICameraMatrix(context);
        mImageMatrix = new ImageMatrix(context);
        playerPrepared = false;
        synchronized (this) {
            updateSurface = false;
        }
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertexData);
        textureVertexBuffer.position(0);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, Uri.parse(videoPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);

        mediaPlayer.setOnVideoSizeChangedListener(this);


    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {



        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        textureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        ShaderUtils.checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);

        Surface surface = new Surface(surfaceTexture);
        mediaPlayer.setSurface(surface);
        surface.release();

        if (!playerPrepared) {
            try {
                mediaPlayer.prepare();
                playerPrepared = true;
            } catch (IOException t) {
                Log.e(TAG, "media player prepare failed");
            }
            mediaPlayer.start();
            playerPrepared = true;
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: " + width + " " + height+"  thread"+Thread.currentThread().getId());
        screenWidth = width;
        screenHeight = height;

        mImageMatrix.init();

        isChanged=true;
    }

    @Override
    public void onDrawFrame(GL10 gl) {


        Log.d(TAG, "onDrawFrame: " );

        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        synchronized (this) {
//            if (updateSurface) {
                surfaceTexture.updateTexImage();
                updateSurface = false;
//            }
        }
        if (isChanged&&mVideoWidth>0){
            isChanged=false;
            readOriginArray = new byte[mVideoWidth * mVideoHeight * 4];
            Log.d(TAG, "nativeCreateBeautyHandle: " + mVideoWidth );
            readOriginBuffer = ByteBuffer.wrap(readOriginArray);
            dstText = OpenglUtil.initTextureID(mVideoWidth, mVideoHeight)[0];
            srcText = OpenglUtil.initTextureID(mVideoWidth, mVideoHeight)[0];

            mCameraMatrix.init(false);
            mCameraMatrix.initCameraFrameBuffer(mVideoWidth, mVideoHeight);
            mCameraMatrix.onOutputSizeChanged(mVideoWidth, mVideoHeight);

            BeaurifyJniSdk.preViewInstance().nativeCreateBeautyHandle(context, mVideoWidth,
                    mVideoHeight, 0, Util.MG_FPP_DENSEDETECTIONMODE_PREVIEW,
                    ConUtil.getFileContent(context, R.raw.mgbeautify_1_2_4_model), ConUtil.getFileContent(context, R.raw.detect_model),ConUtil.getFileContent(context, R.raw.dense_model));
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        }else if (mVideoWidth<=0){
            return;
        }


        surfaceTexture.getTransformMatrix(mSTMatrix);
        mCameraMatrix.setTextureTransformMatrix(mSTMatrix);







        int srcTextureID = mCameraMatrix.onDrawToTexture(textureId);



        if (isChanged==false){
            readOriginBuffer.clear();

            BeaurifyJniSdk.preViewInstance().nativeProcessTexture(srcTextureID, dstText,1);
            GLES20.glReadPixels(0, 0, mVideoWidth, mVideoHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                    readOriginBuffer);
            BeaurifyJniSdk.preViewInstance().nativeDetectFace(readOriginBuffer.array(), mVideoWidth, mVideoHeight,Util.MG_IMAGEMODE_RGBA);
            GLES20.glGetError();

        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
        GLES20.glViewport(0, 0, screenWidth, screenHeight);
        mImageMatrix.onDrawFrame(dstText, vertexBuffer, textureVertexBuffer);

    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        updateSurface = true;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.d(TAG, "onVideoSizeChanged: " + width + " " + height+"  thread"+Thread.currentThread().getId());
        mVideoWidth = width;
        mVideoHeight = height;
        updateProjection(width, height);

    }

    private void updateProjection(int videoWidth, int videoHeight) {
        float screenRatio = (float) screenWidth / screenHeight;
        float videoRatio = (float) videoWidth / videoHeight;
        if (videoRatio > screenRatio) {
            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, -videoRatio / screenRatio, videoRatio / screenRatio, -1f, 1f);
        } else
            Matrix.orthoM(projectionMatrix, 0, -screenRatio / videoRatio, screenRatio / videoRatio, -1f, 1f, -1f, 1f);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void destroy() {
        BeaurifyJniSdk.preViewInstance().nativeReleaseResources();
    }
}