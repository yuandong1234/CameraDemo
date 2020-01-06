package com.yuong.camera;

public interface CaptureListener {
    void takePicture();

    void recordStart();

    void recordEnd();

    void recordShort();
}
