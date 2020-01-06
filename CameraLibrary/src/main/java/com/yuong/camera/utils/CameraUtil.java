package com.yuong.camera.utils;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraUtil {
    private static final String TAG = CameraUtil.class.getSimpleName();

    private CameraSizeComparator sizeComparator = new CameraSizeComparator();

    private CameraUtil() {
    }

    private static class Holder {
        private static final CameraUtil INSTANCE = new CameraUtil();
    }

    public static CameraUtil getInstance() {
        return Holder.INSTANCE;
    }

    public Camera.Size getPreviewSize(List<Camera.Size> list, float th, float tw) {
        float rate = th / tw;
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            Log.i(TAG, "支持预览分辨率 :w = " + s.width + " h = " + s.height);
            //camera 中的宽度和高度相反
            if ((s.width > th) && equalRate(s, rate)) {
                Log.i(TAG, "Preview :w = " + s.width + " h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            return getBestSize(list, rate);
        } else {
            return list.get(i);
        }
    }

    private boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.2;
    }

    private Camera.Size getBestSize(List<Camera.Size> list, float rate) {
        float previewDisparity = 100;
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            Camera.Size cur = list.get(i);
            float prop = (float) cur.width / (float) cur.height;
            if (Math.abs(rate - prop) < previewDisparity) {
                previewDisparity = Math.abs(rate - prop);
                index = i;
            }
        }
        return list.get(index);
    }

    public Camera.Size getPictureSize(List<Camera.Size> list, float th, float tw) {
        Collections.sort(list, sizeComparator);
        float rate = th / tw;
        int i = 0;
        for (Camera.Size s : list) {
            Log.i(TAG, "支持图片尺寸 :w = " + s.width + " h = " + s.height);
            if ((s.width > th) && equalRate(s, rate)) {
                Log.i(TAG, "Picture :w = " + s.width + " h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            return getBestSize(list, rate);
        } else {
            return list.get(i);
        }
    }

    public Camera.Size getVideoSize(List<Camera.Size> list, float th, float tw,float adjustH) {
        float rate = th / tw;
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            Log.i(TAG, "支持视频预览分辨率 :w = " + s.width + " h = " + s.height);
            //camera 中的宽度和高度相反
            if ((s.width > adjustH) && equalRate(s, rate)) {
                Log.i(TAG, "video :w = " + s.width + " h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            return getBestSize(list, rate);
        } else {
            return list.get(i);
        }
    }

    private class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public int getCameraDisplayOrientation(Context context, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = wm.getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
}
