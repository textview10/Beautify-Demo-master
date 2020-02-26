package com.megvii.beautify.cameragl;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Message;
import android.util.Log;

import com.megvii.beautify.main.MainActivity;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by xiejiantao on 3017/7/25.
 * 提供两种texture转buffer的实现方式
 * 压缩方式分辨率会下降 但是速度会提升
 * 另一种反之
 * <p>
 * 压缩生命周期 new ->onOutputSizeChanged(4 prama)-> getTextureBufferCompress->destroy
 * 非压缩生命周期 new ->onOutputSizeChanged(2 prama)->getOriginPixels->destroy  ---内部有framebuffer管理
 * 推荐第一种用法
 */

public class TexureToBufferHelper {

    private final String mVertexShader =
            "attribute vec4 position;" +
                    "attribute vec4 inputTextureCoordinate;" +
                    "varying vec2 textureCoordinate;" +
                    "void main()" +
                    "{" +
                    "  textureCoordinate = (inputTextureCoordinate).xy;" +
                    "  gl_Position = position;" +
                    "}";
    private final String mFragmentShader =
            "precision mediump float;" +
                    "varying vec2 textureCoordinate;" +
                    "uniform sampler2D inputImageTexture;" +
                    "void main()" +
                    "{" +
                    "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);" +
                    "}";
    protected int mGLProgId;
    protected int mGLAttribPosition;
    protected int mGLUniformTexture;
    protected int mGLAttribTextureCoordinate;
    protected int mOutputWidth;
    protected int mOutputHeight;
    protected int mOrignWidth;
    protected int mOrginHeight;
    protected boolean mIsInitialized;
    protected FloatBuffer mGLCubeBuffer;
    protected FloatBuffer mGLTextureBuffer;

    protected int[] mFrameBuffers = null;
    protected int[] mFrameBufferTextures = null;
    private int mFrameWidth = -1;
    private int mFrameHeight = -1;

    byte readCompressArray[];
    ByteBuffer readCompressBuffer;

    byte readOriginArray[];
    ByteBuffer readOriginBuffer;

    public static final int COMPRESS_TYPE = 0;
    public static final int ORIGIN_TYPE = 1;


    private final float vertexPoint[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    private final float texturePoint[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };

    private final float textureBackCam[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

//    public static final float TEXTURE_NO_ROTATION[] = { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, };


    public TexureToBufferHelper() {

        mGLCubeBuffer = ByteBuffer.allocateDirect(vertexPoint.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mGLCubeBuffer.put(vertexPoint).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(OpenglUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
                mGLTextureBuffer.put(texturePoint).position(0);
//        mGLTextureBuffer.put(TEXTUR／E_NO_ROTATION).position(0);

//        mGLTextureBuffer.put(OpenglUtil.TEXTURE_ROTATED_270).position(0);

    }

    public void switchTextureCoords(boolean isFrontCam) {
        mGLTextureBuffer.put(isFrontCam ? texturePoint : textureBackCam).position(0);
    }



    /**
     * 0-压缩 1，不压缩
     */
    private void init(int type) {
//        if(!mIsInitialized){
            onInit();
//        }
        mIsInitialized = true;
    }

    protected void onInit() {
        mGLProgId = OpenglUtil.loadProgram(mVertexShader, mFragmentShader);
        mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, "position");
        mGLUniformTexture = GLES20.glGetUniformLocation(mGLProgId, "inputImageTexture");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mGLProgId,
                "inputTextureCoordinate");
    }


    /***
     * 压缩方式初始化
     * 初始化必须调用
     * 只支持rgba的texture 非rgba请自己修改为三字节即可
     * outputWidth和outputHeight这两个是要读取的texture的输出宽高，如果texture经过旋转，宽高请互换即可
     * @param outputWidth   读区texture的宽 对应的 out宽  采用不压缩方式可以为0
     * @param outputHeight  读区texture的高 对应的 out高  采用不压缩方式可以为0
     * @param orginWidth    原始宽无压缩输出
     * @param originHeight  原始高无压缩输出
     */

    public void onOutputSizeChanged(final int outputWidth, final int outputHeight, int orginWidth, int originHeight) {
        init(COMPRESS_TYPE);
        mOutputWidth = outputWidth;
        mOutputHeight = outputHeight;
        readCompressArray = new byte[outputWidth * outputHeight * 4];

        readCompressBuffer = ByteBuffer.wrap(readCompressArray);

        mOrignWidth = orginWidth;
        mOrginHeight = originHeight;

        initCameraFrameBuffer(mOutputWidth, mOutputHeight, COMPRESS_TYPE);
    }

    /***
     * 无压缩方式初始化
     * 初始化必须调用
     * 只支持rgba的texture  非rgba请自己修改为三字节即可
     * @param orginWidth    原始宽无压缩输出
     * @param originHeight  原始高无压缩输出
     */

    public void onOutputSizeChanged(int orginWidth, int originHeight) {
        init(ORIGIN_TYPE);

        readOriginArray = new byte[orginWidth * originHeight * 4];

        readOriginBuffer = ByteBuffer.wrap(readOriginArray);

        mOrignWidth = orginWidth;
        mOrginHeight = originHeight;
        initCameraFrameBuffer(orginWidth, originHeight, ORIGIN_TYPE);

    }

    /**
     * 压缩的处理方法
     *
     * @param textureId
     * @return
     */
    public ByteBuffer getTextureBufferCompress(int textureId) {
        return textureToByteBuffer(textureId);
    }

    /**
     * 非压缩原始大小的处理方法
     *
     * @param textureId
     * @return
     */

    public ByteBuffer getOriginPixels(int textureId) {
        int currentFrameid[] = new int[1];

        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, currentFrameid, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);

        ByteBuffer buffer = readOriginPixels();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, currentFrameid[0]);
        return buffer;
    }

    /**
     * 非压缩原始大小的处理方法
     *
     * @param textureId
     * @return
     */

    public ByteBuffer getOriginPixelsByBindTexureId(int textureId) {
                int currentTexured[] = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_TEXTURE_BINDING_2D, currentTexured, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);
//        int currentFrameid[] = new int[1];
//        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, currentFrameid, 0);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
//                GLES20.GL_TEXTURE_2D, textureId, 0);

        ByteBuffer buffer = readOriginPixels();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, currentTexured[0]);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, currentFrameid[0]);
        return buffer;
    }


    public final void destroy() {
        mIsInitialized = false;
        destroyFramebuffers();
        GLES20.glDeleteProgram(mGLProgId);
    }


    private void initCameraFrameBuffer(int width, int height, int type) {
        if (mFrameBuffers != null && (mFrameWidth != width || mFrameHeight != height))
            destroyFramebuffers();
        if (mFrameBuffers == null) {
            mFrameWidth = width;
            mFrameHeight = height;
            mFrameBuffers = new int[1];
            GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
            if (type == COMPRESS_TYPE) {
                mFrameBufferTextures = new int[1];
                GLES20.glGenTextures(1, mFrameBufferTextures, 0);
                bindFrameBuffer(mFrameBufferTextures[0], mFrameBuffers[0], width, height);
            }
        }
    }


    private void bindFrameBuffer(int textureId, int frameBuffer, int width, int height) {

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private ByteBuffer textureToByteBuffer(int textureId) {


        int currentFrameid[] = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, currentFrameid, 0);

        if (mFrameBuffers == null)
            return null;

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
        GLES20.glUseProgram(mGLProgId);
        if (!mIsInitialized) {
            return null;
        }

        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

//        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);

        if (textureId != OpenglUtil.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

//        GLES10.glRotatef(90,3.0f,3.0f,3.0f);
        readCompressBuffer.clear();

        long time1 = System.currentTimeMillis();
        GLES20.glReadPixels(0, 0, mOutputWidth, mOutputHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                readCompressBuffer);

        Log.e("xie", "textureToByteBuffer: readtime" + (System.currentTimeMillis() - time1));

        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, currentFrameid[0]);
//        GLES20.glViewport(0, 0,mOutputWidth  , mOutputHeight);
        return readCompressBuffer;
    }


    private ByteBuffer readOriginPixels() {
        readOriginBuffer.clear();
        long time1 = System.currentTimeMillis();

        GLES20.glReadPixels(0, 0, mOrignWidth, mOrginHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                readOriginBuffer);

//        Log.e("xie", "xie readOriginPixels  time" + (System.currentTimeMillis() - time1));
        return readOriginBuffer;
    }


    private void destroyFramebuffers() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
        mFrameWidth = -1;
        mFrameHeight = -1;
    }


    //下面是测试接口

    private long time = System.currentTimeMillis();

    private int count = 0;
    private long sum;
    private int num = 10;

    public int testCompress(int textureId, MainActivity activity) {
        long time1 = System.currentTimeMillis();

//
        ByteBuffer buffer = getTextureBufferCompress(textureId);
        Bitmap bitmap = getTextureTestBitmap(buffer, mOutputWidth, mOutputHeight);


        Message msg = new Message();
        msg.obj = bitmap;
        activity.bitmapTestHandle.sendMessage(msg);

        if (num > 0) {
            num--;
        } else {
            count++;
            sum = sum + (System.currentTimeMillis() - time1);
            Log.e("xie", "xie getreadpixels  compress time count" + count + "ave" + (sum * 1.0f) / count + "w" + mOutputWidth + "h" + mOutputHeight + "cameraw" + mOrignWidth + "camerah" + mOrginHeight);
        }
        return 0;
    }

    public static int printBufferId(String tag) {

        int currentFrameid[] = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, currentFrameid, 0);

        Log.e("xie", "xie current frameid " +tag+ currentFrameid[0]);
        return currentFrameid[0];
    }


    public int testReadOrigin(int textureId, MainActivity activity) {
//        printBufferId();
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textureId);//bind也不行不bind也不行
        long time1 = System.currentTimeMillis();

        ByteBuffer buffer = readOriginPixels();
        Bitmap bitmap = getTextureTestBitmap(buffer, mOrignWidth, mOrginHeight);


        Message msg = new Message();
        msg.obj = bitmap;
        activity.bitmapTestHandle.sendMessage(msg);
        if (num > 0) {
            num--;
        } else {
            count++;
            sum = sum + (System.currentTimeMillis() - time1);
            Log.e("xie", "xie getreadpixels  origin time count" + count + "ave" + (sum * 1.0f) / count + "w" + mOutputWidth + "h" + mOutputHeight + "cameraw" + mOrignWidth + "camerah" + mOrginHeight);
        }
        return 0;
    }

    public int testReadOriginSave(int textureId, MainActivity activity) {

        ByteBuffer buffer = readOriginPixels();
        Bitmap bitmap = getTextureTestBitmap(buffer, mOrignWidth, mOrginHeight);


        Message msg = new Message();
        msg.obj = bitmap;
        activity.bitmapTestHandle.sendMessage(msg);

        if (Util.switchcount<3&& Util.switchcamera){
            ConUtil.saveBitmap(bitmap);

        }
        Util.switchcount++;
        return 0;
    }

    //useFrameBuffer
    public int testReadOrigin2(int textureId, MainActivity activity) {

        long time1 = System.currentTimeMillis();
        ByteBuffer buffer = getOriginPixels(textureId);
        Bitmap bitmap = getTextureTestBitmap(buffer, mOrignWidth, mOrginHeight);

        Message msg = new Message();
        msg.obj = bitmap;
        activity.bitmapTestHandle.sendMessage(msg);
        if (num > 0) {
            num--;
        } else {
            count++;
            sum = sum + (System.currentTimeMillis() - time1);
            Log.e("xie", "xie getreadpixels  origin time count" + count + "ave" + (sum * 1.0f) / count + "w" + mOutputWidth + "h" + mOutputHeight + "cameraw" + mOrignWidth + "camerah" + mOrginHeight);
        }
        return 0;
    }


    //onlybindTextureid
    public int testReadOrigin3(int textureId, MainActivity activity) {

        long time1 = System.currentTimeMillis();
        ByteBuffer buffer = getOriginPixelsByBindTexureId(textureId);
        Bitmap bitmap = getTextureTestBitmap(buffer, mOrignWidth, mOrginHeight);

        Message msg = new Message();
        msg.obj = bitmap;
        activity.bitmapTestHandle.sendMessage(msg);
        if (num > 0) {
            num--;
        } else {
            count++;
            sum = sum + (System.currentTimeMillis() - time1);
            Log.e("xie", "xie getreadpixels  origin time count" + count + "ave" + (sum * 1.0f) / count + "w" + mOutputWidth + "h" + mOutputHeight + "cameraw" + mOrignWidth + "camerah" + mOrginHeight);
        }
        return 0;
    }


    /**
     * 宽高对应buffer的实际宽高
     *
     * @param buffer
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getTextureTestBitmap(ByteBuffer buffer, int width, int height) {

        byte bitmapBuffer[] = buffer.array();
//   rgba   argb
        int bitmapSource[] = new int[width * height];
        for (int i = 0, j = 0; i < width * height * 4; i++, j++) {

            bitmapSource[j] = (int) (((bitmapBuffer[i++] & 0xFF) << 16)
                    | ((bitmapBuffer[i++] & 0xFF) << 8)
                    | ((bitmapBuffer[i++] & 0xFF))
                    | (bitmapBuffer[i] & 0xFF) << 24);

        }

        return Bitmap.createBitmap(bitmapSource, width, height, Bitmap.Config.ARGB_8888);
    }


    private static void readStaticPixels(int width, int height) {
        byte bitmapBuffer[] = new byte[width * height * 4];

        ByteBuffer intBuffer = ByteBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);
        long time1 = System.currentTimeMillis();

        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                intBuffer);

        Log.e("xie", "xie getreadpixels  sanguo time" + (System.currentTimeMillis() - time1));
    }

    public static void saveBitmap(Bitmap  bm) {
        File f = new File("/sdcard/", "beauty.png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}
