package com.megvii.beautify.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.util.Pair;
import android.view.View;

import com.megvii.beautify.BuildConfig;
import com.megvii.beautify.R;
import com.megvii.beautify.jni.BeaurifyJniSdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * Created by liyanshun on 2017/6/28.
 */

public class Util {
    public static final String[] KEYS = {"KEY_STICKER", "KEY_FILTER"};
    public static final int TYPE_STICKER = 0;
    public static final int TYPE_FILTER = 1;

    public static boolean isPreView = true;

    public static final int DEFAULT_OPTION_SUM = 10;                //默认选项个数

    public static final float DEFAULT_BEAUTIRY_VALUE = 0;
    public static final int   BEAUTIFY_TRANS_COEFFICIENT = 20;     //界面与sdk之间系数转换比

    //染眉默认值
    public static KeyPoints[] DEFAULT_EYEBROW_KEYPOINTS = new KeyPoints[DEFAULT_OPTION_SUM];
    public static final int[] DEFAULT_EYEBROW_COLOR = {43, 43, 43,
            80, 61, 55,
            94, 69, 74,
            76, 74, 86,
            120,87, 86,
            150,98, 69};
    public static  String[] DEFAULT_EYEBROW_ASSERTS = {"eyebrow/standared_eyebrow.png","eyebrow/star_eyebrow.png","eyebrow/salix_eyebrow.png","eyebrow/europe_eyebrow.png","eyebrow/silk_eyebrow.png"};


    public static  Bitmap[][] DEFAULT_EYEBROW_TEMPLATE = new Bitmap[DEFAULT_OPTION_SUM][1];

    //美唇默认值
    public static final int[] DEAFULT_LIP_COLOR = {

//            210, 85, 100,    //豆沙粉
//            155, 33,33,    //复古红
//            191,65,89,     //梅子色
//            215, 94,94,    //珊瑚色
//            237, 134, 157, //少女粉
//            207,47, 61,    //丝绒红
//            198,52,64,     //西瓜红
//            239,107,110,   //西柚色
//            214,84,68,     //元气橘
//            163,48,31      //脏橘色


            226,  14,  25,    //珊瑚色 27% 阿尔法融合
            200,  40,  90,    //豆沙粉 70% 阿尔法融合
            230, 100, 172,    //少女粉 18% 颜色加深

            211,  46,   25,    //元气橘 30% 颜色加深
            // 240,  20,   5,    //元气橘 30% 颜色加深
            240,  50,  50,    //西柚色 30%
            195,  20,  30,    //西瓜红 30%

            210,  10,  50,      //丝绒红 30% 线性加深
            80,    0,  0,      //复古红 30% 线性加深
            160,  90,  50,      //脏橘色 30%
            180,  10, 130       //梅子色 25% 线性加深
    };

    //美瞳默认值
    public static  Bitmap[][] DEFAULT_CONTACT_LENS_TEMPLATE = new Bitmap[DEFAULT_OPTION_SUM][1];
    public static  String[] DEFAULT_CONTACT_LENS_ASSERTS = {"contactlens/10_mt.png","contactlens/190_mt.png","contactlens/631_mt.png","contactlens/801_mt.png",
            "contactlens/803_mt.png","contactlens/8011_mt.png","contactlens/7_mt.png","contactlens/8_mt.png","contactlens/9_mt.png","contactlens/05_mt.png"};

    //腮红默认值
    public static  Bitmap[][] DEFAULT_BLUSH_TEMPLATE = new Bitmap[DEFAULT_OPTION_SUM][2]; //腮红每个模板有两份
    public static final int[] DEFAULT_BLUSH_COLOR = {
            220, 100,  60,     //晒伤、甜橙、心机
            243,  88, 126,     //俏皮、微醺
            253, 103, 103,     //日常    +   椭圆  OK
            243,  85, 131,     //蜜桃    +   心形  OK
              8,   8,   8,
              8,   8,   8,
              8,   8,   8,
              8,   8,   8,
              8,   8,   8,
              8,   8,   8,
    };
    public static String[] DEFAULT_BLUSH_ASSERTS = {"blush/leftFaceBlushMaskRect_1.png","blush/rightFaceBlushMaskRect_1.png",
            "blush/leftFaceBlushMaskRect_5.png","blush/rightFaceBlushMaskRect_5.png",
            "blush/leftFaceBlushMaskRect_2.png","blush/rightFaceBlushMaskRect_2.png",
            "blush/leftFaceBlushMaskRect_4.png","blush/rightFaceBlushMaskRect_4.png",
            "blush/leftFaceBlushMaskRect.png","blush/rightFaceBlushMaskRect.png"

    };
    public static KeyPoints[] DEFAULT_BLUSH_KEYPOINTS = new KeyPoints[DEFAULT_OPTION_SUM];

    //眼影默认值
    public static  Bitmap[][] DEFAULT_EYESHADOW_TEMPLATE = new Bitmap[DEFAULT_OPTION_SUM][2]; //腮红每个模板有两份
    public static final int[] DEFAULT_EYESHADOW_COLOR = {220, 27, 36,
            204, 76,125,
            233,125,125,
            254, 90,123,
            246, 51, 70,
            252,108, 94,
            233,125,125,
            254, 90,123,
            246, 51, 70,
            252,108, 94};
    public static String[] DEFAULT_EYESHADOW_ASSERTS = {"eyeshadow/eyeShadowMaskLeft.png","eyeshadow/eyeShadowMaskRight.png",
            "eyeshadow/eyeShadowMaskLeft.png","eyeshadow/eyeShadowMaskRight.png",
            "eyeshadow/eyeShadowMaskLeft.png","eyeshadow/eyeShadowMaskRight.png",
            "eyeshadow/eyeShadowMaskLeft.png","eyeshadow/eyeShadowMaskRight.png",
            "eyeshadow/eyeShadowMaskLeft.png","eyeshadow/eyeShadowMaskRight.png",
            "eyeshadow/eyeShadowMaskLeft.png","eyeshadow/eyeShadowMaskRight.png",
    };
    public static KeyPoints[] DEFAULT_EYESHADOW_KEYPOINTS = new KeyPoints[DEFAULT_OPTION_SUM];

    //修容默认值
    public static Vector<Pair<Bitmap[],Bitmap[]> > DEFAULT_SHADING_TEMPLATE = new Vector<>();
    public static String[] DEFAULT_SHADING_ASSERTS = {"lightingMask/highlight01_forehead_mask..png","lightingMask/highlight04_cheek_mask..png","lightingMask/highlight05_chin_mask..png",
    "lightingMask/shadow01_forehead_mask..png","lightingMask/shadow04_cheek_mask..png","lightingMask/shadow05_chin_mask..png","lightingMask/highlight02_nose_mask..png","lightingMask/shadow02_nose_mask..png"};


    public static KeyPoints[] DEFAULT_SHADING_KEYPOINTS = new KeyPoints[DEFAULT_OPTION_SUM];
    public static final int SHADING_ORI_TEMPLATE_SUM = 8;




    //美颜当前值
    public static float CURRENT_MG_BEAUTIFY_DENOISE = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_BRIGHTNESS = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_TOOTH = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_ADD_PINK = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_REMOVE_SPECKLES = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_REMOVE_EYEBAGS = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_HIGH_NOSE_BRIDGE = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_UP_CHEEK = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_SKIN_BALANCE = DEFAULT_BEAUTIRY_VALUE;
    //祛斑模型
    public static byte[] mMuvarModel = null;
    public static String mRandomFrestModelPath = "";

    //美型当前值
    public static float CURRENT_MG_BEAUTIFY_SHRINK_FACE = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_ENLARGE_EYE = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_THIN_FACE = DEFAULT_BEAUTIRY_VALUE;
    public static float CURRENT_MG_BEAUTIFY_REMOVE_EYEBROW = DEFAULT_BEAUTIRY_VALUE;

    //美妆当前值
    // 染眉
    public static float CURRENT_MG_BEAUTIFY_EYEBROW = DEFAULT_BEAUTIRY_VALUE;
    public static int CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_R = DEFAULT_EYEBROW_COLOR[0];
    public static int CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_G = DEFAULT_EYEBROW_COLOR[1];
    public static int CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_B = DEFAULT_EYEBROW_COLOR[2];
    public static int CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX = 0;
    //美唇
    public static float CURRENT_MG_BEAUTIFY_LIP = DEFAULT_BEAUTIRY_VALUE;
    public static int CURRENT_MG_BEAUTIFY_LIP_COLOR_R = DEAFULT_LIP_COLOR[0];
    public static int CURRENT_MG_BEAUTIFY_LIP_COLOR_G = DEAFULT_LIP_COLOR[1];
    public static int CURRENT_MG_BEAUTIFY_LIP_COLOR_B = DEAFULT_LIP_COLOR[2];

    //腮红
    public static float CURRENT_MG_BEAUTIFY_BLUSH = DEFAULT_BEAUTIRY_VALUE;
    public static int CURRENT_MG_BEAUTIFY_BLUSH_COLOR_R = DEFAULT_BLUSH_COLOR[0];
    public static int CURRENT_MG_BEAUTIFY_BLUSH_COLOR_G = DEFAULT_BLUSH_COLOR[1];
    public static int CURRENT_MG_BEAUTIFY_BLUSH_COLOR_B = DEFAULT_BLUSH_COLOR[2];
    public static int CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX = 0;

    //眼影
    public static float CURRENT_MG_BEAUTIFY_EYESHADOW = DEFAULT_BEAUTIRY_VALUE;
    public static int CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_R = DEFAULT_EYESHADOW_COLOR[0];
    public static int CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_G = DEFAULT_EYESHADOW_COLOR[1];
    public static int CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_B = DEFAULT_EYESHADOW_COLOR[2];
    public static int CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX = 0;

    //修容
    public static float CURRENT_MG_BEAUTIFY_SHADING = DEFAULT_BEAUTIRY_VALUE;
    public static int CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX = 0;

    //美瞳
    public static float CURRENT_MG_BEAUTIFY_CONTACTLENS = DEFAULT_BEAUTIRY_VALUE;
    public static int CURRENT_MG_BEAUTIFY_CONTACT_LENS_TEMPLATE_INDEX = 0;

    public static final int LandMarkPointSize = 2000;

    public volatile static boolean isStickerChanged;             //贴纸对象有变化
    public volatile static boolean isStickerParamInited = false; //贴纸对象初始化
    public volatile static String sCurrentStickerPath;

    public static boolean isDebuging = false;
    public static volatile boolean isDebugingLandMark = false;

    public volatile static boolean isFilterChanged;               //滤镜对象有变化
    public volatile static boolean isFilterParamInited = false;   //滤镜参数初始化
    public volatile static String filterPath;

    public volatile static boolean isShowOriPic = false;    //预览是否显示原图

    public static boolean switchcamera = false;
    public static int switchcount = 0;

    public static final int MG_FPP_DENSEDETECTIONMODE_PREVIEW = 0;
    public static final int MG_FPP_DENSEDETECTIONMODE_FULL_SIZE = 1;




    public static final int MG_IMAGEMODE_GRAY = 0;      ///< 灰度图像

    public static final int MG_IMAGEMODE_BGR = 1;           ///< BGR图像

    public static final int MG_IMAGEMODE_NV21 = 2;         ///< YUV420（nv21）图像

    public static final int MG_IMAGEMODE_RGBA = 3;       ///< RGBA图像

    public static final int MG_IMAGEMODE_RGB = 4;        ///< RGB图像

    public static final int MG_IMAGEMODE_COUNT = 5;        ///< 支持图像总数


    static BeaurifyJniSdk beaurifyImageJniSdk;
    public static boolean isTestHAL = false;

    static byte[] aligned;
    static byte[] outAligned;
    public synchronized static void testHALReleaseNV21Video(){
        if(beaurifyImageJniSdk!= null) {
            beaurifyImageJniSdk.nativeReleaseResources();
            beaurifyImageJniSdk = null;
        }
    }

    public synchronized static void testHALProcessNV21Video(Context context, byte[] input, byte[] output, int width, int height){

        int inW = width;
        int inH = height;
        width = BeaurifyJniSdk.getAlignSize(inW);
        height = BeaurifyJniSdk.getAlignSize(inH);
        if(beaurifyImageJniSdk == null){
            beaurifyImageJniSdk = BeaurifyJniSdk.videoInstance();
            beaurifyImageJniSdk.nativeCreateBeautyHandle(context, width,
                    height, 270, Util.MG_FPP_DENSEDETECTIONMODE_PREVIEW,
                    ConUtil.getFileContent(context, R.raw.mgbeautify_1_2_4_model)
                    , ConUtil.getFileContent(context, R.raw.detect_model),
                    ConUtil.getFileContent(context, R.raw.dense_model)
                    );

        }
        if(aligned == null || aligned.length!= width*height*3/2) {
            aligned = new byte[width * height * 3 / 2];
        }

        if(outAligned == null || outAligned.length!= width*height*3/2) {
            outAligned = new byte[width * height * 3 / 2];
        }
       // byte[] outAligned = new byte[width*height*3/2];

        BeaurifyJniSdk.alignNV21Data(input, inW, inH, aligned, width, height);

        beaurifyImageJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_DENOISE, Util.CURRENT_MG_BEAUTIFY_DENOISE);
        beaurifyImageJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS, Util.CURRENT_MG_BEAUTIFY_BRIGHTNESS);
        beaurifyImageJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTEN_EYE,Util.CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE);
        beaurifyImageJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_TOOTH,Util.CURRENT_MG_BEAUTIFY_TOOTH);
        beaurifyImageJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK, Util.CURRENT_MG_BEAUTIFY_ADD_PINK);

        beaurifyImageJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ENLARGE_EYE, Util.CURRENT_MG_BEAUTIFY_ENLARGE_EYE);
        beaurifyImageJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_SHRINK_FACE, Util.CURRENT_MG_BEAUTIFY_SHRINK_FACE);

        beaurifyImageJniSdk.nativeProcessImageNV21(aligned, outAligned
                , width
                , height);

        BeaurifyJniSdk.deAlignNV21Data(output, inW,inH, outAligned, width, height);

    }

    public static void resetDefaultBeautifyParam(){
        CURRENT_MG_BEAUTIFY_DENOISE = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_BRIGHTNESS = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_TOOTH = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_REMOVE_SPECKLES = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_REMOVE_EYEBAGS = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_HIGH_NOSE_BRIDGE = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_UP_CHEEK = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_SKIN_BALANCE = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_ADD_PINK = DEFAULT_BEAUTIRY_VALUE;

        CURRENT_MG_BEAUTIFY_SHRINK_FACE = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_ENLARGE_EYE = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_THIN_FACE = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_REMOVE_EYEBROW = DEFAULT_BEAUTIRY_VALUE;

        //美妆
        CURRENT_MG_BEAUTIFY_EYEBROW = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_R = DEFAULT_EYEBROW_COLOR[0];
        CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_G = DEFAULT_EYEBROW_COLOR[1];
        CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_B = DEFAULT_EYEBROW_COLOR[2];
        CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX = 0;

        CURRENT_MG_BEAUTIFY_BLUSH = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_BLUSH_COLOR_R = DEFAULT_BLUSH_COLOR[0];
        CURRENT_MG_BEAUTIFY_BLUSH_COLOR_G = DEFAULT_BLUSH_COLOR[1];
        CURRENT_MG_BEAUTIFY_BLUSH_COLOR_B = DEFAULT_BLUSH_COLOR[2];
        CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX = 0;

        CURRENT_MG_BEAUTIFY_EYESHADOW = 0;
        CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_R = DEFAULT_EYESHADOW_COLOR[0];
        CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_G = DEFAULT_EYESHADOW_COLOR[1];
        CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_B = DEFAULT_EYESHADOW_COLOR[2];
        CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX = 0;

        CURRENT_MG_BEAUTIFY_SHADING = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX = 0;

        CURRENT_MG_BEAUTIFY_CONTACTLENS = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_CONTACT_LENS_TEMPLATE_INDEX = 0;

        CURRENT_MG_BEAUTIFY_LIP = DEFAULT_BEAUTIRY_VALUE;
        CURRENT_MG_BEAUTIFY_LIP_COLOR_R = DEAFULT_LIP_COLOR[0];
        CURRENT_MG_BEAUTIFY_LIP_COLOR_G = DEAFULT_LIP_COLOR[1];
        CURRENT_MG_BEAUTIFY_LIP_COLOR_B = DEAFULT_LIP_COLOR[2];

    }

    public static void initTemplates(Context context){

        //加载美瞳模板资源
        for(int i=0;i<DEFAULT_OPTION_SUM;i++){

            InputStream is = null;
            try {
                is = context.getAssets().open(DEFAULT_CONTACT_LENS_ASSERTS[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DEFAULT_CONTACT_LENS_TEMPLATE[i][0] = BitmapFactory.decodeStream(is);
        }
        //加载眉毛资源
        Vector<KeyPoints> eyebrowKeyPointsVec = new Vector<>();
        //标准眉
        KeyPoints eyebrowKeyPointsTemp = new KeyPoints(16);
        eyebrowKeyPointsTemp.mKeyPoints[0]  =  27; eyebrowKeyPointsTemp.mKeyPoints[1]  = 75;

        eyebrowKeyPointsTemp.mKeyPoints[2]  = 100; eyebrowKeyPointsTemp.mKeyPoints[3]  = 49;
        eyebrowKeyPointsTemp.mKeyPoints[4]  = 189; eyebrowKeyPointsTemp.mKeyPoints[5]  = 48;
        eyebrowKeyPointsTemp.mKeyPoints[6]  = 280; eyebrowKeyPointsTemp.mKeyPoints[7]  = 52;

        eyebrowKeyPointsTemp.mKeyPoints[8]  = 375; eyebrowKeyPointsTemp.mKeyPoints[9]  = 93;

        eyebrowKeyPointsTemp.mKeyPoints[10] = 280; eyebrowKeyPointsTemp.mKeyPoints[11] = 83;
        eyebrowKeyPointsTemp.mKeyPoints[12] = 189; eyebrowKeyPointsTemp.mKeyPoints[13] = 85;
        eyebrowKeyPointsTemp.mKeyPoints[14] = 100; eyebrowKeyPointsTemp.mKeyPoints[15] = 92;

        eyebrowKeyPointsTemp.mKeyPoints[16]  =  79; eyebrowKeyPointsTemp.mKeyPoints[17]  = 93;

        eyebrowKeyPointsTemp.mKeyPoints[18]  = 174; eyebrowKeyPointsTemp.mKeyPoints[19]  = 52;
        eyebrowKeyPointsTemp.mKeyPoints[20]  = 265; eyebrowKeyPointsTemp.mKeyPoints[21]  = 48;
        eyebrowKeyPointsTemp.mKeyPoints[22]  = 354; eyebrowKeyPointsTemp.mKeyPoints[23]  = 49;

        eyebrowKeyPointsTemp.mKeyPoints[24]  = 427; eyebrowKeyPointsTemp.mKeyPoints[25]  = 75;

        eyebrowKeyPointsTemp.mKeyPoints[26] = 354; eyebrowKeyPointsTemp.mKeyPoints[27] = 92;
        eyebrowKeyPointsTemp.mKeyPoints[28] = 265; eyebrowKeyPointsTemp.mKeyPoints[29] = 85;
        eyebrowKeyPointsTemp.mKeyPoints[30] = 174; eyebrowKeyPointsTemp.mKeyPoints[31] = 83;


        eyebrowKeyPointsVec.add(eyebrowKeyPointsTemp);

        //流星眉
        eyebrowKeyPointsTemp = new KeyPoints(16);
        eyebrowKeyPointsTemp.mKeyPoints[0]  =  21; eyebrowKeyPointsTemp.mKeyPoints[1]  = 86;
        eyebrowKeyPointsTemp.mKeyPoints[2]  = 101; eyebrowKeyPointsTemp.mKeyPoints[3]  = 56;
        eyebrowKeyPointsTemp.mKeyPoints[4]  = 182; eyebrowKeyPointsTemp.mKeyPoints[5]  = 51;
        eyebrowKeyPointsTemp.mKeyPoints[6]  = 273; eyebrowKeyPointsTemp.mKeyPoints[7]  = 56;
        eyebrowKeyPointsTemp.mKeyPoints[8]  = 362; eyebrowKeyPointsTemp.mKeyPoints[9]  = 87;
        eyebrowKeyPointsTemp.mKeyPoints[10] = 273; eyebrowKeyPointsTemp.mKeyPoints[11] = 81;
        eyebrowKeyPointsTemp.mKeyPoints[12] = 182; eyebrowKeyPointsTemp.mKeyPoints[13] = 85;
        eyebrowKeyPointsTemp.mKeyPoints[14] = 101; eyebrowKeyPointsTemp.mKeyPoints[15] = 96;
        eyebrowKeyPointsTemp.mKeyPoints[16]  =  92; eyebrowKeyPointsTemp.mKeyPoints[17]  = 87;
        eyebrowKeyPointsTemp.mKeyPoints[18]  = 181; eyebrowKeyPointsTemp.mKeyPoints[19]  = 56;
        eyebrowKeyPointsTemp.mKeyPoints[20]  = 272; eyebrowKeyPointsTemp.mKeyPoints[21]  = 51;
        eyebrowKeyPointsTemp.mKeyPoints[22]  = 353; eyebrowKeyPointsTemp.mKeyPoints[23]  = 56;
        eyebrowKeyPointsTemp.mKeyPoints[24]  = 433; eyebrowKeyPointsTemp.mKeyPoints[25]  = 86;
        eyebrowKeyPointsTemp.mKeyPoints[26] = 353; eyebrowKeyPointsTemp.mKeyPoints[27] = 96;
        eyebrowKeyPointsTemp.mKeyPoints[28] = 272; eyebrowKeyPointsTemp.mKeyPoints[29] = 85;
        eyebrowKeyPointsTemp.mKeyPoints[30] = 181; eyebrowKeyPointsTemp.mKeyPoints[31] = 81;
        eyebrowKeyPointsVec.add(eyebrowKeyPointsTemp);

        //柳叶眉
        eyebrowKeyPointsTemp = new KeyPoints(16);
        eyebrowKeyPointsTemp.mKeyPoints[0]  =  37; eyebrowKeyPointsTemp.mKeyPoints[1]  = 84;
        eyebrowKeyPointsTemp.mKeyPoints[2]  = 111; eyebrowKeyPointsTemp.mKeyPoints[3]  = 52;
        eyebrowKeyPointsTemp.mKeyPoints[4]  = 190; eyebrowKeyPointsTemp.mKeyPoints[5]  = 47;
        eyebrowKeyPointsTemp.mKeyPoints[6]  = 273; eyebrowKeyPointsTemp.mKeyPoints[7]  = 51;
        eyebrowKeyPointsTemp.mKeyPoints[8]  = 358; eyebrowKeyPointsTemp.mKeyPoints[9]  = 89;
        eyebrowKeyPointsTemp.mKeyPoints[10] = 273; eyebrowKeyPointsTemp.mKeyPoints[11] = 75;
        eyebrowKeyPointsTemp.mKeyPoints[12] = 190; eyebrowKeyPointsTemp.mKeyPoints[13] = 76;
        eyebrowKeyPointsTemp.mKeyPoints[14] = 111; eyebrowKeyPointsTemp.mKeyPoints[15] = 92;
        eyebrowKeyPointsTemp.mKeyPoints[16]  =  96; eyebrowKeyPointsTemp.mKeyPoints[17]  = 89;
        eyebrowKeyPointsTemp.mKeyPoints[18]  = 181; eyebrowKeyPointsTemp.mKeyPoints[19]  = 51;
        eyebrowKeyPointsTemp.mKeyPoints[20]  = 264; eyebrowKeyPointsTemp.mKeyPoints[21]  = 47;
        eyebrowKeyPointsTemp.mKeyPoints[22]  = 343; eyebrowKeyPointsTemp.mKeyPoints[23]  = 52;
        eyebrowKeyPointsTemp.mKeyPoints[24]  = 417; eyebrowKeyPointsTemp.mKeyPoints[25]  = 84;
        eyebrowKeyPointsTemp.mKeyPoints[26] = 343; eyebrowKeyPointsTemp.mKeyPoints[27] = 92;
        eyebrowKeyPointsTemp.mKeyPoints[28] = 264; eyebrowKeyPointsTemp.mKeyPoints[29] = 76;
        eyebrowKeyPointsTemp.mKeyPoints[30] = 181; eyebrowKeyPointsTemp.mKeyPoints[31] = 75;
        eyebrowKeyPointsVec.add(eyebrowKeyPointsTemp);

        //欧美桃眉
        eyebrowKeyPointsTemp = new KeyPoints(16);
        eyebrowKeyPointsTemp.mKeyPoints[0]  =  33; eyebrowKeyPointsTemp.mKeyPoints[1]  = 83;
        eyebrowKeyPointsTemp.mKeyPoints[2]  = 116; eyebrowKeyPointsTemp.mKeyPoints[3]  = 51;
        eyebrowKeyPointsTemp.mKeyPoints[4]  = 194; eyebrowKeyPointsTemp.mKeyPoints[5]  = 44;
        eyebrowKeyPointsTemp.mKeyPoints[6]  = 283; eyebrowKeyPointsTemp.mKeyPoints[7]  = 47;
        eyebrowKeyPointsTemp.mKeyPoints[8]  = 363; eyebrowKeyPointsTemp.mKeyPoints[9]  = 84;
        eyebrowKeyPointsTemp.mKeyPoints[10] = 283; eyebrowKeyPointsTemp.mKeyPoints[11] = 68;
        eyebrowKeyPointsTemp.mKeyPoints[12] = 194; eyebrowKeyPointsTemp.mKeyPoints[13] = 73;
        eyebrowKeyPointsTemp.mKeyPoints[14] = 116; eyebrowKeyPointsTemp.mKeyPoints[15] = 92;
        eyebrowKeyPointsTemp.mKeyPoints[16]  =  91; eyebrowKeyPointsTemp.mKeyPoints[17]  = 84;
        eyebrowKeyPointsTemp.mKeyPoints[18]  = 171; eyebrowKeyPointsTemp.mKeyPoints[19]  = 47;
        eyebrowKeyPointsTemp.mKeyPoints[20]  = 260; eyebrowKeyPointsTemp.mKeyPoints[21]  = 44;
        eyebrowKeyPointsTemp.mKeyPoints[22]  = 338; eyebrowKeyPointsTemp.mKeyPoints[23]  = 51;
        eyebrowKeyPointsTemp.mKeyPoints[24]  = 421; eyebrowKeyPointsTemp.mKeyPoints[25]  = 83;
        eyebrowKeyPointsTemp.mKeyPoints[26] = 338; eyebrowKeyPointsTemp.mKeyPoints[27] = 92;
        eyebrowKeyPointsTemp.mKeyPoints[28] = 260; eyebrowKeyPointsTemp.mKeyPoints[29] = 73;
        eyebrowKeyPointsTemp.mKeyPoints[30] = 171; eyebrowKeyPointsTemp.mKeyPoints[31] = 68;
        eyebrowKeyPointsVec.add(eyebrowKeyPointsTemp);

        //丝雾眉
        eyebrowKeyPointsTemp = new KeyPoints(16);
        eyebrowKeyPointsTemp.mKeyPoints[0]  =  30; eyebrowKeyPointsTemp.mKeyPoints[1]  = 86;

        eyebrowKeyPointsTemp.mKeyPoints[2]  = 108; eyebrowKeyPointsTemp.mKeyPoints[3]  = 54;
        // eyebrowKeyPointsTemp.mKeyPoints[2]  =  70; eyebrowKeyPointsTemp.mKeyPoints[3]  = 60;
        eyebrowKeyPointsTemp.mKeyPoints[4]  = 190; eyebrowKeyPointsTemp.mKeyPoints[5]  = 48;
        eyebrowKeyPointsTemp.mKeyPoints[6]  = 280; eyebrowKeyPointsTemp.mKeyPoints[7]  = 53;

        eyebrowKeyPointsTemp.mKeyPoints[8]  = 366; eyebrowKeyPointsTemp.mKeyPoints[9]  = 96;

        eyebrowKeyPointsTemp.mKeyPoints[10] = 280; eyebrowKeyPointsTemp.mKeyPoints[11] = 82;
        eyebrowKeyPointsTemp.mKeyPoints[12] = 190; eyebrowKeyPointsTemp.mKeyPoints[13] = 84;
        eyebrowKeyPointsTemp.mKeyPoints[14] = 108; eyebrowKeyPointsTemp.mKeyPoints[15] = 96;
        // eyebrowKeyPointsTemp.mKeyPoints[14] = 70; eyebrowKeyPointsTemp.mKeyPoints[15] = 102;


        eyebrowKeyPointsTemp.mKeyPoints[16]  =  88; eyebrowKeyPointsTemp.mKeyPoints[17]  = 96;

        eyebrowKeyPointsTemp.mKeyPoints[18]  = 174; eyebrowKeyPointsTemp.mKeyPoints[19]  = 53;
        eyebrowKeyPointsTemp.mKeyPoints[20]  = 264; eyebrowKeyPointsTemp.mKeyPoints[21]  = 48;
        eyebrowKeyPointsTemp.mKeyPoints[22]  = 346; eyebrowKeyPointsTemp.mKeyPoints[23]  = 54;

        eyebrowKeyPointsTemp.mKeyPoints[24]  = 424; eyebrowKeyPointsTemp.mKeyPoints[25]  = 86;

        eyebrowKeyPointsTemp.mKeyPoints[26] = 346; eyebrowKeyPointsTemp.mKeyPoints[27] = 96;
        eyebrowKeyPointsTemp.mKeyPoints[28] = 264; eyebrowKeyPointsTemp.mKeyPoints[29] = 84;
        eyebrowKeyPointsTemp.mKeyPoints[30] = 174; eyebrowKeyPointsTemp.mKeyPoints[31] = 82;
        eyebrowKeyPointsVec.add(eyebrowKeyPointsTemp);

        //读取眉毛素材
        Bitmap [] oriEyebrowAssert = new Bitmap[DEFAULT_EYEBROW_ASSERTS.length];
        for(int i=0;i<DEFAULT_EYEBROW_ASSERTS.length;i++){
            InputStream is = null;
            try {
                is = context.getAssets().open(DEFAULT_EYEBROW_ASSERTS[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            oriEyebrowAssert[i] = BitmapFactory.decodeStream(is);
        }

        for(int i = 0;i<DEFAULT_OPTION_SUM;i++){
            if(i >=DEFAULT_EYEBROW_ASSERTS.length){
                DEFAULT_EYEBROW_KEYPOINTS[i] = eyebrowKeyPointsVec.get(i%DEFAULT_EYEBROW_ASSERTS.length);
                DEFAULT_EYEBROW_TEMPLATE[i][0] = oriEyebrowAssert[i%DEFAULT_EYEBROW_ASSERTS.length];
                continue;
            }
            DEFAULT_EYEBROW_KEYPOINTS[i] = eyebrowKeyPointsVec.get(i);
            DEFAULT_EYEBROW_TEMPLATE[i][0] = oriEyebrowAssert[i];
        }



        //加载腮红资源
        KeyPoints BlushKeyPointsTemp = new KeyPoints(81);
        BlushKeyPointsTemp.mKeyPoints[0] = 362.87f; BlushKeyPointsTemp.mKeyPoints[1] = 517.022f;
        BlushKeyPointsTemp.mKeyPoints[2] = 320.142f; BlushKeyPointsTemp.mKeyPoints[3] = 521.374f;
        BlushKeyPointsTemp.mKeyPoints[4] = 408.491f; BlushKeyPointsTemp.mKeyPoints[5] = 529.15f;
        BlushKeyPointsTemp.mKeyPoints[6] = 363.114f; BlushKeyPointsTemp.mKeyPoints[7] = 503.056f;
        BlushKeyPointsTemp.mKeyPoints[8] = 361.258f; BlushKeyPointsTemp.mKeyPoints[9] = 533.356f;
        BlushKeyPointsTemp.mKeyPoints[10] = 338.816f; BlushKeyPointsTemp.mKeyPoints[11] = 507.826f;
        BlushKeyPointsTemp.mKeyPoints[12] = 338.569f; BlushKeyPointsTemp.mKeyPoints[13] = 529.143f;
        BlushKeyPointsTemp.mKeyPoints[14] = 388.817f; BlushKeyPointsTemp.mKeyPoints[15] = 510.407f;
        BlushKeyPointsTemp.mKeyPoints[16] = 385.686f; BlushKeyPointsTemp.mKeyPoints[17] = 531.579f;
        BlushKeyPointsTemp.mKeyPoints[18] = 555.843f; BlushKeyPointsTemp.mKeyPoints[19] = 515.516f;
        BlushKeyPointsTemp.mKeyPoints[20] = 512.377f; BlushKeyPointsTemp.mKeyPoints[21] = 527.556f;
        BlushKeyPointsTemp.mKeyPoints[22] = 599.29f; BlushKeyPointsTemp.mKeyPoints[23] = 516.917f;
        BlushKeyPointsTemp.mKeyPoints[24] = 555.168f; BlushKeyPointsTemp.mKeyPoints[25] = 500.493f;
        BlushKeyPointsTemp.mKeyPoints[26] = 558.566f; BlushKeyPointsTemp.mKeyPoints[27] = 530.507f;
        BlushKeyPointsTemp.mKeyPoints[28] = 530.623f; BlushKeyPointsTemp.mKeyPoints[29] = 508.916f;
        BlushKeyPointsTemp.mKeyPoints[30] = 534.66f; BlushKeyPointsTemp.mKeyPoints[31] = 529.838f;
        BlushKeyPointsTemp.mKeyPoints[32] = 579.79f; BlushKeyPointsTemp.mKeyPoints[33] = 504.217f;
        BlushKeyPointsTemp.mKeyPoints[34] = 581.091f; BlushKeyPointsTemp.mKeyPoints[35] = 525.591f;
        BlushKeyPointsTemp.mKeyPoints[36] = 276.762f; BlushKeyPointsTemp.mKeyPoints[37] = 459.298f;
        BlushKeyPointsTemp.mKeyPoints[38] = 408.513f; BlushKeyPointsTemp.mKeyPoints[39] = 447.123f;
        BlushKeyPointsTemp.mKeyPoints[40] = 340.471f; BlushKeyPointsTemp.mKeyPoints[41] = 430.59f;
        BlushKeyPointsTemp.mKeyPoints[42] = 341.387f; BlushKeyPointsTemp.mKeyPoints[43] = 455.362f;
        BlushKeyPointsTemp.mKeyPoints[44] = 304.96f; BlushKeyPointsTemp.mKeyPoints[45] = 436.591f;
        BlushKeyPointsTemp.mKeyPoints[46] = 308.812f; BlushKeyPointsTemp.mKeyPoints[47] = 456.361f;
        BlushKeyPointsTemp.mKeyPoints[48] = 375.967f; BlushKeyPointsTemp.mKeyPoints[49] = 431.931f;
        BlushKeyPointsTemp.mKeyPoints[50] = 374.994f; BlushKeyPointsTemp.mKeyPoints[51] = 454.486f;
        BlushKeyPointsTemp.mKeyPoints[52] = 506.207f; BlushKeyPointsTemp.mKeyPoints[53] = 444.851f;
        BlushKeyPointsTemp.mKeyPoints[54] = 638.774f; BlushKeyPointsTemp.mKeyPoints[55] = 455.753f;
        BlushKeyPointsTemp.mKeyPoints[56] = 574.439f; BlushKeyPointsTemp.mKeyPoints[57] = 427.733f;
        BlushKeyPointsTemp.mKeyPoints[58] = 573.854f; BlushKeyPointsTemp.mKeyPoints[59] = 450f;
        BlushKeyPointsTemp.mKeyPoints[60] = 538.586f; BlushKeyPointsTemp.mKeyPoints[61] = 430.184f;
        BlushKeyPointsTemp.mKeyPoints[62] = 540.05f; BlushKeyPointsTemp.mKeyPoints[63] = 450.733f;
        BlushKeyPointsTemp.mKeyPoints[64] = 610.112f; BlushKeyPointsTemp.mKeyPoints[65] = 433.456f;
        BlushKeyPointsTemp.mKeyPoints[66] = 606.535f; BlushKeyPointsTemp.mKeyPoints[67] = 451.426f;
        BlushKeyPointsTemp.mKeyPoints[68] = 463.164f; BlushKeyPointsTemp.mKeyPoints[69] = 641.757f;
        BlushKeyPointsTemp.mKeyPoints[70] = 463.487f; BlushKeyPointsTemp.mKeyPoints[71] = 675.654f;
        BlushKeyPointsTemp.mKeyPoints[72] = 432.965f; BlushKeyPointsTemp.mKeyPoints[73] = 527.003f;
        BlushKeyPointsTemp.mKeyPoints[74] = 490.61f; BlushKeyPointsTemp.mKeyPoints[75] = 524.751f;
        BlushKeyPointsTemp.mKeyPoints[76] = 420.762f; BlushKeyPointsTemp.mKeyPoints[77] = 618.361f;
        BlushKeyPointsTemp.mKeyPoints[78] = 505.594f; BlushKeyPointsTemp.mKeyPoints[79] = 617.233f;
        BlushKeyPointsTemp.mKeyPoints[80] = 408.447f; BlushKeyPointsTemp.mKeyPoints[81] = 652.453f;
        BlushKeyPointsTemp.mKeyPoints[82] = 517.967f; BlushKeyPointsTemp.mKeyPoints[83] = 651.52f;
        BlushKeyPointsTemp.mKeyPoints[84] = 434.546f; BlushKeyPointsTemp.mKeyPoints[85] = 666.303f;
        BlushKeyPointsTemp.mKeyPoints[86] = 492.494f; BlushKeyPointsTemp.mKeyPoints[87] = 665.683f;
        BlushKeyPointsTemp.mKeyPoints[88] = 377.404f; BlushKeyPointsTemp.mKeyPoints[89] = 719.558f;
        BlushKeyPointsTemp.mKeyPoints[90] = 553.483f; BlushKeyPointsTemp.mKeyPoints[91] = 720.042f;
        BlushKeyPointsTemp.mKeyPoints[92] = 463.767f; BlushKeyPointsTemp.mKeyPoints[93] = 714.328f;
        BlushKeyPointsTemp.mKeyPoints[94] = 463.872f; BlushKeyPointsTemp.mKeyPoints[95] = 728.005f;
        BlushKeyPointsTemp.mKeyPoints[96] = 445.074f; BlushKeyPointsTemp.mKeyPoints[97] = 711.068f;
        BlushKeyPointsTemp.mKeyPoints[98] = 482.533f; BlushKeyPointsTemp.mKeyPoints[99] = 710.886f;
        BlushKeyPointsTemp.mKeyPoints[100] = 410.653f; BlushKeyPointsTemp.mKeyPoints[101] = 714.469f;
        BlushKeyPointsTemp.mKeyPoints[102] = 518.377f; BlushKeyPointsTemp.mKeyPoints[103] = 714.485f;
        BlushKeyPointsTemp.mKeyPoints[104] = 420.973f; BlushKeyPointsTemp.mKeyPoints[105] = 723.81f;
        BlushKeyPointsTemp.mKeyPoints[106] = 508.175f; BlushKeyPointsTemp.mKeyPoints[107] = 723.942f;
        BlushKeyPointsTemp.mKeyPoints[108] = 464.471f; BlushKeyPointsTemp.mKeyPoints[109] = 753.378f;
        BlushKeyPointsTemp.mKeyPoints[110] = 464.219f; BlushKeyPointsTemp.mKeyPoints[111] = 778.666f;
        BlushKeyPointsTemp.mKeyPoints[112] = 417.469f; BlushKeyPointsTemp.mKeyPoints[113] = 743.682f;
        BlushKeyPointsTemp.mKeyPoints[114] = 512.279f; BlushKeyPointsTemp.mKeyPoints[115] = 743.761f;
        BlushKeyPointsTemp.mKeyPoints[116] = 398.016f; BlushKeyPointsTemp.mKeyPoints[117] = 747.889f;
        BlushKeyPointsTemp.mKeyPoints[118] = 424.459f; BlushKeyPointsTemp.mKeyPoints[119] = 769.53f;
        BlushKeyPointsTemp.mKeyPoints[120] = 504.169f; BlushKeyPointsTemp.mKeyPoints[121] = 769.733f;
        BlushKeyPointsTemp.mKeyPoints[122] = 531.603f; BlushKeyPointsTemp.mKeyPoints[123] = 748.112f;
        BlushKeyPointsTemp.mKeyPoints[124] = 246.552f; BlushKeyPointsTemp.mKeyPoints[125] = 526.588f;
        BlushKeyPointsTemp.mKeyPoints[126] = 673.195f; BlushKeyPointsTemp.mKeyPoints[127] = 518.348f;
        BlushKeyPointsTemp.mKeyPoints[128] = 469.935f; BlushKeyPointsTemp.mKeyPoints[129] = 882.101f;
        BlushKeyPointsTemp.mKeyPoints[130] = 250.866f; BlushKeyPointsTemp.mKeyPoints[131] = 577.321f;
        BlushKeyPointsTemp.mKeyPoints[132] = 259.794f; BlushKeyPointsTemp.mKeyPoints[133] = 626.09f;
        BlushKeyPointsTemp.mKeyPoints[134] = 271.993f; BlushKeyPointsTemp.mKeyPoints[135] = 674.15f;
        BlushKeyPointsTemp.mKeyPoints[136] = 287.78f; BlushKeyPointsTemp.mKeyPoints[137] = 721.127f;
        BlushKeyPointsTemp.mKeyPoints[138] = 310.595f; BlushKeyPointsTemp.mKeyPoints[139] = 764.725f;
        BlushKeyPointsTemp.mKeyPoints[140] = 339.472f; BlushKeyPointsTemp.mKeyPoints[141] = 804.5f;
        BlushKeyPointsTemp.mKeyPoints[142] = 373.502f; BlushKeyPointsTemp.mKeyPoints[143] = 840.386f;
        BlushKeyPointsTemp.mKeyPoints[144] = 415.114f; BlushKeyPointsTemp.mKeyPoints[145] = 870.392f;
        BlushKeyPointsTemp.mKeyPoints[146] = 670.83f; BlushKeyPointsTemp.mKeyPoints[147] = 568.596f;
        BlushKeyPointsTemp.mKeyPoints[148] = 664.201f; BlushKeyPointsTemp.mKeyPoints[149] = 617.428f;
        BlushKeyPointsTemp.mKeyPoints[150] = 655.01f; BlushKeyPointsTemp.mKeyPoints[151] = 666.074f;
        BlushKeyPointsTemp.mKeyPoints[152] = 642.338f; BlushKeyPointsTemp.mKeyPoints[153] = 713.585f;
        BlushKeyPointsTemp.mKeyPoints[154] = 621.823f; BlushKeyPointsTemp.mKeyPoints[155] = 757.984f;
        BlushKeyPointsTemp.mKeyPoints[156] = 595.07f; BlushKeyPointsTemp.mKeyPoints[157] = 798.389f;
        BlushKeyPointsTemp.mKeyPoints[158] = 562.773f; BlushKeyPointsTemp.mKeyPoints[159] = 835.54f;
        BlushKeyPointsTemp.mKeyPoints[160] = 523.51f; BlushKeyPointsTemp.mKeyPoints[161] = 867.696f;

        BitmapFactory.Options optionsGray = new BitmapFactory.Options();
        optionsGray.inPreferredConfig = Bitmap.Config.ALPHA_8;
        for(int i=0;i<DEFAULT_OPTION_SUM;i++){
            DEFAULT_BLUSH_KEYPOINTS[i] = BlushKeyPointsTemp;
            if(i >= (DEFAULT_BLUSH_ASSERTS.length/2)){
                DEFAULT_BLUSH_TEMPLATE[i][0] = DEFAULT_BLUSH_TEMPLATE[i%DEFAULT_BLUSH_ASSERTS.length][0];
                DEFAULT_BLUSH_TEMPLATE[i][1] = DEFAULT_BLUSH_TEMPLATE[i%DEFAULT_BLUSH_ASSERTS.length][1];
                continue;
            }
            InputStream is = null;
            try {
                is = context.getAssets().open(Util.DEFAULT_BLUSH_ASSERTS[i*2]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DEFAULT_BLUSH_TEMPLATE[i][0] = BitmapFactory.decodeStream(is, null, optionsGray);

            try {
                is = context.getAssets().open(Util.DEFAULT_BLUSH_ASSERTS[i*2+1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DEFAULT_BLUSH_TEMPLATE[i][1] = BitmapFactory.decodeStream(is, null, optionsGray);
        }

        //加载眼眼影资源
        for(int i=0;i<DEFAULT_OPTION_SUM;i++){
            DEFAULT_EYESHADOW_KEYPOINTS[i] = BlushKeyPointsTemp;

            if(i >= (DEFAULT_EYESHADOW_ASSERTS.length/2)){
                DEFAULT_EYESHADOW_TEMPLATE[i][0] = DEFAULT_EYESHADOW_TEMPLATE[i%DEFAULT_EYESHADOW_ASSERTS.length][0];
                DEFAULT_EYESHADOW_TEMPLATE[i][1] = DEFAULT_EYESHADOW_TEMPLATE[i%DEFAULT_EYESHADOW_ASSERTS.length][1];
                continue;
            }

            InputStream is = null;
            try {
                is = context.getAssets().open(Util.DEFAULT_EYESHADOW_ASSERTS[i*2]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DEFAULT_EYESHADOW_TEMPLATE[i][0] = BitmapFactory.decodeStream(is, null, optionsGray);

            try {
                is = context.getAssets().open(Util.DEFAULT_EYESHADOW_ASSERTS[i*2+1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DEFAULT_EYESHADOW_TEMPLATE[i][1] = BitmapFactory.decodeStream(is, null, optionsGray);
        }

        //加载修容资源
        KeyPoints ShadingKeyPointsTemp = new KeyPoints(38);
        ShadingKeyPointsTemp.mKeyPoints[0] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[1] = 682.0f;
        ShadingKeyPointsTemp.mKeyPoints[2] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[3] = 722.0f;
        ShadingKeyPointsTemp.mKeyPoints[4] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[5] = 762.0f;
        ShadingKeyPointsTemp.mKeyPoints[6] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[7] = 802.0f;
        ShadingKeyPointsTemp.mKeyPoints[8] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[9] = 842.0f;
        ShadingKeyPointsTemp.mKeyPoints[10] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[11] = 885.0f;
        ShadingKeyPointsTemp.mKeyPoints[12] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[13] = 925.0f;
        ShadingKeyPointsTemp.mKeyPoints[14] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[15] = 965.0f;
        ShadingKeyPointsTemp.mKeyPoints[16] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[17] = 1005.0f;
        ShadingKeyPointsTemp.mKeyPoints[18] = 236.0f;        ShadingKeyPointsTemp.mKeyPoints[19] = 658.0f;
        ShadingKeyPointsTemp.mKeyPoints[20] = 329.0f;        ShadingKeyPointsTemp.mKeyPoints[21] = 613.0f;
        ShadingKeyPointsTemp.mKeyPoints[22] = 433.0f;        ShadingKeyPointsTemp.mKeyPoints[23] = 683.0f;
        ShadingKeyPointsTemp.mKeyPoints[24] = 329.0f;        ShadingKeyPointsTemp.mKeyPoints[25] = 704.0f;
        ShadingKeyPointsTemp.mKeyPoints[26] = 470.0f;        ShadingKeyPointsTemp.mKeyPoints[27] = 682.0f;
        ShadingKeyPointsTemp.mKeyPoints[28] = 440.0f;        ShadingKeyPointsTemp.mKeyPoints[29] = 963.0f;
        ShadingKeyPointsTemp.mKeyPoints[30] = 656.0f;        ShadingKeyPointsTemp.mKeyPoints[31] = 963.0f;
        ShadingKeyPointsTemp.mKeyPoints[32] = 626.0f;        ShadingKeyPointsTemp.mKeyPoints[33] = 682.0f;
        ShadingKeyPointsTemp.mKeyPoints[34] = 363.0f;        ShadingKeyPointsTemp.mKeyPoints[35] = 1126.0f;
        ShadingKeyPointsTemp.mKeyPoints[36] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[37] = 1091.0f;
        ShadingKeyPointsTemp.mKeyPoints[38] = 733.0f;        ShadingKeyPointsTemp.mKeyPoints[39] = 1126.0f;
        ShadingKeyPointsTemp.mKeyPoints[40] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[41] = 1225.0f;
        ShadingKeyPointsTemp.mKeyPoints[42] = 104.0f;        ShadingKeyPointsTemp.mKeyPoints[43] = 963.0f;
        ShadingKeyPointsTemp.mKeyPoints[44] = 171.0f;        ShadingKeyPointsTemp.mKeyPoints[45] = 1159.0f;
        ShadingKeyPointsTemp.mKeyPoints[46] = 247.0f;        ShadingKeyPointsTemp.mKeyPoints[47] = 1267.0f;
        ShadingKeyPointsTemp.mKeyPoints[48] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[49] = 1414.0f;
        ShadingKeyPointsTemp.mKeyPoints[50] = 849.0f;        ShadingKeyPointsTemp.mKeyPoints[51] = 1267.0f;
        ShadingKeyPointsTemp.mKeyPoints[52] = 925.0f;        ShadingKeyPointsTemp.mKeyPoints[53] = 1159.0f;
        ShadingKeyPointsTemp.mKeyPoints[54] = 992.0f;        ShadingKeyPointsTemp.mKeyPoints[55] = 963.0f;
        ShadingKeyPointsTemp.mKeyPoints[56] = 219.0f;        ShadingKeyPointsTemp.mKeyPoints[57] = 212.0f;
        ShadingKeyPointsTemp.mKeyPoints[58] = 548.0f;        ShadingKeyPointsTemp.mKeyPoints[59] = 114.0f;
        ShadingKeyPointsTemp.mKeyPoints[60] = 877.0f;        ShadingKeyPointsTemp.mKeyPoints[61] = 212.0f;
        ShadingKeyPointsTemp.mKeyPoints[62] = 1007.0f;       ShadingKeyPointsTemp.mKeyPoints[63] = 620.0f;
        ShadingKeyPointsTemp.mKeyPoints[64] = 545.0f;        ShadingKeyPointsTemp.mKeyPoints[65] = 944.0f;
        ShadingKeyPointsTemp.mKeyPoints[66] = 540.0f;        ShadingKeyPointsTemp.mKeyPoints[67] = 618.0f;
        ShadingKeyPointsTemp.mKeyPoints[68] = 445.0f;        ShadingKeyPointsTemp.mKeyPoints[69] = 690.0f;
        ShadingKeyPointsTemp.mKeyPoints[70] = 451.0f;        ShadingKeyPointsTemp.mKeyPoints[71] = 868.0f;
        ShadingKeyPointsTemp.mKeyPoints[72] = 652.0f;        ShadingKeyPointsTemp.mKeyPoints[73] = 686.0f;
        ShadingKeyPointsTemp.mKeyPoints[74] = 638.0f;        ShadingKeyPointsTemp.mKeyPoints[75] = 863.0f;

        Bitmap[] shadingOriTemplate = new Bitmap[SHADING_ORI_TEMPLATE_SUM];

        for(int i=0;i<SHADING_ORI_TEMPLATE_SUM;i++){
            InputStream is = null;
            try {
                is = context.getAssets().open(Util.DEFAULT_SHADING_ASSERTS[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            shadingOriTemplate[i] = BitmapFactory.decodeStream(is, null, optionsGray);
        }

        for(int i=0;i<DEFAULT_OPTION_SUM;i++){
            DEFAULT_SHADING_KEYPOINTS[i] = ShadingKeyPointsTemp;

        }
        //模板一
        Bitmap[] templeFace = null;
        Bitmap[] templeNose  = new Bitmap[2];
        templeNose[0] = shadingOriTemplate[6];
        templeNose[1] = shadingOriTemplate[7];
        DEFAULT_SHADING_TEMPLATE.add(new Pair<>(templeFace,templeNose));


        //模板二
        templeFace  = new Bitmap[3];
        templeFace[0] = shadingOriTemplate[0];
        templeFace[1] = shadingOriTemplate[2];
        templeFace[2] = shadingOriTemplate[4];

        templeNose  = new Bitmap[1];
        templeNose[0] = shadingOriTemplate[6];
        DEFAULT_SHADING_TEMPLATE.add(new Pair<>(templeFace,templeNose));

        //模板三
        templeFace = new Bitmap[4];
        templeFace[0] = shadingOriTemplate[0];
        templeFace[1] = shadingOriTemplate[1];
        templeFace[2] = shadingOriTemplate[2];
        templeFace[3] = shadingOriTemplate[5];
//        templeFace[4] = shadingOriTemplate[4];
//        templeFace[5] = shadingOriTemplate[5];
        templeNose = new Bitmap[2];
        templeNose[0] = shadingOriTemplate[6];
        templeNose[1] = shadingOriTemplate[7];
        DEFAULT_SHADING_TEMPLATE.add(new Pair<>(templeFace,templeNose));
        //模板四
        templeFace = new Bitmap[3];
        templeFace[0] = shadingOriTemplate[0];
        templeFace[1] = shadingOriTemplate[1];
        templeFace[2] = shadingOriTemplate[2];
        templeNose = new Bitmap[1];
        templeNose[0] = shadingOriTemplate[6];
        DEFAULT_SHADING_TEMPLATE.add(new Pair<>(templeFace,templeNose));

        //模板五
        templeFace = new Bitmap[4];
        templeFace[0] = shadingOriTemplate[0];
        templeFace[1] = shadingOriTemplate[1];
        templeFace[2] = shadingOriTemplate[2];
        templeFace[3] = shadingOriTemplate[4];
        templeNose = new Bitmap[2];
        templeNose[0] = shadingOriTemplate[6];
        templeNose[1] = shadingOriTemplate[7];
        DEFAULT_SHADING_TEMPLATE.add(new Pair<>(templeFace,templeNose));

    }

    public static Boolean needGone(String feature){

        if(BuildConfig.FEA_ABILITY=="all"){
            return false;
        }


        if(BuildConfig.FEA_ABILITY.contains(feature)){
            return false;
        }else{
            return true;
        }
    }

    public static Boolean needGoneBeautify(){
        if(Util.isPreView){
            if(Util.needGone(",denoise") && Util.needGone(",brightness") && Util.needGone(",brithten_eye") && Util.needGone(",tooth") && Util.needGone(",remove_eyebags") &&
                    Util.needGone(",pink") ){
                return true;
            }else{
                return false;
            }
        }else{
            if(Util.needGone(",denoise") && Util.needGone(",brightness") && Util.needGone(",brithten_eye") && Util.needGone(",tooth") && Util.needGone(",remove_eyebags") &&
                    Util.needGone(",pink") && Util.needGone(",upcheek")){
                return true;
            }else{
                return false;
            }
        }
    }

    public static Boolean needGoneBeautifyShape(){
        if(Util.needGone(",enlarge_eye") && Util.needGone(",thin_face") && Util.needGone(",shrink_face") && Util.needGone(",remove_eyebrow")){
            return true;
        }else{
            return false;
        }
    }

    public static Boolean needGoneMakeup(){
        if(Util.isPreView){
            if(Util.needGone(",eyebrow") && Util.needGone(",lip") && Util.needGone(",add_blush") && Util.needGone(",eyeshadow") && Util.needGone(",contact_lens")){
                return true;
            }else{
                return false;
            }
        }else{
            if(Util.needGone(",eyebrow") && Util.needGone(",lip") && Util.needGone(",add_blush") && Util.needGone(",eyeshadow") &&
                    Util.needGone(",contact_lens") && Util.needGone(",shading")){
                return true;
            }else{
                return false;
            }
        }

    }

}
