package com.yuong.camera.utils;

import android.app.Activity;
import android.view.WindowManager;

public class ActivityUtil {
    public static boolean isFullScreen(Activity activity) {
        return (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }
}
