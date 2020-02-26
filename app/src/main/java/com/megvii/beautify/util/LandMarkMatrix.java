package com.megvii.beautify.util;

import android.opengl.GLES20;
import com.megvii.beautify.cameragl.OpenglUtil;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * This class is used to draw landmark on the specific texture.
 * Using steps:
 * float[] points;
 * int textureId;
 * int width;
 * int height;
 * LandMarkMatrix landMatrix = new LandMarkMatrix();
 * landMatrix.setPoints(points);
 * landMatrix.drawLandMarks(textureId, width, height);
 */
public class LandMarkMatrix {

    private int mGLProgId;
    private int mGLAttribPosition;
    protected int[] mFrameBuffers = null;
    private boolean inited;

    private volatile int[] points = new int[Util.LandMarkPointSize];

    private ByteBuffer byteBuffer = ByteBuffer.allocateDirect(points.length * 4)
            .order(ByteOrder.nativeOrder());

    private final String mVertexShader = "attribute vec2 aPosition;\n" +
            "void main()\n" +
            "{\n" +
            "  gl_Position = vec4(aPosition,1.0,1.0);\n" +
            "  gl_PointSize = 10.0;\n" +
            "}";
    private final String mFragmentShader = "precision mediump float;\n" +
            "void main()\n" +
            "{\n" +
            "\tgl_FragColor = vec4(1.0,0.0,0.0, 1.0);\n" +
            "}";

    /**
     * Set the points to be drawn
     *
     * @param points
     */
    public void setPoints(int[] points) {
        this.points = Arrays.copyOf(points, points.length);
    }

    /**
     * Draw the points to the texture
     * @param width
     * @param height
     */
    public void drawLandMark(int textureId
            , int width, int height) {
        if (points == null) {
            return;
        }
        checkInit();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glUseProgram(mGLProgId);
        float[] points = getLandMarks(width, height);
        byteBuffer.asFloatBuffer().put(points);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        GLES20.glVertexAttribPointer(mGLAttribPosition
                , 2
                , GLES20.GL_FLOAT
                , false
                , 0
                , byteBuffer);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, points.length / 2);
        GLES20.glUseProgram(0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    /**
     * 初始化
     */
    private void checkInit() {
        if (!inited) {
            inited = true;
            mGLProgId = OpenglUtil.loadProgram(mVertexShader, mFragmentShader);
            mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, "aPosition");
            mFrameBuffers = new int[1];
            GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        }
    }

    /**
     * Get the normalized standard
     *
     * @param width
     * @param height
     * @return
     */
    private float[] getLandMarks(int width, int height){
        float[] result = new float[Util.LandMarkPointSize];
        for (int i = 0; i < points.length; i += 2) {
            result[i] = points[i] / (float) width * 2 - 1;
            result[i + 1] = points[i + 1] / (float) height * 2 - 1;
        }
        return result;
    }
}
