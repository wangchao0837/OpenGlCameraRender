package com.example.cameraglrender.widget;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.cameraglrender.filter.SoulFilter;
import com.example.cameraglrender.record.AvcDecoder;
import com.example.cameraglrender.util.DecoderFinishListener;
import com.example.cameraglrender.util.OnDecoderPlayer;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SoulView extends GLSurfaceView implements GLSurfaceView.Renderer, OnDecoderPlayer {

    private final LinkedList<byte[]> queue;
    private final AvcDecoder decoder;
    private SoulFilter soulFilter;
    private int width;
    private int height;
    private int fps;
    private int imgHeight;
    private int imgWidth;
    private DecoderFinishListener onFinishListener;

    public SoulView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        queue = new LinkedList<>();
        decoder = new AvcDecoder();
        decoder.setCallBackListener(this);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        soulFilter = new SoulFilter(getContext());
        soulFilter.onReady(imgWidth, imgHeight, fps);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float scaleX = (float) imgWidth / (float) width;
        float scaleY = (float) imgHeight / (float) height;

        float max = Math.max(scaleX, scaleY);
        soulFilter.prepare((int) (imgWidth / max), (int) (imgHeight / max), 0, height - (int) (imgHeight / max));
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0, 0, 0, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        byte[] data = pool();

        if (null != data) {
            soulFilter.onDrawFrame(data);
        }


    }


    @Override
    public synchronized void offer(byte[] data) {
        byte[] yuv = new byte[data.length];

        System.arraycopy(data, 0, yuv, 0, yuv.length);
        queue.offer(yuv);

        requestRender();
    }

    @Override
    public synchronized byte[] pool() {
        return queue.poll();
    }

    @Override
    public void setVideoParamerter(int width, int height, int fps) {
        this.imgWidth = width;
        this.imgHeight = height;
        this.fps = fps;
    }

    @Override
    public void onFinish() {
        if (onFinishListener != null) onFinishListener.decoderFinish();
    }


    public void start() {
        decoder.prepare();
        decoder.start();
    }

    public void stop() {
        decoder.stop();
        soulFilter.release();


    }

    public void setDataSource(String path) {
        decoder.setDataSource(path);
    }

    public void setOnDecoderListener(DecoderFinishListener decoderListener) {
        this.onFinishListener = decoderListener;
    }
}
