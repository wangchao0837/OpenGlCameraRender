package com.example.cameraglrender.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.example.cameraglrender.R;

public class ScreenFilter extends BaseFilter {


    public ScreenFilter(Context mContext) {
        super(mContext, R.raw.screen_vert, R.raw.screen_frag);

    }


}
