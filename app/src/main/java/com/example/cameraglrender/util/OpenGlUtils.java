package com.example.cameraglrender.util;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OpenGlUtils {


    public static String readRawShaderFile(Context context, int shareId) {
        InputStream is = context.getResources().openRawResource(shareId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        StringBuffer sb = new StringBuffer();
        try {

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static int loadProgram(String mVertexShader, String mFragShader) {
        int vshader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        GLES20.glShaderSource(vshader, mVertexShader);

        GLES20.glCompileShader(vshader);

        int[] status = new int[1];

        GLES20.glGetShaderiv(vshader, GLES20.GL_COMPILE_STATUS, status, 0);

        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("load vertex raw error :" + GLES20.glGetShaderInfoLog(vshader));
        }


        int fshader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        GLES20.glShaderSource(fshader, mFragShader);

        GLES20.glCompileShader(fshader);


        GLES20.glGetShaderiv(fshader, GLES20.GL_SHADER_COMPILER, status, 0);

        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("load fragment raw error :" + GLES20.glGetShaderInfoLog(fshader));
        }


        int programeId = GLES20.glCreateProgram();

        GLES20.glAttachShader(programeId, vshader);
        GLES20.glAttachShader(programeId, fshader);

        GLES20.glLinkProgram(programeId);

        GLES20.glGetProgramiv(programeId, GLES20.GL_LINK_STATUS, status, 0);


        if (status[0] != GLES20.GL_TRUE) {
            throw new IllegalStateException("link program:" + GLES20.glGetProgramInfoLog(programeId));
        }

        GLES20.glDeleteShader(vshader);
        GLES20.glDeleteShader(fshader);

        return programeId;

    }

    public static void glGenTextures(int[] textures) {
        GLES20.glGenTextures(textures.length, textures, 0);


        for (int i = 0; i < textures.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);


            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);


            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        }
    }


    public static void copyAssets2SdCard(Context context, String src, String dst) {
        try {

            File file = new File(dst);
            if (!file.exists()) {
                InputStream is = context.getAssets().open(src);
                FileOutputStream fos = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[2048];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
