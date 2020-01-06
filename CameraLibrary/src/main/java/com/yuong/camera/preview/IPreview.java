package com.yuong.camera.preview;

import android.view.View;

public interface IPreview {
    void startPreview(View view, Object result);

    void stopPreview();
}
