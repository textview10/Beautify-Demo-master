//
// Created by 张英堂 on 2017/12/20.
//

#include "include/gl_context_handler.h"

#include <thread>         // std::thread
#include <android/log.h>

#define  LOGEH(TAG,...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
#define  LOGDH(TAG,...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define  LOGWH(TAG,...)  __android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__)
#define  LOGIH(TAG,...)  __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)

glcontext_manager::glcontext_manager() {
    initSuccess = false;
}

glcontext_manager::~glcontext_manager() {
    release_context();
}

bool glcontext_manager::make_context() {

    if (hasCurrentContext()) return true;
    // EGL config attributes
    if (initSuccess) return initSuccess;

    do{
        const EGLint confAttr[] =
                {
                        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,// very important!  //指定渲染api类别
                        EGL_SURFACE_TYPE,
                        EGL_PBUFFER_BIT,//EGL_WINDOW_BIT EGL_PBUFFER_BIT we will create a pixelbuffer surface
                        EGL_RED_SIZE, 8,
                        EGL_GREEN_SIZE, 8,
                        EGL_BLUE_SIZE, 8,
                        EGL_ALPHA_SIZE, 8,// if you need the alpha channel
                        EGL_DEPTH_SIZE, 8,// if you need the depth buffer
                        EGL_STENCIL_SIZE, EGL_DONT_CARE,
                        EGL_NONE
                };

        // EGL context attributes
        const EGLint ctxAttr[] = {
                EGL_CONTEXT_CLIENT_VERSION, 2,// very important!
                EGL_NONE
        };

        // surface attributes
        // the surface size is set to the input frame size
        const EGLint surfaceAttr[] = {
                EGL_WIDTH, 128,
                EGL_HEIGHT, 128,
//                EGL_LARGEST_PBUFFER, EGL_TRUE,
                EGL_NONE
        };

        EGLint eglMajVers, eglMinVers;
        EGLint numConfigs;

        eglDisp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if (eglDisp == EGL_NO_DISPLAY) {
            //Unable to open connection to local windowing system
            LOGEH(__FUNCTION__,"Unable to open connection to local windowing system");
            break;
        }

        if (!eglInitialize(eglDisp, &eglMajVers, &eglMinVers)) {
            // Unable to initialize EGL. Handle and recover
            LOGEH(__FUNCTION__,"Unable to initialize EGL");
            break;
        }

        LOGIH(__FUNCTION__,"EGL init with version %d.%d", eglMajVers, eglMinVers);
        // choose the first config, i.e. best config
        if (!eglChooseConfig(eglDisp, confAttr, &eglConf, 1, &numConfigs)) {
            LOGEH(__FUNCTION__,"some config is wrong");
            break;
        }
        // create a pixelbuffer surface
        eglSurface = eglCreatePbufferSurface(eglDisp, eglConf, surfaceAttr);
        if (eglSurface == EGL_NO_SURFACE) {
            switch (eglGetError()) {
                case EGL_BAD_ALLOC:
                    // Not enough resources available. Handle and recover
                    LOGEH(__FUNCTION__,"Not enough resources available");
                    break;
                case EGL_BAD_CONFIG:
                    // Verify that provided EGLConfig is valid
                    LOGEH(__FUNCTION__,"provided EGLConfig is invalid");
                    break;
                case EGL_BAD_PARAMETER:
                    // Verify that the EGL_WIDTH and EGL_HEIGHT are
                    // non-negative values
                    LOGEH(__FUNCTION__,"provided EGL_WIDTH and EGL_HEIGHT is invalid");
                    break;
                case EGL_BAD_MATCH:
                    // Check window and EGLConfig attributes to determine
                    // compatibility and pbuffer-texture parameters
                    LOGEH(__FUNCTION__,"Check window and EGLConfig attributes");
                    break;
            }
            LOGWH(__FUNCTION__,"EGL: EGL_NO_SURFACE");
            break;
        }
        eglCtx = eglCreateContext(eglDisp, eglConf, EGL_NO_CONTEXT, ctxAttr);
        if (eglCtx == EGL_NO_CONTEXT) {
            EGLint error = eglGetError();
            if (error == EGL_BAD_CONFIG) {
                // Handle error and recover
                LOGEH(__FUNCTION__,"EGL_BAD_CONFIG");
                break;
            }
        }
        initSuccess = true;
        LOGIH(__FUNCTION__,"make_context thread id: %lu", pthread_self());
        LOGIH(__FUNCTION__,"EGL initialize success!");
    }while (0);

    return initSuccess;
}

bool glcontext_manager::hasCurrentContext() {

    EGLContext current = eglGetCurrentContext();

    LOGIH(__FUNCTION__,"current context : %p %p", current, EGL_NO_CONTEXT);

    if(current == EGL_NO_CONTEXT) return false;

    LOGIH(__FUNCTION__,"current has gl context!");

    return true;
}

bool glcontext_manager::share_context() {
    LOGIH(__FUNCTION__,"share_context thread id: %lu", pthread_self());


    if (hasCurrentContext()) return true;

    bool share_success = initSuccess;

    do{
        if (true != initSuccess) {
            make_context();
        }

        if (!eglMakeCurrent(eglDisp, eglSurface, eglSurface, eglCtx)) {
            LOGEH(__FUNCTION__,"glcontext_manager::share_context MakeCurrent failed");

            share_success = false;
        } else{

            LOGIH(__FUNCTION__,"glcontext_manager::share_context MakeCurrent true");

            share_success = true;
        }

    }while (0);

    return share_success;
}

bool glcontext_manager::done_context() {
    if(hasCurrentContext()) {
        eglMakeCurrent(eglDisp, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        return true;
    }
    return false;
}

void glcontext_manager::release_context() {

    if(initSuccess){
        eglMakeCurrent(eglDisp, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        eglDestroyContext(eglDisp, eglCtx);
        eglDestroySurface(eglDisp, eglSurface);
        eglTerminate(eglDisp);

        eglDisp = EGL_NO_DISPLAY;
        eglSurface = EGL_NO_SURFACE;
        eglCtx = EGL_NO_CONTEXT;
    }
    initSuccess = false;
}