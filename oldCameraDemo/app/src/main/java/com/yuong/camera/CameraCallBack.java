package com.yuong.camera;

public interface CameraCallBack {
    void onCameraClose();

    void onPicture(String path);

    void onVideo(String path);
}
