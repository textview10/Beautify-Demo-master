package com.megvii.beautify.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
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
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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

public class ImageConUtil {

    public final static String TAG = "ImageConUtil";
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
        Log.d(TAG, "getPixel: mBitmapWidth == " + mBitmapWidth + ", " + mBitmapHeight + ", " +
                mBitmapWidth * mBitmapHeight);

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

    public static String saveAssestsData(Context context, String filePath, String newPath, String name) {
        InputStream inputStream;
        try {
            inputStream = context.getResources().getAssets().open(filePath + "/" + name);
//            inputStream = context.getResources().getAssets().open(filePath + "/" + name);
//			File file = context.getCacheDir();
//			File file = new File(Environment.getExternalStorageDirectory() + "/beautify");


            String path = newPath + name;
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
        YuvImage yuvImage = new YuvImage(data, camera.getParameters().getPreviewFormat(), width, height,
                null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
        byte[] jpegData = byteArrayOutputStream.toByteArray();
        // 获取照相后的bitmap
        Bitmap tmpBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        tmpBitmap = tmpBitmap.copy(Config.ARGB_8888, true);
        return tmpBitmap;
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
        tmpBitmap = tmpBitmap.copy(Config.ARGB_8888, true);

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
            ((InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(
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

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI,
                new String[]{MediaStore.Images.ImageColumns.DATA},//
                null, null, null);
        if (cursor == null) result = contentURI.getPath();
        else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(index);
            cursor.close();
        }
        return result;
    }

    public static Bitmap getImagewithRotation(String path) {
        try {
            Bitmap src = BitmapFactory.decodeFile(path);

            ExifInterface exif = new ExifInterface(path);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface
                    .ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();

            Log.i(TAG, "GetImage Rotation" + rotation);
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

    public static Bitmap getImagewithRotation(Bitmap srcImage,int rotation) {
        try {
            Matrix matrix = new Matrix();

            Log.i(TAG, "getImagewithRotation Rotation" + rotation);
            switch (rotation) {
                case 0:
                    return srcImage;
                case 180:
                    matrix.setRotate(180);
                    break;
                case 90:
                    matrix.setRotate(90);
                    break;
                case 270:
                    matrix.setRotate(-90);
                    break;
                default:
                    return srcImage;
            }

            Bitmap dst = Bitmap.createBitmap(srcImage, 0, 0, srcImage.getWidth(), srcImage.getHeight(), matrix, true);
            return dst;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap getImagewithoutRotation(String path) {
        try {
            return BitmapFactory.decodeFile(path);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
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


    public static byte[] getPixelsBGR(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] tmp = new int[width * height];
        image.getPixels(tmp, 0, width, 0, 0, width, height);
        byte[] pixels = new byte[width * height * 3];

        for (int i = 0; i < width * height; ++i) {
            int argb = tmp[i];
            pixels[i * 3 + 0] = (byte) Color.blue(argb);
            pixels[i * 3 + 1] = (byte) Color.green(argb);
            pixels[i * 3 + 2] = (byte) Color.red(argb);
        }

        return pixels;
    }


    public static byte[] setBitmapAlpha(Bitmap image, float[] alphas) {
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
//            pixels[i * 4 + 3] = temp[i * 4 + 3];        //A   无透明通道 byte=-1 就是255或者ff
            if (alphas[i] < 0.15f) {
                alphas[i] = 0.0f;
            } else if (alphas[i] > 0.55f) {
                alphas[i] = 1.0f;
            }
            pixels[i * 4 + 3] = (byte) (0xff * alphas[i]);
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

    public static byte[] getFileContent(String path) {
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = -1;
        try {
            inputStream = new FileInputStream(path);
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
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
                    .versionName;
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
        cv.drawBitmap(bitmap2, new Rect(0, 0, bitmap2.getWidth(), bitmap2.getHeight()), new Rect(0, 0, w,
                h), null);
        return newbBitmap;
    }

    /**
     * 保存bitmap至指定Picture文件夹
     */
    public static String saveBitmap(Context context, Bitmap bitmaptosave) {
        if (bitmaptosave == null)
            return null;

//        File mediaStorageDir = MainApp.getContext().getExternalFilesDir("megvii");
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DCIM).getAbsolutePath() + "/camera");
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

    public static Bitmap mergeBitmapWithLogo(Context context, Bitmap srcBitmap, Bitmap bmpLogo) {
        // 获取ImageView上得Bitmap图片

        // 创建空得背景bitmap
        // 生成画布图像
        Bitmap resultBitmap = Bitmap.createBitmap(srcBitmap.getWidth(),
                srcBitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);// 使用空白图片生成canvas

        // 将bmp1绘制在画布上
        Rect srcRect = new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());// 截取bmp1中的矩形区域
        Rect dstRect = new Rect(0, 0, srcBitmap.getWidth(), srcBitmap.getHeight());// bmp1在目标画布中的位置
        canvas.drawBitmap(srcBitmap, srcRect, dstRect, null);

        // 将bmp2绘制在画布上
        srcRect = new Rect(0, 0, bmpLogo.getWidth(), bmpLogo.getHeight());// 截取bmp1中的矩形区域

        int width = srcBitmap.getWidth();
        int margin = width * 6 / 120;
        int logoWidth = width * 36 / 120;
        int logoHeight = bmpLogo.getHeight() * logoWidth / bmpLogo.getWidth();

        int x = srcBitmap.getWidth() - logoWidth - margin;
        int y = srcBitmap.getHeight() - logoHeight - margin;
        dstRect = new Rect(x, y, x + logoWidth, y + logoHeight);// bmp2在目标画布中的位置

        canvas.drawBitmap(bmpLogo, srcRect, dstRect, null);
        // 将bmp1,bmp2合并显示
        return resultBitmap;
    }

    public static int getExifOrientation(int angle, boolean flip) {
        switch (angle) {
            case 180:
                return flip ? ExifInterface.ORIENTATION_FLIP_VERTICAL : ExifInterface.ORIENTATION_ROTATE_180;
            case 90:
                return flip ? ExifInterface.ORIENTATION_TRANSPOSE : ExifInterface.ORIENTATION_ROTATE_90;
            case 270:
                return flip ? ExifInterface.ORIENTATION_TRANSVERSE : ExifInterface.ORIENTATION_ROTATE_270;
            default:
                return flip ? ExifInterface.ORIENTATION_FLIP_HORIZONTAL : ExifInterface.ORIENTATION_NORMAL;
        }
    }

    public static int getImageOrienata(String path){
        try{
            int ImageOrientation = 0;
            ExifInterface exifInterface = new ExifInterface(path);
            //获取图片的旋转信息
            int orientationFromExif = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch(orientationFromExif){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    ImageOrientation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    ImageOrientation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    ImageOrientation = 270;
                    break;
                default:
                    ImageOrientation = 0;
            }
            Log.i(TAG,"getImageOrienata ImageOrientation: " +ImageOrientation);
            return ImageOrientation;
        }catch (IOException e){
            e.printStackTrace();
            return 0;
        }
    }
}
