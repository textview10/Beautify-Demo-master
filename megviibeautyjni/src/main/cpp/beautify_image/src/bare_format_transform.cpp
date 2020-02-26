//
//  bare_format_transform.cpp
//  AAAAA
//
//  Created by LiangJuzi on 2018/5/7.
//  Copyright © 2018年 Megvii. All rights reserved.
//

#include <bare_format_transform.h>
#include <cstring>

using namespace mghum;

static long int crv_tab[256];
static long int cbu_tab[256];
static long int cgu_tab[256];
static long int cgv_tab[256];
static long int tab_76309[256];
static unsigned char clp[1024];

int format_max(int width, int height) {
    if (width > height) {
        return width;
    }
    return height;
}

int format_min(int width, int height) {
    if (width > height) {
        return height;
    }
    return width;
}

int format_bound(int min, int value, int max) {
    if (value < min) {
        return min;
    } else if (value > max) {
        return max;
    }
    return value;
}

//  BGR2Other
void BaseFormatTransform::bgr2rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    for (int i = 0; i < image_width * image_height; i++) {
        dst[i * 3 + 0] = src[i * 3 + 2];
        dst[i * 3 + 1] = src[i * 3 + 1];
        dst[i * 3 + 2] = src[i * 3 + 0];
    }
}

void BaseFormatTransform::bgr2rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    for (int i = 0; i < image_width * image_height; i++) {
        dst[i * 4 + 0] = src[i * 3 + 2];
        dst[i * 4 + 1] = src[i * 3 + 1];
        dst[i * 4 + 2] = src[i * 3 + 0];
        dst[i * 4 + 3] = 255;
    }
}

void bgr2yuv420p(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height, bool isNV12) {
    int y, u, v;
    int yuv_size = image_width * image_height;
    for (int i = 0; i < image_height; i++) {
        for (int j = 0; j < image_width; j++) {
            int B = src[(i * image_width + j) * 3 + 0];
            int G = src[(i * image_width + j) * 3 + 1];
            int R = src[(i * image_width + j) * 3 + 2];
            
            y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
            u = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
            v = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
            y = y < 16 ? 16 : (y > 255 ? 255 : y);
            u = u < 0 ? 0 : (u > 255 ? 255 : u);
            v = v < 0 ? 0 : (v > 255 ? 255 : v);
            
            dst[i * image_width + j] = y;
            dst[yuv_size + (i >> 1) * image_width + (j & ~1) + 0] = isNV12 ? u : v;
            dst[yuv_size + (i >> 1) * image_width + (j & ~1) + 1] = isNV12 ? v : u;
        }
    }
}

void BaseFormatTransform::bgr2yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    bgr2yuv420p(src, dst, image_width, image_height, false);
}

void BaseFormatTransform::bgr2yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    bgr2yuv420p(src, dst, image_width, image_height, true);
}

void BaseFormatTransform::bgr2fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    int y, u, v;
    int dst_width = image_width / 2;
    int dst_height = image_height / 2;
    for (int i = 0; i < dst_height; i++) {
        for (int j = 0; j < dst_width; j++) {
            int B = src[((i * 2) * image_width + (j * 2)) * 3 + 0];
            int G = src[((i * 2) * image_width + (j * 2)) * 3 + 1];
            int R = src[((i * 2) * image_width + (j * 2)) * 3 + 2];
            
            y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
            u = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
            v = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
            y = y < 16 ? 16 : (y > 255 ? 255 : y);
            u = u < 0 ? 0 : (u > 255 ? 255 : u);
            v = v < 0 ? 0 : (v > 255 ? 255 : v);
            
            dst[(i * dst_width + j) * 3 + 0] = y;
            dst[(i * dst_width + j) * 3 + 1] = u;
            dst[(i * dst_width + j) * 3 + 2] = v;
        }
    }
}

//  RGB2Other
void BaseFormatTransform::rgb2bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    bgr2rgb(src, dst, image_width, image_height);
}
void BaseFormatTransform::rgb2rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    for (int i = 0; i < image_width * image_height; i++) {
        dst[i * 4 + 0] = src[i * 3 + 0];
        dst[i * 4 + 1] = src[i * 3 + 1];
        dst[i * 4 + 2] = src[i * 3 + 2];
        dst[i * 4 + 3] = 255;
    }
}

void rgb2yuv420p(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height, bool isNV12) {
    int y = 0, u = 0, v = 0;
    int yuv_size = image_width * image_height;
    for (int i = 0; i < image_height; i++) {
        for (int j = 0; j < image_width; j++) {
            int R = src[(i * image_width + j) * 3 + 0];
            int G = src[(i * image_width + j) * 3 + 1];
            int B = src[(i * image_width + j) * 3 + 2];
            
            y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
            u = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
            v = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
            y = y < 16 ? 16 : (y > 255 ? 255 : y);
            u = u < 0 ? 0 : (u > 255 ? 255 : u);
            v = v < 0 ? 0 : (v > 255 ? 255 : v);
            
            dst[i * image_width + j] = y;
            dst[yuv_size + (i >> 1) * image_width + (j & ~1) + 0] = isNV12 ? u : v;
            dst[yuv_size + (i >> 1) * image_width + (j & ~1) + 1] = isNV12 ? v : u;
        }
    }
}

void BaseFormatTransform::rgb2yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    rgb2yuv420p(src, dst, image_width, image_height, false);
}

void BaseFormatTransform::rgb2yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    rgb2yuv420p(src, dst, image_width, image_height, true);
}

void BaseFormatTransform::rgb2fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    int y, u, v;
    int dst_width = image_width / 2;
    int dst_height = image_height / 2;
    for (int i = 0; i < dst_height; i++) {
        for (int j = 0; j < dst_width; j++) {
            int R = src[((i * 2) * image_width + (j * 2)) * 3 + 0];
            int G = src[((i * 2) * image_width + (j * 2)) * 3 + 1];
            int B = src[((i * 2) * image_width + (j * 2)) * 3 + 2];
            
            y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
            u = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
            v = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
            y = y < 16 ? 16 : (y > 255 ? 255 : y);
            u = u < 0 ? 0 : (u > 255 ? 255 : u);
            v = v < 0 ? 0 : (v > 255 ? 255 : v);
            
            dst[(i * dst_width + j) * 3 + 0] = y;
            dst[(i * dst_width + j) * 3 + 1] = u;
            dst[(i * dst_width + j) * 3 + 2] = v;
        }
    }
}

//  RGBA2Other
void BaseFormatTransform::rgba2rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    for (int i = 0; i < image_width * image_height; i++) {
        dst[i * 3 + 0] = src[i * 4 + 0];
        dst[i * 3 + 1] = src[i * 4 + 1];
        dst[i * 3 + 2] = src[i * 4 + 2];
    }
}

void BaseFormatTransform::rgba2bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    for (int i = 0; i < image_width * image_height; i++) {
        dst[i * 3 + 0] = src[i * 4 + 2];
        dst[i * 3 + 1] = src[i * 4 + 1];
        dst[i * 3 + 2] = src[i * 4 + 0];
    }
}

void rgba2yuv420p(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height, bool isNV12) {
    int y = 0, u = 0, v = 0;
    int yuv_size = image_width * image_height;
    for (int i = 0; i < image_height; i++) {
        for (int j = 0; j < image_width; j++) {
            int R = src[(i * image_width + j) * 4 + 0];
            int G = src[(i * image_width + j) * 4 + 1];
            int B = src[(i * image_width + j) * 4 + 2];
            
            y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
            u = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
            v = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
            y = y < 16 ? 16 : (y > 255 ? 255 : y);
            u = u < 0 ? 0 : (u > 255 ? 255 : u);
            v = v < 0 ? 0 : (v > 255 ? 255 : v);
            
            dst[i * image_width + j] = y;
            dst[yuv_size + (i >> 1) * image_width + (j & ~1) + 0] = isNV12 ? u : v;
            dst[yuv_size + (i >> 1) * image_width + (j & ~1) + 1] = isNV12 ? v : u;
        }
    }
}

void BaseFormatTransform::rgba2yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    rgba2yuv420p(src, dst, image_width, image_height, false);
}

void BaseFormatTransform::rgba2yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    rgba2yuv420p(src, dst, image_width, image_height, true);
}

void BaseFormatTransform::rgba2fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    int y, u, v;
    int dst_width = image_width / 2;
    int dst_height = image_height / 2;
    for (int i = 0; i < dst_height; i++) {
        for (int j = 0; j < dst_width; j++) {
            int R = src[((i * 2) * image_width + (j * 2)) * 4 + 0];
            int G = src[((i * 2) * image_width + (j * 2)) * 4 + 1];
            int B = src[((i * 2) * image_width + (j * 2)) * 4 + 2];
            
            y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
            u = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
            v = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
            y = y < 16 ? 16 : (y > 255 ? 255 : y);
            u = u < 0 ? 0 : (u > 255 ? 255 : u);
            v = v < 0 ? 0 : (v > 255 ? 255 : v);
            
            dst[(i * dst_width + j) * 3 + 0] = y;
            dst[(i * dst_width + j) * 3 + 1] = u;
            dst[(i * dst_width + j) * 3 + 2] = v;
        }
    }
}

//  YUVNV212Other
void base_format_init_yuv420p_table() {
    long int crv = 104597, cbu = 132201, cgu = 25675, cgv = 53279;
    int i, ind;
    static int init = 0;

    if (init == 1) return;

    for (i = 0; i < 256; i++) {
        crv_tab[i] = (i - 128) * crv;
        cbu_tab[i] = (i - 128) * cbu;
        cgu_tab[i] = (i - 128) * cgu;
        cgv_tab[i] = (i - 128) * cgv;
        tab_76309[i] = 76309 * (i - 16);
    }

    for (i = 0; i < 384; i++) {
        clp[i] = 0;
    }
    ind = 384;
    for (i = 0;i < 256; i++) {
        clp[ind++] = i;
    }
    ind = 640;
    for (i = 0;i < 384; i++) {
        clp[ind++] = 255;
    }
    init = 1;
}

void yuv420p2RGB888(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height, bool isNV12, bool isRGB) {
    int y1, y2, u, v;
    unsigned char *py1, *py2;
    int i, j, c1, c2, c3, c4;
    unsigned char *d1, *d2;
    unsigned char *src_u;
    
    src_u = (unsigned char *)src + image_width * image_height;   // u
    
    py1 = (unsigned char *)src;   // y
    py2 = py1 + image_width;
    d1 = dst;
    d2 = d1 + 3 * image_width;
    
    base_format_init_yuv420p_table();
    
    for (j = 0; j < image_height; j += 2) {
        for (i = 0; i < image_width; i += 2) {
            if (isNV12) {
                u = *src_u++;
                v = *src_u++;      //  nv12 v紧跟u，在v的下一个位置
            } else {
                v = *src_u++;
                u = *src_u++;      //  nv21 u紧跟v，在v的下一个位置
            }

            c1 = crv_tab[v];
            c2 = cgu_tab[u];
            c3 = cgv_tab[v];
            c4 = cbu_tab[u];
            
            c1 = isRGB ? cbu_tab[u] : crv_tab[v];
            c2 = cgu_tab[u];
            c3 = cgv_tab[v];
            c4 = isRGB ? crv_tab[v] : cbu_tab[u];
            
            //  up-left
            y1 = tab_76309[*py1++];
            *d1++ = clp[384 + ((y1 + c4) >> 16)];
            *d1++ = clp[384 + ((y1 - c2 - c3) >> 16)];
            *d1++ = clp[384 + ((y1 + c1) >> 16)];
            
            //  down-left
            y2 = tab_76309[*py2++];
            *d2++ = clp[384 + ((y2 + c4) >> 16)];
            *d2++ = clp[384 + ((y2 - c2 - c3) >> 16)];
            *d2++ = clp[384 + ((y2 + c1) >> 16)];
            
            //  up-right
            y1 = tab_76309[*py1++];
            *d1++ = clp[384 + ((y1 + c4) >> 16)];
            *d1++ = clp[384 + ((y1 - c2 - c3) >> 16)];
            *d1++ = clp[384 + ((y1 + c1) >> 16)];
            
            
            //  down-right
            y2 = tab_76309[*py2++];
            *d2++ = clp[384 + ((y2 + c4) >> 16)];
            *d2++ = clp[384 + ((y2 - c2 - c3) >> 16)];
            *d2++ = clp[384 + ((y2 + c1) >> 16)];
            
        }
        d1  += 3 * image_width;
        d2  += 3 * image_width;
        py1 += image_width;
        py2 += image_width;
    }
}

void yuv420p2RGBA(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height, bool isNV12) {
    int y1, y2, u, v;
    unsigned char *py1, *py2;
    int i, j, c1, c2, c3, c4;
    unsigned char *d1, *d2;
    unsigned char *src_u;
    
    src_u = (unsigned char *)src + image_width * image_height;   // u
    
    py1 = (unsigned char *)src;   // y
    py2 = py1 + image_width;
    d1 = dst;
    d2 = d1 + 4 * image_width;
    
    base_format_init_yuv420p_table();
    
    for (j = 0; j < image_height; j += 2) {
        for (i = 0; i < image_width; i += 2) {
            if (isNV12) {
                u = *src_u++;
                v = *src_u++;      //  nv12 v紧跟u，在v的下一个位置
            } else {
                v = *src_u++;
                u = *src_u++;      //  nv21 u紧跟v，在v的下一个位置
            }
            
            c1 = crv_tab[v];
            c2 = cgu_tab[u];
            c3 = cgv_tab[v];
            c4 = cbu_tab[u];
            
            c1 = cbu_tab[u];
            c2 = cgu_tab[u];
            c3 = cgv_tab[v];
            c4 = crv_tab[v];
            
            //  up-left
            y1 = tab_76309[*py1++];
            *d1++ = clp[384 + ((y1 + c4) >> 16)];
            *d1++ = clp[384 + ((y1 - c2 - c3) >> 16)];
            *d1++ = clp[384 + ((y1 + c1) >> 16)];
            *d1++ = 255;
            
            //  down-left
            y2 = tab_76309[*py2++];
            *d2++ = clp[384 + ((y2 + c4) >> 16)];
            *d2++ = clp[384 + ((y2 - c2 - c3) >> 16)];
            *d2++ = clp[384 + ((y2 + c1) >> 16)];
            *d2++ = 255;
            
            //  up-right
            y1 = tab_76309[*py1++];
            *d1++ = clp[384 + ((y1 + c4) >> 16)];
            *d1++ = clp[384 + ((y1 - c2 - c3) >> 16)];
            *d1++ = clp[384 + ((y1 + c1) >> 16)];
            *d1++ = 255;
            
            //  down-right
            y2 = tab_76309[*py2++];
            *d2++ = clp[384 + ((y2 + c4) >> 16)];
            *d2++ = clp[384 + ((y2 - c2 - c3) >> 16)];
            *d2++ = clp[384 + ((y2 + c1) >> 16)];
            *d2++ = 255;
        }
        d1  += 4 * image_width;
        d2  += 4 * image_width;
        py1 += image_width;
        py2 += image_width;
    }
}

void BaseFormatTransform::yuvnv212bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    yuv420p2RGB888(src, dst, image_width, image_height, false, false);
}

void BaseFormatTransform::yuvnv212rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    yuv420p2RGB888(src, dst, image_width, image_height, false, true);
}
void BaseFormatTransform::yuvnv212rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    yuv420p2RGBA(src, dst, image_width, image_height, false);
}
void BaseFormatTransform::yuvnv212yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    for (int i = 0; i < image_height * image_width; i++) {
        dst[i] = src[i];
    }
    for (int i = image_height * image_width; i < image_height * 3 / 2 * image_width; i += 2) {
        dst[i] = src[i + 1];
        dst[i + 1] = src[i];
    }
}

void yuv420p2fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height, bool isNV12) {
    int dst_width = image_width / 2;
    int dst_height = image_height / 2;
    int y, u, v;
    int yuv_size = image_width * image_height;
    for (int i = 0; i < dst_height; i++) {
        for (int j = 0; j < dst_width; j++) {
            int y0 = src[i * 2 * image_width + j * 2];
            int y1 = src[i * 2 * image_width + j * 2 + 1];
            int y2 = src[(i * 2 + 1) * image_width + j * 2];
            int y3 = src[(i * 2 + 1) * image_width + j * 2 + 1];
            y = (y0 + y1 + y2 + y3) / 4;
            if (isNV12) {
                v = src[yuv_size + i * image_width + j * 2];
                u = src[yuv_size + i * image_width + j * 2 + 1];
            } else {
                u = src[yuv_size + i * image_width + j * 2];
                v = src[yuv_size + i * image_width + j * 2 + 1];
            }
            
            dst[(i * dst_width + j) * 3 + 0] = y;
            dst[(i * dst_width + j) * 3 + 1] = v;
            dst[(i * dst_width + j) * 3 + 2] = u;
        }
        dst[i] = src[i];
    }
}

void BaseFormatTransform::yuvnv212fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    yuv420p2fullsizeyuv(src, dst, image_width, image_height, false);
}

//  YUVNV122Other
void BaseFormatTransform::yuvnv122bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    yuv420p2RGB888(src, dst, image_width, image_height, true, false);
}
void BaseFormatTransform::yuvnv122rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    yuv420p2RGB888(src, dst, image_width, image_height, true, true);
}
void BaseFormatTransform::yuvnv122rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    yuv420p2RGBA(src, dst, image_width, image_height, true);
}
void BaseFormatTransform::yuvnv122yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    yuvnv212yuvnv12(src, dst, image_width, image_height);
}

void BaseFormatTransform::yuvnv122fullsizeyuv(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int image_width, int image_height) {
    yuv420p2fullsizeyuv(src, dst, image_width, image_height, true);
}

void RGB888Color(unsigned char* __restrict__ dst, int index, int R, int G, int B, int image_channel, bool isRGB) {
    dst[index * image_channel + 0] = isRGB ? R : B;
    dst[index * image_channel + 1] = G;
    dst[index * image_channel + 2] = isRGB ? B : R;
    if (image_channel == 4) {
        dst[index * image_channel + 3] = 255;
    }
}

//  FULLSIZEYUV2Other
void fullsizeyuv2RGB888(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height, int image_channel, bool isRGB) {
    int image_width = src_width * 2;
    int image_height = src_height * 2;
    int y = 0, u = 0, v = 0;
    int r_dif = 0, g_dif = 0, b_dif = 0;
    int R = 0, G = 0, B = 0;
    for (int i = 0; i < src_height; i++) {
        for (int j = 0; j < src_width; j++) {
            y = src[(i * src_width + j) * 3 + 0];
            u = src[(i * src_width + j) * 3 + 1] - 128;
            v = src[(i * src_width + j) * 3 + 2] - 128;
            r_dif = v + ((v * 103) >> 8);
            g_dif = -((u * 88) >> 8) - ((v * 183) >> 8);
            b_dif = u +((u * 198) >> 8);
            
            R = y + r_dif;
            G = y + g_dif;
            B = y + b_dif;
            R = format_bound(0, R, 255);
            G = format_bound(0, G, 255);
            B = format_bound(0, B, 255);
            
            RGB888Color(dst, i * 2 * image_width + j * 2, R, G, B, image_channel, isRGB);
            RGB888Color(dst, i * 2 * image_width + j * 2 + 1, R, G, B, image_channel, isRGB);
            RGB888Color(dst, (i * 2 + 1) * image_width + j * 2, R, G, B, image_channel, isRGB);
            RGB888Color(dst, (i * 2 + 1) * image_width + j * 2 + 1, R, G, B, image_channel, isRGB);
        }
    }
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::fullsizeyuv2bgr(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height) {
    fullsizeyuv2RGB888(src, dst, src_width, src_height, dst_width, dst_height, 3, false);
}

void BaseFormatTransform::fullsizeyuv2rgb(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height) {
    fullsizeyuv2RGB888(src, dst, src_width, src_height, dst_width, dst_height, 3, true);
}

void BaseFormatTransform::fullsizeyuv2rgba(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height) {
    fullsizeyuv2RGB888(src, dst, src_width, src_height, dst_width, dst_height, 4, true);
}

void fullsizeyuv2yuv420p(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height, bool isNV12) {
    int image_width = src_width * 2;
    int image_height = src_height * 2;
    int y, u, v;
    int yuv420p_len = image_width * image_height;
    for (int i = 0; i < src_height; i++) {
        for (int j = 0; j < src_width; j++) {
            y = src[(i * src_width + j) * 3 + 0];
            u = src[(i * src_width + j) * 3 + 1];
            v = src[(i * src_width + j) * 3 + 2];
            dst[i * 2 * image_width + j * 2] = y;
            dst[(i * 2 + 1) * image_width + j * 2] = y;
            dst[i * 2 * image_width + j * 2 + 1] = y;
            dst[(i * 2 + 1) * image_width + j * 2 + 1] = y;
            
            dst[yuv420p_len + i * image_width + j * 2 + 0] = isNV12 ? u : v;
            dst[yuv420p_len + i * image_width + j * 2 + 1] = isNV12 ? v : u;
        }
    }
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::fullsizeyuv2yuvnv21(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height) {
    fullsizeyuv2yuv420p(src, dst, src_width, src_height, dst_width, dst_height, false);
}

void BaseFormatTransform::fullsizeyuv2yuvnv12(const unsigned char* __restrict__ src, unsigned char* __restrict__ dst, int src_width, int src_height, int& dst_width, int& dst_height) {
    fullsizeyuv2yuv420p(src, dst, src_width, src_height, dst_width, dst_height, true);
}

//  Rorate
int rorateOrientation(int orientation) {
    if (orientation % 90 != 0) {
        return -1;
    }
    return orientation % 360;
}

void rotateRGB888(const unsigned char* __restrict__ src, int src_width, int src_height, int src_channel, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height) {
    int direction = rorateOrientation(orientation);
    if (direction == -1) {
        return;
    }
    int i = 0, j = 0, k = 0, image_size = src_width * src_height - 1;
    int index = 0, channel = src_channel;
    switch (direction) {
        case 0: {
            for (i = 0; i <= image_size; i++) {
                for (index = 0; index < channel; index++) {
                    dst[i * channel + index] = src[i * channel + index];
                }
            }
            dst_width = src_width;
            dst_height = src_height;
        }
            break;
        case 90: {
            for (k = 0, j = src_height - 1; j >= 0; --j) {
                for (i = j; i <= image_size; i += src_height) {
                    for (index = 0; index < channel; index++) {
                        dst[i * channel + index] = src[k * channel + index];
                    }
                    k++;
                }
            }
            dst_width = src_height;
            dst_height = src_width;
        }
            break;
        case 180: {
            for (i = 0, j = image_size; i <= image_size; ++i, --j) {
                for (index = 0; index < channel; index++) {
                    dst[j * channel + index] = src[i * channel + index];
                }
            }
            dst_width = src_width;
            dst_height = src_height;
        }
            break;
        case 270: {
            for (k = 0, j = 0; j < src_height; ++j) {
                for (i = image_size - src_height + j; i >= 0; i -= src_height) {
                    for (index = 0; index < channel; index++) {
                        dst[i * channel + index] = src[k * channel + index];
                    }
                    k++;
                }
            }
            dst_width = src_height;
            dst_height = src_width;
        }
            break;
        default:
            break;
    }
}

/**
 * rotate with RGB or BGR.
 * @param orientation must be in multiples of 90.
 */
void BaseFormatTransform::rotate_rgb(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height) {
    rotateRGB888(src, src_width, src_height, 3, orientation, dst, dst_width, dst_height);
}

/**
 * rotate with RGBA or BGRA, ARGB, ABGR.
 * @param orientation must be in multiples of 90.
 */
void BaseFormatTransform::rotate_rgba(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height) {
    rotateRGB888(src, src_width, src_height, 4, orientation, dst, dst_width, dst_height);
}

/**
 * rotate with gray or depth.
 * @param orientation must be in multiples of 90.
 */
void BaseFormatTransform::rotate_singlechannel(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height) {
    rotateRGB888(src, src_width, src_height, 1, orientation, dst, dst_width, dst_height);
}

/**
 * rotate with FULLSIZEYUV.
 * @param orientation must be in multiples of 90.
 */
void BaseFormatTransform::rotate_fullsizeyuv(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height) {
    rotateRGB888(src, src_width, src_height, 3, orientation, dst, dst_width, dst_height);
}

/**
 * rotate with YUNNV21 or YUNNV12.
 * @param orientation must be in multiples of 90.
 */
void BaseFormatTransform::rotate_yuv420p(const unsigned char* __restrict__ src, int src_width, int src_height, int orientation, unsigned char* __restrict__ dst, int& dst_width, int& dst_height) {
    int direction = rorateOrientation(orientation);
    if (direction == -1) {
        return;
    }
    int i = 0, j = 0, k = 0, image_size = src_width * src_height - 1;
    switch (direction) {
        case 0: {
            for (i = 0; i <= image_size; i++) {
                dst[i] = src[i];
            }
            for (i = image_size + 1; i < src_width * src_height * 3 / 2; i++) {
                dst[i] = src[i];
            }
            dst_width = src_width;
            dst_height = src_height;
        }
            break;
        case 90: {
            for (k = 0, j = src_height - 1; j >= 0; --j) {
                for (i = j; i <= image_size; i += src_height) {
                    dst[i] = src[k++];
                }
            }
            for (k = image_size, j = src_height; j >= 0; j -= 2) {
                for (i = image_size + j; i < src_width * src_height * 3 / 2; i += src_height) {
                    dst[i] = src[k++];
                    dst[i + 1] = src[k++];
                }
            }
            dst_width = src_height;
            dst_height = src_width;
        }
            break;
        case 180: {
            for (i = 0, j = image_size; i <= image_size; ++i, --j) {
                dst[j] = src[i];
            }
            for (i = image_size + 1, j = src_width * src_height * 3 / 2 - 2; i <= src_width * src_height * 3 / 2 - 1; i += 2, j -= 2) {
                dst[j] = src[i];
                dst[j + 1] = src[i + 1];
            }
            dst_width = src_width;
            dst_height = src_height;
        }
            break;
        case 270: {
            for (k = 0, j = 0; j < src_height; ++j) {
                for (i = image_size - src_height + j; i >= 0; i -= src_height) {
                    dst[i] = src[k++];
                }
            }
            for (k = image_size, j = 0; j < src_height; j += 2) {
                for (i = src_width * src_height * 3 / 2 - src_height - 1 + j; i >= image_size; i -= src_height) {
                    dst[i] = src[k++];
                    dst[i + 1] = src[k++];
                }
            }
            dst_width = src_height;
            dst_height = src_width;
        }
            break;
        default:
            break;
    }
}

void BaseFormatTransform::resize_scale(const unsigned char* __restrict__ src, MGHUM_TRANSFORM_IMAGE_TYPE image_type, int src_width, int src_height,
                                       float scale, unsigned char* __restrict__ dst, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int resize_width = src_width / scale;
    int resize_height = src_height / scale;
    dst_width = resize_width;
    dst_height = resize_height;
    resize_specified_size(src, image_type, src_width, src_height, resize_width, resize_height, dst);
}

void BaseFormatTransform::resize_maxedge(const unsigned char* __restrict__ src, MGHUM_TRANSFORM_IMAGE_TYPE image_type, int src_width, int src_height, unsigned char* __restrict__ dst, int max_edge, int& dst_width, int& dst_height) {
    if (max_edge <= 0 || max_edge > format_max(src_width, src_height)) {
        return;
    }
    float scale = 1.0f * format_max(src_width, src_height) / max_edge;
    int resize_width = src_width / scale;
    int resize_height = src_height / scale;
    dst_width = resize_width;
    dst_height = resize_height;
    resize_specified_size(src, image_type, src_width, src_height, resize_width, resize_height, dst);
}

void BaseFormatTransform::resize_minedge(const unsigned char* __restrict__ src, MGHUM_TRANSFORM_IMAGE_TYPE image_type, int src_width, int src_height, unsigned char* __restrict__ dst, int min_edge, int& dst_width, int& dst_height) {
    if (min_edge <= 0 || min_edge > format_min(src_width, src_height)) {
        return;
    }
    float scale = 1.0f * format_min(src_width, src_height) / min_edge;
    int resize_width = src_width / scale;
    int resize_height = src_height / scale;
    dst_width = resize_width;
    dst_height = resize_height;
    resize_specified_size(src, image_type, src_width, src_height, resize_width, resize_height, dst);
}

void resize_scaleline(const unsigned char* __restrict__ src, int image_channel, int src_width, int dst_width, unsigned char* __restrict__ dst, bool isChannelswap) {
    int number_pixels = dst_width;
    int int_part = src_width / dst_width;
    int fract_part = src_width % dst_width;
    int index = 0;
    
    while (number_pixels-- > 0) {
        for (int i = 0; i < image_channel; i++) {
            dst[i] = isChannelswap ? src[image_channel - 1 - i] : src[i];
        }
        dst += image_channel;
        src += int_part * image_channel;
        index += fract_part;
        if (index >= dst_width) {
            index -= dst_width;
            src += image_channel;
        }
    }
}

void resize_specified_RGB888(const unsigned char* __restrict__ src, int image_channel, int src_width, int src_height, int dst_width, int dst_height, unsigned char* __restrict__ dst, bool isChannelswap) {
    int number_pixels = dst_height;
    int int_part = (src_height / dst_height) * src_width;
    int fract_part = src_height % dst_height;
    int index = 0;
    while (number_pixels-- > 0) {
        resize_scaleline(src, image_channel, src_width, dst_width, dst, isChannelswap);
        dst += dst_width * image_channel;
        src += int_part * image_channel;
        index += fract_part;
        if (index >= dst_height) {
            index -= dst_height;
            src += src_width * image_channel;
        }
    }
}

void resize_specified_YUV420P(const unsigned char* __restrict__ src, int src_width, int src_height, int dst_width, int dst_height, unsigned char* __restrict__ dst, bool uvswap) {
    const unsigned char* __restrict__ uv_src = src + src_width * src_height;
    unsigned char* __restrict__ uv_dst = dst + dst_width * dst_height;
    
    int y_channel = 1;
    resize_specified_RGB888(src, y_channel, src_width, src_height, dst_width, dst_height, dst, uvswap);
    
    int uv_src_width = src_width;
    int uv_src_height = src_height / 2;
    int uv_dst_width = dst_width;
    int uv_dst_height = dst_height / 2;
    int uv_channel = 2;
    resize_specified_RGB888(uv_src, uv_channel, uv_src_width, uv_src_height, uv_dst_width, uv_dst_height, uv_dst, uvswap);
}

void BaseFormatTransform::resize_specified_size(const unsigned char* __restrict__ src, MGHUM_TRANSFORM_IMAGE_TYPE image_type, int src_width, int src_height, int dst_width, int dst_height, unsigned char* __restrict__ dst) {
    if (image_type == MGHUM_TRANSFORM_IMAGE_TYPE_YUVNV12 ||
        image_type == MGHUM_TRANSFORM_IMAGE_TYPE_YUVNV21) {
        resize_specified_YUV420P(src, src_width, src_height, dst_width, dst_height, dst, false);
    } else {
        int image_channel = 0;
        switch (image_type) {
            case MGHUM_TRANSFORM_IMAGE_TYPE_GRAY:
                image_channel = 1;
                break;
            case MGHUM_TRANSFORM_IMAGE_TYPE_BGR:
            case MGHUM_TRANSFORM_IMAGE_TYPE_RGB:
            case MGHUM_TRANSFORM_IMAGE_TYPE_FULLSIZEYUVNV:
                image_channel = 3;
                break;
            case MGHUM_TRANSFORM_IMAGE_TYPE_RGBA:
                image_channel = 4;
                break;
            default:
                break;
        }
        resize_specified_RGB888(src, image_channel, src_width, src_height, dst_width, dst_height, dst, false);
    }
}

//  image combination transform
void BaseFormatTransform::resize_yuvnv212yuvnv12(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    resize_specified_YUV420P(src, src_width, src_height, image_width, image_height, dst, true);
    dst_width = image_width;
    dst_height = image_height;
}

//  Error code with yuv420p2fullsizeyuv 1
void resize_scaleline_yuv420p2fullsizeyuv(const unsigned char* __restrict__ y_src, const unsigned char* __restrict__ uv_src, int src_width, int dst_width, unsigned char* __restrict__ dst, bool isNV12) {
    int number_pixels = dst_width;
    int int_part = src_width / dst_width;
    int fract_part = src_width % dst_width;
    int index = 0;
    int image_channel = 3;
    int y_channel = 1;
    int uv_channel = 2;
    int y = 0, u = 0, v = 0;
    while (number_pixels-- > 0) {
        y = y_src[0];
        if (isNV12) {
            u = uv_src[0];
            v = uv_src[1];
        } else {
            v = uv_src[0];
            u = uv_src[1];
        }
        
        RGB888Color(dst, 0, y, u, v, image_channel, true);
        
        dst += image_channel;
        y_src += int_part * y_channel;
        if (number_pixels % 2 == 0) {
            uv_src += int_part * uv_channel;
        }
        index += fract_part;
        if (index >= dst_width) {
            index -= dst_width;
            y_src += y_channel;
            if (number_pixels % 2 == 0) {
                uv_src += uv_channel;
            }
        }
    }
    
//    int number_pixels = dst_width;
//    int int_part = src_width / dst_width;
//    int fract_part = src_width % dst_width;
//    int index = 0;
//    int image_channel = 3;
//    int y_channel = 1;
//    int uv_channel = 1;
//    int y = 0, u = 0, v = 0;
//    while (number_pixels-- > 0) {
//        int y0 = y_src[0];
//        int y1 = y_src[1];
//        int y2 = y_src[src_width + 0];
//        int y3 = y_src[src_width + 1];
//        y = (y0 + y1 + y2 + y3) / 4;
//        if (isNV12) {
//            u = uv_src[0];
//            v = uv_src[1];
//        } else {
//            v = uv_src[0];
//            u = uv_src[1];
//        }
//        dst[0] = y;
//        dst[1] = u;
//        dst[2] = v;
//        dst += image_channel;
//        y_src += int_part * y_channel;
//        uv_src += int_part * uv_channel;
//        index += fract_part;
//        if (index >= dst_width) {
//            index -= dst_width;
//            y_src += y_channel;
//            uv_src += uv_channel;
//        }
//    }
}

void resize_specified_yuv420p2fullsizeyuv(const unsigned char* __restrict__ src, int src_width, int src_height, int dst_width, int dst_height, unsigned char* __restrict__ dst, bool isNV12) {
    const unsigned char* __restrict__ y_src = src;
    const unsigned char* __restrict__ uv_src = src + src_width * src_height;
    int number_pixels = dst_height;
    int int_part = (src_height / dst_height) * src_width;
    int fract_part = src_height % dst_height;
    int index = 0;
    int image_channel = 3;
    int yuv_channel = 1;
    while (number_pixels-- > 0) {
        resize_scaleline_yuv420p2fullsizeyuv(y_src, uv_src, src_width, dst_width, dst, isNV12);
        dst += dst_width * image_channel;
        y_src += int_part * yuv_channel;
        if (number_pixels % 2 == 0) {
            uv_src += int_part * yuv_channel;
        }
        index += fract_part;
        if (index >= dst_height) {
            index -= dst_height;
            y_src += src_width * yuv_channel;
            if (number_pixels % 2 == 0) {
                uv_src += src_width * yuv_channel;
            }
        }
    }

    
//    const unsigned char* __restrict__ y_src = src;
//    const unsigned char* __restrict__ uv_src = src + src_width * src_height;
//    int number_pixels = dst_height;
//    int int_part = (src_height / dst_width) * src_width;
//    int fract_part = src_height % dst_height;
//    int index = 0;
//    int image_channel = 3;
//    int yuv_channel = 1;
//    while (number_pixels-- > 0) {
//        resize_scaleline_yuv420p2fullsizeyuv(y_src, uv_src, src_width, dst_width, dst, isNV12);
//        dst += dst_width * image_channel;
//        y_src += int_part * yuv_channel;
//        uv_src += int_part * yuv_channel;
//        index += fract_part;
//        if (index >= dst_height) {
//            index -= dst_height;
//            y_src += src_width + src_width * yuv_channel;
//            uv_src += src_width * yuv_channel;
//        }
//    }
}

void resize_scaleline_yuv420p2RGB888(const unsigned char* __restrict__ y_src, const unsigned char* __restrict__ uv_src, int src_width, int dst_width, int dst_channel, unsigned char* __restrict__ dst, bool isNV12, bool isRGB) {
    int number_pixels = dst_width;
    int int_part = src_width / dst_width;
    int fract_part = src_width % dst_width;
    int index = 0;
    int image_channel = dst_channel;
    int y_channel = 1;
    int uv_channel = 2;
    int y = 0, u = 0, v = 0;
    int r_dif = 0, g_dif = 0, b_dif = 0;
    int R = 0, G = 0, B = 0;
    while (number_pixels-- > 0) {
        y = y_src[0];
        if (isNV12) {
            u = uv_src[0] - 128;
            v = uv_src[1] - 128;
        } else {
            v = uv_src[0] - 128;
            u = uv_src[1] - 128;
        }
        r_dif = v + ((v * 103) >> 8);
        g_dif = -((u * 88) >> 8) - ((v * 183) >> 8);
        b_dif = u +((u * 198) >> 8);
        R = y + r_dif;
        G = y + g_dif;
        B = y + b_dif;
        R = format_bound(0, R, 255);
        G = format_bound(0, G, 255);
        B = format_bound(0, B, 255);
        RGB888Color(dst, 0, R, G, B, image_channel, isRGB);
        
        dst += image_channel;
        y_src += int_part * y_channel;
        if (number_pixels % 2 == 0) {
            uv_src += int_part * uv_channel;
        }
        index += fract_part;
        if (index >= dst_width) {
            index -= dst_width;
            y_src += y_channel;
            if (number_pixels % 2 == 0) {
                uv_src += uv_channel;
            }
        }
    }
}

void resize_specified_yuv420p2RGB888(const unsigned char* __restrict__ src, int src_width, int src_height, int dst_width, int dst_height, int dst_channel, unsigned char* __restrict__ dst, bool isNV12, bool isRGB) {
    const unsigned char* __restrict__ y_src = src;
    const unsigned char* __restrict__ uv_src = src + src_width * src_height;
    int number_pixels = dst_height;
    int int_part = (src_height / dst_height) * src_width;
    int fract_part = src_height % dst_height;
    int index = 0;
    int image_channel = dst_channel;
    int yuv_channel = 1;
    while (number_pixels-- > 0) {
        resize_scaleline_yuv420p2RGB888(y_src, uv_src, src_width, dst_width, dst_channel, dst, isNV12, isRGB);
        dst += dst_width * image_channel;
        y_src += int_part * yuv_channel;
        if (number_pixels % 2 == 0) {
            uv_src += int_part * yuv_channel;
        }
        index += fract_part;
        if (index >= dst_height) {
            index -= dst_height;
            y_src += src_width * yuv_channel;
            if (number_pixels % 2 == 0) {
                uv_src += src_width * yuv_channel;
            }
        }
    }
}

//  Error code with yuv420p2fullsizeyuv 2
void resize_scalelineyuvnv212fullsizeyuv_y(const unsigned char* __restrict__ src, int src_width, int dst_width, unsigned char* __restrict__ dst) {
    int number_pixels = dst_width;
    int int_part = src_width / dst_width;
    int fract_part = src_width % dst_width;
    int index = 0;
    while (number_pixels-- > 0) {
        dst[0] = src[0];
        dst += 3;
        src += int_part * 1;
        index += fract_part;
        if (index >= dst_width) {
            index -= dst_width;
            src += 1;
        }
    }
}

void resize_specified_yuvnv212fullsizeyuv_y(const unsigned char* __restrict__ src, int src_width, int src_height, int dst_width, int dst_height, unsigned char* __restrict__ dst) {
    int number_pixels = dst_height;
    int int_part = (src_height / dst_height) * src_width;
    int fract_part = src_height % dst_height;
    int index = 0;
    while (number_pixels-- > 0) {
        resize_scalelineyuvnv212fullsizeyuv_y(src, src_width, dst_width, dst);
        dst += dst_width * 3;
        src += int_part * 1;
        index += fract_part;
        if (index >= dst_height) {
            index -= dst_height;
            src += src_width * 1;
        }
    }
}

void resize_scaleline_yuvnv212fullsizeyuv_uv(const unsigned char* __restrict__ src, int src_width, int dst_width, unsigned char* __restrict__ dst, bool isNV12) {
    int number_pixels = dst_width;
    int int_part = src_width / dst_width;
    int fract_part = src_width % dst_width;
    int index = 0;
    int image_channel = 2;
    while (number_pixels-- > 0) {
        for (int i = 0; i < image_channel; i++) {
            dst[1 + i] = isNV12 ? src[image_channel - 1 - i] : src[i];
        }
        dst += 3;
        src += int_part * image_channel;
        index += fract_part;
        if (index >= dst_width) {
            index -= dst_width;
            src += image_channel;
        }
    }
}

void resize_specified_yuvnv212fullsizeyuv_uv(const unsigned char* __restrict__ src, int src_width, int src_height, int dst_width, int dst_height, unsigned char* __restrict__ dst, bool isNV12) {
    int number_pixels = dst_height;
    int int_part = (src_height / dst_height) * src_width;
    int fract_part = src_height % dst_height;
    int index = 0;
    while (number_pixels-- > 0) {
        resize_scaleline_yuvnv212fullsizeyuv_uv(src, src_width, dst_width, dst, isNV12);
        dst += dst_width * 3;
        src += int_part * 2;
        index += fract_part;
        if (index >= dst_height) {
            index -= dst_height;
            src += src_width * 2;
        }
    }
}

void BaseFormatTransform::resize_yuvnv212fullsizeyuv(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale / 2;
    int image_height = src_height / scale / 2;
    
    resize_specified_yuv420p2fullsizeyuv(src, src_width, src_height, image_width, image_height, dst, false);
    
    dst_width = image_width;
    dst_height = image_height;
//    if (scale <= 0 || scale > format_min(src_width, src_height)) {
//        return;
//    }
//    const unsigned char* __restrict__ uv_src = src + src_width * src_height;
//    int image_width = src_width / scale / 2;
//    int image_height = src_height / scale / 2;
//    resize_specified_yuvnv212fullsizeyuv_y(src, src_width, src_height, image_width, image_height, dst);
//    int uv_src_width = src_width;
//    int uv_src_height = src_height / 2;
//    int uv_dst_width = src_width / scale;
//    int uv_dst_height = src_height / scale;
//    resize_specified_yuvnv212fullsizeyuv_uv(uv_src, uv_src_width, uv_src_height, uv_dst_width, uv_dst_height, dst, false);
//    dst_width = image_width;
//    dst_height = image_height;
}

void BaseFormatTransform::resize_yuvnv212bgr(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    
    resize_specified_yuv420p2RGB888(src, src_width, src_height, image_width, image_height, 3, dst, false, false);
    
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::resize_yuvnv212rgba(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    
    resize_specified_yuv420p2RGB888(src, src_width, src_height, image_width, image_height, 4, dst, false, true);
    
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::resize_yuvnv122fullsizeyuv(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale / 2;
    int image_height = src_height / scale / 2;
    
    resize_specified_yuv420p2fullsizeyuv(src, src_width, src_height, image_width, image_height, dst, true);
    
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::resize_yuvnv122bgr(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    
    resize_specified_yuv420p2RGB888(src, src_width, src_height, image_width, image_height, 3, dst, true, false);
    
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::resize_yuvnv122rgba(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    
    resize_specified_yuv420p2RGB888(src, src_width, src_height, image_width, image_height, 4, dst, true, true);
    
    dst_width = image_width;
    dst_height = image_height;
}

void resize_scaleline_RGB8882yuv420p(const unsigned char* __restrict__ src, int src_width, int image_channel, bool isRGB, int dst_width, unsigned char* __restrict__ dst, unsigned char* __restrict__ uv_dst, bool isNV12) {
    int number_pixels = dst_width;
    int int_part = src_width / dst_width;
    int fract_part = src_width % dst_width;
    int index = 0;
    int y_channel = 1;
    int uv_channel = 2;
    int R = 0, G = 0, B = 0;
    int y = 0, u = 0, v = 0;
    while (number_pixels-- > 0) {
        R = isRGB ? src[0] : src[2];
        G = src[1];
        B = isRGB ? src[2] : src[0];
        y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
        y = y < 16 ? 16 : (y > 255 ? 255 : y);
        dst[0] = y;
        if (number_pixels % 2 == 1) {
            u = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
            v = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;
            u = u < 0 ? 0 : (u > 255 ? 255 : u);
            v = v < 0 ? 0 : (v > 255 ? 255 : v);
            uv_dst[0] = isNV12 ? u : v;
            uv_dst[1] = isNV12 ? v : u;
            uv_dst += uv_channel;
        }
        dst += y_channel;
        src += int_part * image_channel;
        index += fract_part;
        if (index >= dst_width) {
            index -= dst_width;
            src += image_channel;
        }
    }
}

void resize_RGB8882yuv420p(const unsigned char* __restrict__ src, int src_width, int src_height, int image_channel, bool isRGB, int dst_width, int dst_height, unsigned char* __restrict__ dst, bool isNV12) {
    int y_channel = 1;
    unsigned char* __restrict__ uv_dst = dst + dst_width * dst_height;
    
    int number_pixels = dst_height;
    int int_part = (src_height / dst_height) * src_width;
    int fract_part = src_height % dst_height;
    int index = 0;
    while (number_pixels-- > 0) {
        resize_scaleline_RGB8882yuv420p(src, src_width, image_channel, isRGB, dst_width, dst, uv_dst, isNV12);
        if (number_pixels % 2 == 1) {
            uv_dst += dst_width;
        }
        dst += dst_width * y_channel;
        src += int_part * image_channel;
        index += fract_part;
        if (index >= dst_height) {
            index -= dst_height;
            src += src_width * image_channel;
        }
    }
}

void BaseFormatTransform::resize_bgr2yuvnv21(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    int image_channel = 3;
    resize_RGB8882yuv420p(src, src_width, src_height, image_channel, false, image_width, image_height, dst, false);
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::resize_bgr2yuvnv12(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    int image_channel = 3;
    resize_RGB8882yuv420p(src, src_width, src_height, image_channel, false, image_width, image_height, dst, true);
    dst_width = image_width;
    dst_height = image_height;
}

void resize_scaleline_bgr2rgba(const unsigned char* __restrict__ src, int src_width, int dst_width, unsigned char* __restrict__ dst) {
    int number_pixels = dst_width;
    int int_part = src_width / dst_width;
    int fract_part = src_width % dst_width;
    int index = 0;
    int bgr_channel = 3;
    int rgba_channel = 4;
    while (number_pixels-- > 0) {
        for (int i = 0; i < bgr_channel; i++) {
            dst[i] = src[bgr_channel - 1 - i];
        }
        dst[rgba_channel - 1] = 255;
        dst += rgba_channel;
        src += int_part * bgr_channel;
        index += fract_part;
        if (index >= dst_width) {
            index -= dst_width;
            src += bgr_channel;
        }
    }
}

void BaseFormatTransform::resize_bgr2rgba(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    int number_pixels = image_height;
    int int_part = (src_height / image_height) * src_width;
    int fract_part = src_height % image_height;
    int index = 0;
    int bgr_channel = 3;
    int rgba_channel = 4;
    while (number_pixels-- > 0) {
        resize_scaleline_bgr2rgba(src, src_width, image_width, dst);
        dst += image_width * rgba_channel;
        src += int_part * bgr_channel;
        index += fract_part;
        if (index >= image_height) {
            index -= image_height;
            src += src_width * bgr_channel;
        }
    }
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::resize_rgba2yuvnv21(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    int image_channel = 4;
    resize_RGB8882yuv420p(src, src_width, src_height, image_channel, true, image_width, image_height, dst, false);
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::resize_rgba2yuvnv12(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    int image_channel = 4;
    resize_RGB8882yuv420p(src, src_width, src_height, image_channel, true, image_width, image_height, dst, true);
    dst_width = image_width;
    dst_height = image_height;
}

void resize_scaleline_rgba2bgr(const unsigned char* __restrict__ src, int src_width, int dst_width, unsigned char* __restrict__ dst) {
    int number_pixels = dst_width;
    int int_part = src_width / dst_width;
    int fract_part = src_width % dst_width;
    int index = 0;
    int rgba_channel = 4;
    int bgr_channel = 3;
    while (number_pixels-- > 0) {
        for (int i = 0; i < bgr_channel; i++) {
            dst[i] = src[rgba_channel - 1 - 1 - i];
        }
        dst[rgba_channel - 1] = 255;
        dst += bgr_channel;
        src += int_part * rgba_channel;
        index += fract_part;
        if (index >= dst_width) {
            index -= dst_width;
            src += rgba_channel;
        }
    }
}

void BaseFormatTransform::resize_rgba2bgr(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width / scale;
    int image_height = src_height / scale;
    int number_pixels = image_height;
    int int_part = (src_height / image_height) * src_width;
    int fract_part = src_height % image_height;
    int index = 0;
    int rgba_channel = 4;
    int bgr_channel = 3;
    while (number_pixels-- > 0) {
        resize_scaleline_rgba2bgr(src, src_width, image_width, dst);
        dst += image_width * bgr_channel;
        src += int_part * rgba_channel;
        index += fract_part;
        if (index >= image_height) {
            index -= image_height;
            src += src_width * rgba_channel;
        }
    }
    dst_width = image_width;
    dst_height = image_height;
}

void resize_scaleline_fullsizeyuv2yuv420p(const unsigned char* __restrict__ src, int src_width, int dst_width, unsigned char* __restrict__ dst, unsigned char* __restrict__ uv_dst, bool isNV12) {
    int number_pixels = dst_width;
    int int_part = src_width / dst_width;
    int fract_part = src_width % dst_width;
    int index = 0;
    int image_channel = 3;
    int y_channel = 1;
    int uv_channel = 2;
    int y = 0, u = 0, v = 0;
    while (number_pixels-- > 0) {
        if (number_pixels % 2 == 1) {
            u = src[1];
            v = src[2];
            uv_dst[0] = isNV12 ? u : v;
            uv_dst[1] = isNV12 ? v : u;
            uv_dst += uv_channel;
        }
        dst[0] = src[0];
        dst += y_channel;
        src += int_part * image_channel;
        index += fract_part;
        if (index >= dst_width) {
            index -= dst_width;
            src += image_channel;
        }
    }
}

void resize_fullsizeyuv2yuv420p(const unsigned char* __restrict__ src, int src_width, int src_height, int dst_width, int dst_height, unsigned char* __restrict__ dst, bool isNV12) {
    int y_channel = 1;
    unsigned char* __restrict__ uv_dst = dst + dst_width * dst_height;
    
    int number_pixels = dst_height;
    int int_part = (src_height / dst_height) * src_width;
    int fract_part = src_height % dst_height;
    int index = 0;
    int image_channel = 3;
    while (number_pixels-- > 0) {
        resize_scaleline_fullsizeyuv2yuv420p(src, src_width, dst_width, dst, uv_dst, isNV12);
        if (number_pixels % 2 == 1) {
            uv_dst += dst_width;
        }
        dst += dst_width * y_channel;
        src += int_part * image_channel;
        index += fract_part;
        if (index >= dst_height) {
            index -= dst_height;
            src += src_width * image_channel;
        }
    }
}

void BaseFormatTransform::resize_fullsizeyuv2yuvnv12(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width * 2 / scale;
    int image_height = src_height * 2 / scale;
    resize_fullsizeyuv2yuv420p(src, src_width, src_height, image_width, image_height, dst, true);
    dst_width = image_width;
    dst_height = image_height;
}

void BaseFormatTransform::resize_fullsizeyuv2yuvnv21(const unsigned char* __restrict__ src, int src_width, int src_height, unsigned char* __restrict__ dst, int scale, int& dst_width, int& dst_height) {
    if (scale <= 0 || scale > format_min(src_width, src_height)) {
        return;
    }
    int image_width = src_width * 2 / scale;
    int image_height = src_height * 2 / scale;
    resize_fullsizeyuv2yuv420p(src, src_width, src_height, image_width, image_height, dst, false);
    dst_width = image_width;
    dst_height = image_height;
}
