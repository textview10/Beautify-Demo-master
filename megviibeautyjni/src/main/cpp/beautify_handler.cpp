//
// Created by Li Yanshun on 2017/7/6.
//
#ifndef HUMANEFFECTS_beautify_handler_H
#define HUMANEFFECTS_beautify_handler_H

#include <include/MG_Beautify.h>
#include "include/MG_Sticker.h"
#include "include/mg_dense_landmark.h"
#include "include/MG_Common.h"
#include "include/mg_timer.h"
#include <string>
#include <mutex>
#include <memory>
#include <sys/system_properties.h>
#include <stdlib.h>
#include <GLES2/gl2.h>
#include <time.h>
#include <android/log.h>
#include <beautify_image/MG_BeautifyImage.h>
#include <include/gl_context_handler.h>
#include <include/WorkerThread.h>
#include <list>
#include <math.h>
#include <beautify_image/include/bare_format_transform.h>
#include <include/MG_Common.h>
#include <include/beautify_image/MG_BeautifyImage.h>

#define ONLY_USE_DENSE_LM

#define CAP(x) ((x)>=1?1:((x)<=-1)?-1:x)

extern int GLOBAL_LOG_LEVEL_JNI;

#define LOGEH(TAG,...)                         \
{                                           \
    if(GLOBAL_LOG_LEVEL_JNI == 1 || GLOBAL_LOG_LEVEL_JNI == 2 || GLOBAL_LOG_LEVEL_JNI == 3 || GLOBAL_LOG_LEVEL_JNI == 4){ \
        __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__); \
    }                                       \
}

#define LOGDH(TAG,...)                                                          \
{                                                                            \
    if(GLOBAL_LOG_LEVEL_JNI == 1 || GLOBAL_LOG_LEVEL_JNI == 2 || GLOBAL_LOG_LEVEL_JNI == 4) {  \
        __android_log_print(ANDROID_LOG_WARN, TAG,__VA_ARGS__);          \
    }                                                                        \
}


#define LOGWH(TAG,...)                                                          \
{                                                                            \
    if(GLOBAL_LOG_LEVEL_JNI == 4){   \
        __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__);           \
    }                                                                        \
}

#define LOGIH(TAG,...)                                          \
{                                                            \
    if(GLOBAL_LOG_LEVEL_JNI == 1 || GLOBAL_LOG_LEVEL_JNI == 4){    \
        __android_log_print(ANDROID_LOG_INFO, TAG,__VA_ARGS__); \
    }                                                        \
}

#define PROFILE "PROFILE_PERFORMANCE"

const static int ABILITY_TYPE_PREVIEW = 0;
const static int ABILITY_TYPE_IMAGE = 1;
const static int ABILITY_TYPE_VIDEO = 2;
const static int ABILITY_TYPE_DEFAULT = ABILITY_TYPE_PREVIEW;

const int SMOOTH_QUEUE_SIZE = 2;

static void nv21resize(const unsigned char *src, int src_width, int src_height,
                                     unsigned char *dst, int dst_width, int dst_height) {
    /*
    Mat original(src_height + src_height / 2, src_width, CV_8UC1, (unsigned char *)src);
    Mat source(dst_height + dst_height / 2, dst_width, CV_8UC1, dst);
    resize(original, source, Size(dst_width, dst_height), (static_cast<void>(0), 0), (static_cast<void>(0), 0), cv::INTER_LINEAR);
*/
    {
        int sw = src_width;  //keyword is for local var to accelorate
        int sh = src_height;
        int dw = dst_width;
        int dh = dst_height;
        int y, x;
        unsigned long int srcy, srcx, src_index;// dst_index;
        unsigned long int xrIntFloat_16 = (sw << 16) / dw + 1; //better than float division
        unsigned long int yrIntFloat_16 = (sh << 16) / dh + 1;

        uint8_t* dst_uv = dst + dh * dw; //memory start pointer of dest uv
        uint8_t* src_uv = (unsigned char *)src + sh * sw; //memory start pointer of source uv
        uint8_t* dst_uv_yScanline;
        uint8_t* src_uv_yScanline;
        uint8_t* dst_y_slice = dst; //memory start pointer of dest y
        uint8_t* src_y_slice;
        uint8_t* sp;
        uint8_t* dp;

        for (y = 0; y < (dh & ~7); ++y)  //'dh & ~7' is to generate faster assembly code
        {
            srcy = (y * yrIntFloat_16) >> 16;
            src_y_slice = (unsigned char *)src + srcy * sw;

            if((y & 1) == 0)
            {
                dst_uv_yScanline = dst_uv + (y / 2) * dw;
                src_uv_yScanline = src_uv + (srcy / 2) * sw;
            }

            for(x = 0; x < (dw & ~7); ++x)
            {
                srcx = (x * xrIntFloat_16) >> 16;
                dst_y_slice[x] = src_y_slice[srcx];

                if((y & 1) == 0) //y is even
                {
                    if((x & 1) == 0) //x is even
                    {
                        src_index = (srcx / 2) * 2;

                        sp = dst_uv_yScanline + x;
                        dp = src_uv_yScanline + src_index;
                        *sp = *dp;
                        ++sp;
                        ++dp;
                        *sp = *dp;
                    }
                }
            }
            dst_y_slice += dw;
        }
    }
}


class FunctionWrapper{
public:
    MG_FACE* pFaces;
    int count;
    MG_BEAUTIFY_HANDLE beautify_handle;
    MG_ROTATION orientation;
    long abilityType;

    FunctionWrapper(MG_BEAUTIFY_HANDLE handle
            , MG_FACE* pFaces
            , int count
            , MG_ROTATION orientation, int _abilityType){
        abilityType  = _abilityType;
        this->pFaces = pFaces;
        this->count = count;
        this->beautify_handle = handle;
        this->orientation = orientation;
    }


    MGB_IMAGE_RETCODE ProcessTexture(int inTexture, int outTexture, int width, int height,bool isPreveiew){

        if (abilityType != ABILITY_TYPE_VIDEO) {
            mg_beautify.ResetHandle(beautify_handle, width, height, orientation);
        }

        MG_RETCODE retcode = mg_beautify.ProcessTexture(beautify_handle, inTexture, outTexture,
                pFaces,nullptr, count,isPreveiew);
        if(retcode == MG_RETCODE_OK) {
            return MGB_IMAGE_OK;
        } else {
            return MGB_IMAGE_FAILED;
        }
    }

    static MGB_IMAGE_RETCODE ProcessTextureProc(void* cookie, int inTexture, int outTexture, int width, int height,bool isPreveiew){
        if(cookie!= nullptr){
            FunctionWrapper* functionWrapper = (FunctionWrapper*)cookie;
            return functionWrapper->ProcessTexture(inTexture, outTexture, width, height,isPreveiew);
        }
        return MGB_IMAGE_FAILED;
    }
};


struct FaceDetectOutput{
    MGDenseFaces sparseOutput;
    MGDenseOutput denseLMOutput;
    FaceDetectOutput(){
        memset(&sparseOutput,0,sizeof(MGDenseFaces));
        memset(&denseLMOutput,0,sizeof(MGDenseOutput));
    }
};

class beautify_handler {
public:
    beautify_handler() {
    }

    ~beautify_handler() {

        const char TAG[] = "~beautify_handler";
        LOGIH(TAG,"Exit thread before...");
        if (abilityType == ABILITY_TYPE_VIDEO) {
            workerThread.ExitThread();
        }

        LOGIH(TAG,"Exit thread after...");
        if (beautifyHandle != nullptr) {
            mg_beautify.ReleaseHandle(beautifyHandle);
            beautifyHandle = nullptr;
        }
        if (stickerHandle != nullptr) {
            mg_sticker.ReleaseHandle(stickerHandle);
            stickerHandle = nullptr;
        }

        if(faceppDenseLMApiHandle != nullptr){
            faceppDenseLMApi.release_handle(&faceppDenseLMApiHandle);
        }

        if (mgFaces != nullptr) {
            delete[] mgFaces;
        }
        if (mgProcessFaces!= nullptr){
            delete [] mgProcessFaces;
        }



        MG_opengl_release_texture(&stickerTextureId);

        releaseGLContext();


    }


    void prepareGLContext(){
        if(pGlcontext_manager== nullptr){
            pGlcontext_manager = new glcontext_manager();
        }
        pGlcontext_manager->make_context();
        pGlcontext_manager->share_context();
    }

    void releaseGLContext(){
        if(pGlcontext_manager!= nullptr){
            pGlcontext_manager->done_context();
            pGlcontext_manager->release_context();
            delete pGlcontext_manager;
            pGlcontext_manager = nullptr;
        }
    }


    bool wight_hight(int &width ,int &height)
    {
        GLint max;
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, &max);
        LOGIH(__FUNCTION__,"GL_MAX_TEXTURE_SIZE GL_MAX_TEXTURE_SIZE %d", max);
        if(width > max || height > max)
        {
            int whmax = width > height ? width :height;

            float scale = 1.0f * whmax / max;
            width = width / scale;
            height = height / scale;
        }
        return true;
    }


    /**
     * 美颜初始化
     * @param env
     * @param context
     * @param model_data
     * @param model_length
     * @param image_width
     * @param image_height
     * @param orientation
     * @return
     */
    int init(JNIEnv *env, jobject context, const unsigned char *model_data, int model_length,
             int image_width, int image_height, MG_ROTATION orientation) {

        const char TAG[] = "mg_beautify init";
        wight_hight(image_width,image_height);

        LOGIH(TAG,"width %d， height %d", image_width, image_height);

        int retCode = mg_beautify.CreateHandle(env, context, model_data, model_length, image_width,
                                               image_height, (MG_ROTATION) orientation,
                                               &beautifyHandle);

        LOGDH(TAG,"mg_beautify.CreateHandle ret code  %d", retCode);
        stickerHandle = mg_sticker.CreateHandle(beautifyHandle);
        char value[10];
        __system_property_get("ro.build.version.sdk", value);
        version = atoi(value);
//        LOGDH(TAG,"mg_sticker.CreateHandleretcode: %d", retCode);
//        if(abilityType == ABILITY_TYPE_VIDEO){
//            retCode = mg_beautify_image.CreateVideoHandle(&mg_beautify_ext_handle);
//        } else {
//            retCode = mg_beautify_image.CreateHandle(&mg_beautify_ext_handle);
//        }
        LOGDH(TAG,"CreateBeautyExtHandle retcode: %d", retCode);
        intTextureIdss(image_width, image_height);
        LOGDH(TAG,"glGetError %x line %d", glGetError(), __LINE__);

        if (abilityType == ABILITY_TYPE_VIDEO) {
            workerThread.CreateThread();
        }

        return retCode;
    }

    /**
     * 美颜reset
     * @param width
     * @param height
     * @param oritation
     * @return
     */

    int reset(int width, int height, int oritation) {
        LOGIH(__FUNCTION__,"start width:%d,height:%d orientation:%d",width,height,oritation);
        faceDetectOrientation = static_cast<MG_ROTATION>(oritation);
        needFaceppDetectPost = true;
        wight_hight(width,height);

       int retCode = mg_beautify.ResetHandle(beautifyHandle, width, height,
                                              (MG_ROTATION) oritation);

        intTextureIdss(width, height);

        memory_lock.lock();
        faceCount = 0;
        memory_lock.unlock();

        LOGIH(__FUNCTION__,"end,retCode:%d", retCode);
        return retCode;
    }


    /**
     * 人脸检测相关
     * @param env
     * @param context
     * @param model_data
     * @param model_length
     * @return
     */
    int createFaceppHandler(JNIEnv *env, jobject context) {

        int retCode = mg_dense_landmark_get_api(&faceppDenseLMApi);
        if (retCode == MG_DENSE_OK) {
            faceppDenseLMApi.set_log_level(0);
            retCode |= faceppDenseLMApi.create_handler(&faceppDenseLMApiHandle);
        }
#ifndef ONLY_USE_DENSE_LM
        retCode |= mg_facepp.CreateApiHandle(model_data, model_length,
                                                &faceppApiHandle);
#endif
        LOGDH(__FUNCTION__,"faceppDenseLMApi.create_handler %d", retCode);
        return retCode;
    }

    int setFaceConfig(int orientation, int detectMode,const unsigned char *model_data,
                      int model_length,const unsigned char *denseLM_model_data,int denseLM_model_length) {

        MGDenseStatus retCode = MG_DENSE_OK;
        faceDetectOrientation = orientation;
        do{
            if (faceppDenseLMApiHandle != nullptr) {
                //初始化人脸检测

                MGDenseConfig faceDetectConfig;
                faceDetectConfig.ability = (DlmkAbility)detectMode;
                LOGIH(__FUNCTION__,"faceDetectConfig.ability:%d",faceDetectConfig.ability);
                faceDetectConfig.models.buf = model_data;
                faceDetectConfig.models.size = model_length;
                faceDetectConfig.cachePath = "/sdcard";
                retCode = faceppDenseLMApi.init_lmk_handle(faceppDenseLMApiHandle,&faceDetectConfig);
                if(retCode!= MG_DENSE_OK){
                    break;
                }
                if(denseLM_model_data == nullptr || denseLM_model_length == 0){
                    isOnlyFaceppDetect = true;
                    break;
                }

                //初始化稠密点检测

                MGDenseConfig denseLMConfig = faceDetectConfig;
                LOGIH(__FUNCTION__,"denseLMConfig.ability:%d",denseLMConfig.ability);
                denseLMConfig.models.buf = denseLM_model_data;
                denseLMConfig.models.size = denseLM_model_length;
                retCode = faceppDenseLMApi.init_denselmk_handle(faceppDenseLMApiHandle,&denseLMConfig);
                if(retCode!=MG_DENSE_OK){
                    break;
                }

            }else{
                retCode = MG_DENSE_INVALID_HANDLE;
            }
        }while(0);

        return retCode;
    }

    int releaseFaceData() {
        faceCount = 0;
        return 0;

    }

    static int compare(const void *a, const void *b)
    {
        MG_FACE *pa = (MG_FACE *)a;
        MG_FACE *pb = (MG_FACE *)b;
        return  (abs(pb->rect.top-pb->rect.bottom))-abs((pa->rect.top-pa->rect.bottom ));  //从da到xiao排序
    }

    int detectFaceppSort(double xScale, double yScale, int cameraWidth, int cameraHeight, jbyte *img_data, int detectFormat,int rotation) {
        MGDenseStatus status = MG_DENSE_OK;

        if (faceppDenseLMApiHandle == nullptr) {
            return -1001;
        }

        MGDenseRotation rotationforDenseImage = MG_DENSE_ROTATION_0;
        switch (rotation) {
            case 90:
                rotationforDenseImage = MG_DENSE_ROTATION_90;
                break;
            case 180:
                rotationforDenseImage = MG_DENSE_ROTATION_180;
                break;
            case 270:
                rotationforDenseImage = MG_DENSE_ROTATION_270;
                break;
            default:
                rotationforDenseImage = MG_DENSE_ROTATION_0;
                break;
        }

        MGDenseImageType denseImageMode = MG_DENSE_IMG_NV21;
        size_t dataSize = 0;

        switch(detectFormat){
            case MG_IMAGEMODE_GRAY:
                denseImageMode = MG_DENSE_IMG_GRAY;
                dataSize = cameraWidth*cameraHeight;
                break;
            case MG_IMAGEMODE_BGR:
                denseImageMode = MG_DENSE_IMG_BGR;
                dataSize = cameraWidth*cameraHeight*3;
                break;
            case MG_IMAGEMODE_RGB:
                denseImageMode = MG_DENSE_IMG_RGB;
                dataSize = cameraWidth*cameraHeight*3;
                break;
            case MG_IMAGEMODE_RGBA:
                denseImageMode = MG_DENSE_IMG_RGBA;
                dataSize = cameraWidth*cameraHeight*4;
                break;
            case MG_IMAGEMODE_NV12:
                denseImageMode = MG_DENSE_IMG_NV12;
                dataSize = cameraWidth*cameraHeight*3/2;
                break;
            case MG_IMAGEMODE_NV21:
                denseImageMode = MG_DENSE_IMG_NV21;
                dataSize = cameraWidth*cameraHeight*3/2;
                break;
            default:
                LOGIH(__FUNCTION__,"unsupported image format:%d",detectFormat);
                return MG_DENSE_INVALID_ARGUMENT;
        }


        MGDenseInpBlob image_data{img_data, dataSize};
        MGDenseImage image{denseImageMode, rotationforDenseImage, image_data, cameraWidth, cameraHeight, 0, 0};

        FaceDetectOutput faceDetectOutputTemp;
        do {
            MGDenseInput hum_input;
            hum_input.image = image;
            hum_input.detailType = D_LMK_FACE_DETAIL_LEFT_EYE | D_LMK_FACE_DETAIL_RIGHT_EYE | D_LMK_FACE_DETAIL_MOUTH | D_LMK_FACE_DETAIL_HAIRLINE | D_LMK_FACE_DETAIL_LEFT_EYEBROW | D_LMK_FACE_DETAIL_RIGHT_EYEBROW ;
            hum_input.deviceType = D_LMK_DEVICE_POSITION_FRONT;

            status = faceppDenseLMApi.process_lmk(faceppDenseLMApiHandle, hum_input, &faceDetectOutputTemp.sparseOutput);
            LOGDH(__FUNCTION__,"process_lm faceCount %d",faceDetectOutputTemp.sparseOutput.face_num);
            if(status!= MG_DENSE_OK){
                LOGWH(__FUNCTION__,"process_lmk error,errCode:%d\n",status);
                break;
            }else if(faceDetectOutputTemp.sparseOutput.face_num!=0 && !isOnlyFaceppDetect){
                status = faceppDenseLMApi.process_denselmk(faceppDenseLMApiHandle, hum_input,faceDetectOutputTemp.sparseOutput, &faceDetectOutputTemp.denseLMOutput);
                if(status!= MG_DENSE_OK){
                    LOGEH(__FUNCTION__,"process_denselmk error,errCode:%d\n",status);
                    break;
                }
            }
        } while (0);
        setFaces(faceDetectOutputTemp);
        LOGIH(__FUNCTION__,"dense LM face num is %zu", faceDetectOutputTemp.denseLMOutput.result.face_num);
        return status;
    }

    void setFaces(const FaceDetectOutput& denseLMOutput){
        std::unique_lock<std::mutex> t(denseLMCacmeMutex);
        if(denseLMCache.size() == SMOOTH_QUEUE_SIZE){
            denseLMCache.pop_front();
        }
        denseLMCache.push_back(denseLMOutput);
    }


    void setFaces(const MG_FACE* faces, const int& count){
        memory_lock.lock();
        faceCount = 0;
        for (int i = 0; i < count; ++i) {
            LOGDH(__FUNCTION__,"the blurness is %.3f" , float(faces[i].blurness));
            //LOGEH("the 3d pos  is %.3f, %.3f, %.3f" , faces[i].pose.pitch, faces[i].pose.roll,
             //     faces[i].pose.yaw);
            if (faces[i].confidence > 0.6f) {
                memcpy(&mgFaces[faceCount], &faces[i], sizeof(MG_FACE));
                faceCount++;
            }
        }
        memory_lock.unlock();
    }

    void transNewFaceOut2OriFaceOut(MG_FACE* faces,const FaceDetectOutput& faceDetectOutput,int& count){
        std::unique_lock<std::mutex> t1(memory_lock);
        count = faceDetectOutput.sparseOutput.face_num;

        if (faces != nullptr) {
            for (int i = 0; i < count; ++i) {
                const MGDenseLandMarks& lmTemp = faceDetectOutput.sparseOutput.items[i].points;
                for (auto j = 0; j < lmTemp.count; j++) {
                    faces[i].points.point[j] = {lmTemp.point[j].x,lmTemp.point[j].y};
                }
                faces[i].track_id = faceDetectOutput.sparseOutput.items[i].track_id;
            }
        }

    }

    FaceDetectOutput getDenseLMFaceswithSmooth(){
        FaceDetectOutput output;
        float alpha = 0.3;
        float beta = 1-alpha;
        int index = 0;
        const float coefficient[SMOOTH_QUEUE_SIZE] = {alpha,beta};

        for(auto it = denseLMCache.begin();it!=denseLMCache.end();it++){
            //暂时只支持一张人脸
            DLmkFaceDetailResult& faceResultTemp = it->denseLMOutput.result;
            int sparkFaceCount = it->sparseOutput.face_num;
            if(faceResultTemp.face_num<=0 && sparkFaceCount <=0){
                faceCount = 0;
                break;
            }

            if(it == denseLMCache.begin()){
                output = *it;
                faceCount = sparkFaceCount;
            }else{
                faceCount = faceCount<sparkFaceCount?faceCount:sparkFaceCount;
            }
            LOGDH("getDenseLMFaceswithSmooth"," faceCount %d",faceCount);
            DLmkFaceDetail& faceDetailTemp = faceResultTemp.detail[0];
            for(auto j=0;j<faceDetailTemp.size;j++){
                    DLmkFaceLmks& lmksTemp = faceDetailTemp.lmk[j];
                    if(lmksTemp.type == D_LMK_FACE_DETAIL_MOUTH || lmksTemp.type == D_LMK_FACE_DETAIL_HAIRLINE ||
                       lmksTemp.type == D_LMK_FACE_DETAIL_LEFT_EYEBROW || lmksTemp.type == D_LMK_FACE_DETAIL_RIGHT_EYEBROW
                            ){
                        for(auto k=0;k<lmksTemp.size;k++){
                            lmksTemp.data[k].x *= scaleForDenseLM;
                            lmksTemp.data[k].y *= scaleForDenseLM;
                            if(index == 0){
                                output.denseLMOutput.result.detail[0].lmk[j].data[k].x *= coefficient[index];
                                output.denseLMOutput.result.detail[0].lmk[j].data[k].y *= coefficient[index];
                            }else{
                                output.denseLMOutput.result.detail[0].lmk[j].data[k].x += coefficient[index]*lmksTemp.data[k].x;
                                output.denseLMOutput.result.detail[0].lmk[j].data[k].y += coefficient[index]*lmksTemp.data[k].y;
                            }
                        }
                    }else if(lmksTemp.type == D_LMK_FACE_DETAIL_LEFT_EYE ||
                             lmksTemp.type == D_LMK_FACE_DETAIL_RIGHT_EYE){
                        for(auto k=0;k<lmksTemp.size;k++){
                            lmksTemp.data[k].x *= scaleForDenseLM;
                            lmksTemp.data[k].y *= scaleForDenseLM;

                            if(index == 0){
                                output.denseLMOutput.result.detail[0].lmk[j].data[k].x *= coefficient[index];
                                output.denseLMOutput.result.detail[0].lmk[j].data[k].y *= coefficient[index];
                            }else{
                                output.denseLMOutput.result.detail[0].lmk[j].data[k].x += coefficient[index]*lmksTemp.data[k].x;
                                output.denseLMOutput.result.detail[0].lmk[j].data[k].y += coefficient[index]*lmksTemp.data[k].y;
                            }
                            //output.denseLMOutput.result.detail[0].lmk[j].data[k].x = lmksTemp.data[k].x;
                            //output.denseLMOutput.result.detail[0].lmk[j].data[k].y = lmksTemp.data[k].y;
                        }
                        lmksTemp.desc.eye_ext.pupil_center.x *= scaleForDenseLM;
                        lmksTemp.desc.eye_ext.pupil_center.y *= scaleForDenseLM;
                        lmksTemp.desc.eye_ext.pupil_radius *= scaleForDenseLM;
                        if(index == 0){
                            output.denseLMOutput.result.detail[0].lmk[j].desc.eye_ext.pupil_center.x *= coefficient[index];
                            output.denseLMOutput.result.detail[0].lmk[j].desc.eye_ext.pupil_center.y *= coefficient[index];
                            output.denseLMOutput.result.detail[0].lmk[j].desc.eye_ext.pupil_radius *= coefficient[index];
                        }else{
                            output.denseLMOutput.result.detail[0].lmk[j].desc.eye_ext.pupil_center.x += coefficient[index]*lmksTemp.desc.eye_ext.pupil_center.x;
                            output.denseLMOutput.result.detail[0].lmk[j].desc.eye_ext.pupil_center.y += coefficient[index]*lmksTemp.desc.eye_ext.pupil_center.y;
                            output.denseLMOutput.result.detail[0].lmk[j].desc.eye_ext.pupil_radius += coefficient[index]*lmksTemp.desc.eye_ext.pupil_radius;
                        }
                        //output.denseLMOutput.result.detail[0].lmk[j].desc.pupil_center.x = lmksTemp.desc.pupil_center.x;
                        //output.denseLMOutput.result.detail[0].lmk[j].desc.pupil_center.y = lmksTemp.desc.pupil_center.y;
                        //output.denseLMOutput.result.detail[0].lmk[j].desc.pupil_radius = lmksTemp.desc.pupil_radius;

                }
            }
            index++;
        }
        return output;
    }

    void reduzateFacedetectResult(){
        //稀疏点还原
        for(auto sparseFaceCount = 0;sparseFaceCount< m_faceDetectOutput.sparseOutput.face_num;sparseFaceCount++){
            MGDenseLandMarks& sparseLMTemp = m_faceDetectOutput.sparseOutput.items[sparseFaceCount].points;
            for (auto j = 0; j < sparseLMTemp.count; j++) {
                sparseLMTemp.point[j].x = sparseLMTemp.point[j].x*scaleForDenseLM;
                sparseLMTemp.point[j].y = sparseLMTemp.point[j].y*scaleForDenseLM;
            }
        }

        //稠密点还原
        for(auto denseLMfaceCount = 0;denseLMfaceCount <m_faceDetectOutput.denseLMOutput.result.face_num;denseLMfaceCount++){
            for(auto j=0;j<m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].size;j++){
                if(m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j].type == D_LMK_FACE_DETAIL_MOUTH ||
                   m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j].type == D_LMK_FACE_DETAIL_HAIRLINE ||
                   m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j].type == D_LMK_FACE_DETAIL_NOSE ||
                   m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j].type == D_LMK_FACE_DETAIL_FACE_MIDLINE ||
                   m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j].type == D_LMK_FACE_DETAIL_LEFT_EYEBROW ||
                   m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j].type == D_LMK_FACE_DETAIL_RIGHT_EYEBROW ){
                    DLmkFaceLmks& lmkTemp = m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j];
                    for(auto k=0;k<lmkTemp.size;k++){
                        lmkTemp.data[k].x *= scaleForDenseLM;
                        lmkTemp.data[k].y *= scaleForDenseLM;
                    }
                }else if(m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j].type == D_LMK_FACE_DETAIL_LEFT_EYE ||
                         m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j].type == D_LMK_FACE_DETAIL_RIGHT_EYE){
                    DLmkFaceLmks& lmkTemp = m_faceDetectOutput.denseLMOutput.result.detail[denseLMfaceCount].lmk[j];
                    for(auto k=0;k<lmkTemp.size;k++){
                        lmkTemp.data[k].x *= scaleForDenseLM;
                        lmkTemp.data[k].y *= scaleForDenseLM;
                    }
                    lmkTemp.desc.eye_ext.pupil_center.x *= scaleForDenseLM;
                    lmkTemp.desc.eye_ext.pupil_center.y *= scaleForDenseLM;
                    lmkTemp.desc.eye_ext.pupil_radius *= scaleForDenseLM;
                    LOGDH(__FUNCTION__,"pupil_center : (%f,%f) pupil_radius:%f" , lmkTemp.desc.eye_ext.pupil_center.x,lmkTemp.desc.eye_ext.pupil_center.y,lmkTemp.desc.eye_ext.pupil_radius);
                }
            }
        }
    }

    void getFaces(MG_FACE* faces, FaceDetectOutput& faceDetectOutput,int& count){
        memory_lock.lock();
        count = m_faceDetectOutput.sparseOutput.face_num;

        if (faces != nullptr) {
            for (int i = 0; i < count; ++i) {
                const MGDenseLandMarks& sparseLMTemp = m_faceDetectOutput.sparseOutput.items[i].points;
                for (auto j = 0; j < sparseLMTemp.count; j++) {
                    faces[i].points.point[j] = {sparseLMTemp.point[j].x,sparseLMTemp.point[j].y};
                }
                faces[i].track_id = m_faceDetectOutput.sparseOutput.items[i].track_id;
            }
        }
        faceDetectOutput = m_faceDetectOutput;
        memory_lock.unlock();
    }



    int intTextureIdss(int width, int height) {
        MG_opengl_release_texture(&stickerTextureId);
        initTexture(width, height, &stickerTextureId);
        return 0;
    }

    class DetectFaceppImpl : public  Callable<MGB_IMAGE_RETCODE>{
    public:
        beautify_handler* handler;
        MG_BeautifyImage inImage;
        DetectFaceppImpl(beautify_handler* handler){
            this->handler = handler;
        }
        MGB_IMAGE_RETCODE call(){

#ifndef ONLY_USE_DENSE_LM
            this->handler->detectFaceppForImage(inImage);
#endif
            this->handler->detectFaceppForImageDenseLM(inImage);

            return MGB_IMAGE_OK;
        }
    };

    int scaleImageAndDetectDenseLM(int width, int height, const MG_BYTE* image, MG_BeautifyImageType type, int *faceNum, float *scale_ret)
    {
        MGDenseStatus retCode = MG_DENSE_OK;
        float maxsize = 700.0f;
        float scale = 1.0f;
        if(((width > height) ? width : height) > maxsize)
        {
            scale = (float)((width > height) ? width : height) / maxsize;
        }
        int scalew = (int)width/scale;
        int scaleh = (int)height/scale;
        *scale_ret = scale;

        scalew = scalew%2 == 1? (scalew-1):scalew;
        scaleh = scaleh%2 == 1? (scaleh-1):scaleh;

        size_t imageDataSize = 0;
        MGDenseImageType imageDenseType;
        if(IMAGE_TYPE_RGBA == type){
            imageDataSize = scalew * scaleh * 4;
            imageDenseType = MG_DENSE_IMG_RGBA;
        }else if(IMAGE_TYPE_NV21 == type){
            imageDataSize = scalew * scaleh * 3/2;
            imageDenseType = MG_DENSE_IMG_NV21;
        }else if(IMAGE_TYPE_NV12 == type){
            imageDataSize = scalew * scaleh * 3/2;
            imageDenseType = MG_DENSE_IMG_NV12;
        }else{
            LOGEH(__FUNCTION__,"support %d unsupport",type);
            return MG_DENSE_INVALID_ARGUMENT;
        }

        std::unique_ptr<MG_BYTE[]> scale_image(new MG_BYTE[imageDataSize]);

        //long start = get_current_ms();

        if(type == IMAGE_TYPE_RGBA){
            mghum::BaseFormatTransform::resize_specified_size((MG_BYTE *)image, MGHUM_TRANSFORM_IMAGE_TYPE_RGBA ,
                                                              width, height,scalew, scaleh, scale_image.get());
        }else if(type == IMAGE_TYPE_NV21 || type == IMAGE_TYPE_NV12){
            nv21resize((MG_BYTE *)image,width,height,scale_image.get(),scalew,scaleh);
        }

        //LOGEH("yxt_debug: transform image time use: %ld\n", get_current_ms() - start );

        MGDenseRotation rotationforDenseImage = MG_DENSE_ROTATION_0;
        switch (faceDetectOrientation) {
            case 90:
                rotationforDenseImage = MG_DENSE_ROTATION_90;
                break;
            case 180:
                rotationforDenseImage = MG_DENSE_ROTATION_180;
                break;
            case 270:
                rotationforDenseImage = MG_DENSE_ROTATION_270;
                break;
            default:
                rotationforDenseImage = MG_DENSE_ROTATION_0;
                break;
        }

        MGDenseInpBlob image_data{scale_image.get(), imageDataSize};
        MGDenseImage imageForProcess{imageDenseType , rotationforDenseImage, image_data, scalew, scaleh};

        do {
            MGDenseInput hum_input;
            hum_input.image = imageForProcess;
            hum_input.detailType = D_LMK_FACE_DETAIL_LEFT_EYE | D_LMK_FACE_DETAIL_RIGHT_EYE | D_LMK_FACE_DETAIL_MOUTH | D_LMK_FACE_DETAIL_HAIRLINE |D_LMK_FACE_DETAIL_NOSE| D_LMK_FACE_DETAIL_FACE_MIDLINE |D_LMK_FACE_DETAIL_LEFT_EYEBROW |D_LMK_FACE_DETAIL_RIGHT_EYEBROW;

            {
                std::unique_lock<std::mutex> t1(memory_lock);

                retCode = faceppDenseLMApi.process_lmk(faceppDenseLMApiHandle, hum_input, &m_faceDetectOutput.sparseOutput);

                if(retCode!= MG_DENSE_OK){
                    LOGWH(__FUNCTION__,"process_lmk error,errCode:%d",retCode);
                    break;
                }else if(m_faceDetectOutput.sparseOutput.face_num!=0 && !isOnlyFaceppDetect) {
                    retCode = faceppDenseLMApi.process_denselmk(faceppDenseLMApiHandle, hum_input,
                                                                m_faceDetectOutput.sparseOutput,
                                                                &m_faceDetectOutput.denseLMOutput);
                    if (retCode != MG_DENSE_OK) {
                        LOGEH(__FUNCTION__,"process_denselmk error,errCode:%d", retCode);
                        break;
                    }
                }
            }
        } while (0);
        return retCode;
    }

    int detectFaceppForImageDenseLM(const MG_BeautifyImage& inImage) {
        int retCode = 0;
        int count = 0;
        MG_FACE faces[MAX_FACE_NUM];
        int faceNum = 0;
        LOGDH(__FUNCTION__,"faceppDenseLMApiHandle %p", faceppDenseLMApiHandle);

        retCode |= scaleImageAndDetectDenseLM(inImage.width, inImage.height, (MG_BYTE *)inImage.data, inImage.image_type, &faceNum, &scaleForDenseLM);
        //还原回去缩放
        reduzateFacedetectResult();
        return retCode;

    }


    int processImageInTextureOut(const MG_BeautifyImage& inImage, unsigned int outTextureIndex){


        int retCode = 0;
        FaceDetectOutput faceDetectOutput = getDenseLMFaceswithSmooth();
        transNewFaceOut2OriFaceOut(mgProcessFaces,faceDetectOutput,processFaceCount);
        LOGIH(__FUNCTION__,"the face count is %d ", faceCount);


        MG_BEAUTIFY_IMAGE mg_inImage = {reinterpret_cast<MG_BYTE *>(inImage.data), static_cast<MG_UINT32>(inImage.width),
                                        static_cast<MG_UINT32>(inImage.height), MG_IMAGEMODE_NV21};
       // static int iTimeCount = 0;
       // static long sumTime = 0;
        //glFinish();
        //long  startTime=get_current_ms();
        if (isSticker == true && stickerHandle != nullptr &&!isResetFace) {
            //稠密点需要重新计算3D pose
            for(auto faceIndex = 0;faceIndex < faceCount;faceIndex++){
                Calculate3DPoseFromLandmarks(mgProcessFaces[faceIndex].points,&(mgProcessFaces[faceIndex].pose));
            }

            if(isOnlyFaceppDetect){
                retCode |= mg_beautify.ProcessImageInTextureOut(beautifyHandle,&mg_inImage,stickerTextureId,mgProcessFaces,nullptr,faceCount);
            }else{
                retCode |= mg_beautify.ProcessImageInTextureOut(beautifyHandle,&mg_inImage,stickerTextureId,mgProcessFaces,&faceDetectOutput.denseLMOutput.result.detail[0],faceCount);
            }

           // LOGDH(__FUNCTION__,"stick beautytime %ld",(get_current_ms()-startTime));
           // startTime=get_current_ms();
            retCode |= mg_sticker.ProcessTexture(stickerHandle, stickerTextureId, outTextureIndex,
                                                 mgProcessFaces, processFaceCount);
           // LOGDH(__FUNCTION__,"stick sticktime %ld",(get_current_ms()-startTime));
//            retCode |= mg_sticker.ProcessTexture(stickerHandle, oldTextureIndex, newTextureIndex,
//                                                 mgFaces, faceCount);
//            LOGEH("processTexture stick %d", retCode);
        } else {
            //稠密点需要重新计算3D pose
            for(auto faceIndex = 0;faceIndex < faceCount;faceIndex++) {
                Calculate3DPoseFromLandmarks(mgProcessFaces[faceIndex].points, &(mgProcessFaces[faceIndex].pose));
            }
            if(isOnlyFaceppDetect){
                retCode |= mg_beautify.ProcessImageInTextureOut(beautifyHandle,&mg_inImage,outTextureIndex,mgProcessFaces, nullptr ,faceCount);
            }else{
                retCode |= mg_beautify.ProcessImageInTextureOut(beautifyHandle,&mg_inImage,outTextureIndex,mgProcessFaces,&faceDetectOutput.denseLMOutput.result.detail[0],faceCount);
            }

            //glFinish();
           // long CurrentTimeCost = (get_current_ms()-startTime);
           // sumTime += CurrentTimeCost;

           // LOGDH(__FUNCTION__,"beautytime average %ld ms,current: %ld ms",sumTime/(++iTimeCount),CurrentTimeCost);
        }
        LOGDH(__FUNCTION__,"beauty retcode %d", retCode);
        isResetFace=false;
        return retCode;

    }

    int processImageInImageOut(const MG_BeautifyImage& inImage, MG_BeautifyImage& outImage){


        int retCode = 0;
        FaceDetectOutput faceDetectOutput = getDenseLMFaceswithSmooth();
        transNewFaceOut2OriFaceOut(mgProcessFaces,faceDetectOutput,processFaceCount);
        LOGIH(__FUNCTION__,"the face count is %d ", faceCount);
//        Timer timerAll;
//        timerAll.start(PROFILE, "mg_beautify_image.ProcessImageInTextureOut");

        MG_BEAUTIFY_IMAGE mg_inImage = {reinterpret_cast<MG_BYTE *>(inImage.data), static_cast<MG_UINT32>(inImage.width),
                                        static_cast<MG_UINT32>(inImage.height), MG_IMAGEMODE_NV21};

        MG_BEAUTIFY_IMAGE mg_outImage = {reinterpret_cast<MG_BYTE *>(outImage.data), static_cast<MG_UINT32>(outImage.width),
                                        static_cast<MG_UINT32>(outImage.height), MG_IMAGEMODE_NV21};

        long  startTime=get_current_ms();

        if (isSticker == true && stickerHandle != nullptr &&!isResetFace) {
            LOGEH(__FUNCTION__,"unsupport sticker in image mode");
            return MG_RETCODE_INVALID_ARGUMENT;
        }

        if(inImage.image_type == IMAGE_TYPE_NV21) {
            mg_inImage.image_mode = MG_IMAGEMODE_NV21;
            mg_outImage.image_mode = MG_IMAGEMODE_NV21;

        }else if(inImage.image_type == IMAGE_TYPE_NV12){
            mg_inImage.image_mode = MG_IMAGEMODE_NV12;
            mg_outImage.image_mode = MG_IMAGEMODE_NV12;
        }
        retCode |= mg_beautify.ProcessImageInImageOut(beautifyHandle,&mg_inImage,&mg_outImage,mgProcessFaces,&faceDetectOutput.denseLMOutput.result.detail[0],faceCount);
//        LOGDH(__FUNCTION__,"beauty beautytime %ld",(get_current_ms()-startTime));
//        LOGDH(__FUNCTION__,"beauty retcode %d", retCode);
        isResetFace=false;
        return retCode;

    }


    int processImage(const MG_BeautifyImage& inImage, MG_BeautifyImage& outImage) {
        Timer processImagetimerAll;
        processImagetimerAll.start(__FUNCTION__,"JNI processImage all time");
        Timer everyFeatureTimer;
        everyFeatureTimer.start(__FUNCTION__,"JNI face detect time");
        MG_FACE faces[MAX_FACE_NUM];
        FaceDetectOutput faceDetectOutput;
        int count = 0;
        if(needFaceppDetectPost){
            if(abilityType == ABILITY_TYPE_VIDEO){
                DetectFaceppImpl* detectFacepp = new DetectFaceppImpl(this);
                detectFacepp->inImage = inImage;
                workerThread.asynCall(detectFacepp);
            } else {
                DetectFaceppImpl* detectFacepp = new DetectFaceppImpl(this);
                detectFacepp->inImage = inImage;
                detectFacepp->call();
                delete detectFacepp;
            }
            needFaceppDetectPost = false;
        }
        getFaces(faces, faceDetectOutput,count);
        everyFeatureTimer.end();
        int retCode = 0;

        LOGIH(__FUNCTION__,"the face count is %d ", count);


        MG_BEAUTIFY_IMAGE mg_inImage = {reinterpret_cast<MG_BYTE *>(inImage.data), static_cast<MG_UINT32>(inImage.width),
                               static_cast<MG_UINT32>(inImage.height), static_cast<MG_IMAGEMODE>(0)};

        MG_BEAUTIFY_IMAGE mg_outImage = {reinterpret_cast<MG_BYTE *>(outImage.data), static_cast<MG_UINT32>(outImage.width), static_cast<MG_UINT32>(outImage.height), static_cast<MG_IMAGEMODE>(0)};

        if(inImage.image_type == IMAGE_TYPE_RGBA) {
            mg_inImage.image_mode = MG_IMAGEMODE_RGBA;
            mg_outImage.image_mode = MG_IMAGEMODE_RGBA;

        }else if(inImage.image_type == IMAGE_TYPE_NV21){
            mg_inImage.image_mode = MG_IMAGEMODE_NV21;
            mg_outImage.image_mode = MG_IMAGEMODE_NV21;
        }else if(inImage.image_type == IMAGE_TYPE_NV12){
            mg_inImage.image_mode = MG_IMAGEMODE_NV21;
            mg_outImage.image_mode = MG_IMAGEMODE_NV21;
        }else{
            return MG_RETCODE_INVALID_ARGUMENT;
        }
        //稠密点需要重新计算3D pose
        Calculate3DPoseFromLandmarks(faces[0].points,&faces[0].pose);

        everyFeatureTimer.start(__FUNCTION__, "mg_beautify sdk process image time");
        if(isOnlyFaceppDetect){
            retCode = mg_beautify.ProcessImage(beautifyHandle,&mg_inImage,&mg_outImage,faces, nullptr,count);
        }else{
            retCode = mg_beautify.ProcessImage(beautifyHandle,&mg_inImage,&mg_outImage,faces,&faceDetectOutput.denseLMOutput.result.detail[0],count);
        }
        everyFeatureTimer.end();
        processImagetimerAll.end();
        LOGIH(__FUNCTION__,"finish retCode,retCode:%d",retCode);
        return retCode;
    }

    /**
     * 纹理处理必须先美颜后贴纸
     * @param oldTextureIndex
     * @param newTextureIndex
     * @return
     */
    int processTexture(unsigned int oldTextureIndex, unsigned int newTextureIndex,bool isPreview) {
        int retCode = 0;
        LOGDH(__FUNCTION__,"isSticker 1= %d", isSticker);
        LOGDH(__FUNCTION__,"stickerHandle %d", (stickerHandle == nullptr ? 1 : 0));


        retCode = processBeautyTexture(oldTextureIndex, newTextureIndex, retCode,isPreview);
        LOGDH(__FUNCTION__,"processBeautyTexture retCode  %d", retCode);

        return retCode;
    }

    long get_current_ms() {
        struct timespec res;
        clock_gettime(CLOCK_REALTIME, &res);
        return 1000 * res.tv_sec + res.tv_nsec / 1e6;
    }

    int processBeautyTexture(unsigned int oldTextureIndex, unsigned int newTextureIndex,
                             int retCode,bool isPreview)  {

        FaceDetectOutput faceDetectOutput = getDenseLMFaceswithSmooth();
        transNewFaceOut2OriFaceOut(mgProcessFaces,faceDetectOutput,processFaceCount);
        LOGDH(__FUNCTION__,"processFaceCount %d",processFaceCount);
        //稠密点需要重新计算3D pose
        for(auto faceIndex = 0;faceIndex < faceCount;faceIndex++){
            Calculate3DPoseFromLandmarks(mgProcessFaces[faceIndex].points,&(mgProcessFaces[faceIndex].pose));
        }
        long  startTime=get_current_ms();
        if (isSticker == true && stickerHandle != nullptr &&!isResetFace) {
            if(isOnlyFaceppDetect){
                retCode |= mg_beautify.ProcessTexture(beautifyHandle, oldTextureIndex,
                                                      stickerTextureId,
                                                      mgProcessFaces, nullptr,processFaceCount,isPreview);
            }else{
                retCode |= mg_beautify.ProcessTexture(beautifyHandle, oldTextureIndex,
                                                      stickerTextureId,
                                                      mgProcessFaces, &faceDetectOutput.denseLMOutput.result.detail[0],processFaceCount,isPreview);
            }

            LOGDH(__FUNCTION__,"in stick branch mg_beautify.ProcessTexture time %ld",(get_current_ms()-startTime));
            startTime=get_current_ms();
            retCode |= mg_sticker.ProcessTexture(stickerHandle, stickerTextureId, newTextureIndex,
                                                 mgProcessFaces, processFaceCount);
            LOGDH(__FUNCTION__,"mg_sticker.ProcessTexture time %ld",(get_current_ms()-startTime));

        } else {
            if(isOnlyFaceppDetect){
                retCode |= mg_beautify.ProcessTexture(beautifyHandle, oldTextureIndex, newTextureIndex,
                                                      mgProcessFaces, nullptr, processFaceCount,isPreview);
            }else{
                retCode |= mg_beautify.ProcessTexture(beautifyHandle, oldTextureIndex, newTextureIndex,
                                                      mgProcessFaces, &faceDetectOutput.denseLMOutput.result.detail[0],processFaceCount,isPreview);
            }

            LOGDH(__FUNCTION__,"mg_beautify.ProcessTexture time %ld",(get_current_ms()-startTime));
        }
        LOGIH(__FUNCTION__,"retcode %d", retCode);
        isResetFace=false;
        return retCode;
    }


    int setParamProperty(int type, float value) {
        if (type == 1) {    //1：大眼
            if (value > 0.01) {//maybe float comp 0 exception
                isEnlargeEye = true;
            } else {
                isEnlargeEye = false;
            }
        }
        if (type == 2) { //2:小脸
            if (value > 0.01) {//maybe float comp 0 exception
                isShrinkFace = true;
            } else {
                isShrinkFace = false;
            }
        }


        int retCode = mg_beautify.SetParamProperty(beautifyHandle, (MG_BEAUTIFY_TYPE) type, value);
        LOGIH(__FUNCTION__,"retcode: %d type:%d,value:%f", retCode,type,value);
        return retCode;
    }

    int setParamProperty2(int type, MG_BEAUTIFY_MULTI_PARAM value) {
        if (type == 1) {     //1:大眼
            if (value.coefficient > 0.01) {//maybe float comp 0 exception
                isEnlargeEye = true;
            } else {
                isEnlargeEye = false;
            }
        }
        if (type == 2) {   //2：小脸
            if (value.coefficient > 0.01) {//maybe float comp 0 exception
                isShrinkFace = true;
            } else {
                isShrinkFace = false;
            }
        }

        int retCode = mg_beautify.SetParamProperty2(beautifyHandle, (MG_BEAUTIFY_TYPE) type, &value);
        LOGIH(__FUNCTION__,"retcode: %d type:%d,value:%f", retCode,type,value.coefficient);
        return retCode;
    }

    int setStickerProperty(int value){
        return mg_sticker.SetParamProperty(stickerHandle,MG_STICKER_OVERTURN,value);
    }

    int updateSticker(std::string path) {
#ifdef ONLY_USE_DENSE_LM
        if (faceppDenseLMApiHandle == nullptr) {
            return 0;
        }
#else
        if (faceppApiHandle == nullptr) {
            return 0;
        }
#endif
        LOGIH(__FUNCTION__,"start");
        isSticker = true;
        const char *outPath;
        int retCode = mg_sticker.ChangePackage(stickerHandle, path.c_str(), &outPath);
        return retCode;

    }


    int disableSticker() {
#ifdef ONLY_USE_DENSE_LM
        if (faceppDenseLMApiHandle == nullptr) {
            return 0;
        }
#else
        if (faceppApiHandle == nullptr) {
            return 0;
        }
#endif
        LOGIH(__FUNCTION__,"start");
        isSticker = false;
        int retCode = mg_sticker.DisablePackage(stickerHandle);
        return retCode;
    }

    int preparePackage(const char *path){
        int retCode=mg_sticker.PreparePackage(stickerHandle,path);
        return retCode;
    }


    int setFilter(const char *path) {

        MG_RETCODE ret = mg_beautify.SetFilter(beautifyHandle, path);
        isFilter = true;
        return ret;
    }

    int removeFilter() {
        isFilter = false;
        MG_RETCODE ret = mg_beautify.RemoveFilter(beautifyHandle);
        return ret;
    }

    int useFastFilter(jboolean useFastFilt) {
        return mg_beautify.UseFastFilter(beautifyHandle, useFastFilt);
    }


    int initTexture(int width, int height, unsigned int *textureId) {
        LOGIH(__FUNCTION__,"start");
        glGenTextures(1, textureId);

        glBindTexture(GL_TEXTURE_2D, *textureId);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE,
                     NULL); //GL_HALF_FLOAT_OES, GL_UNSIGNED_BYTE
        LOGIH(__FUNCTION__,"finish");
        return 0;
    }

    void MG_opengl_release_texture(GLuint *tex) {
        if (0 != *tex) {
            glDeleteTextures(1, tex);
            *tex = 0;
        }
    }

    void Calculate3DPoseFromLandmarks(const MG_FACELANDMARKS &points,  MG_3DPOSE *op_pose) {
        // record points of interest
        float left_eye_center_x = points.point[0].x;
        float left_eye_center_y = - points.point[0].y;

        float right_eye_center_x = points.point[9].x;
        float right_eye_center_y = - points.point[9].y;


        float chin_x = points.point[64].x;
        float chin_y = - points.point[64].y;

        float nose_low_middle_x = points.point[35].x;
        float nose_low_middle_y = - points.point[35].y;

        // compute roll
        float roll_estimation = (float) atan2((right_eye_center_y-left_eye_center_y), (right_eye_center_x-left_eye_center_x));

        // compute distances
        float eye_mean_x = (left_eye_center_x+right_eye_center_x) /2.0f;
        float eye_mean_y = (left_eye_center_y+right_eye_center_y) /2.0f;

        float yaw_estimation = 0.0f;

        {
            MG_POINT left_eye_center = points.point[0];
            MG_POINT right_eye_center = points.point[9];
            MG_POINT nose = points.point[35];

            MG_POINT alpha {nose.x-left_eye_center.x, nose.y-left_eye_center.y};
            MG_POINT beta {nose.x-right_eye_center.x, nose.y-right_eye_center.y};
            MG_POINT gama {right_eye_center.x-left_eye_center.x, right_eye_center.y-left_eye_center.y};

            float L=(alpha.x*gama.x+alpha.y*gama.y)/sqrt(gama.x*gama.x+gama.y*gama.y+1e-7f);
            float R=(-beta.x*gama.x-beta.y*gama.y)/sqrt(gama.x*gama.x+gama.y*gama.y+1e-7f);



            // compute yaw

            float distance_left = L;
            float distance_right = R;
            if (distance_left >= distance_right) {
                float sin_yaw = (distance_left/(distance_left+distance_right+0.000000001f)-0.5f)/0.5f;
                yaw_estimation = (float) asin(CAP(sin_yaw/1.1f));
            }
            else {
                float sin_yaw = (distance_right/(distance_left+distance_right+0.000000001f)-0.5f)/0.5f;
                yaw_estimation = (float) -asin(CAP(sin_yaw/1.1f));
            }
        }


        // compute pitch
        float distance_top = sqrt((nose_low_middle_y-eye_mean_y)*(nose_low_middle_y-eye_mean_y)+
                                  (nose_low_middle_x-eye_mean_x)*(nose_low_middle_x-eye_mean_x));;
        float distance_bottom = sqrt((chin_y-nose_low_middle_y)*(chin_y-nose_low_middle_y) +
                                     (chin_x - nose_low_middle_x)*(chin_x - nose_low_middle_x));

        float pitch_estimation = 0.0f;
        if (distance_top >= distance_bottom) {
            float sin_pitch = (distance_top/(distance_top+distance_bottom+0.000000001f)-0.5f)/0.5f;
            pitch_estimation = (float) asin(CAP(sin_pitch/1.1f));
        }
        else {
            float sin_pitch = (distance_bottom/(distance_top+distance_bottom+0.000000001f)-0.5f)/0.5f;
            pitch_estimation = (float) -asin(CAP(sin_pitch/1.1f));
        }

        // assignment
        op_pose->roll = roll_estimation;//-(float)M_PI/2.0f;  // rotate
        if (op_pose->roll < -(float)M_PI) {
            op_pose->roll += 2.0f*(float)M_PI;
        }

        op_pose->pitch = pitch_estimation+(float)M_PI/20.0f;  // compensate offset in neutral pose

        op_pose->yaw = yaw_estimation;
    }


public:
    bool isSticker = false;  //需要人脸
    bool isFilter = false;
    bool isBeautyFace = false; //需要人脸

    bool isBeautyBody = false;  //需要人脸

    bool isEnlargeEye = false;
    bool isShrinkFace = false;

    bool isBeautySeg = false;   //需要自己的人脸

    bool isResetFace=true;//临时解决切换摄像头贴纸下第一帧出现马赛克的问题

    int version = 21;

    unsigned int stickerTextureId = 0;


    int faceWidth = 0, faceHeight = 0;

    int const MAX_FACE_NUM = 3;

    //对mgFaces和faceCount加锁，detect 和process 是两个不同的线程，防止不一致。
    std::mutex memory_lock;

    FaceDetectOutput m_faceDetectOutput;

    MG_FACE *mgFaces = new MG_FACE[MAX_FACE_NUM];


    int faceCount = 0;

    int faceDetectOrientation = 0;

    float scaleForDenseLM = 1.0f;
    MG_FACE *mgProcessFaces = new MG_FACE[MAX_FACE_NUM];
    int processFaceCount = 0;

    MG_BEAUTIFY_HANDLE beautifyHandle = nullptr;
    MG_STICKER_HANDLE stickerHandle = nullptr;


    MGDenseApiFunctions faceppDenseLMApi;
    MGDenseHandle faceppDenseLMApiHandle;

    glcontext_manager* pGlcontext_manager = nullptr;
    WorkerThread workerThread;
    bool isTracking = false;
    long abilityType;

    //
    std::mutex denseLMCacmeMutex;
    std::list<FaceDetectOutput> denseLMCache;   //稠密lm点平滑缓存

    bool isOnlyFaceppDetect = false;      //使用稠密库，但只做人脸检测
    bool needFaceppDetectPost = true;
};

#endif