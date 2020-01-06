package com.yuong.camera.core;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.yuong.camera.callback.CameraCaptureCallBack;
import com.yuong.camera.callback.CameraVideoCallBack;
import com.yuong.camera.listener.CameraStatusListener;

public interface ICamera {

    void checkAvailableCameras();

    void openCamera(int cameraId);

    void startCamera(CameraStatusListener listener);

    void startPreview(Context context, SurfaceHolder holder, float width, float height);

    void stopPreview();

    void destroyCamera();

    void switchCamera(CameraStatusListener listener);

    void takePicture(Context context, CameraCaptureCallBack callBack);

    void startRecord(Context context, Surface surface);

    void stopRecord(boolean isShort, CameraVideoCallBack callback);

    void registerSensor(Context context);

    void unRegisterSensor();
}
