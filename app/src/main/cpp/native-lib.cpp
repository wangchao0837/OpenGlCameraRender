#include <jni.h>
#include <string>
#include "FaceTracker.h"

#include "FaceTracker.h"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "native-lib", __VA_ARGS__)

extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_cameraglrender_face_FaceTracker_native_1create(JNIEnv *env, jobject thiz,
                                                                jstring model_, jstring seeta_) {

    const char *model = env->GetStringUTFChars(model_, 0);
    const char *seeta = env->GetStringUTFChars(seeta_, 0);

    FaceTracker *faceTracker = new FaceTracker(model, seeta);
    env->ReleaseStringUTFChars(model_, model);
    env->ReleaseStringUTFChars(seeta_, seeta);
    return reinterpret_cast<jlong>(faceTracker);

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_cameraglrender_face_FaceTracker_native_1start(JNIEnv *env, jobject thiz,
                                                               jlong self) {
    if (self == 0) {
        return;
    }
    FaceTracker *me = (FaceTracker *) self;
    me->startTracking();
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_cameraglrender_face_FaceTracker_native_1stop(JNIEnv *env, jobject thiz,
                                                              jlong self) {
    if (self == 0) {
        return;
    }
    FaceTracker *me = (FaceTracker *) self;
    me->stopTracking();
    delete me;
}extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_cameraglrender_face_FaceTracker_native_1detector(JNIEnv *env, jobject thiz,
                                                                  jlong self, jbyteArray data_,
                                                                  jint camera_id, jint width,
                                                                  jint height) {

    if (self == 0) {
        return NULL;
    }

    jbyte *data = env->GetByteArrayElements(reinterpret_cast<jbyteArray>(data_), NULL);

    FaceTracker *faceTracker = reinterpret_cast<FaceTracker *>(self);

    Mat src(height + height / 2, width, CV_8UC1, data);
    //颜色格式的转换 nv21->RGBA
    //将 nv21的yuv数据转成了rgba
    cvtColor(src, src, COLOR_YUV2RGBA_I420);
    // 正在写的过程 退出了，导致文件丢失数据

    if (camera_id == 1) {
        //前置摄像头，需要逆时针旋转90度
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE);
        //水平翻转 镜像
        flip(src, src, 1);
    } else {
        //顺时针旋转90度
        rotate(src, src, ROTATE_90_CLOCKWISE);
    }


    Mat gray;
    //灰色
    cvtColor(src, gray, COLOR_RGBA2GRAY);
    //增强对比度 (直方图均衡)
    equalizeHist(gray, gray);
    vector<Rect2f> rects;
    //送去定位
    faceTracker->detector(gray, rects);

    env->ReleaseByteArrayElements(data_, data, 0);

    int w = src.cols;
    int h = src.rows;
    src.release();
    int ret = rects.size();
    LOGD(" ret :%d", ret);
    if (ret) {
        jclass clazz = env->FindClass("com/example/cameraglrender/face/Face");
        jmethodID costruct = env->GetMethodID(clazz, "<init>", "(IIII[F)V");
        int size = ret * 2;
        //创建java 的float 数组
        jfloatArray floatArray = env->NewFloatArray(size);
        for (int i = 0, j = 0; i < size; j++) {
            float f[2] = {rects[j].x, rects[j].y};
            env->SetFloatArrayRegion(floatArray, i, 2, f);
            i += 2;
        }
        Rect2f faceRect = rects[0];
        int width = faceRect.width;
        int height = faceRect.height;
        jobject face = env->NewObject(clazz, costruct, width, height, w, h,
                                      floatArray);
        return face;
    }
    return 0;
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_cameraglrender_face_FaceTracker_saveYUV(JNIEnv *env, jclass clazz,
                                                         jbyteArray yuv) {


    jbyte *data = env->GetByteArrayElements(yuv, 0);

    FILE *stream;
    if ((stream = fopen("sdcard/yuv", "wb")) == NULL) {

    }
    fwrite(data, 1179648, 1, stream);
    env->ReleaseByteArrayElements(yuv, data, 0);
}