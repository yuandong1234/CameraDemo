package com.yuong.camera;

import android.graphics.Bitmap;

public interface TakePictureCallBack {
    void captureResult(Bitmap bitmap, String path, boolean isVertical);
}
