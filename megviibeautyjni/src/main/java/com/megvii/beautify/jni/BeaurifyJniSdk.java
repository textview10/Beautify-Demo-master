package com.megvii.beautify.jni;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicResize;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;



/**
 * Created by xiejiantao on 2017/7/6.
 * 这里是demo的使用场景，最好按照自己的业务需求去实现jni
 */
public class BeaurifyJniSdk {

    //按照sdk的顺序
    public final static int MG_BEAUTIFY_DENOISE = 4;
    public final static int MG_BEAUTIFY_BRIGHTNESS = 3;
    public final static int MG_BEAUTIFY_BRIGHTEN_EYE = 7;
    public final static int MG_BEAUTIFY_TOOTH = 8;
    public final static int MG_BEAUTIFY_ADD_PINK = 5;

    public final static int MG_BEAUTIFY_SHRINK_FACE = 2;
    public final static int MG_BEAUTIFY_ENLARGE_EYE = 1;
    public final static int MG_BEAUTIFY_THIN_FACE = 6;

    public final static int MG_BEAUTIFY_EYEBROW = 9;
    public final static int MG_BEAUTIFY_CONTACT_LENS = 10;
    public final static int MG_BEAUTIFY_LIP = 11;
    public final static int MG_BEAUTIFY_REMOVE_SPECKLES = 12;
    public final static int MG_BEAUTIFY_REMOVE_EYEBAGS = 13;
    public final static int MG_BEAUTIFY_HIGH_NOSEBRIDGE = 14;
    public final static int MG_BEAUTIFY_UPCHEEK= 15;
    public final static int MG_BEAUTIFY_REMOVE_EYEBROW = 16;
    public final static int MG_BEAUTIFY_SKIN_BALANCE = 17;

    public final static int MG_BEAUTIFY_THIN_NOSE = 18;             ///< 瘦鼻
    public final static int MG_BEAUTIFY_LONG_NOSE = 19;             ///< 长鼻
    public final static int MG_BEAUTIFY_WARPED_NOSE = 20;           ///< 翘鼻
    public final static int MG_BEAUTIFY_ADD_BLUSH = 21;              ///< 加腮红
    public final static int MG_BEAUTIFY_SHADING = 22;                ///< 修容
    public final static int MG_BEAUTIFY_EYESHADOW = 23;             //眼影

    private final static int PROCESS_PREVIEW_ID = 1;
    private final static int PROCESS_IMAGE_ID = 2;

    public final static int ABILITY_TYPE_PREVIEW = 0;
    public final static int ABILITY_TYPE_IMAGE = 1;
    public final static int ABILITY_TYPE_VIDEO = 2;

    public final static int ABILITY_TYPE_DEFAULT = ABILITY_TYPE_PREVIEW;

    //log level
    public final static int MG_LOG_LEVEL_DISABLE = 0;
    public final static int MG_LOG_LEVEL_INFO = 1;
    public final static int MG_LOG_LEVEL_WARNING = 2;
    public final static int MG_LOG_LEVEL_ERROR = 3;
    public final static int MG_LOG_LEVEL_DEBUG = 4;

    /* mObject is used by native code, do not remove or rename */
    private volatile long mObject = 0;

    /* mAbilityType is used by native code, do not remove or rename */
    private long mAbilityType = ABILITY_TYPE_DEFAULT;

    private static BeaurifyJniSdk sPreviewBeaurifyJniSdk = new BeaurifyJniSdk(ABILITY_TYPE_PREVIEW);

    private static BeaurifyJniSdk sImageBeaurifyJniSdk = new BeaurifyJniSdk(ABILITY_TYPE_IMAGE);

    //仅仅作为测试用，测试HAL层API
    private static BeaurifyJniSdk sVideoBeaurifyJniSdk = new BeaurifyJniSdk(ABILITY_TYPE_VIDEO);


    static {
        System.loadLibrary("MGBeauty");
        System.loadLibrary("MegviiDlmk");
        System.loadLibrary("MegviiBeautify-jni");
    }

    public synchronized static BeaurifyJniSdk preViewInstance(){
        return sPreviewBeaurifyJniSdk;
    }

    public synchronized static BeaurifyJniSdk imageInstance(){
        return sImageBeaurifyJniSdk;
    }

    public synchronized static BeaurifyJniSdk videoInstance(){
        return sVideoBeaurifyJniSdk;
    }

    public BeaurifyJniSdk(int type){
        mAbilityType = type;
    }

    public synchronized native String nativeGetBeautyVersion();
    public synchronized native int nativeSetLogLevel(int logLevel);

    /**
     * facceppmodel 为null，简单的美白不检测人脸也可以但是效果不好 后面的人脸检测会根据里面的handle判断空操作
     *
     * @param context
     * @param cameraWidth
     * @param cameraHeight
     * @param orientation
     * @param beautyModel
     * @param faceppModel
     * @return
     */
    public synchronized native int nativeCreateBeautyHandle
    (Context context, int cameraWidth, int cameraHeight,
     int orientation,int detectMode, byte[] beautyModel
            , byte[] faceppModel,byte[] denseLMfaceppModel);

    public synchronized native int nativeShareGLContext();
    public synchronized native int nativeDoneGLContext();
    public synchronized native int nativeReset(int cameraWidth, int cameraHeight, int orientation);
    public synchronized native int nativeReleaseResources();

    public synchronized native int nativeProcessTexture(int oldTextureIndex, int newTextureIndex,int isPreview);

    public synchronized native int nativeProcessImage(Bitmap inBitmap, Bitmap outBitmap);

    public synchronized native int nativeProcessImageInTextureOut(byte[] inData,int w, int h,int newTextureIndex);

    public synchronized native int nativeProcessImageInImageOutNV21(byte[] inData, byte[] outData, int w, int h);
    public synchronized native int nativeProcessImageInImageOutNV12(byte[] inData, byte[] outData, int w, int h);

    public synchronized native int nativeProcessImageNV21(byte[] inData, byte[] outData, int w, int h, boolean platform);
    public synchronized native int nativeProcessImageNV12(byte[] inData, byte[] outData, int w, int h, boolean platform);

    public synchronized int nativeProcessImageNV21(byte[] inData, byte[] outData, int w, int h){
        boolean isQcom = false;
        String hardware = android.os.Build.HARDWARE;
        if (hardware.matches("qcom")) {
            isQcom = true;
        }
        return nativeProcessImageNV21(inData, outData, w, h, isQcom);
    }

    public synchronized native int nativeSetBeautyParam(int beautyType, float beautyValue);

    public synchronized native int nativeSetBeautyParam2(int beautyType, float beautyValue, int r, int g, int b, Bitmap[] Templates, Bitmap[] Templates2, float[] keypoint, int keypointSize);

    public synchronized native int nativeSetBeautyRemoveSpeckles(float beautyValue,byte[] muvarModel_,String RFCModelPath_);

    public synchronized native int nativeSetFilter(String path);

    public synchronized native int nativeRemoveFilter();


    /**
     * @brief 更改贴纸
     * @param[in] path 贴纸压缩包所在路径
     */
    public synchronized native int nativeChangePackage(String path);

    public synchronized native int nativeSetStickerParam(float beautyValue);

    public synchronized native int nativePreparePackage(String path);

    /**
     * @brief 停止贴纸
     */
    public synchronized native int nativeDisablePackage();

    /**
     * 默认检测
     * @param data
     * @param cameraWidth
     * @param cameraHeight
     * @param format
     * @return
     */

    public synchronized native int nativeDetectFace(byte[] data, int cameraWidth, int cameraHeight,int format);

    /**
     * 支持四个角度检测人脸
     * @param data
     * @param cameraWidth
     * @param cameraHeight
     * @param format
     * @param rotation
     * @return
     */
    public synchronized native int nativeDetectFaceOrientation(byte[] data
            , double xScale
            , double yScale
            ,  int cameraWidth, int cameraHeight
            ,int format,int rotation);


    /**
     * 81 points, 162 value;
     * @param points
     */
    public synchronized native int nativeGetPoints(int[] points);
    public synchronized native int nativeGetDenseLMPoints(int[] points);

    public synchronized native int nativeUseFastFilter(boolean b);

    //test
    /**
     * camera 和image方式检测人脸的参数有所不同
     *
     * @param detectMode 默认0-camera，1-image
     * @return
     */

    public synchronized native int nativeSetDetectMode(int detectMode);


    /**
     * 测试接口，不要使用。
     * @param faceppModel
     * @param data
     * @param width
     * @param height
     * @return
     */
    @Deprecated
    public synchronized native static int testFacePP(byte[] faceppModel, byte[] data, int width, int height);


    @Deprecated
    public synchronized native static void alignNV21Data(byte[] data, int width, int height,
                                                         byte[] aligned,  int alignedW, int alignedH);


    public synchronized native static void deAlignNV21Data(byte[] data, int width, int height,
                                                         byte[] aligned,  int alignedW, int alignedH);


    public static int getAlignSize(int size){
        int ALIGN_BASE = 64;
        int outW = (size/ALIGN_BASE)*ALIGN_BASE;
        if(outW!= size){
            outW = outW + ALIGN_BASE;
        }
        return outW;
    }

}
