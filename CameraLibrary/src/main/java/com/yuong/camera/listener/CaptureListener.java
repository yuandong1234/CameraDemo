package com.yuong.camera.listener;

public interface CaptureListener {
    void takePicture();

    void recordStart();

    void recordEnd();

    void recordShort();
}
