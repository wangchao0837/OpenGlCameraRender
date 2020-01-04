package com.example.cameraglrender.face;

import java.util.Arrays;

/**
 * @author Lance
 * @date 2018/10/9
 */
public class Face {
    //每两个 保存 一个点 x+y
    //0、1 : 保存人脸的 x与y
    // 后面的 保存人脸关键点坐标 有序的
    public float[] landmarks;
    // 保存人脸的宽、高
    public int width;
    public int height;

    //送去检测图片的宽、高
    public int imgWidth;
    public int imgHeight;

    Face(int width, int height, int imgWidth, int imgHeight, float[] landmarks) {
        this.width = width;
        this.height = height;
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.landmarks = landmarks;
    }

    @Override
    public String toString() {
        return "Face{" +
                "landmarks=" + Arrays.toString(landmarks) +
                ", width=" + width +
                ", height=" + height +
                ", imgWidth=" + imgWidth +
                ", imgHeight=" + imgHeight +
                '}';
    }
}
