package com.megvii.beautify.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.megvii.beautify.app.MainApp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ConUtil {
    public final static String TAG = "ConUtil";

    public static float[] rotateFloat(float[] data, int width, int height, int angle, boolean isMirror) {
        if (angle == 90)
            return rotateFloat_90(data, width, height, isMirror);
        else if (angle == 180)
            return rotateFloat_180(data, width, height, isMirror);
        else if (angle == 270)
            return rotateFloat_270(data, width, height, isMirror);
        else
            return data;
    }


    public static float[] rotateFloat_90(float[] data, int width, int height, boolean isMirror) {
        if (data == null || width == 0 || height == 0)
            return null;

        float[][] data_1 = new float[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int index = i * width + j;
                float d = data[index];
                data_1[i][j] = d;
            }
        }

        float[] data_2 = new float[width * height];
        int n = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (isMirror)
                    data_2[n] = data_1[height - j - 1][width - 1 - i];
                else
                    data_2[n] = data_1[height - j - 1][i];
                ++n;
            }
        }
        return data_2;
    }

    public static float[] rotateFloat_270(float[] data, int width, int height, boolean isMirror) {
        if (data == null || width == 0 || height == 0)
            return null;

        float[][] data_1 = new float[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int index = i * width + j;
                float d = data[index];
                data_1[i][j] = d;
            }
        }

        float[] data_2 = new float[width * height];

        int n = 0;
        for (int i = width - 1; i >= 0; i--) {
            for (int j = height - 1; j >= 0; j--) {
                if (isMirror)
                    data_2[n] = data_1[height - j - 1][width - 1 - i];
                else
                    data_2[n] = data_1[height - j - 1][i];
                ++n;
            }
        }
        return data_2;
    }

    public static float[] rotateFloat_180(float[] data, int width, int height, boolean isMirror) {
        if (data == null || width == 0 || height == 0)
            return null;

        float[] data_1 = new float[width * height];

        for (int i = 0; i < data.length; i++) {
            data_1[i] = data[data.length - 1 - i];
        }

        return data_1;
    }

    public static Bitmap setBitmapPixel(Context context, int width, int height, byte[] data) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        int mBitmapWidth = bitmap.getWidth();
        int mBitmapHeight = bitmap.getHeight();

        for (int i = 0; i < mBitmapHeight; i++) {
            for (int j = 0; j < mBitmapWidth; j++) {
                int index = (i * mBitmapWidth + j) * 4;
                byte d = data[index];
                bitmap.setPixel(j, i, Color.rgb(data[index] + 0, data[index] + 1, data[index] + 2));
            }
        }
        return bitmap;
    }

    public static Bitmap bitmap;

    public static Bitmap setBitmapPixel(Context context, int width, int height, float[] data) {
        if (bitmap == null)
            bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        //bitmap = bitmap.copy(Config.ARGB_8888, true);
        int mBitmapWidth = bitmap.getWidth();
        int mBitmapHeight = bitmap.getHeight();

        Log.d(TAG, "getPixel: data.length == " + data.length + ", " + width + ", " + height);
        Log.d(TAG, "getPixel: mBitmapWidth == " + mBitmapWidth + ", " + mBitmapHeight + ", " + mBitmapWidth * mBitmapHeight);

        for (int i = 0; i < mBitmapHeight; i++) {
            for (int j = 0; j < mBitmapWidth; j++) {
                int index = i * mBitmapWidth + j;
                float d = data[index];
                //int color = bitmap.getPixel(i, j);
                //if (color >= Color.rgb(200, 200, 200) && color <= Color.rgb(255, 255, 255)) {
                bitmap.setPixel(j, i, Color.rgb((int) (d * 255), (int) (d * 255), (int) (d * 255)));
                //}
            }
        }
        return bitmap;
    }

    public static byte[] Ints2Bytes(int[] s) {
        byte bLength = 4;
        byte[] buf = new byte[s.length * bLength];

        for (int iLoop = 0; iLoop < s.length; iLoop++) {
            byte[] temp = getBytes(s[iLoop]);

            System.out.println("1out->" + s[iLoop]);

            for (int jLoop = 0; jLoop < bLength; jLoop++) {
                buf[iLoop * bLength + jLoop] = temp[jLoop];
            }
        }

        return buf;
    }

    public static byte[] getBytes(int s, boolean bBigEnding) {
        byte[] buf = new byte[4];

        if (bBigEnding) {
            for (int i = buf.length - 1; i >= 0; i--) {
                buf[i] = (byte) (s & 0x000000ff);
                s >>= 8;
            }
        } else {
            System.out.println("1");
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x000000ff);
                s >>= 8;
            }
        }

        return buf;
    }

    public static byte[] getBytes(int i) {
        return getBytes(i, testCPU());
    }

    public static boolean testCPU() {
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            // System.out.println("is big ending");
            return true;
        } else {
            // System.out.println("is little ending");
            return false;
        }
    }

    public static ArrayList<HashMap<String, String>> getParserFilter(Context context, String name) {
        ArrayList<HashMap<String, String>> filterList = new ArrayList<HashMap<String, String>>();

        InputStream inputStream = null;
        ByteArrayOutputStream baos = null;
        try {
            inputStream = context.getResources().getAssets().open(name);
            byte[] buffer = new byte[1024];
            baos = new ByteArrayOutputStream();

            int count = 0;
            while ((count = inputStream.read(buffer)) > 0) {
                baos.write(buffer, 0, count);
            }

            String filterStr = new String(baos.toByteArray());

            JSONArray rootJson = new JSONArray(filterStr);
            for (int i = 0; i < rootJson.length(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                JSONObject json = rootJson.getJSONObject(i);
                map.put("name", json.getString("title_chinese"));
                map.put("name_english", json.getString("title_english"));
                map.put("filter", json.getString("filter"));
                map.put("image", json.getString("sample"));
                filterList.add(map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return filterList;
    }

    public static String saveAssestsData(Context context, String filePath, String newPath,String name) {
        InputStream inputStream;
        try {
            inputStream = context.getResources().getAssets().open(filePath + "/" + name);
//            inputStream = context.getResources().getAssets().open(filePath + "/" + name);
//			File file = context.getCacheDir();
//			File file = new File(Environment.getExternalStorageDirectory() + "/beautify");


            String path = newPath  + name;
            File stickerFile = new File(path);
            if (stickerFile.exists())
                return stickerFile.getAbsolutePath();

            FileOutputStream fileOutputStream = new FileOutputStream(path);
            byte[] buffer = new byte[512];
            int count = 0;
            while ((count = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            return path;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getJsonString(HashMap<String, byte[]> featureMap) {
        try {
            JSONObject jsonObject = new JSONObject(featureMap);
            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getYUVBitMap(byte[] data, Camera camera, int Angle) {

        int width = camera.getParameters().getPreviewSize().width;
        int height = camera.getParameters().getPreviewSize().height;
        if (Angle == 90 || Angle == 270) {
            width = camera.getParameters().getPreviewSize().height;
            height = camera.getParameters().getPreviewSize().width;
        }
        YuvImage yuvImage = new YuvImage(data, camera.getParameters().getPreviewFormat(), width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
        byte[] jpegData = byteArrayOutputStream.toByteArray();
        // 获取照相后的bitmap
        Bitmap tmpBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        tmpBitmap = tmpBitmap.copy(Config.ARGB_8888, true);
        return tmpBitmap;
    }

    static void RGBToYUV(int Red, int Green, int Blue, int[] yuv, int offset)
    {
        yuv[offset +0] = ((Red << 6) + (Red << 3) + (Red << 2) + Red + (Green << 7) + (Green << 4) + (Green << 2) + (Green << 1) + (Blue << 4) + (Blue << 3) + (Blue << 2) + Blue) >> 8;
        yuv[offset +1]= (-((Red << 5) + (Red << 2) + (Red << 1)) - ((Green << 6) + (Green << 3) + (Green << 1)) + ((Blue << 6) + (Blue << 5) + (Blue << 4))) >> 8;
        yuv[offset +2] = ((Red << 7) + (Red << 4) + (Red << 3) + (Red << 2) + (Red << 1) - ((Green << 7) + (Green << 2)) - ((Blue << 4) + (Blue << 3) + (Blue << 1))) >> 8;
    }
    static void YUVToRGB(int Y, int U, int V, int[] rgb, int offset)
    {
        rgb[offset +0]   = ((Y << 8) + ((V << 8) + (V << 5) + (V << 2))) >> 8;
        rgb[offset +1] = ((Y << 8) - ((U << 6) + (U << 5) + (U << 2)) - ((V << 7) + (V << 4) + (V << 2) + V)) >> 8;
        rgb[offset +2] = ((Y << 8) + (U << 9) + (U << 3)) >> 8;
    }

    static public void decodeYUV420SPRGB3(byte[] rgb, byte[] yuv420sp, int width,
                                          int height) {
        for(int i = 0; i < width*height; ++ i){
            int rgbIndex = i<<2;
            rgb[rgbIndex] = yuv420sp[i];
            rgb[rgbIndex+1] = yuv420sp[i];
            //rgb[startOffset++] = (byte) 0x00;
            rgb[rgbIndex+2] =  yuv420sp[i];
            rgb[rgbIndex+3] = (byte) 0xff;
        }
    }

    static public void decodeYUV420SPRGB2(byte[] rgb, byte[] yuv420sp, int width,
                                          int height) {
        int size = width*height;
        for (int y = 0; y < height; y++) {
            int offsetY = y * width;
            int offsetRGB = offsetY <<2;
            int yoffsetUV = (y / 2) * (width / 2);
            for (int x = 0; x < width; x++) {
                int xoffsetUV = x / 2;
                int Y = yuv420sp[offsetY + x]&0xff;
                int pos = size + yoffsetUV * 2 + xoffsetUV * 2;
                int U = yuv420sp[pos]&0xff;
                int V = yuv420sp[pos + 1]&0xff;
                int startOffset = offsetRGB + x*4;

                double B = 1.164*(Y - 16)                    + 2.018*(U - 128);
                double G = 1.164*(Y - 16) - 0.813*(V - 128)  -  0.391*(U - 128);
                double R = 1.164*(Y - 16) + 1.596*(V - 128);

                rgb[startOffset++] = (byte) R;
                rgb[startOffset++] = (byte) G;
                //rgb[startOffset++] = (byte) 0x00;
                rgb[startOffset++] = (byte) B;
                rgb[startOffset++] = (byte) 0xff;
            }
        }
    }


    static public void decodeYUV420SPRGB(byte[] rgb, byte[] yuv420sp, int width,
                                      int height) {
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

                rgb[4*yp] = (byte) ((r>>10)&0xff);
                rgb[4*yp + 1] = (byte) ((g>>10)&0xff);
                rgb[4*yp + 2] = (byte) ((b>>10)&0xff);
                rgb[4*yp + 3] = (byte) (0xff);
            }
        }
    }


    static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width,
                                      int height) {
        Log.d(TAG, "decodeYUV420SP width is " + width);
        Log.d(TAG, "decodeYUV420SP height is " + height);
        Log.d(TAG, "decodeYUV420SP yuv420sp is " + yuv420sp.length);
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

    public static Bitmap getBitMap(byte[] data, Camera camera, boolean mIsFrontalCamera) {
        int width = camera.getParameters().getPreviewSize().width;
        int height = camera.getParameters().getPreviewSize().height;
        YuvImage yuvImage = new YuvImage(data, camera.getParameters()
                .getPreviewFormat(), width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 80,
                byteArrayOutputStream);
        byte[] jpegData = byteArrayOutputStream.toByteArray();
        // 获取照相后的bitmap
        Bitmap tmpBitmap = BitmapFactory.decodeByteArray(jpegData, 0,
                jpegData.length);
        Matrix matrix = new Matrix();
        matrix.reset();
//        if (mIsFrontalCamera) {
//            matrix.setRotate(-90);
//        } else {
//            matrix.setRotate(90);
//        }
        matrix.setRotate(0);
        tmpBitmap = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(),
                tmpBitmap.getHeight(), matrix, true);
        tmpBitmap = tmpBitmap.copy(Bitmap.Config.ARGB_8888, true);

        int hight = tmpBitmap.getHeight() > tmpBitmap.getWidth() ? tmpBitmap
                .getHeight() : tmpBitmap.getWidth();

        float scale = hight / 800.0f;

        if (scale > 1) {
            tmpBitmap = Bitmap.createScaledBitmap(tmpBitmap,
                    (int) (tmpBitmap.getWidth() / scale),
                    (int) (tmpBitmap.getHeight() / scale), false);
        }
        return tmpBitmap;
    }

    /**
     * 时间格式化(格式到秒)
     */
    public static String getFormatterDate(long time) {
        Date d = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String data = formatter.format(d);
        return data;
    }

//    public static String getUUIDString() {
//        String KEY_UUID = "key_uuid";
//        String uuid = SharedUtil.getStringValueByKey(KEY_UUID);
//        if (uuid != null)
//            return uuid;
//
//        uuid = getPhoneNumber();
//
//        if (uuid == null || uuid.trim().length() == 0) {
//            uuid = getMacAddress();
//            if (uuid == null || uuid.trim().length() == 0) {
//                uuid = getDeviceID();
//                if (uuid == null || uuid.trim().length() == 0) {
//                    uuid = UUID.randomUUID().toString();
//                    uuid = Base64.encodeToString(uuid.getBytes(), Base64.DEFAULT);
//                }
//            }
//        }
//        SharedUtil.saveStringValue(KEY_UUID, uuid);
//        return uuid;
//    }

    public static void toggleHideyBar(Activity activity) {
        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;


        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 19) {
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    public static String getPhoneNumber() {
        TelephonyManager phoneMgr = (TelephonyManager) MainApp.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return phoneMgr.getLine1Number();
    }

    public static String getDeviceID() {
        TelephonyManager tm = (TelephonyManager) MainApp.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

//    public static String getMacAddress() {
//        WifiManager wifi = (WifiManager) MainApp.getContext().getSystemService(Context.WIFI_SERVICE);
//        WifiInfo info = wifi.getConnectionInfo();
//        String address = info.getMacAddress();
//        if (address != null && address.length() > 0) {
//            address = address.replace(":", "");
//        }
//        return address;
//    }

    /**
     * 隐藏软键盘
     */
    public static void isGoneKeyBoard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            // 隐藏软键盘
            ((InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static PowerManager.WakeLock wakeLock = null;

    public static void acquireWakeLock(Context context) {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) (context.getSystemService(Context.POWER_SERVICE));
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
            wakeLock.acquire();
        }
    }

    public static void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    public static byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();

        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer

        byte[] temp = buffer.array(); // Get the underlying array containing the data.

        byte[] pixels = new byte[temp.length]; // Allocate for RGBA

        // Copy pixels into place
        for (int i = 0; i < (temp.length / 4); i++) {
            pixels[i * 4 + 0] = temp[i * 4 + 0];       //R
            pixels[i * 4 + 1] = temp[i * 4 + 1];       //G
            pixels[i * 4 + 2] = temp[i * 4 + 2];       //B
            pixels[i * 4 + 3] = temp[i * 4 + 3];       //A
        }

        return pixels;
    }

    /**
     * 获取bitmap的灰度图像
     */
    public static byte[] getGrayscale(Bitmap bitmap) {
        if (bitmap == null)
            return null;

        byte[] ret = new byte[bitmap.getWidth() * bitmap.getHeight()];
        for (int j = 0; j < bitmap.getHeight(); ++j)
            for (int i = 0; i < bitmap.getWidth(); ++i) {
                int pixel = bitmap.getPixel(i, j);
                int red = ((pixel & 0x00FF0000) >> 16);
                int green = ((pixel & 0x0000FF00) >> 8);
                int blue = pixel & 0x000000FF;
                ret[j * bitmap.getWidth() + i] = (byte) ((299 * red + 587 * green + 114 * blue) / 1000);
            }
        return ret;
    }

    public static byte[] getFileContent(Context context, int id) {

        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = -1;
        try {
            inputStream = context.getResources().openRawResource(id);
            while ((count = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, count);
            }
            byteArrayOutputStream.close();
        } catch (IOException e) {
            return null;
        } finally {
            // closeStreamSilently(inputStream);
            inputStream = null;
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static Toast toast;

    /**
     * 输出toast
     */
    public static void showToast(Context context, String str) {
        if (toast != null)
            toast.cancel();
        if (context != null) {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
            // 可以控制toast显示的位置
            toast.setGravity(Gravity.TOP, 0, 30);
            toast.show();
        }
    }

    /**
     * 输出长时间toast
     */
    public static void showLongToast(Context context, String str) {
        if (toast != null)
            toast.cancel();
        if (context != null) {
            toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
            // 可以控制toast显示的位置
            toast.setGravity(Gravity.TOP, 0, 30);
            toast.show();
        }
    }

    /**
     * 获取APP版本名
     */
    public static String getVersionName(Context context) {
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            return versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 镜像旋转
     */
    public static Bitmap convert(Bitmap bitmap, boolean mIsFrontalCamera) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap newbBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newbBitmap);
        Matrix m = new Matrix();
        // m.postScale(1, -1); //镜像垂直翻转
        if (mIsFrontalCamera) {
            m.postScale(-1, 1); // 镜像水平翻转
        }
        // m.postRotate(-90); //旋转-90度
        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
        cv.drawBitmap(bitmap2, new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight()), new Rect(0, 0, w, h), null);
        return newbBitmap;
    }

    /**
     * 保存bitmap至指定Picture文件夹
     */
    public static String saveBitmap(Bitmap bitmaptosave) {
        if (bitmaptosave == null)
            return null;

        //File mediaStorageDir = MainApp.getContext().getExternalFilesDir("megvii");
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath()+"/DCIM/Camera/");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String bitmapFileName = System.currentTimeMillis() + ".jpg";
        // String bitmapFileName = System.currentTimeMillis() + "";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mediaStorageDir + "/" + bitmapFileName);
            boolean successful = bitmaptosave.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            if (successful)
                return mediaStorageDir.getAbsolutePath() + "/" + bitmapFileName;
            else
                return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 把字节数组保存为一个文件
     *
     * @EditTime 2007-8-13 上午11:45:56
     */
    public static String saveBytw(byte[] b) {
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = MainApp.getContext().getExternalFilesDir("megvii");
            FileOutputStream fstream = new FileOutputStream(file + "/" + System.currentTimeMillis());
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file.getAbsolutePath();
    }


    public static Bitmap getImage(String path) {
        try {
            Bitmap src = BitmapFactory.decodeFile(path);

            ExifInterface exif = new ExifInterface(path);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface
                    .ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();

            Log.d(TAG, "GetImage Rotation" + rotation);
            switch (rotation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    return src;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;
                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return src;
            }

            Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
            src.recycle();
            return dst;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 保存bitmap至指定Picture文件夹
     */
    public static String saveBitmap(Context context, Bitmap bitmaptosave, String strPrefix) {
        if (bitmaptosave == null)
            return null;

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DCIM).getAbsolutePath() + "/camera/megBeautify");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String bitmapFileName = "";
        if(strPrefix == null) {
            if(strPrefix != null && !TextUtils.isEmpty(strPrefix)){
                bitmapFileName = strPrefix + "_";
            }
            bitmapFileName += ((Long)System.currentTimeMillis()).toString();
            bitmapFileName +=".jpg";
        } else {
            bitmapFileName = strPrefix;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mediaStorageDir + "/" + bitmapFileName);
            boolean successful = bitmaptosave.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            if (successful) {
                updateAlbum(context, new File(mediaStorageDir + "/" + bitmapFileName));
                return mediaStorageDir.getAbsolutePath() + "/" + bitmapFileName;
            } else
                return null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateAlbum(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }
}
