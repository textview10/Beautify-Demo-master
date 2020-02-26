
/**
 * @file MG_BeautyImage.h
 * @brief 美颜美型等的图片处理接口，转换预览模式接口为后处理模式。
 */
#ifndef mg_beautify_image_h
#define mg_beautify_image_h

#define MGB_IMAGE_EXPORT __attribute__ ((visibility ("default")))

#ifdef __cplusplus
extern "C"{
#endif
/**
 * @brief 美颜美型算法句柄
 */
typedef void *MG_BEAUTIFY_IMAGE_HANDLE;

/**
 * 后处理返回结果
 */
enum MGB_IMAGE_RETCODE{
    MGB_IMAGE_OK,                   //正确运行
    MGB_IMAGE_INVALID_HANDLE,       //错误的句柄
    MGB_IMAGE_INVALID_ARGUMENT,     //错误的参数
    MGB_IMAGE_GL_CONTEXT,           //没有OpenGL上下文
    MGB_IMAGE_FAILED = -1,          //调用失败
};

/**
 * 支持的图片类型
 */
typedef enum {
    IMAGE_TYPE_RGBA,
    IMAGE_TYPE_NV21,
    IMAGE_TYPE_NV12
} MG_BeautifyImageType;

/**
 * 支持的图片类型
 */
typedef enum {
    ANY,
    QCOM,
    MTK,
} MG_Platform;
/**
 * 用到的图片
 */
typedef struct {
    char *data;
    int width;
    int height;
    MG_Platform platform;
    MG_BeautifyImageType image_type;
} MG_BeautifyImage;


/**
 * 原始处理texture的函数。
 * 此函数作为参数传入ProcessImage函数。
 */
typedef struct {

    //作为cookie参数传回给ProcessTexture。存ProcessTexture函数需要访问的其它资源
    void* cookie;

    //处理Texture的函数，可能包含美型美颜滤镜等。
    MGB_IMAGE_RETCODE (*ProcessTexture)(void* cookie, int inTexture, int outTexture, int width, int height);

} MG_BEAUTIFY_FUNCTION;

/**
 * 图片处理函数集合
 */
typedef struct {

    /**
     * 创建图片处理句柄
     * @param ext_handle
     */
    MGB_IMAGE_RETCODE (*CreateHandle)(MG_BEAUTIFY_IMAGE_HANDLE *pHandle);


    /**
        * 创建视频处理句柄
        * @param ext_handle
        */
    MGB_IMAGE_RETCODE (*CreateVideoHandle)(MG_BEAUTIFY_IMAGE_HANDLE *pHandle);


    /**
     * 处理图片，进行美颜或者美型处理
     * @param handle  图片处理句柄
     * @param inImage 输入图片，width和height不能为0
     * @param outImage 输出图片，width和height不能为0，大小建议与inImage相同。
     * @param function 处理texture的函数。
     */
    MGB_IMAGE_RETCODE (*ProcessImage)(MG_BEAUTIFY_IMAGE_HANDLE handle,
                               MG_BeautifyImage inImage,
                               MG_BeautifyImage outImage,
                               MG_BEAUTIFY_FUNCTION function);

    /**
     * 释放图片处理句柄。
     * @param handle
     */
    MGB_IMAGE_RETCODE (*ReleaseHandle)(MG_BEAUTIFY_IMAGE_HANDLE handle);

} MG_BEAUTIFY_IMAGE_API_FUNCTIONS_TYPE;

extern MGB_IMAGE_EXPORT MG_BEAUTIFY_IMAGE_API_FUNCTIONS_TYPE mg_beautify_image;

#ifdef __cplusplus
}
#endif
#endif //mg_beautify_image_h
