package com.example.cameraglrender.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.cameraglrender.R;
import com.example.cameraglrender.util.GLImage;
import com.example.cameraglrender.util.OpenGlUtils;

public class SoulFilter extends BaseFilter {

    private final GLImage soulmage;
    private final GLImage bodyImage;
    private final int mSamplerY;
    private final int mSamplerU;
    private final int mSamplerV;
    private final int alpha;
    private int mFps;
    private float[] matrix = new float[16];
    private int[] mTextures;
    int interval = 0;
    private int imgWidth;
    private int imgHeight;

    public SoulFilter(Context mContext) {
        super(mContext, R.raw.soul_vertex, R.raw.soul_frag);

        bodyImage = new GLImage();
        soulmage = new GLImage();

        mSamplerY = GLES20.glGetUniformLocation(mProgramId, "sampler_y");
        mSamplerU = GLES20.glGetUniformLocation(mProgramId, "sampler_u");
        mSamplerV = GLES20.glGetUniformLocation(mProgramId, "sampler_v");
        alpha = GLES20.glGetUniformLocation(mProgramId, "alpha");


        mTextures = new int[3];
        OpenGlUtils.glGenTextures(mTextures);

    }


    public void onReady(int width, int height, int fps) {
        mFps = fps;
        bodyImage.initSize(width, height);
        soulmage.initSize(width, height);

        imgWidth = width;
        imgHeight = height;

    }


    public void onDrawFrame(byte[] yuv) {

        bodyImage.initData(yuv);
        if (!bodyImage.hasImage()) {
            return;
        }

        GLES20.glViewport(x, y, mOutputWidth, mOutputHeight);


        GLES20.glUseProgram(mProgramId);

        Matrix.setIdentityM(matrix, 0);

        GLES20.glUniformMatrix4fv(vMatrix, 1, false, matrix, 0);

        GLES20.glUniform1f(alpha, 1);

        onDrawBody(bodyImage);

        onDrawSoul(yuv);

    }

    private void onDrawSoul(byte[] yuv) {

        interval++;

        if (!soulmage.hasImage() || interval > mFps) {
            interval = 1;
            soulmage.initData(yuv);
        }

        if (!soulmage.hasImage()) {
            return;
        }

        GLES20.glEnable(GLES20.GL_BLEND);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);

        Matrix.setIdentityM(matrix, 0);

        float scale = 1.0f + interval / (mFps * 2.f);

        Matrix.scaleM(matrix, 0, scale, scale, 0);

        GLES20.glUniformMatrix4fv(vMatrix, 1, false, matrix, 0);

        GLES20.glUniform1f(alpha, 0.1f + (mFps - interval) / 100.f);


        onDrawBody(soulmage);
    }

    private void onDrawBody(GLImage image) {

        mGlVertexBuffer.position(0);

        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mGlTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                imgWidth, imgHeight, 0, GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, image.getY());
        GLES20.glUniform1i(mSamplerY, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[1]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                imgWidth / 2, imgHeight / 2, 0, GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, image.getU());
        GLES20.glUniform1i(mSamplerU, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[2]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                imgWidth / 2, imgHeight / 2, 0, GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE, image.getV());
        GLES20.glUniform1i(mSamplerV, 2);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }


    @Override
    public void release() {
        super.release();

        for (int i = 0; i < mTextures.length; i++) {
            GLES20.glDeleteTextures(1, mTextures, i);
        }
        mTextures = null;
    }
}
