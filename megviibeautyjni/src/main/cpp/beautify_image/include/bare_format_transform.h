//
//  bare_format_transform.h
//
//  Created by LiangJuzi on 2018/5/7.
//  Copyright © 2018年 Megvii. All rights reserved.
//

#ifndef bare_format_transform_h
#define bare_format_transform_h

#include <stdio.h>

typedef enum MGHUM_TRANSFORM_IMAGE_TYPE {
    MGHUM_TRANSFORM_IMAGE_TYPE_GRAY,
    MGHUM_TRANSFORM_IMAGE_TYPE_BGR,
    MGHUM_TRANSFORM_IMAGE_TYPE_RGB,
    MGHUM_TRANSFORM_IMAGE_TYPE_RGBA,
    MGHUM_TRANSFORM_IMAGE_TYPE_YUVNV21,
    MGHUM_TRANSFORM_IMAGE_TYPE_YUVNV12,
    MGHUM_TRANSFORM_IMAGE_TYPE_FULLSIZEYUVNV,
} MGHUM_TRANSFORM_IMAGE_TYPE;

namespace mghum {
    class BaseFormatTransform {
    public:
        static void bgr2rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void bgr2rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void bgr2yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void bgr2yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void bgr2fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        
        static void rgb2bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void rgb2rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void rgb2yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void rgb2yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void rgb2fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        
        static void rgba2rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void rgba2bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void rgba2yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void rgba2yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void rgba2fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        
        static void yuvnv212bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void yuvnv212rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void yuvnv212rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void yuvnv212yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void yuvnv212fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        
        static void yuvnv122bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void yuvnv122rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void yuvnv122rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void yuvnv122yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        static void yuvnv122fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height);
        
        static void fullsizeyuv2bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height);
        static void fullsizeyuv2rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height);
        static void fullsizeyuv2rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height);
        static void fullsizeyuv2yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height);
        static void fullsizeyuv2yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height);
        /**
         * rotate with RGB or BGR.
         * @param orientation must be in multiples of 90.
         */
        static void rotate_rgb(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height);
        
        /**
         * rotate with RGBA or BGRA, ARGB, ABGR.
         * @param orientation must be in multiples of 90.
         */
        static void rotate_rgba(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height);
        
        /**
         * rotate with gray or depth.
         * @param orientation must be in multiples of 90.
         */
        static void rotate_singlechannel(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height);
        
        /**
         * rotate with FULLSIZEYUV.
         * @param orientation must be in multiples of 90.
         */
        static void rotate_fullsizeyuv(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height);
        
        /**
         * rotate with YUNNV21 or YUNNV12.
         * @param orientation must be in multiples of 90.
         */
        static void rotate_yuv420p(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height);
    
        /**
         * resize and retota public param.
         * @param src[input] original image data buffer
         * @param src_width[input] original image width
         * @param src_height[input] original image height
         * @param dst[output] export image data buffer
         */
        static void resize_scale(const unsigned char* __restrict__ src, MGHUM_TRANSFORM_IMAGE_TYPE image_type, int src_width, int src_height, float scale, unsigned char* __restrict__ dst, int& dst_width, int& dst_height);
        
        /**
         * resize max edge
         * @param max_edge[input] max(dst_width, dst_height) = max_edge
         * @param dst_width[output] export image width
         * @param dst_height[output] export image height
         */
        static void resize_maxedge(const unsigned char* __restrict__ src, MGHUM_TRANSFORM_IMAGE_TYPE image_type, int src_width, int src_height, unsigned char* __restrict__ dst, int max_edge, int& dst_width, int& dst_height);
        
        /**
         * resize min edge
         * @param min_edge[input] min(dst_width, dst_height) = min_edge
         * @param dst_width[output] export image width
         * @param dst_height[output] export image height
         */
        static void resize_minedge(const unsigned char* __restrict__ src, MGHUM_TRANSFORM_IMAGE_TYPE image_type, int src_width, int src_height, unsigned char* __restrict__ dst, int min_edge, int& dst_width, int& dst_height);
        
        /**
         * resize specified size
         * @param dst_width[input] specified resize result image width
         * @param dst_height[input] specified resize result image height
         */
        static void resize_specified_size(const unsigned char* __restrict__ src, MGHUM_TRANSFORM_IMAGE_TYPE image_type, int src_width, int src_height, int dst_width, int dst_height, unsigned char* __restrict__ dst);
        
        //  image combination transform
        static void resize_yuvnv212yuvnv12(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_yuvnv212fullsizeyuv(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_yuvnv212bgr(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);

        static void resize_yuvnv212rgba(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_yuvnv122fullsizeyuv(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_yuvnv122bgr(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_yuvnv122rgba(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);

        static void resize_bgr2yuvnv21(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_bgr2yuvnv12(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_bgr2rgba(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_rgba2yuvnv21(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_rgba2yuvnv12(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_rgba2bgr(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_fullsizeyuv2yuvnv12(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
        
        static void resize_fullsizeyuv2yuvnv21(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height);
    };
}

#endif /* bare_format_transform_h */
