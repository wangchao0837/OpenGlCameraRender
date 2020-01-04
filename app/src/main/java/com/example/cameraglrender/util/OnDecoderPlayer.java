package com.example.cameraglrender.util;

public interface OnDecoderPlayer {

    void offer(byte[] data);

    byte[] pool();

    void setVideoParamerter(int width, int height, int fps);

    void onFinish();
}
