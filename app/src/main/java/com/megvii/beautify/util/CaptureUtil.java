package com.megvii.beautify.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;

import com.megvii.beautify.cameragl.CameraRender;
import com.megvii.beautify.cameragl.CameraSurfaceView;
import com.megvii.beautify.cameragl.TexureToBufferHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.greenrobot.eventbus.EventBus.TAG;

/**
 * Created by wangshuai on 2018/3/20.
 */

public class CaptureUtil {

    private Context mContext;

    private GLExecutor mGlExecutor;

    private TexureToBufferHelper mTexureToBufferHelper;

    private ExecutorService threadPoolExecutor = (ExecutorService) Executors.newSingleThreadExecutor();

    public CaptureUtil(Context context, GLExecutor glExecutor) {
        mContext = context.getApplicationContext();
        mGlExecutor = glExecutor;
        mTexureToBufferHelper = new TexureToBufferHelper();
    }

    public interface GLExecutor {
        void runOnRenderThread(Runnable runnable);
    }

    /**
     *
     * @param textureId texture id
     * @param width the texture width
     * @param height the texture height
     * @param callback the texture success call back. if failed, no call back
     */
    public void takePicture(int textureId
            , final int texWidth
            , final int texHeight
            , final int width
            , final int height
            , final boolean isFrontCam
            , final Runnable callback) {

        takePicture(new CameraRender.TakePictureCallBack() {
            @Override
            public void onTakPicture(final Buffer byteBuffer) {
                threadPoolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddhhmmss");
                        String _formated = sdf1.format(new Date()) + ".png";
                        String fileName = _formated;
                        Bitmap bitmap = getBitmap(byteBuffer, width, height);
                      //  Bitmap rotatedBmp = rotateBitmap(bitmap, -90);
                        save(bitmap, fileName);
                        callback.run();
                    }
                });
            }
        }, textureId, texWidth, texHeight, width, height,isFrontCam);
    }

    /**
     * 选择变换
     *
     * @param origin 原图
     * @param alpha  旋转角度，可正可负
     * @return 旋转后的图片
     */
    private Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public void takePicture(final CameraRender.TakePictureCallBack callback
            , final int texId
            , final int texWidth
            , final int texHeight
            , final int width
            , final int height
            , final boolean isFrontCam) {
        if (mGlExecutor != null) {
            mGlExecutor.runOnRenderThread(new Runnable() {
                @Override
                public void run() {
                    mTexureToBufferHelper.switchTextureCoords(isFrontCam);
                    mTexureToBufferHelper.onOutputSizeChanged(width, height, texWidth, texHeight);
                    ByteBuffer byteBuffer = mTexureToBufferHelper.getTextureBufferCompress(texId);
                    callback.onTakPicture(byteBuffer);
                }
            });
        }
    }

    private void reverseBuf(ByteBuffer buf, int width, int height) {
        long ts = System.currentTimeMillis();
        int i = 0;
        byte[] tmp = new byte[width * 4];
        while (i++ < height / 2) {
            buf.get(tmp);
            System.arraycopy(buf.array(), buf.limit() - buf.position(), buf.array(), buf.position() - width * 4, width * 4);
            System.arraycopy(tmp, 0, buf.array(), buf.limit() - buf.position(), width * 4);
        }
        buf.rewind();
        Log.d(TAG, "reverseBuf took " + (System.currentTimeMillis() - ts) + "ms");
    }

    public Bitmap getBitmap(Buffer buf, int width, int height) {
       // reverseBuf((ByteBuffer) buf, width,height);
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buf);
        return bmp;
    }

    public void save(Bitmap bmp, String filename) {
        ImageUtils.saveImageToGallery(mContext, bmp,filename);
    }
}
