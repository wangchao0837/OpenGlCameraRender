package com.example.cameraglrender.record;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.view.Surface;

import com.example.cameraglrender.filter.ScreenFilter;

public class EglConfigBase {


    private EGLDisplay eglDisplay;
    private EGLConfig mEglConfig;
    private EGLContext mCurrentEglContext;
    private final EGLSurface eglSurface;
    private final ScreenFilter screenFilter;

    public EglConfigBase(Context context, int width, int height, Surface surface, EGLContext eglContext) {

        createEGLContext(eglContext);

        int[] attrib_list = {
                EGL14.EGL_NONE
        };

        //创建EGLSurface
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, mEglConfig, surface, attrib_list, 0);

        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("eglCreateWindowSurface 失败！");
        }

        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, mCurrentEglContext)) {
            throw new RuntimeException("eglMakeCurrent 失败！");
        }

        screenFilter = new ScreenFilter(context);
        screenFilter.prepare(width, height, 0, 0);

    }

    private void createEGLContext(EGLContext eglContext) {
        //创建虚拟屏幕
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        int[] versions = new int[2];
        //初始化elgdisplay
        if (!EGL14.eglInitialize(eglDisplay, versions, 0, versions, 1)) {
            throw new RuntimeException("eglInitialize failed");
        }

        int[] attr_list = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };

        EGLConfig[] configs = new EGLConfig[1];

        int[] num_configs = new int[1];

        //配置eglDisplay 属性
        if (!EGL14.eglChooseConfig(eglDisplay, attr_list, 0,
                configs, 0, configs.length,
                num_configs, 0)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }


        mEglConfig = configs[0];

        int[] ctx_attrib_list = {
                //TODO
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };

        //创建EGL 上下文
        mCurrentEglContext = EGL14.eglCreateContext(eglDisplay, mEglConfig, eglContext, ctx_attrib_list, 0);

        if (mCurrentEglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("EGL Context Error.");
        }
    }

    public void draw(int textureId, long timestamp) {
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, mCurrentEglContext)) {
            throw new RuntimeException("eglMakeCurrent 失败！");
        }


        screenFilter.onDrawFrame(textureId);

        EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, timestamp);
        //交换数据，输出到mediacodec InputSurface中
        EGL14.eglSwapBuffers(eglDisplay, eglSurface);

    }


    public void release() {
        EGL14.eglDestroySurface(eglDisplay, eglSurface);
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, mCurrentEglContext);
        EGL14.eglDestroyContext(eglDisplay, mCurrentEglContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(eglDisplay);
    }
}
