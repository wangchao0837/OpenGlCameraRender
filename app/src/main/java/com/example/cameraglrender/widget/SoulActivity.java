package com.example.cameraglrender.widget;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cameraglrender.R;
import com.example.cameraglrender.util.DecoderFinishListener;

public class SoulActivity extends AppCompatActivity implements DecoderFinishListener {

    private SoulView soulView;
    private Button mReplayer;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soul);

        String path = getIntent().getStringExtra("path");

        soulView = findViewById(R.id.soul_view);
        mReplayer = findViewById(R.id.replayer);
        soulView.setDataSource(path);

        soulView.start();

        soulView.setOnDecoderListener(this);

        handler = new Handler();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        soulView.stop();
    }

    @Override
    public void decoderFinish() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                mReplayer.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onReplay(View view) {

        mReplayer.setVisibility(View.GONE);

        soulView.start();
    }
}
