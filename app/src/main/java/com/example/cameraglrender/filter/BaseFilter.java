package com.example.cameraglrender.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.example.cameraglrender.util.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BaseFilter {

    private Context mContext;
    protected int mVertexShaderId;
    protected int mFragShaderId;
    protected final FloatBuffer mGlVertexBuffer;
    protected final FloatBuffer mGlTextureBuffer;
    protected String mVertexShader;
    protected String mFragShader;
    protected int mProgramId;
    protected int vTexture;
    protected int vMatrix;
    protected int vPosition;
    protected int vCoord;
    protected int mOutputHeight;
    protected int mOutputWidth;
    protected int y;
    protected int x;

    public BaseFilter(Context mContext, int mVertexShaderId, int mFragShaderId) {
        this.mContext = mContext;
        this.mVertexShaderId = mVertexShaderId;
        this.mFragShaderId = mFragShaderId;


        mGlVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGlVertexBuffer.clear();

//        float[] VERTEXT = {
//                -1.0f, -1.0f,
//                1.0f, -1.0f,
//                -1.0f, 1.0f,
//                1.0f, 1.0f
//        };

        float[] VERTEXT = {
                -1.0f, 1.0f,
                1.0f, 1.0f,
                -1.0f, -1.0f,
                1.0f, -1.0f
        };


        mGlVertexBuffer.put(VERTEXT);

        mGlTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGlTextureBuffer.clear();

//        float[] TEXTURE = {
//                0.0f, 1.0f,
//                1.0f, 1.0f,
//                0.0f, 0.0f,
//                1.0f, 0.0f,
//        };

        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };


        mGlTextureBuffer.put(TEXTURE);

        initilize(mContext);

        resetCoordinate();

    }


    private void initilize(Context mContext) {
        mVertexShader = OpenGlUtils.readRawShaderFile(mContext, mVertexShaderId);
        mFragShader = OpenGlUtils.readRawShaderFile(mContext, mFragShaderId);

        mProgramId = OpenGlUtils.loadProgram(mVertexShader, mFragShader);

        vPosition = GLES20.glGetAttribLocation(mProgramId, "vPosition");
        vCoord = GLES20.glGetAttribLocation(mProgramId, "vCoord");
        vMatrix = GLES20.glGetUniformLocation(mProgramId, "vMatrix");
        vTexture = GLES20.glGetUniformLocation(mProgramId, "vTexture");

    }

    public void prepare(int width, int height, int x, int y) {
        mOutputWidth = width;
        mOutputHeight = height;
        this.x = x;
        this.y = y;

    }

    public int onDrawFrame(int textureId) {
        GLES20.glViewport(x, y, mOutputWidth, mOutputHeight);

        GLES20.glUseProgram(mProgramId);

        mGlVertexBuffer.position(0);

        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGlTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        return textureId;


    }

    public void release() {
        GLES20.glDeleteProgram(mProgramId);
    }

    protected void resetCoordinate() {

    }


}
