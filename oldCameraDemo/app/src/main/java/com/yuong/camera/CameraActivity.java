package com.yuong.camera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class CameraActivity extends AppCompatActivity implements CameraCallBack {

    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        int options = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(options);
        setContentView(R.layout.activity_camera);
        initView();
    }

    private void initView() {
        mCameraView = findViewById(R.id.cameraView);
        mCameraView.setCallBack(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.stop();
    }

    @Override
    public void onCameraClose() {
        this.finish();
    }

    @Override
    public void onPicture(String path) {
        Log.e("CameraActivity", "path : " + path);
    }

    @Override
    public void onVideo(String path) {

    }

    @Override
    public void onBackPressed() {
        boolean isClose = mCameraView.onBackPressed();
        if (isClose) {
            finish();
        }
    }
}
