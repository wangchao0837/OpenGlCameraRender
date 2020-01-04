package com.example.cameraglrender.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.example.cameraglrender.R;
import com.example.cameraglrender.face.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BigEyeFilter extends AbstractFBOFilter {

    private final int left_eye;
    private final int right_eye;
    private final FloatBuffer left;
    private final FloatBuffer right;
    private Face mFace;

    public BigEyeFilter(Context mContext) {
        super(mContext, R.raw.screen_vert, R.raw.bigeye_frag);

        left_eye = GLES20.glGetUniformLocation(mProgramId, "left_eye");
        right_eye = GLES20.glGetUniformLocation(mProgramId, "right_eye");


        left = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    }

    public void setFace(Face face) {
        mFace = face;
    }


    @Override
    public int onDrawFrame(int textureId) {

        if (mFace == null) return textureId;
        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        GLES20.glUseProgram(mProgramId);

        mGlVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);


        mGlTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);


        /**
         * 传递眼睛的坐标 给GLSL
         */
        float[] landmarks = mFace.landmarks;
        //左眼的x 、y  opengl : 0-1
        float x = landmarks[2] / mFace.imgWidth;
        float y = landmarks[3] / mFace.imgHeight;

        left.clear();
        left.put(x);
        left.put(y);
        left.position(0);
        GLES20.glUniform2fv(left_eye, 1, left);

        //右眼的x、y
        x = landmarks[4] / mFace.imgWidth;
        y = landmarks[5] / mFace.imgHeight;
        right.clear();
        right.put(x);
        right.put(y);
        right.position(0);
        GLES20.glUniform2fv(right_eye, 1, right);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return mFBOTextures[0];
    }
}
