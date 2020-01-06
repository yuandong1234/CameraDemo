package com.yuong.camera.callback;

public interface CameraCallBack {
    void onCameraClose();

    void onPicture(String path);

    void onVideo(String path);

    void onOpenAlbum();

    void onNext();
}
