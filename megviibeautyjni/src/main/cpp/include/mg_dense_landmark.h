#ifndef MG_HUMAN_EFFECTS_H
#define MG_HUMAN_EFFECTS_H

#include <stddef.h>
#include <stdint.h>

#define MG_HUMAN_PUBLIC __attribute__((visibility("default")))

/// API 头文件的版本号
#define MG_HUM_VERSION_MAJOR 1
#define MG_HUM_VERSION_MINOR 0
#define MG_HUM_VERSION_PATCH 110

#ifdef __cplusplus
extern "C" {
#endif


///< 句柄类型
struct _MegDLSdkHandleImpl;
typedef struct _MegDLSdkHandleImpl *MGDenseHandle;


/// 函数返回状态
typedef enum MGDenseStatus {
    MG_DENSE_OK = 0x0000,       ///< 接口状态正常
    MG_DENSE_INVALID_ARGUMENT,  ///< 传入的参数错误
    MG_DENSE_INVALID_HANDLE,    ///< 传入的handle对象不合法
    MG_DENSE_INVALID_MODEL,     ///< 传入模型错误
    MG_DENSE_OPENGLES_CONTEXT,  ///< OpenGles context 错误
    MG_DENSE_DETECT_FAILURE,    ///< 检测人脸失败
    MG_DENSE_CONFIG_ERROR,      ///< 设置错误，一般是指setconfig设置的属性跟模型不匹配
    MG_DENSE_NOT_FOUND_FACE,    ///< 未找到符合要求的人脸
    MG_DENSE_RES_LOSE,          ///< 未设置渲染资源
    MG_DENSE_RES_ERROR,         ///< 渲染资源错误
    MG_DENSE_GL_ERROR,          ///< 当前GLcontext存在异常，调用者应当检查glGetError并解决
    MG_DENSE_FAILURE,           ///< 其他错误，比如调用流程不当
} MGDenseStatus;

typedef enum DLmkFaceDetailType {
    D_LMK_FACE_DETAIL_HAIRLINE = 1 << 0,
    D_LMK_FACE_DETAIL_LEFT_EYEBROW = 1 << 1,
    D_LMK_FACE_DETAIL_RIGHT_EYEBROW = 1 << 2,
    D_LMK_FACE_DETAIL_LEFT_EYE = 1 << 3,
    D_LMK_FACE_DETAIL_RIGHT_EYE = 1 << 4,
    D_LMK_FACE_DETAIL_NOSE_MIDLINE = 1 << 5,
    D_LMK_FACE_DETAIL_NOSE = 1 << 6,
    D_LMK_FACE_DETAIL_MOUTH = 1 << 7,
    D_LMK_FACE_DETAIL_CONTOUR = 1 << 8,
    D_LMK_FACE_DETAIL_FACE_MIDLINE = 1 << 9,
    D_LMK_FACE_DETAIL_LMK_81 = 1 << 10
} DLmkFaceDetailType;

/// SDK包含的能力
typedef enum DlmkAbility {
    D_LMK_ABILITY_PREVIEW = 0,     ///< 预览
    D_LMK_ABILITY_FULL_SIZE,       ///< 后处理
} DlmkAbility;

/// 图像逆时针旋转角度, 旋转后为人体向上正常
typedef enum MGDenseRotation {
    MG_DENSE_ROTATION_0 = 0,          ///< 不旋转
    MG_DENSE_ROTATION_90 = 90,        ///< 图像右时针旋转 90 度
    MG_DENSE_ROTATION_180 = 180,      ///< 图像右时针旋转 180 度
    MG_DENSE_ROTATION_270 = 270,      ///< 图像右时针旋转 270 度
} MGDenseRotation;

/// 图像数据格式
typedef enum MGDenseImageType {
    MG_DENSE_IMG_NV21,    ///< nv21
    MG_DENSE_IMG_NV12,    ///< nv12
    MG_DENSE_IMG_BGR,     ///< BGR
    MG_DENSE_IMG_RGB,     ///< RGGB
    MG_DENSE_IMG_RGBA,    ///< RGBA
    MG_DENSE_IMG_FULLYUV, ///< RGBA
    MG_DENSE_IMG_GRAY,    ///<
    MG_DENSE_IMG_DEPTH,   ///< 单通道 depth
    MG_DENSE_IMG_DEPTH_2, ///< 单通道 depth
    MG_DENSE_IMG_NORMAL   ///< 法向量
} MGDenseImageType;


/// 带长度信息的输出数组
typedef struct MGDenseOutBlob {
    void *buf;
    size_t size;
} MGDenseOutBlob;

/// 带长度信息的输入数组
typedef struct MGDenseInpBlob {
    const void *buf;
    size_t size;
} MGDenseInpBlob;

/**
 * @brief 图像中平行于量坐标轴的矩形框
 *
 * 在图像中表示一个双边平行于坐标轴的矩形框，
 */
typedef struct MGDenseRect {
    int left;   ///< 矩形框最左边的坐标值
    int top;    ///< 矩形框最上边的坐标值
    int width;  ///< 矩形框的宽度
    int height; ///< 矩形框的高度
} MGDenseRect;


/// 输入图像结构体
typedef struct MGDenseImage {
    /// 图像类型
    MGDenseImageType type;
    /// 图像旋转角度
    MGDenseRotation rotation;
    /// 图像数据
    MGDenseInpBlob data;
    /// 宽度和高度；图像存储格式为行优先的[height][width]
    int width, height;
    /// 步长；图像每行的实际字节数，小于(width*每像素字节数)时无效
    int stride;
    /// 列步长；图像数据对齐后的行数，小于(height*每像素行数)时无效
    int scanline;
} MGDenseImage;


/// 输出图像结构体
typedef struct MGDenseOutImage {
    /// 图像类型
    MGDenseImageType type;
    /// 图像旋转角度
    MGDenseRotation rotation;
    /// 图像数据
    MGDenseOutBlob data;
    /// 宽度和高度；图像存储格式为行优先的[height][width]
    int width, height;
    /// 图像步长:
    int stride;
    /// 图像列步长:
    int scanline;
} MGDenseOutImage;


/**
 * @brief 坐标点类型
 *
 * 表示一个二维平面上的坐标（笛卡尔坐标系）。
 */
typedef struct DLmkPoint2f {
    float x;    ///< 坐标点x轴的值
    float y;    ///< 坐标点y轴的值
} DLmkPoint2f;

/**
 * @brief 人脸的关键点集合
 */
typedef struct MGDenseLandMarks {
    DLmkPoint2f point[600];              ///< 人脸关键点，最多支持 600 点
    int count;                           ///< 关键点数量
} MGDenseLandMarks;

/**
 * @brief 记录人脸信息的类型
 *
 * 记录了人脸所有属性信息，关键点信息的类型。
 */
typedef struct MGDenseFace {
    int track_id;                       ///< 人脸的跟踪标记。
    ///< 如果只对单张图做人脸检测则固定为 -1，
    ///< 否则在不同帧中相同的 track_id 表示同一个人脸。
    MGDenseRotation rotation;             ///< 人脸框方向
    MGDenseRect rect;                     ///< 人脸在图像中的位置，以一个矩形框来刻画。
    MGDenseLandMarks points;              ///< 人脸关键点信息。
} MGDenseFace;

/**
 * @brief 记录人脸信息的数组
 */
typedef struct MGDenseFaces {
#ifdef __APPLE__
    int face_num;
#else
    int face_num = 0;                       ///< 人脸数量
#endif
    MGDenseFace items[10];                ///< 人脸数组，最大数量为 10
} MGDenseFaces;

typedef struct DlmkEyeDescriptor {
    DLmkPoint2f pupil_center;           ///< 瞳孔中心点坐标
    float pupil_radius;                 ///< 瞳孔半径
} DlmkEyeDescriptor;

typedef struct DlmkContourDescriptor {
    DLmkPoint2f ext_pts[16];                ///< 下轮廓额外的点
    size_t size;
} DlmkContourDescriptor;

typedef struct DlmkDescriptor {
    bool is_valid;                            ///<是否是有效的
//    union {
        DlmkEyeDescriptor eye_ext;
        DlmkContourDescriptor contour_ext;
//    };
} DlmkDescriptor;

/**
 *  一个类型对应的致密点数据
 */
typedef struct DLmkFaceLmks {
    DLmkPoint2f data[200];              ///< 数据数组
    size_t size;                        ///< 有效数据大小，表示点多数量
    DLmkFaceDetailType type;            ///< detail类型
    DlmkDescriptor desc;                ///< 当前类型的额外描述信息
} DLmkFaceLmks;

/**
 *  一个人脸对应的所有类型的致密landmark数据
 */
typedef struct DLmkFaceDetail {
    DLmkFaceLmks lmk[15];               ///< 数据数组
    size_t size;                        ///< 有效数据大小，表示detail类型数量
} DLmkFaceDetail;

/**
 *  所有人脸致密landmark数据集合
 */
typedef struct DLmkFaceDetailResult {
    DLmkFaceDetail detail[3];           ///< 数据数组
    size_t face_num;                   ///< 有效数据大小，表示人脸数量
} DLmkFaceDetailResult;

/**
 * @brief 可调的配置选项, 以下设置，只允许初始化时候设置
 */
typedef struct MGDenseConfig {
    DlmkAbility ability;                ///< sdk能力
    MGDenseInpBlob models;              ///< 模型数据
    const char *cachePath;              ///< SDK 存放缓存位置，用于加速SDK使用
} MGDenseConfig;


/**
 * @brief texture
 */
typedef struct MGDenseInpTexture {
    int id;                                 ///< 输入纹理 ID
    int out_id;                             ///< 输出纹理 ID
    int width;                              ///< 纹理宽度
    int height;                             ///< 纹理高度
    MGDenseRotation orientation;              ///< 纹理方向
    bool samplerExternalOES;                ///< 输入纹理是否为 samplerExternalOES 格式
} MGDenseInpTexture;


/// 摄像头设备类型
typedef enum DlmkDevicePosition {
    D_LMK_DEVICE_POSITION_BACK,        ///< 后置摄像头
    D_LMK_DEVICE_POSITION_FRONT       ///< 前置摄像头
} DlmkDevicePosition;

/**
 * @brief  输入参数选项
 */
typedef struct MGDenseInput {
    MGDenseImage image;                   ///< 普通 RGBA/NV21 12图片
    MGDenseInpTexture texure;             ///< 纹理信息，仅在实时预览需要合法参数
    int detailType;             ///< 需要的人脸部位类型，具体见 DLmkFaceDetailTpye，支持多个类型 a | b
    int deviceType;             ///< 区分前后摄像头类型,具体见 DlmkDevicePosition 类型
} MGDenseInput;

/**
 * @brief  输出参数选项
 */
typedef struct MGDenseOutput {
    DLmkFaceDetailResult result;    ///< 输出的landmark点
} MGDenseOutput;


/**
 * @brief 影像算法函数集合
 */
typedef struct MGDenseApiFunctions {
    /**
     * @brief 设置全局 log 级别
     *
     * 设为 0-3，分别对应debug info warnning error，值越小显示的信息越多 。4为disable 不显示日志。
     * 注意：在最终发布版中请关闭 log！
     *
     * @param[in] level                 要设置的 log 级别
     *
     * @return 始终返回 MG_HUM_OK
     */
    MGDenseStatus (*set_log_level)(int level);

    /**
     * 获取当前sdk的版本信息；版本号依据 semantic versioning 规则管理
     *
     * @param[out] major                主版本号
     * @param[out] minor                附版本号
     * @param[out] patch                补丁版本号
     *
     * @return 始终返回 MG_HUM_OK
     */
    MGDenseStatus (*get_version)(int *major, int *minor, int *patch);

    /**
     * @brief 创建人脸特效算法句柄（handle），并进行初始化
     *
     * @param[out] handle               人脸算法句柄的指针，成功创建后会修改其值
     * @param[in] config                配置信息，不允许传入为 null, 该配置会确定 handler 能力，之后不允许修改能力。
     *
     * @return 成功则返回 MG_HUM_OK
     */
//    MGDenseStatus (*create_handle)(MGDenseHandle *handle, const MGDenseConfig *config);

    MGDenseStatus (*create_handler)(MGDenseHandle *handle);

    /**
     * @brief 释放内存资源，及人脸特效句柄本身
     *
     * @param[in,out] handle             人脸特效句柄；将被置零
     *
     * @return 成功则返回 MG_HUM_OK
     */
    MGDenseStatus (*release_handle)(MGDenseHandle *handle);

    /**
     * @brief 图像处理, 根据 handler 初始化时候，来确定渲染类型（实时预览、相机后处理或者相册后处理）
     *
     * @param[in] handle            人脸特效句柄
     * @param[in] inputData         输入的参数结构体
     * @param[out] output           输出的参数结构体
     *
     * @return 成功则返回 MG_HUM_OK
     */
//    MGDenseStatus (*process_image)(MGDenseHandle handle, MGDenseInput inputData, MGDenseOutput *output);

    /**
     * 初始化 81点的模型和配置
     * @param handle  人脸特效句柄
     * @param config  配置信息
     * @return
     */
    MGDenseStatus (*init_lmk_handle)(MGDenseHandle handle, const MGDenseConfig *config);

    /**
     * 初始化 稠密点的模型和配置
     * @param handle  人脸特效句柄
     * @param config  配置信息
     * @return
     */
    MGDenseStatus (*init_denselmk_handle)(MGDenseHandle handle, const MGDenseConfig *config);

    /**
     * 处理 81点的图像数据，返回人脸数据
     * @param handle     人脸特效句柄
     * @param inputData  输入图像数据
     * @param output     输出的81点人脸数据
     * @return
     */
    MGDenseStatus (*process_lmk)(MGDenseHandle handle, MGDenseInput inputData, MGDenseFaces *output);

    /**
     * 处理 稠密点的图像数据，返回人脸数据
     * @param handle     人脸特效句柄
     * @param inputData  输入图像数据
     * @param inputLmk   输入的人脸81点数据
     * @param output     输出的稠密点人脸数据
     * @return
     */
    MGDenseStatus (*process_denselmk)(MGDenseHandle handle, MGDenseInput inputData, MGDenseFaces inputLmk,
                                      MGDenseOutput *output);
} MGDenseApiFunctions;

/// 内部函数，请勿直接调用
MG_HUMAN_PUBLIC MGDenseStatus _mg_dense_landmark_get_api_impl(
        MGDenseApiFunctions *api, int api_size, int major, int minor, int patch);
/// 内部函数，请勿直接调用
MG_HUMAN_PUBLIC MGDenseStatus _mg_dense_landmark_init_default_config(
        MGDenseConfig *config, int config_size, int major, int minor, int patch);
#ifdef __cplusplus
};
#endif

/**
 * 取得所有 API 的指针
 *
 * @param[out] api                      API 指针返回结果
 *
 * @return MG_HUM_OK 表示成功；MG_HUM_FAILURE 表示头文件版本错误
 */
static inline MGDenseStatus mg_dense_landmark_get_api(MGDenseApiFunctions *api) {
    return _mg_dense_landmark_get_api_impl(
            api, sizeof(MGDenseApiFunctions), MG_HUM_VERSION_MAJOR,
            MG_HUM_VERSION_MINOR, MG_HUM_VERSION_PATCH);
}

/**
 * 初始化默认的config
 *
 * @param[out] config                   初始化的config指针返回结果
 *
 * @return MG_HUM_OK 表示成功；MG_HUM_FAILURE 表示头文件版本错误
 */
static inline MGDenseStatus mg_dense_landmark_init_default_config(
        MGDenseConfig *config) {
    return _mg_dense_landmark_init_default_config(
            config, sizeof(MGDenseConfig), MG_HUM_VERSION_MAJOR,
            MG_HUM_VERSION_MINOR, MG_HUM_VERSION_PATCH);
}

#endif  // MG_HUMAN_EFFECTS_H

