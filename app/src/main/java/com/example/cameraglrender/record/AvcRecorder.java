package com.example.cameraglrender.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import com.example.cameraglrender.util.OnRecordListener;

import java.io.IOException;
import java.nio.ByteBuffer;


public class AvcRecorder {


    private int mWidth;
    private int mHeight;
    private EGLContext eglContext;
    private String mSavePath;
    private Context mContext;

    private float mSpeed;
    private Surface inputSurface;
    private MediaMuxer mediaMuxer;
    private Handler mHandler;
    private boolean isPlaying;

    private EglConfigBase eglConfigBase;
    private MediaCodec mediaCodec;
    private int avcIndex;
    private OnRecordListener onRecordListener;
    private int fps = 20;

    public AvcRecorder(Context context, int width, int height, EGLContext eglContext) {
        mWidth = width;
        mHeight = height;
        mContext = context;
        this.eglContext = eglContext;
    }


    public void start(float speed, String savePath) {
        mSavePath = savePath;
        mSpeed = speed;

        try {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mWidth, mHeight);

            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mWidth * mHeight * fps / 5);

            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);

            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, fps);

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            inputSurface = mediaCodec.createInputSurface();

            mediaMuxer = new MediaMuxer(mSavePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);


            //-------------配置EGL环境----------------

            HandlerThread handlerThread = new HandlerThread("elgCodec");
            handlerThread.start();

            mHandler = new Handler(handlerThread.getLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    eglConfigBase = new EglConfigBase(mContext, mWidth, mHeight, inputSurface, eglContext);
                    mediaCodec.start();
                    isPlaying = true;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void encodeFrame(final int textureId, final long timeStamp) {

        if (!isPlaying) return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                eglConfigBase.draw(textureId, timeStamp);
                getCodec(false);
            }
        });

    }

    private void getCodec(boolean endOfStream) {
        if (endOfStream) {
            mediaCodec.signalEndOfInputStream();
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        int status = mediaCodec.dequeueOutputBuffer(bufferInfo, 10_000);
        //表示请求超时，10_000毫秒内没有数据到来
        if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {

        } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            //编码格式改变 ，第一次start 都会调用一次
            MediaFormat outputFormat = mediaCodec.getOutputFormat();
            //设置mediaMuxer 的视频轨
            avcIndex = mediaMuxer.addTrack(outputFormat);
            mediaMuxer.start();
        } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            //Outputbuffer  改变了
        } else {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(status);
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                bufferInfo.size = 0;
            }


            if (bufferInfo.size > 0) {
                bufferInfo.presentationTimeUs = (long) (bufferInfo.presentationTimeUs / mSpeed);
                outputBuffer.position(bufferInfo.offset);

                outputBuffer.limit(bufferInfo.size - bufferInfo.offset);
                //交给mediaMuxer 保存
                mediaMuxer.writeSampleData(avcIndex, outputBuffer, bufferInfo);

            }

            mediaCodec.releaseOutputBuffer(status, false);

            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {

            }
        }

    }

    public void stop() {
        isPlaying = false;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getCodec(true);
                mediaCodec.stop();
                mediaCodec.release();
                mediaCodec = null;
                mediaMuxer.stop();
                mediaMuxer.release();
                mediaMuxer = null;
                eglConfigBase.release();
                eglConfigBase = null;
                inputSurface.release();
                inputSurface = null;
                mHandler.getLooper().quitSafely();
                mHandler = null;
                if (onRecordListener != null) {
                    onRecordListener.recordFinish(mSavePath);
                }
            }
        });
    }


    public void setOnRecordListener(OnRecordListener onRecordListener) {
        this.onRecordListener = onRecordListener;
    }
}
