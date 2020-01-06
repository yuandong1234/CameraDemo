package com.yuong.camera.callback;

import android.graphics.Bitmap;

public interface CameraCaptureCallBack {
    void captureResult(Bitmap bitmap, String path, boolean isVertical);
}
