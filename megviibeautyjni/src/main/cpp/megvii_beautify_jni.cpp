#include <android/log.h>
#include <jni.h>
#include "include/MG_Sticker.h"

#include "include/MG_Beautify.h"
#include "beautify_handler.cpp"
#include <vector>
#include <algorithm>
#include <string>
#include <chrono>
#include <cmath>
#include <time.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <include/MG_Common.h>
#include <android/bitmap.h>
#include <include/gl_context_handler.h>
#include <include/beautify_image/MG_BeautifyImage.h>
#include <include/MG_Beautify.h>

int GLOBAL_LOG_LEVEL_JNI = 0;

extern "C"
{
//beautify_handler *handler;

beautify_handler* getHandler(JNIEnv *env, jobject sdkObject){
    jclass handlerClass = env->GetObjectClass(sdkObject);
    jfieldID handlerField = env->GetFieldID(handlerClass, "mObject", "J");
    return reinterpret_cast<beautify_handler *>(env->GetLongField(sdkObject, handlerField));
}

long getAbilityType(JNIEnv *env, jobject sdkObject){
    jclass handlerClass = env->GetObjectClass(sdkObject);
    jfieldID abilityTypeField = env->GetFieldID(handlerClass, "mAbilityType", "J");
    return env->GetLongField(sdkObject, abilityTypeField);
}


void setHandler(JNIEnv *env, jobject sdkObject, beautify_handler* handler){
    jclass handlerClass = env->GetObjectClass(sdkObject);
    jfieldID handlerField = env->GetFieldID(handlerClass, "mObject", "J");
    env->SetLongField(sdkObject, handlerField, (jlong)handler);
}

long get_current_ms() {
    struct timespec res;
    clock_gettime(CLOCK_REALTIME, &res);
    return 1000 * res.tv_sec + res.tv_nsec / 1e6;
}


JNIEXPORT jstring JNICALL
  Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeGetBeautyVersion(JNIEnv *env, jobject object){
    return env->NewStringUTF(mg_beautify.GetApiVersion());
}

JNIEXPORT int JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeSetLogLevel(JNIEnv *env, jobject object, jint logLevel){
    GLOBAL_LOG_LEVEL_JNI = static_cast<MG_BEAUTIFY_LOG_LEVEL>(logLevel);
    return mg_beautify.SetLogLevel(static_cast<MG_BEAUTIFY_LOG_LEVEL>(logLevel));
}


JNIEXPORT int JNICALL
  Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeCreateBeautyHandle(JNIEnv *env, jobject object,
                                                                         jobject context,
                                                                         jint cameraWidth,
                                                                         jint cameraHeight,
                                                                         jint orientation,
                                                                         jint detectMode,
                                                                         jbyteArray beautyModel_,
                                                                         jbyteArray faceppModel_,jbyteArray faceppDenseLMModel_) {


   // jobject obj, jfieldID fieldID, jint value

    int retCode = 0;
    beautify_handler* handler = getHandler(env, object);

    jbyte *beautyModel = env->GetByteArrayElements(beautyModel_, NULL);
    const unsigned char *beautyModelBuf = (unsigned char *) beautyModel;
    long beautyModelLen = env->GetArrayLength(beautyModel_);

    jbyte *faceppModel = env->GetByteArrayElements(faceppModel_, NULL);
    const unsigned char *faceppModelBuf = (unsigned char*) faceppModel;
    long faceppModelLen = env->GetArrayLength(faceppModel_);

    jbyte *faceppDenseLMModel = nullptr;
    unsigned char *faceppDenseLMModelBuf = nullptr;
    long faceppDenseLMModelLen = 0;
    if(faceppDenseLMModel_!= NULL){
        faceppDenseLMModel = env->GetByteArrayElements(faceppDenseLMModel_, NULL);
        faceppDenseLMModelBuf = (unsigned char*)faceppDenseLMModel;
        faceppDenseLMModelLen = env->GetArrayLength(faceppDenseLMModel_);
    }


    if (handler != nullptr) {
        retCode = handler->reset(cameraWidth, cameraHeight, orientation);
        handler->releaseFaceData();
        retCode |= handler->setFaceConfig(orientation,detectMode,faceppModelBuf,
                                          faceppModelLen,faceppDenseLMModelBuf,faceppDenseLMModelLen);
    }else{


        handler = new beautify_handler();
        setHandler(env, object, handler);

        //prepare the opengl context.
        long abilityType = getAbilityType(env, object);
        handler->abilityType = abilityType;
        LOGIH(__FUNCTION__,"the ability type is %d ", (int)abilityType);

        if (abilityType == ABILITY_TYPE_IMAGE
            || abilityType == ABILITY_TYPE_VIDEO) {
            handler->prepareGLContext();
        }

        retCode |= handler->init(env, context, beautyModelBuf, beautyModelLen, cameraWidth, cameraHeight,
                                   (MG_ROTATION) (orientation));


        //facepp
        if (faceppModel_ != NULL) {
            retCode |= handler->createFaceppHandler(env, context);
            retCode |= handler->setFaceConfig(orientation,detectMode, faceppModelBuf,
                                              faceppModelLen,faceppDenseLMModelBuf,faceppDenseLMModelLen);
        }


    }
    env->ReleaseByteArrayElements(beautyModel_, beautyModel, 0);
    env->ReleaseByteArrayElements(faceppModel_, faceppModel, 0);
    if(faceppDenseLMModel_!= NULL){
        env->ReleaseByteArrayElements(faceppDenseLMModel_,faceppDenseLMModel,0);
    }

    return retCode;
}

JNIEXPORT int JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeReset(JNIEnv *env, jobject object,
                                                                     jint cameraWidth,
                                                                     jint cameraHeight,
                                                                     jint orientation){
    int retCode = 0;
    beautify_handler* handler = getHandler(env, object);
    if(handler != nullptr) {
        retCode = handler->reset(cameraWidth,cameraHeight,orientation);
    }
    else{
        retCode  = MG_RETCODE_INVALID_HANDLE;
    }
    return retCode;
}

JNIEXPORT int JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeShareGLContext(JNIEnv *env, jobject object){
    int retCode = 0;
    beautify_handler* handler = getHandler(env, object);
    if(handler != nullptr){
        if (handler->abilityType == ABILITY_TYPE_IMAGE
            || handler->abilityType == ABILITY_TYPE_VIDEO) {
            LOGDH(__FUNCTION__,"line: %d ", __LINE__);
            handler->pGlcontext_manager->share_context();
        }
        return 0;
    }else{
        return 1;
    }

}

JNIEXPORT int JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeDoneGLContext(JNIEnv *env, jobject object){
    int retCode = 0;
    beautify_handler* handler = getHandler(env, object);
    if(handler != nullptr){
        if (handler->abilityType == ABILITY_TYPE_IMAGE
            || handler->abilityType == ABILITY_TYPE_VIDEO) {
            handler->pGlcontext_manager->done_context();
        }
        return 0;
    }else{
        return 1;
    }
}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeDetectFace(JNIEnv *env, jclass object,
                                                                 jbyteArray data_, jint cameraWidth,
                                                                 jint cameraHeight,jint detectFormat) {
//此接口废弃不用
//    beautify_handler* handler = getHandler(env, object);
//
//    if (handler == nullptr){
//        return MG_RETCODE_INVALID_HANDLE;
//    }
//    jbyte *data = env->GetByteArrayElements(data_, NULL);
//    int result=0;
//
//    if (handler != nullptr && handler->faceppApiHandle != nullptr) {
////        result = handler->detectFaceppSort(cameraWidth, cameraHeight, data,detectFormat);
//        result = handler->detectFacepp(1.0, 1.0, cameraWidth, cameraHeight, data,detectFormat,-1);
//    }
//
//    env->ReleaseByteArrayElements(data_, data, 0);
//
    return 0;
}



extern "C"
JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeProcessImageNV21(JNIEnv *env, jobject object,
                                                                   jbyteArray inData_,
                                                                   jbyteArray outData_, jint w,
                                                                   jint h, jboolean isQcom) {
    Timer timer;
    timer.start(PROFILE, "BeaurifyJniSdk_nativeProcessImageNV21");

    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    jbyte *inData = env->GetByteArrayElements(inData_, NULL);
    jbyte *outData = env->GetByteArrayElements(outData_, NULL);

    //TODO
    unsigned char *inImage = reinterpret_cast<unsigned char *>(inData);
    unsigned char *outImage = reinterpret_cast<unsigned char *>(outData);

    LOGDH(__FUNCTION__,"inRGBA is %p ", inImage);
    MG_Platform platform;
    if(isQcom){
        platform = QCOM;
    } else{
        platform = MTK;
    }
    MG_BeautifyImage image{reinterpret_cast<char *>(inImage), w, h, platform, IMAGE_TYPE_NV21};

    MG_BeautifyImage out_result{reinterpret_cast<char *>(outImage), w, h,  platform, IMAGE_TYPE_NV21};


    int status = handler->processImage(image, out_result);

    LOGIH(__FUNCTION__,"handler->processImage status %d", status);

    env->ReleaseByteArrayElements(inData_, inData, 0);
    env->ReleaseByteArrayElements(outData_, outData, 0);
    timer.end();
    return status;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeProcessImageNV12(JNIEnv *env, jobject object,
                                                                   jbyteArray inData_,
                                                                   jbyteArray outData_, jint w,
                                                                   jint h, jboolean isQcom) {
    Timer timer;
    timer.start(PROFILE, "BeaurifyJniSdk_nativeProcessImageNV21");

    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    jbyte *inData = env->GetByteArrayElements(inData_, NULL);
    jbyte *outData = env->GetByteArrayElements(outData_, NULL);

    //TODO
    unsigned char *inImage = reinterpret_cast<unsigned char *>(inData);
    unsigned char *outImage = reinterpret_cast<unsigned char *>(outData);

    LOGDH(__FUNCTION__,"inRGBA is %p ", inImage);
    MG_Platform platform;
    if(isQcom){
        platform = QCOM;
    } else{
        platform = MTK;
    }
    MG_BeautifyImage image{reinterpret_cast<char *>(inImage), w, h, platform, IMAGE_TYPE_NV21};

    MG_BeautifyImage out_result{reinterpret_cast<char *>(outImage), w, h,  platform, IMAGE_TYPE_NV21};


    int status = handler->processImage(image, out_result);

    LOGIH(__FUNCTION__,"handler->processImage status %d", status);

    env->ReleaseByteArrayElements(inData_, inData, 0);
    env->ReleaseByteArrayElements(outData_, outData, 0);
    timer.end();
    return status;
}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeDetectFaceOrientation(JNIEnv *env, jclass object,
                                                                        jbyteArray data_, jdouble xScale, jdouble yScale,  jint cameraWidth,
                                                                        jint cameraHeight,jint detectFormat,jint rotation) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr ){
        return MG_RETCODE_INVALID_HANDLE;
    }
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    if (data == nullptr){
        env->ReleaseByteArrayElements(data_, data, 0);
        return MG_RETCODE_INVALID_ARGUMENT;
    }
    int result=0;
    Timer timerDetectFace;

    if (handler != nullptr && handler->faceppDenseLMApiHandle != nullptr) {

        result = handler->detectFaceppSort(xScale, yScale,cameraWidth, cameraHeight, data,detectFormat,rotation);
    }
    timerDetectFace.start("nativeDetectFaceOrientation", "Face detect");
#ifndef ONLY_USE_DENSE_LM
    if (handler != nullptr && handler->faceppApiHandle != nullptr) {

        result = handler->detectFaceppSort(xScale, yScale,cameraWidth, cameraHeight, data,detectFormat);
        //result = handler->detectFacepp(xScale, yScale, cameraWidth, cameraHeight, data,detectFormat,rotation);
    }
#endif
    timerDetectFace.end();
    env->ReleaseByteArrayElements(data_, data, 0);

    return result;
}



JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeProcessTexture(JNIEnv *env, jclass object,
                                                                     jint oldTextureIndex,
                                                                     jint newTextureIndex,jint isPreview) {

    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    return handler->processTexture(oldTextureIndex, newTextureIndex,static_cast<bool>(isPreview));

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeProcessImageInTextureOut(JNIEnv *env, jclass object,
                                                                           jbyteArray inData, jint w,
                                                                           jint h,int outTextureIndex) {

    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }

    jbyte *inImageDatajbyte = env->GetByteArrayElements(inData, NULL);
    unsigned char *inImageDataUC = reinterpret_cast<unsigned char *>(inImageDatajbyte);
    MG_BeautifyImage inImage{reinterpret_cast<char *>(inImageDataUC), w, h, ANY, IMAGE_TYPE_NV21};
    int status =  handler->processImageInTextureOut(inImage, outTextureIndex);
    env->ReleaseByteArrayElements(inData, inImageDatajbyte, 0);
    return status;

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeProcessImageInImageOutNV21(JNIEnv *env, jclass object,
                                                                           jbyteArray inData, jbyteArray outData, jint w,
                                                                           jint h) {

    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }

    jbyte *inImageDatajbyte = env->GetByteArrayElements(inData, NULL);
    unsigned char *inImageDataUC = reinterpret_cast<unsigned char *>(inImageDatajbyte);
    MG_BeautifyImage inImage{reinterpret_cast<char *>(inImageDataUC), w, h, ANY, IMAGE_TYPE_NV21};

    jbyte *outImageDatajbyte = env->GetByteArrayElements(outData, NULL);
    char *outImageData = reinterpret_cast<char *>(outImageDatajbyte);
    MG_BeautifyImage outImage{outImageData,w,h,ANY,IMAGE_TYPE_NV21};

    int status =  handler->processImageInImageOut(inImage, outImage);
    env->ReleaseByteArrayElements(inData, inImageDatajbyte, 0);
    env->ReleaseByteArrayElements(outData,outImageDatajbyte,0);
    return status;

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeProcessImageInImageOutNV12(JNIEnv *env, jclass object,
                                                                             jbyteArray inData, jbyteArray outData, jint w,
                                                                             jint h) {

    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }

    jbyte *inImageDatajbyte = env->GetByteArrayElements(inData, NULL);
    unsigned char *inImageDataUC = reinterpret_cast<unsigned char *>(inImageDatajbyte);
    MG_BeautifyImage inImage{reinterpret_cast<char *>(inImageDataUC), w, h, ANY, IMAGE_TYPE_NV12};

    jbyte *outImageDatajbyte = env->GetByteArrayElements(outData, NULL);
    char *outImageData = reinterpret_cast<char *>(outImageDatajbyte);
    MG_BeautifyImage outImage{outImageData,w,h,ANY,IMAGE_TYPE_NV12};

    int status =  handler->processImageInImageOut(inImage, outImage);
    env->ReleaseByteArrayElements(inData, inImageDatajbyte, 0);
    env->ReleaseByteArrayElements(outData,outImageDatajbyte,0);
    return status;

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeSetBeautyParam(JNIEnv *env, jclass object,
                                                                     jint beautyType,
                                                                     jfloat beautyValue) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    return handler->setParamProperty(beautyType, beautyValue * 2);

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeSetBeautyParam2(JNIEnv *env, jclass object,
                                                                 jint beautyType,
                                                                 jfloat beautyValue,int r,int g,int b,jobjectArray templateObjects, jobjectArray templateObjects2, jfloatArray keypoint, int keypointSize) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }

    auto formatTrans = [=](int32_t templateFormat)->MG_IMAGEMODE{
        MG_IMAGEMODE outFormat;
        switch(templateFormat){
            case ANDROID_BITMAP_FORMAT_RGBA_8888:
                outFormat = MG_IMAGEMODE_RGBA;
                break;
            case ANDROID_BITMAP_FORMAT_A_8:
                outFormat = MG_IMAGEMODE_GRAY;
                break;
            default:
                LOGEH("formatTrans","unsupported format %d",templateFormat);
                outFormat = MG_IMAGEMODE_COUNT;
        }
        return outFormat;
    };

    MG_BEAUTIFY_MULTI_PARAM beautifyMultiParam;
    beautifyMultiParam.coefficient = beautyValue*2;
    beautifyMultiParam.color = {(MG_UINT8)r,(MG_UINT8)g,(MG_UINT8)b};
    beautifyMultiParam.templateObjectsSize = 0;
    if(templateObjects != nullptr){
        beautifyMultiParam.templateObjectsSize ++;
        beautifyMultiParam.templateObjects[0].templateObjectSize = env->GetArrayLength(templateObjects);
        LOGDH("nativeSetBeautyParam2","beautifyMultiParam.templateObjectSize:%d",beautifyMultiParam.templateObjects[0].templateObjectSize);
        for(auto i=0;i< beautifyMultiParam.templateObjects[0].templateObjectSize;i++){
            jobject templateObject = env->GetObjectArrayElement(templateObjects,i);
            AndroidBitmapInfo templateObjectInfo;
            AndroidBitmap_getInfo(env, templateObject, &templateObjectInfo);
            uint32_t templateObjectWidth1 = templateObjectInfo.width, templateObjectHeight1 = templateObjectInfo.height;

            void *templatePixels = nullptr;
            AndroidBitmap_lockPixels(env, templateObject, &templatePixels);

            if(templatePixels != nullptr){
                unsigned char *template1RGBA = reinterpret_cast<unsigned char *>(templatePixels);
                MG_BEAUTIFY_IMAGE templateImage{reinterpret_cast<MG_BYTE *>(template1RGBA), templateObjectWidth1, templateObjectHeight1, formatTrans(templateObjectInfo.format)};
                beautifyMultiParam.templateObjects[0].templateObject[i] = templateImage;
            }
            AndroidBitmap_unlockPixels(env,templateObject);
        }
    }else{
        beautifyMultiParam.templateObjects[0].templateObjectSize = 0;
    }

    if(templateObjects2 != nullptr){
        beautifyMultiParam.templateObjectsSize++;
        beautifyMultiParam.templateObjects[1].templateObjectSize = env->GetArrayLength(templateObjects2);
        LOGDH("nativeSetBeautyParam2","beautifyMultiParam.templateObjectSize:%d",beautifyMultiParam.templateObjects[1].templateObjectSize);
        for(auto i=0;i< beautifyMultiParam.templateObjects[1].templateObjectSize;i++){
            LOGDH("nativeSetBeautyParam2","in for");
            jobject templateObject = env->GetObjectArrayElement(templateObjects2,i);
            AndroidBitmapInfo templateObjectInfo;
            AndroidBitmap_getInfo(env, templateObject, &templateObjectInfo);
            uint32_t templateObjectWidth1 = templateObjectInfo.width, templateObjectHeight1 = templateObjectInfo.height;

            void *templatePixels = nullptr;
            AndroidBitmap_lockPixels(env, templateObject, &templatePixels);

            if(templatePixels != nullptr){
                unsigned char *template1RGBA = reinterpret_cast<unsigned char *>(templatePixels);
                MG_BEAUTIFY_IMAGE templateImage{reinterpret_cast<MG_BYTE *>(template1RGBA), templateObjectWidth1, templateObjectHeight1, formatTrans(templateObjectInfo.format)};
                beautifyMultiParam.templateObjects[1].templateObject[i] = templateImage;
            }
            AndroidBitmap_unlockPixels(env,templateObject);
        }
    }else{
        beautifyMultiParam.templateObjects[1].templateObjectSize = 0;
    }

    if(keypoint!= nullptr){
        jfloat *keypointData = env->GetFloatArrayElements(keypoint, NULL);

        if(keypointData!= nullptr && keypointSize!=0){
            for(auto i=0;i<keypointSize;i++){
                beautifyMultiParam.keypoint[i].x = keypointData[i*2];
                beautifyMultiParam.keypoint[i].y = keypointData[i*2+1];
            }

            beautifyMultiParam.keypointSize = keypointSize;
        }
    }

    LOGIH(__FUNCTION__,"beautifyMultiParam.coefficient:%f", beautifyMultiParam.coefficient);
    int retCode = handler->setParamProperty2(beautyType, beautifyMultiParam);

    return retCode;

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeSetBeautyRemoveSpeckles(JNIEnv *env, jclass object,
                                                                          jfloat beautyValue,jbyteArray muvarModel_,jstring RFCModelPath_){
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }

    const char *RFCModelPath = env->GetStringUTFChars(RFCModelPath_, 0);


    jbyte *muvarModel = env->GetByteArrayElements(muvarModel_, NULL);
    uint8_t *muvarModelBuf = (unsigned char *) muvarModel;
    size_t muvarModelLen = env->GetArrayLength(muvarModel_);

    MG_BEAUTIFY_MULTI_PARAM beautifyMultiParam;
    beautifyMultiParam.coefficient = beautyValue*2;
    beautifyMultiParam.removeSpecklesModels= {muvarModelBuf,muvarModelLen};
    beautifyMultiParam.classifierModelPath = RFCModelPath;
    LOGIH(__FUNCTION__,"beautifyMultiParam.coefficient:%f", beautifyMultiParam.coefficient);
    int retCode = handler->setParamProperty2(12, beautifyMultiParam);  //12:祛斑
    env->ReleaseStringUTFChars(RFCModelPath_, RFCModelPath);
    env->ReleaseByteArrayElements(muvarModel_, muvarModel, 0);
    return retCode;
}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeSetFilter(JNIEnv *env, jclass object,
                                                                jstring path_) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    const char *path = env->GetStringUTFChars(path_, 0);
    int ret = handler->setFilter(path);
    env->ReleaseStringUTFChars(path_, path);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeRemoveFilter(JNIEnv *env, jclass object) {

    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    return handler->removeFilter();

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeUseFastFilter(JNIEnv *env, jclass object,
                                                                    jboolean b) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    int retCode = handler->useFastFilter(b);

    return retCode;

}


JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeChangePackage(JNIEnv *env, jclass object,
                                                                    jstring path_) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    const char *package = env->GetStringUTFChars(path_, 0);


    int retCode = handler->updateSticker(package);

    env->ReleaseStringUTFChars(path_, package);
    return retCode;

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeDisablePackage(JNIEnv *env, jclass object) {

    beautify_handler* handler = getHandler(env, object);

    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    int retCode = handler->disableSticker();

    return retCode;

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeReleaseResources(JNIEnv *env, jclass object) {

    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    setHandler(env, object, nullptr);
    delete handler;
    handler = nullptr;
    return 0;

}



JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeSetDetectMode(JNIEnv *env, jclass type,
                                                                jint detectMode) {

    return 0;

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeSetStickerParam(JNIEnv *env, jclass object,
                                                                  jfloat beautyValue) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    int retCode=handler->setStickerProperty(beautyValue);
    return retCode;

}

JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativePreparePackage(JNIEnv *env, jclass object,
                                                                 jstring path_) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    const char *path = env->GetStringUTFChars(path_, 0);

   int retCode=handler->preparePackage(path);

    env->ReleaseStringUTFChars(path_, path);
    return retCode;
}


}
extern "C"
JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeGetPoints(JNIEnv *env, jclass object,
                                                            jintArray points_) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    jint *points = env->GetIntArrayElements(points_, NULL);
    // TODO
    if (handler == nullptr) {
        env->ReleaseIntArrayElements(points_, points, 0);
        return 0;
    }

    int count = handler->faceCount;
    MG_FACE* _mgFaces = handler->mgProcessFaces;

    LOGIH(__FUNCTION__,"count is %d ", count);
    jint* pts = points;
    if (count > 0) {
        for(auto faceNum = 0;faceNum<count;faceNum++){
            MG_FACELANDMARKS face = _mgFaces[faceNum].points;
            for (int i = 0; i < MG_LANDMARK_NR; ++i) {
                pts[faceNum*MG_LANDMARK_NR*2 + i * 2] = face.point[i].x;
                pts[faceNum*MG_LANDMARK_NR*2 + i * 2 + 1] = face.point[i].y;
        }

        }
    } else{
        for (int i = 0; i < MG_LANDMARK_NR; ++i) {
            pts[i * 2] = 0;
            pts[i * 2 + 1] = 0;
        }
    }
    env->SetIntArrayRegion(points_, 0, MG_LANDMARK_NR * 2, pts);
    env->ReleaseIntArrayElements(points_, points, 0);
    return count;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeGetDenseLMPoints(JNIEnv *env, jclass object,
                                                            jintArray points_) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    jint *points = env->GetIntArrayElements(points_, NULL);
    // TODO
    if (handler == nullptr) {
        env->ReleaseIntArrayElements(points_, points, 0);
        return 0;
    }
    FaceDetectOutput denseLMForShow = handler->getDenseLMFaceswithSmooth();
    int count =  handler->faceCount;
    LOGIH(__FUNCTION__,"nativeGetDenseLMPoints count is %d ", count);
    int DLMPointCount = 0;
    jint* pts = points;
    for (int i=0;i<count;i++) {
        DLmkFaceDetail &faceDetailTemp = denseLMForShow.denseLMOutput.result.detail[i];
        for (int j = 0; j < faceDetailTemp.size; j++) {
            DLmkFaceLmks &DLmkTemp = faceDetailTemp.lmk[j];
            if(DLmkTemp.type == D_LMK_FACE_DETAIL_LEFT_EYE || DLmkTemp.type == D_LMK_FACE_DETAIL_RIGHT_EYE){
                pts[DLMPointCount * 2] = DLmkTemp.desc.eye_ext.pupil_center.x;
                pts[DLMPointCount * 2 + 1] = DLmkTemp.desc.eye_ext.pupil_center.y;
                DLMPointCount++;
            }
            for (int k = 0; k < DLmkTemp.size;k++) {
                pts[DLMPointCount * 2] = DLmkTemp.data[k].x;
                pts[DLMPointCount * 2 + 1] = DLmkTemp.data[k].y;
                DLMPointCount++;
            }
        }
    }

    env->SetIntArrayRegion(points_, 0, MG_LANDMARK_NR * 2, pts);
    env->ReleaseIntArrayElements(points_, points, 0);
    return count;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_nativeProcessImage(JNIEnv *env, jclass object,
                                                               jobject inBitmap,
                                                               jobject outBitmap) {
    beautify_handler* handler = getHandler(env, object);
    if (handler == nullptr){
        return MG_RETCODE_INVALID_HANDLE;
    }
    LOGDH(__FUNCTION__,"the handler is %p ", handler);
    // TODO
    AndroidBitmapInfo info;
    AndroidBitmap_getInfo(env, inBitmap, &info);
    int w = info.width, h = info.height;

    void *inPixels, *outPixels;
    AndroidBitmap_lockPixels(env, inBitmap, &inPixels);
    AndroidBitmap_lockPixels(env, outBitmap, &outPixels);

    unsigned char *inRGBA = reinterpret_cast<unsigned char *>(inPixels);
    unsigned char *outRGBA = reinterpret_cast<unsigned char *>(outPixels);
    LOGIH(__FUNCTION__,"the inRGBA is %p ", inRGBA);
    MG_BeautifyImage image{reinterpret_cast<char *>(inRGBA), w, h, ANY, IMAGE_TYPE_RGBA};

    MG_BeautifyImage out_result{reinterpret_cast<char *>(outRGBA), w, h, ANY, IMAGE_TYPE_RGBA};


    int status = handler->processImage(image, out_result);

    LOGDH(__FUNCTION__,"handler->processImage status:%d the out_result is %d %d ", status,out_result.width,out_result.height);
    if (status == MG_RETCODE_OK) {
        memcpy(outRGBA, out_result.data, out_result.width*out_result.height*4);
    } else {
        memcpy(outRGBA, inRGBA, out_result.width*out_result.height*4);
    }
    AndroidBitmap_unlockPixels(env, inBitmap);
    AndroidBitmap_unlockPixels(env, outBitmap);

    return MG_RETCODE_OK;

}


extern "C"
JNIEXPORT jint JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_testFacePP(JNIEnv *env, jobject instance,
                                                       jbyteArray faceppModel_, jbyteArray data_,
                                                       jint width, jint height) {
//此接口废弃
//    jbyte *faceppModel = env->GetByteArrayElements(faceppModel_, NULL);
//    long model_len = env->GetArrayLength(faceppModel_);
//    jbyte *data = env->GetByteArrayElements(data_, NULL);
//
///**
//#if MGAPI_BUILD_ON_ANDROID
//    JNIEnv* env,
//    jobject jobj,
//#endif
//    const MG_BYTE *model_data,
//    MG_INT32 model_length,
//    MG_FPP_APIHANDLE _OUT *api_handle_ptr**/
//
//    // TODO
//    MG_FPP_APIHANDLE apihandle;
//    MG_ALGORITHMINFO algorithm_info;
//    mg_facepp.GetAlgorithmInfo(reinterpret_cast<const MG_BYTE *>(faceppModel), model_len,
//                               &algorithm_info);
//    int ability = algorithm_info.ability;
//
//    LOGEH("wangshuai test ability = %04x ", ability);
//    int detectbit = ability&MG_FPP_DETECT;
//    int trackbit = ability&MG_FPP_TRACK;
//    LOGEH("wangshuai test detectbit = %04x ", detectbit);
//    LOGEH("wangshuai test trackbit = %04x ", trackbit);
//    mg_facepp.CreateApiHandle(reinterpret_cast<const MG_BYTE *>(faceppModel)
//            , model_len, &apihandle);
//    MG_FPP_APICONFIG config;
//    mg_facepp.GetDetectConfig(apihandle, &config);
//    config.detection_mode = MG_FPP_DETECTIONMODE_NORMAL;
//    mg_facepp.SetDetectConfig(apihandle, &config);
//    MG_FPP_IMAGEHANDLE imghandle;
//    mg_facepp.CreateImageHandle(width, height, &imghandle);
//    mg_facepp.SetImageData(imghandle, reinterpret_cast<const MG_BYTE *>(data), MG_IMAGEMODE_RGBA);
//    MG_INT32 faceNum = 0;
//   int ret =  mg_facepp.Detect(apihandle, imghandle, &faceNum);
//    LOGEH("wangshuai test detect ret %d, faceNum %d", ret, faceNum);
//    env->ReleaseByteArrayElements(faceppModel_, faceppModel, 0);
//    env->ReleaseByteArrayElements(data_, data, 0);
//    mg_facepp.ReleaseImageHandle(imghandle);
//    mg_facepp.ReleaseApiHandle(apihandle);
    return 0;
}extern "C"
JNIEXPORT void JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_alignNV21Data(JNIEnv *env, jclass type,
                                                          jbyteArray data_, jint inW, jint inH,
                                                          jbyteArray aligned_, jint outW,
                                                          jint outH) {
    Timer timer;
    timer.start(PROFILE, "BeaurifyJniSdk_alignNV21Data");

    jbyte *data = env->GetByteArrayElements(data_, NULL);
    jbyte *aligned = env->GetByteArrayElements(aligned_, NULL);

    // TODO
    for (int y = 0; y < inH; ++y) {
        memcpy(aligned + y * outW, data + y * inW, inW);
    }
    int inOffset = inW * inH;
    int outOffset = outW * outH;
    int uvHeight = inH / 2;
    for (int y = 0; y < uvHeight; ++y) {
        char *pOutRow = reinterpret_cast<char *>(aligned + outOffset + y * outW);
        char *pInRow = reinterpret_cast<char *>(data + inOffset + y * inW);
        memcpy(pOutRow, pInRow, inW);
    }

    env->ReleaseByteArrayElements(data_, data, 0);
    env->ReleaseByteArrayElements(aligned_, aligned, 0);
    timer.end();
    return;
}extern "C"
JNIEXPORT void JNICALL
Java_com_megvii_beautify_jni_BeaurifyJniSdk_deAlignNV21Data(JNIEnv *env, jclass type,
                                                            jbyteArray data_, jint width,
                                                            jint height, jbyteArray aligned_,
                                                            jint alignedW, jint alignedH) {
    Timer timer;
    timer.start(PROFILE, "BeaurifyJniSdk_deAlignNV21Data");

    jbyte *data = env->GetByteArrayElements(data_, NULL);
    jbyte *aligned = env->GetByteArrayElements(aligned_, NULL);

    int inW = alignedW;
    int inH = alignedH;
    int outW = width;
    int outH = height;
    for (int y = 0; y < outH; ++ y)
    {
        //copy to out image y
        memcpy(data+ y*outW, aligned + y*inW, outW);
    }

    int inOffset = inW*inH;
    int outOffset = outW*outH;
    int uvHeight = outH /2;
    for (int y = 0; y < uvHeight; ++ y)
    {
        char* pOutRow = reinterpret_cast<char *>(data + outOffset + y * outW);
        char* pInRow = reinterpret_cast<char *>(aligned + inOffset + y * inW);
        memcpy(pOutRow, pInRow, outW);
    }

    env->ReleaseByteArrayElements(data_, data, 0);
    env->ReleaseByteArrayElements(aligned_, aligned, 0);
    timer.end();
}