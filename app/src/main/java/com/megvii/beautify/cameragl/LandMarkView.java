package com.megvii.beautify.cameragl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.util.Util;

import java.io.ByteArrayOutputStream;

/**
 * Created by wangshuai on 2018/2/19.
 */

public class LandMarkView extends View {

    public static volatile LandMarkView markViewDrawYUV;

    private int[] points = new int[Util.LandMarkPointSize];

    private Bitmap bmp;
    private boolean drawYUV = false;
    private boolean visible;

    public boolean isVisible(){
        return visible;
    }
    public void setVisible(boolean visible){
        this.visible = visible;
        invalidate();
    }

    public void setYUV(byte[] data, int width, int height) {
        if(visible) {
            int[] mIntArray = new int[width * height];
            decodeYUV420SP(mIntArray, data, width, height);
            bmp = Bitmap.createBitmap(mIntArray, width, height, Bitmap.Config.ARGB_8888);

    //        ByteArrayOutputStream out = new ByteArrayOutputStream();
    //        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
    //        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 50, out);
    //        byte[] imageBytes = out.toByteArray();
    //        bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

            invalidate();
        }
    }

    public void setPoints(int[] points){
        if(visible) {
            this.points = points;
            invalidate();
        }
    }

    public LandMarkView(Context context) {
        super(context);
    }

    public LandMarkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LandMarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        markViewDrawYUV = this;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        markViewDrawYUV = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!visible) {
            return;
        }
        if (drawYUV) {
            if (bmp != null) {
                canvas.drawBitmap(bmp, 0, 0, null);
            }
            Paint paint = new Paint();
            paint.setStrokeWidth(10);
            paint.setColor(Color.RED);
            for (int i = 0; i < points.length; i += 2) {
                canvas.drawPoint(points[i], points[i + 1], paint);
            }
        } else {
//            Matrix matrix = new Matrix();
//            matrix.setRotate(90);
           // canvas.concat(matrix);
            Matrix m = new Matrix();
            // m.postScale(0.5f, 0.5f);
            m.postRotate(270, 0, 0);
            m.postTranslate(0, canvas.getHeight());
            m.postScale( -1 , 1 );
            m.postTranslate(canvas.getWidth(), 0);
            //m.postScale(0.5f, 0.5f);
            canvas.setMatrix(m);
            if (bmp != null) {
                canvas.drawBitmap(bmp, 0, 0, null);
            }
            Paint paint2 = new Paint();
            paint2.setStrokeWidth(10);
            paint2.setColor(Color.RED);
            for (int i = 0; i < points.length; i += 2) {
                canvas.drawPoint(points[i],  points[i + 1], paint2);
            }
        }
    }

    static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
                                      int height) {
        Log.e("wangshuai", "decodeYUV420SP width is " + width);
        Log.e("wangshuai", "decodeYUV420SP height is " + height);
        Log.e("wangshuai", "decodeYUV420SP yuv420sp is " + yuv420sp.length);
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
                        0xff00) | ((b >> 10) & 0xff);
            }
        }
    }

    public static Matrix reverseH() {

        Matrix matrix = new Matrix();

        //水平翻转
        matrix.setValues(new float[] { -1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f });

        //垂直反转
        //new float[] { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
        return matrix;
    }
}
