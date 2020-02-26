
/**
 * @file MG_Beautify.h
 * @brief 美颜算法的头文件
 *
 * 包含 Face++ 的美颜算法
 */

#ifndef mg_beautify_h
#define mg_beautify_h

#include "MG_Common.h"
#include "mg_dense_landmark.h"

#ifdef __cplusplus
extern "C"{
#endif
    
#define MGB_ATTR_BEAUTIFY 0x01          ///< 磨皮美白标识位
#define MGB_ATTR_TRANS 0x02             ///< 大眼瘦脸标识位
#define MGB_ATTR_FILTER 0x04            ///< 滤镜标识位
#define MGB_ATTR_STICKER 0x08           ///< 贴纸标识位
#define MGB_ATTR_BUDY 0x10              ///< 魔法背景标识位
#define MGB_ATTR_SKIN 0x20              ///< 人脸换肤标识位
#define MAX_KEYPOINT_SIZE 150            ///< 关键点最大数量
#define MAX_TEMPLATE_SIZE 6             ///< 模板数量
#define MAX_TEMPLATE_GROUP_SIZE 2        ///< 模板组数量
    /**
     * @brief 美颜美型参数
     *
     * 以下参数区间均为 [0， 10]，0为没有效果，10为效果最高
     */
    typedef enum MG_BEAUTIFY_TYPE_{
        MG_BEAUTIFY_ENLARGE_EYE = 1,            ///< 眼镜变大
        MG_BEAUTIFY_SHRINK_FACE = 2,            ///< 小脸
        MG_BEAUTIFY_BRIGHTNESS = 3,             ///< 亮度
        MG_BEAUTIFY_DENOISE = 4,                ///< 磨皮程度
        MG_BEAUTIFY_PINK = 5,                   ///< 粉嫩程度
        MG_BEAUTIFY_THIN_FACE = 6,              ///< 瘦脸
        MG_BEAUTIFY_BRITHTEN_EYE = 7,           ///< 亮眼
        MG_BEAUTIFY_TOOTH = 8,                  ///< 美牙
        MG_BEAUTIFY_EYEBROW = 9,                ///< 染眉
        MG_BEAUTIFY_CONTACT_LENS = 10,          ///< 美瞳
        MG_BEAUTIFY_LIP = 11,                   ///< 美唇
        MG_BEAUTIFY_REMOVE_SPECKLES = 12,       ///< 祛斑
        MG_BEAUTIFY_REMOVE_EYEBAGS = 13,        ///< 祛眼袋
        MG_BEAUTIFY_HIGH_NOSEBRIDGE = 14,       ///< 高鼻染
        MG_BEAUTIFY_UPCHEEK = 15,               ///< 提脸颊
        MG_BEAUTIFY_REMOVE_EYEBROW = 16,        ///< 去眉毛
        MG_BEAUTIFY_SKIN_BALANCE = 17,           ///< 肤色均衡
        MG_BEAUTIFY_THIN_NOSE = 18,             ///< 瘦鼻
        MG_BEAUTIFY_LONG_NOSE = 19,             ///< 长鼻
        MG_BEAUTIFY_WARPED_NOSE = 20,           ///< 翘鼻
        MG_BEAUTIFY_ADD_BLUSH = 21,             ///< 加腮红
        MG_BEAUTIFY_SHADING = 22,                ///< 修容
        MG_BEAUTIFY_EYESHADOW = 23,               ///< 眼影

    }MG_BEAUTIFY_TYPE;
    
    
    struct _MG_BEAUTIFY;

    /**
 * @brief 图像数据信息
 *
 * 表示图像数据的相关信息
 */
typedef struct {
 
    MG_BYTE *data;              ///< 图像数据指针
    MG_UINT32 width;            ///< 图像宽度
    MG_UINT32 height;           ///< 图像高度
    MG_IMAGEMODE image_mode;    ///< 图像格式

} MG_BEAUTIFY_IMAGE;

typedef struct {
    MG_UINT8 r;
    MG_UINT8 g;
    MG_UINT8 b;

}MG_BEAUTIFY_RGB_FORMAT;

typedef struct {
    uint8_t* binarityData;    //内存由外部管理，外部保证内存生命周期
    size_t binarityDataSize;  //内存大小
}MG_BINARITY_DATA;

typedef struct {
    MG_BEAUTIFY_IMAGE templateObject[MAX_TEMPLATE_SIZE];  //图像模板对象，如美瞳模板(可选)
    unsigned int templateObjectSize;                      //图像模板数量
}MG_TEMPLATEOBJECT;

typedef struct {
    float coefficient;                                    //强度系数(必选)
    MG_BEAUTIFY_RGB_FORMAT color;                         //颜色信息(可选)
    MG_TEMPLATEOBJECT templateObjects[MAX_TEMPLATE_GROUP_SIZE];  //图像模板组对象，如美瞳模板(可选)
    unsigned int templateObjectsSize;                      //图像模板组数量
    MG_POINT keypoint[MAX_KEYPOINT_SIZE];                 //关键点信息，染眉、腮红会用（可选）
    unsigned int keypointSize;                            //关键点数量，染眉、腮红会用（可选）
    MG_BINARITY_DATA removeSpecklesModels;                //祛斑模型，只有祛斑会用（可选）
    const char*      classifierModelPath;                 //祛斑分类模型路径，只有祛斑会用（可选）
}MG_BEAUTIFY_MULTI_PARAM;

typedef enum {
    MG_LOG_LEVEL_DISABLE = 0,       //所有日志都不打开。
    MG_LOG_LEVEL_INFO    = 1,       //只会打开error、warning、info级别日志，debug级别不打开。
    MG_LOG_LEVEL_WARNING = 2,       //只会打开error、warning级别日志，info、debug级别不打开。
    MG_LOG_LEVEL_ERROR   = 3,       //只会打开error级别日志。
    MG_LOG_LEVEL_DEBUG   = 4        //所有日志都会打开,包括info、warning、error、debug。
}MG_BEAUTIFY_LOG_LEVEL;
    /**
     * @brief 美颜美型算法句柄
     */
    typedef struct _MG_BEAUTIFY* MG_BEAUTIFY_HANDLE;
    
    typedef struct {
        
        /**
         * @brief 获取算法版本信息
         *
         * @return 返回一个字符串，表示算法版本号及相关信息
         */
        const char* (*GetApiVersion)();
        
        
        /**
         * @brief 创建美颜美型算法句柄（handle）
         *
         * 传入算法模型数据，创建一个算法句柄。
         *
         * @param[in] env               Android jni 的环境变量，仅在 Android SDK 中使用
         * @param[in] jobj              Android 调用的上下文，仅在 Android SDK 中使用
         * @param[in] model_data        算法模型的二进制数据
         * @param[in] model_length      算法模型的字节长度
         * @param[in] image_width       要处理图像的高度
         * @param[in] image_height      要处理图像的宽度
         * @param[in] orientation       输入图像顺时针旋转 rotation 度之后为正常的重力方向。
         *
         * @param[out] handle   算法句柄的指针，成功创建后会修改其值
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*CreateHandle)(
#if MGAPI_BUILD_ON_ANDROID
                                   JNIEnv*,jobject context,
#endif
                                   const unsigned char* model_data, int model_length,
                                   int image_width, int image_height, MG_ROTATION orientation,
                                   MG_BEAUTIFY_HANDLE *handle);
        
        /**
         * @brief 在输入的 图像texture 宽度/高度 要改变时，请调用该方法重置
         *
         * @param[in] handle            美颜美型句柄
         * @param[in] image_width       图像的高度
         * @param[in] image_height      图像的的宽度
         * @param[in] orientation       输入图像顺时针旋转 rotation 度之后为正常的重力方向
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*ResetHandle)(MG_BEAUTIFY_HANDLE handle,
                                  int image_width, int image_height,
                                  MG_ROTATION orientation);

        
        
        /**
         * @brief 释放美颜美型句柄（handle）
         *
         * @param[in] handle 美颜美型句柄
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*ReleaseHandle)(MG_BEAUTIFY_HANDLE handle);
        
        
        /**
         * @brief 设置美颜美型的参数
         *
         * @param[in] handle 美颜美型句柄
         * @param[in] MG_BEAUTIFY_TYPE 设置参数的类型
         * @param[in] value 设置数值 【0 - 10】之间
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*SetParamProperty)(MG_BEAUTIFY_HANDLE handle,
                                       MG_BEAUTIFY_TYPE type, float value);
        
        /**
         * @brief 对图像按照设置的参数进行渲染，此接口用于视频模式，图片后处理模式
         *
         * @param[in] handle 美颜美型句柄
         * @param[in] oldTextureIndex 原始图像 texture
         * @param[in] newTextureIndex 渲染后的图像 texture
         * @param[in] faces 人脸信息数组
         * @param[in] denseLMFaces 稠密点人脸数据信息
         * @param[in] facesCount 人脸数量
         * @param[in] isPreview 是否是视频模式
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*ProcessTexture)(MG_BEAUTIFY_HANDLE handle,
                                     unsigned int oldTextureIndex,
                                     unsigned int newTextureIndex,
                                     MG_FACE *faces,DLmkFaceDetail* denseLMFaces,
                                     int facesCount,bool isPreview);
        
        /**
         * @brief 图像添加滤镜效果
         *
         * @param[in] handle 美颜美型句柄
         * @param[in] filterLocation 滤镜数据路径
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*SetFilter)(MG_BEAUTIFY_HANDLE handle,
                                const char* filterLocation);
        
        /**
         * @brief  图像移除滤镜效果
         *
         * @param[in] handle 美颜美型句柄
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*RemoveFilter)(MG_BEAUTIFY_HANDLE handle);
        
        /**
         * @brief  使用加速的磨皮方法
         *
         * @param[in] handle 美颜美型句柄
         * @param[in] value 是否使用加速的磨皮方法
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*UseFastFilter)(MG_BEAUTIFY_HANDLE handle, bool value);

        /**
         * @brief 传入的图像为MG_IMAGE类型，对图像按照设置的参数进行美化处理，支持图片rgba,nv12,nv21格式。
         * 输入输出格式设置通过MG_IMAGE类型内的image_mode设置，此接口用于图片模式
         *
         * @param[in] handle 美颜美型句柄
         * @param[in] inImage 原始图像
         * @param[in] outImage 渲染后的图像
         * @param[in] faces 人脸信息数组
         * @param[in] denseLMFaces 稠密人脸信息数组
         * @param[in] facesCount 人脸数量
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*ProcessImage)(MG_BEAUTIFY_HANDLE handle,
                                     MG_BEAUTIFY_IMAGE *inImage,
                                     MG_BEAUTIFY_IMAGE *outImage,
                                     MG_FACE *faces, DLmkFaceDetail* denseLMFaces,int facesCount);
        /**
         * @brief 传入的图像为MG_IMAGE类型，对图像按照设置的参数进行渲染,输出texture，且包含扩展的算法,此接口用于视频模式
         *
         * @param[in] handle 美颜美型句柄
         * @param[in] inImage 原始图像
         * @param[in] outTextureIndex 渲染后的图像(texture)
         * @param[in] faces 人脸信息数组
         * @param[in] denseLMFaces 稠密人脸信息数组
         * @param[in] facesCount 人脸数量
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*ProcessImageInTextureOut)(MG_BEAUTIFY_HANDLE handle,
                                                MG_BEAUTIFY_IMAGE *inImage,
                                                unsigned int outTextureIndex,
                                                MG_FACE *faces,DLmkFaceDetail* denseLMFaces, int facesCount);

        /**
         * @brief 设置美颜美型的参数
         *
         * @param[in] handle 美颜美型句柄
         * @param[in] MG_BEAUTIFY_TYPE 设置参数的类型
         * @param[in] value 设置复合参数,强度【0-10】
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*SetParamProperty2)(MG_BEAUTIFY_HANDLE handle,
                                       MG_BEAUTIFY_TYPE type, MG_BEAUTIFY_MULTI_PARAM* value);

        /**
         * @brief 传入的图像为MG_IMAGE类型，对图像按照设置的参数进行渲染，且包含扩展的算法。
         * 输入输出图片支持nv12、nv21。数据格式通过MG_IMAGE内的image_mode设置，此接口用于视频模式,使用此接口前请确保提供的so是非Android普适版本
         * @param[in] handle 美颜美型句柄
         * @param[in] inImage 原始图像
         * @param[in] outImage 渲染后的图像,此对象数据内存由外部管理，同时要指定宽、高、格式信息，否则，行为未定义
         * @param[in] faces 人脸信息数组
         * @param[in] denseLMFaces 稠密人脸信息数组
         * @param[in] facesCount 人脸数量
         *
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*ProcessImageInImageOut)(MG_BEAUTIFY_HANDLE handle,
                                                MG_BEAUTIFY_IMAGE *inImage,
                                                MG_BEAUTIFY_IMAGE * outImage,
                                                MG_FACE *faces,DLmkFaceDetail* denseLMFaces, int facesCount);
        
        /**
         * @brief 设置日志级别,此接口要第一个调用，否则日志显示结果可能不正确
         * @param[in] logLevel 日志级别
         * @return 成功则返回 MG_RETCODE_OK
         */
        MG_RETCODE (*SetLogLevel)(MG_BEAUTIFY_LOG_LEVEL logLevel);

    } MG_BEAUTIFY_API_FUNCTIONS_TYPE;

    /**
     * @brief 美颜美型算法域
     *
     * Example:
     *      mg_beautify.CreateHandle(...
     *      mg_beautify.ResetHandle(...
     */
    extern MG_EXPORT MG_BEAUTIFY_API_FUNCTIONS_TYPE mg_beautify;
    
#ifdef __cplusplus
}
#endif

#endif /* mg_beautify_h */
