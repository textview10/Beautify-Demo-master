package com.megvii.beautify.ui.test.ImageTest;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Created by xiejiantao on 2017/8/11.
 */

public class RbGlsurfaceview extends GLSurfaceView {

    RbRender mIRenderer;
    public RbGlsurfaceview(Context context) {
        super(context);
        init();
    }

    public RbGlsurfaceview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {

        mIRenderer=new RbRender(this.getContext());
        setEGLContextClientVersion(2);// 创建一个OpenGL ES 2.0
        // context
        setRenderer(mIRenderer);// 设置渲染器进入gl
        setRenderMode(RENDERMODE_WHEN_DIRTY);// 设置渲染器模式

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIRenderer.destroy();
    }
}
