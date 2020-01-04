package com.example.cameraglrender.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Lance
 * @date 2018/6/22
 */

public class GLImage {
    private int y_len;
    private int uv_len;
    private byte[] yBytes;
    private byte[] uvBytes;
    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;
    private boolean hasImage;

    /**
     * 初始化
     * @param width
     * @param height
     */
    public void initSize(int width, int height) {
        //初始化 y、u、v数据缓存 y的数据长度
        y_len = width * height;
        //u和v的字节长度
        uv_len = width / 2 * height / 2;
        //存储y的字节
        yBytes = new byte[y_len];
        uvBytes = new byte[uv_len];
        //保存y、u、v数据
        y = ByteBuffer.allocateDirect(y_len).order(ByteOrder.nativeOrder());
        u = ByteBuffer.allocateDirect(uv_len).order(ByteOrder.nativeOrder());
        v = ByteBuffer.allocateDirect(uv_len).order(ByteOrder.nativeOrder());
    }

    /**
     * 分离yuv
     * @param data
     * @return
     */
    public boolean initData(byte[] data) {
        hasImage = readBytes(data, y, 0, y_len) && readBytes(data, u, y_len, uv_len) && readBytes
                (data, v, y_len + uv_len, uv_len);
        return hasImage;
    }

    public ByteBuffer getY() {
        return y;
    }

    public ByteBuffer getU() {
        return u;
    }

    public ByteBuffer getV() {
        return v;
    }

    private boolean readBytes(byte[] data, ByteBuffer buffer, int offset, int len) {
        //有没有这么长的数据刻度
        if (data.length < offset + len) {
            return false;
        }
        byte[] bytes;
        if (len == yBytes.length) {
            bytes = yBytes;
        } else {
            bytes = uvBytes;
        }
        System.arraycopy(data, offset, bytes, 0, len);
        buffer.position(0);
        buffer.put(bytes);
        buffer.position(0);
        return true;
    }

    public boolean hasImage() {
        return hasImage;
    }
}
