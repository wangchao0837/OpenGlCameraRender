package com.example.cameraglrender.record;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import com.example.cameraglrender.util.OnDecoderPlayer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AvcDecoder {


    private OnDecoderPlayer onDecoderPlayer;
    private MediaExtractor mediaExtractor;
    private String mPath;
    private int mWidth;
    private int mHeight;
    private int fps;
    private boolean isDecoding;
    private byte[] outData;
    private CodecTask codecTask;
    private MediaCodec mediaCodec;

    public void setCallBackListener(OnDecoderPlayer onDecoderPlayer) {
        this.onDecoderPlayer = onDecoderPlayer;
    }

    /**
     * 设置要解码的视频地址
     *
     * @param path
     */
    public void setDataSource(String path) {
        mPath = path;
    }

    public void prepare() {

        try {
            mediaExtractor = new MediaExtractor();

            mediaExtractor.setDataSource(mPath);

            int trackCount = mediaExtractor.getTrackCount();
            int videoIndex = -1;
            MediaFormat videoFormat = null;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);

                String mime = trackFormat.getString(MediaFormat.KEY_MIME);

                if (mime.startsWith("video/")) {
                    videoIndex = i;
                    videoFormat = trackFormat;
                    break;
                }
            }


            if (videoFormat != null) {
                mWidth = videoFormat.getInteger(MediaFormat.KEY_WIDTH);
                mHeight = videoFormat.getInteger(MediaFormat.KEY_HEIGHT);
                fps = 20;
                if (videoFormat.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                    fps = videoFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                }
                //KEY_DURATION

                videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);

                mediaCodec = MediaCodec.createDecoderByType(videoFormat.getString(MediaFormat.KEY_MIME));

                mediaCodec.configure(videoFormat, null, null, 0);

                mediaExtractor.selectTrack(videoIndex);

            }

            if (onDecoderPlayer != null) {
                onDecoderPlayer.setVideoParamerter(mWidth, mHeight, fps);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        isDecoding = true;

        outData = new byte[mWidth * mHeight * 3 / 2];
        codecTask = new CodecTask();

        codecTask.start();
    }

    public void stop() {
        isDecoding = false;
        if (null != codecTask && codecTask.isAlive()) {
            try {
                codecTask.join(3_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (codecTask.isAlive()) {
                codecTask.interrupt();
            }

            codecTask = null;

        }

        if (onDecoderPlayer != null) onDecoderPlayer.onFinish();

    }

    class CodecTask extends Thread {

        private boolean isEOF;

        @Override
        public void run() {
            super.run();

            if (null == mediaCodec) {
                return;
            }

            mediaCodec.start();


            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            while (!isInterrupted()) {

                if (!isDecoding) {
                    break;
                }
                if (!isEOF) {
                    isEOF = dequeueInputBuffer();
                }

                int status = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);

                if (status >= 0) {
                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(status);
                    if (bufferInfo.size == outData.length) {
                        outputBuffer.get(outData);
                        if (onDecoderPlayer != null) {
                            onDecoderPlayer.offer(outData);
                        }
                    }
                    mediaCodec.releaseOutputBuffer(status, false);

                    try {
                        Thread.sleep(1_000 / fps);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (onDecoderPlayer != null) onDecoderPlayer.onFinish();
                    break;
                }
            }

            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
            mediaExtractor.release();
            mediaExtractor = null;
        }

        private boolean dequeueInputBuffer() {
            int status = mediaCodec.dequeueInputBuffer(0);

            if (status > 0) {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(status);
                inputBuffer.clear();

                int size = mediaExtractor.readSampleData(inputBuffer, 0);

                if (size < 0) {
                    mediaCodec.queueInputBuffer(status, 0, 0, 0, mediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    return true;
                } else {
                    mediaCodec.queueInputBuffer(status, 0, size, mediaExtractor.getSampleTime(), 0);
                    mediaExtractor.advance();
                    return false;
                }
            }
            return false;
        }
    }
}
