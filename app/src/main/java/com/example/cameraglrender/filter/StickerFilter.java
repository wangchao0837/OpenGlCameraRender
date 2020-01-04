package com.example.cameraglrender.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.example.cameraglrender.R;
import com.example.cameraglrender.face.Face;
import com.example.cameraglrender.util.OpenGlUtils;

public class StickerFilter extends AbstractFBOFilter {
    private Face mFace;
    private int[] mTextureId;
    private final Bitmap mBitmap;

    public StickerFilter(Context mContext) {
        super(mContext, R.raw.screen_vert, R.raw.screen_frag);
        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.erduo_000);
    }


    @Override
    public void prepare(int width, int height, int x, int y) {
        super.prepare(width, height,x,y);

        mTextureId = new int[1];

        OpenGlUtils.glGenTextures(mTextureId);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }


    public void setFace(Face face) {
        this.mFace = face;
    }


    @Override
    public int onDrawFrame(int textureId) {

        if (null == mFace) return textureId;

        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

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

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        onDrawStick();

        return mFBOTextures[0];
    }

    private void onDrawStick() {
        GLES20.glEnable(GLES20.GL_BLEND);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        float x = mFace.landmarks[0];
        float y = mFace.landmarks[1];

        x = x / mFace.imgWidth * mOutputWidth;
        y = y / mFace.imgHeight * mOutputHeight;

        GLES20.glViewport((int) x, (int) y - mBitmap.getHeight(), (int) ((float) mFace.width / mFace.imgWidth * mOutputWidth), mBitmap.getHeight());

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);

        GLES20.glUseProgram(mProgramId);

        mGlVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mGlVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);


        mGlTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mGlTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId[0]);
        GLES20.glUniform1i(vTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisable(GLES20.GL_BLEND);

    }

    @Override
    public void release() {
        super.release();
        mBitmap.recycle();
    }
}
