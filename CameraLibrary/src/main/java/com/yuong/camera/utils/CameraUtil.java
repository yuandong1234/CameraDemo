package com.yuong.camera.utils;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static android.hardware.Camera.CameraInfo;
import static android.hardware.Camera.getCameraInfo;

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

    public Camera.Size getVideoSize(List<Camera.Size> list, float th, float tw, float adjustH) {
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
        CameraInfo info = new CameraInfo();
        getCameraInfo(cameraId, info);
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
        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    /**
     * 获得适合的图片分辨率
     *
     * @param list
     * @param th
     * @param tw
     * @param maxWidth
     * @return
     */
    public Camera.Size getBestPictureSize2(List<Camera.Size> list, float th, float tw, float maxWidth) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) tw / (double) th;
        Camera.Size optimalSize = null;

        Collections.sort(list, sizeComparator);

        StringBuilder builder = new StringBuilder();
        for (Camera.Size supportedPicResolution : list) {
            double rate = (double) supportedPicResolution.height / (double) supportedPicResolution.width;
            builder.append("\n")
                    .append(supportedPicResolution.width)
                    .append('x')
                    .append(supportedPicResolution.height)
                    .append("   rate : " + rate);
        }

        Log.i(TAG, "Supported picture size: " + builder.toString());
        Log.i(TAG, "targetRatio " + targetRatio);
        Iterator<Camera.Size> it = list.iterator();

        while (it.hasNext()) {
            Camera.Size size = it.next();
            int width = size.width;
            int height = size.height;

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比

            double ratio = (double) height / (double) width;
            double diff = Math.abs(ratio - targetRatio);
            if (height > maxWidth || diff > ASPECT_TOLERANCE) {
                it.remove();
            }
        }

        StringBuilder builder2 = new StringBuilder();
        for (Camera.Size supportedPicResolution : list) {
            builder2.append("\n")
                    .append(supportedPicResolution.width)
                    .append('x')
                    .append(supportedPicResolution.height);

        }

        Log.i(TAG, "Supported picture size2: " + builder2.toString());

        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的
        if (!list.isEmpty()) {
            optimalSize = list.get(list.size() - 1);
            Log.e(TAG, "picture size:  width : " + optimalSize.width + "  height : " + optimalSize.height);
        }

        return optimalSize;
    }

    /**
     * 获得适合的图片分辨率
     *
     * @param list
     * @param th
     * @param tw
     * @param maxWidth 最大图片的宽度
     * @return
     */
    public Camera.Size getBestPictureSize(List<Camera.Size> list, float th, float tw, float maxWidth) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) tw / (double) th;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        Collections.sort(list, sizeComparator);

        StringBuilder builder = new StringBuilder();
        for (Camera.Size supportedPicResolution : list) {
            double rate = (double) supportedPicResolution.height / (double) supportedPicResolution.width;
            builder.append("\n")
                    .append(supportedPicResolution.width)
                    .append('x')
                    .append(supportedPicResolution.height)
                    .append("   rate : " + rate);
        }

        Log.i(TAG, "Supported picture size: " + builder.toString());
        Log.i(TAG, "targetRatio " + targetRatio);

        for (Camera.Size size : list) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            Log.i(TAG, " size width : " + size.width + "  height : " + size.height);
            if (size.height <= maxWidth) {
                optimalSize = size;
            }
        }

        if (optimalSize == null) {
            for (Camera.Size size : list) {
                if (Math.abs(size.height - maxWidth) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - maxWidth);
                }
            }
        }
        Log.e(TAG, "picture size:  width : " + optimalSize.width + "  height : " + optimalSize.height);
        return optimalSize;
    }
}
