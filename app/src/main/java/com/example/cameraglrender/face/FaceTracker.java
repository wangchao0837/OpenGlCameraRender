package com.example.cameraglrender.face;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.example.cameraglrender.util.Camera2Helper;

public class FaceTracker {
    static {
        System.loadLibrary("native-lib");
    }

    private Camera2Helper mCameraHelper;

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    private long self;
    //结果
    public Face mFace;

    public FaceTracker(String model, String seeta, final Camera2Helper cameraHelper) {
        mCameraHelper = cameraHelper;
        self = native_create(model, seeta);
        mHandlerThread = new HandlerThread("track");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                //子线程 耗时再久 也不会对其他地方 (如：opengl绘制线程) 产生影响
                synchronized (FaceTracker.this) {
                    //定位 线程中检测
                    mFace = native_detector(self, (byte[]) msg.obj,
                            cameraHelper.getCameraId(), cameraHelper.getSize().getWidth(),cameraHelper.getSize().getHeight());

                }
            }
        };
    }


    public void startTrack() {
        native_start(self);
    }

    public void stopTrack() {
        synchronized (this) {
            mHandlerThread.quitSafely();
            mHandler.removeCallbacksAndMessages(null);
            native_stop(self);
            self = 0;
        }
    }

    public void detector(byte[] data) {
        //把积压的 11号任务移除掉
        mHandler.removeMessages(11);
        //加入新的11号任务
        Message message = mHandler.obtainMessage(11);
        message.obj = data;
        mHandler.sendMessage(message);
    }
    //传入模型文件， 创建人脸识别追踪器和人眼定位器
    private native long native_create(String model, String seeta);

    //开始追踪
    private native void native_start(long self);

    //停止追踪
    private native void native_stop(long self);

    //检测人脸
    private native Face native_detector(long self, byte[] data, int cameraId, int width, int
            height);


}
