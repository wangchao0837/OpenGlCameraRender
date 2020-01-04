package com.example.cameraglrender.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import com.example.cameraglrender.R;

public class CameraFilter extends AbstractFBOFilter {


    protected float[] matrix;

    public CameraFilter(Context mContext) {
        super(mContext, R.raw.camera_vertex, R.raw.camera_frag);
    }


    @Override
    protected void resetCoordinate() {
//        mGlTextureBuffer.clear();
//        float[] TEXTURE = {
//                0.0f, 1.0f,
//                1.0f, 1.0f,
//                0.0f, 0.0f,
//                1.0f, 0.0f,
//        };
//        mGlTextureBuffer.put(TEXTURE);


    }

    @Override
    public int onDrawFrame(int textureId) {


        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        //使用着色器
        GLES20.glUseProgram(mProgramId);

        mGlVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);


        mGlTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glUniformMatrix4fv(vMatrix, 1, false, matrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        GLES20.glUniform1i(vTexture, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        return mFBOTextures[0];
    }


    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }


}
