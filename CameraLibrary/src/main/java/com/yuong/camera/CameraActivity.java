package com.yuong.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.yuong.camera.callback.CameraCallBack;
import com.yuong.camera.constant.CameraConfig;
import com.yuong.camera.entity.LocalMedia;
import com.yuong.camera.view.CameraView;

import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements CameraCallBack {

    private CameraView mCameraView;
    private String mPicturePath;
    private String mVideoPath;

    public static void actionStart(Activity activity) {
        Intent intent = new Intent(activity, CameraActivity.class);
        activity.startActivityForResult(intent, CameraConfig.CAMERA_REQUEST_CODE);
    }

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
        mCameraView.setCameraCallBack(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraView.stop();
    }

    @Override
    public void onCameraClose() {
        finish();
    }

    @Override
    public void onPicture(String path) {
        this.mPicturePath = path;
    }

    @Override
    public void onVideo(String path) {
        this.mVideoPath = path;
    }

    @Override
    public void onOpenAlbum() {
        PictureSelectorActivity.actionStart(this, 9);
    }

    @Override
    public void onNext() {
        next();
    }

    private void next() {
        Intent intent = new Intent();

        List<LocalMedia> images = new ArrayList<>();
        LocalMedia media = new LocalMedia();

        int previewType = mCameraView.getPreviewType();
        if (previewType == CameraConfig.PREVIEW_PICTURE) {
            media.setPath(mPicturePath);
            intent.putExtra(CameraConfig.CAMERA_RESULT_TYPE, CameraConfig.TYPE_PICTURE);
        } else if (previewType == CameraConfig.PREVIEW_VIDEO) {
            media.setPath(mVideoPath);
            intent.putExtra(CameraConfig.CAMERA_RESULT_TYPE, CameraConfig.TYPE_VIDEO);
        }
        images.add(media);
        intent.putParcelableArrayListExtra(CameraConfig.CAMERA_RESULT, (ArrayList<? extends Parcelable>) images);
        setResult(CameraConfig.CAMERA_RESULT_CODE, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        boolean isClose = mCameraView.onBackPressed();
        if (isClose) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CameraConfig.PICTURE_SELECTOR_REQUEST_CODE) {
            if (data != null && resultCode == CameraConfig.PICTURE_SELECTOR_RESULT_CODE) {
                List<LocalMedia> selectImages = data.getParcelableArrayListExtra(CameraConfig.PICTURE_SELECTED_RESULT);

                data.putParcelableArrayListExtra(CameraConfig.CAMERA_RESULT, (ArrayList<? extends Parcelable>) selectImages);
                data.putExtra(CameraConfig.CAMERA_RESULT_TYPE, CameraConfig.TYPE_PICTURE);
                setResult(CameraConfig.CAMERA_RESULT_CODE, data);
                CameraActivity.this.finish();
            }
        }
    }
}
