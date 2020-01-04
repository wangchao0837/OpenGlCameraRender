package com.example.cameraglrender.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.example.cameraglrender.R;

public class BeautifyFilter extends AbstractFBOFilter {

    private final int width;
    private final int height;

    public BeautifyFilter(Context mContext) {
        super(mContext, R.raw.screen_vert, R.raw.beauty_frag);

        width = GLES20.glGetUniformLocation(mProgramId, "width");
        height = GLES20.glGetUniformLocation(mProgramId, "height");

    }


    @Override
    public int onDrawFrame(int textureId) {

        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        GLES20.glUseProgram(mProgramId);

        GLES20.glUniform1i(width, mOutputWidth);
        GLES20.glUniform1i(height, mOutputHeight);

        //传递坐标
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

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        return mFBOTextures[0];
    }
}
