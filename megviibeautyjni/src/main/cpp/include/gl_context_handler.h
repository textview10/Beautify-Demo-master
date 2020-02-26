//
// Created by 张英堂 on 2017/12/20.
//

#ifndef HUMANEFFECTS_GL_CONTEXT_HANDLER_H
#define HUMANEFFECTS_GL_CONTEXT_HANDLER_H

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

//typedef enum {
//    EGLCONTEXT_2 = 2,
//}EGLCONTEXTTYPE;

class glcontext_manager{

public:
    glcontext_manager();
    ~glcontext_manager();

    /** 创建 OpenGL context */
    bool make_context();

    /** 共享 context 到当前线程 */
    bool share_context();

    //将context与当前线程解绑
    bool done_context();

    /** context */
    void release_context();
private:


    bool hasCurrentContext();

    bool initSuccess;

    /** 后续为 安卓 4.2 一下版本，做适配使用*/
    int MG_CONTEXT_VERSION_A_4_0 = 0x3098;

    /** elgcontext */
    EGLConfig eglConf;
    EGLSurface eglSurface;
    EGLContext eglCtx;
    EGLDisplay eglDisp;
};


#endif //HUMANEFFECTS_GL_CONTEXT_HANDLER_H
