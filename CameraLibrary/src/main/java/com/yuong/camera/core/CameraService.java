package com.yuong.camera.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.yuong.camera.SensorController;
import com.yuong.camera.callback.CameraCaptureCallBack;
import com.yuong.camera.callback.CameraVideoCallBack;
import com.yuong.camera.constant.CameraConfig;
import com.yuong.camera.listener.CameraStatusListener;
import com.yuong.camera.utils.CameraUtil;
import com.yuong.camera.utils.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CameraService implements ICamera, Camera.PreviewCallback {
    private final static String TAG = CameraService.class.getSimpleName();

    private int CAMERA_FRONT = -1;//前摄像头
    private int CAMERA_BACK = -1;//后摄像头
    private int CAMERA_DEFAULT = CAMERA_BACK;//默认后摄像头

    private SurfaceHolder mHolder = null;
    private Camera mCamera;
    private Camera.Parameters mParams;
    private int mCameraAngle = 90;//摄像头角度   默认为90度
    private int mAngle;
    private int nowAngle;
    private boolean isCanCapture;
    private boolean isPreviewing;

    private Bitmap videoFirstFrame = null;
    private byte[] firstFrame_data;
    private String videoPath;

    private MediaRecorder mediaRecorder;
    private int mediaQuality = CameraConfig.MEDIA_QUALITY_MIDDLE;
    private boolean isRecording;

    private float mWidth, mHeight;
    private int mPreviewWidth, mPreviewHeight;

    private SensorController mSensorController;

    private CameraService() {
        checkAvailableCameras();
    }

    private static class Holder {
        private static final CameraService INSTANCE = new CameraService();
    }

    public static CameraService getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void checkAvailableCameras() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cameraNum = Camera.getNumberOfCameras();
        for (int i = 0; i < cameraNum; i++) {
            Camera.getCameraInfo(i, info);
            switch (info.facing) {
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    CAMERA_FRONT = info.facing;
                    break;
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    CAMERA_BACK = info.facing;
                    break;
            }
        }
        CAMERA_DEFAULT = CAMERA_BACK;
    }

    @Override
    public void openCamera(int cameraId) {
        try {
            this.mCamera = Camera.open(cameraId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1 && this.mCamera != null) {
            try {
                this.mCamera.enableShutterSound(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startCamera(CameraStatusListener listener) {
        if (!isCameraAvailable(CAMERA_DEFAULT)) {
            if (listener != null) {
                listener.onCameraUnavailable();
            }
            return;
        }
        if (mCamera == null) {
            openCamera(CAMERA_DEFAULT);
        }

        if (listener != null) {
            listener.onCameraOpened();
        }
    }

    @Override
    public void startPreview(Context context, SurfaceHolder holder, float width, float height) {
        if (isPreviewing) {
            Log.i(TAG, "camera isPreviewing");
        }
        this.mWidth = width;
        this.mHeight = height;

        mCameraAngle = CameraUtil.getInstance().getCameraDisplayOrientation(context, CAMERA_DEFAULT);

        if (holder == null) {
            return;
        }
        this.mHolder = holder;
        if (mCamera != null) {
            try {
                mParams = mCamera.getParameters();
                Camera.Size previewSize = CameraUtil.getInstance().getPreviewSize(mParams.getSupportedPreviewSizes(), height, width);
//                Camera.Size pictureSize = CameraUtil.getInstance().getPictureSize(mParams.getSupportedPictureSizes(), height, width);
                Camera.Size pictureSize = CameraUtil.getInstance().getBestPictureSize(mParams.getSupportedPictureSizes(), height, width, mWidth);

                mParams.setPreviewSize(previewSize.width, previewSize.height);
                Log.i(TAG, "preview_width : " + previewSize.width + "  preview_height : " + previewSize.height);

                mPreviewWidth = previewSize.width;
                mPreviewHeight = previewSize.height;

                if (pictureSize != null) {
                    mParams.setPictureSize(pictureSize.width, pictureSize.height);
                } else {
                    mParams.setPictureSize(1280, 720);//找不到合适的分辨率，则用通用的分辨率
                }

                if (isSupportedFocusMode(mParams.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                } else if (isSupportedFocusMode(mParams.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_AUTO)) {
                    mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }


                if (isSupportedPictureFormats(mParams.getSupportedPictureFormats(), ImageFormat.JPEG)) {
                    mParams.setPictureFormat(ImageFormat.JPEG);
                    mParams.setJpegQuality(100);
                }
                mCamera.setParameters(mParams);
                mParams = mCamera.getParameters();
                mCamera.setPreviewDisplay(holder);  //SurfaceView
                mCamera.setDisplayOrientation(mCameraAngle);//浏览角度
                mCamera.setPreviewCallback(this); //每一帧回调
                mCamera.startPreview();//启动浏览
                isCanCapture = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopPreview() {
        if (null != mCamera) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera.setPreviewDisplay(null);
                isPreviewing = false;
                Log.i(TAG, "=== Stop Preview ===");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroyCamera() {
        if (null != mCamera) {
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                //这句要在stopPreview后执行，不然会卡顿或者花屏
                mCamera.setPreviewDisplay(null);
                mHolder = null;
                isPreviewing = false;
                mCamera.release();
                mCamera = null;
                Log.i(TAG, "=== Destroy Camera ===");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "=== Camera  Null===");
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void switchCamera(CameraStatusListener listener) {
        if (CAMERA_DEFAULT == CAMERA_BACK) {
            CAMERA_DEFAULT = CAMERA_FRONT;
        } else {
            CAMERA_DEFAULT = CAMERA_BACK;
        }
        destroyCamera();
        startCamera(listener);
    }

    @Override
    public void takePicture(Context context, CameraCaptureCallBack callBack) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        String focusMode = parameters.getFocusMode();
        if (focusMode.contains("continuous")) {
            mCamera.cancelAutoFocus();
        }
        if (isCanCapture) {
            takePictureInternal(context, callBack);
            isCanCapture = false;
        }
    }

    /**
     * 拍照
     */

    public void takePictureInternal(final Context context, final CameraCaptureCallBack callback) {
        if (mCamera == null) {
            return;
        }
        switch (mCameraAngle) {
            case 90:
                nowAngle = Math.abs(mAngle + mCameraAngle) % 360;
                break;
            case 270:
                nowAngle = Math.abs(mCameraAngle - mAngle);
                break;
        }
//
        Log.i(TAG, mAngle + " = " + mCameraAngle + " = " + nowAngle);
        Log.e(TAG, "准备拍照 " + System.currentTimeMillis());
        mCamera.takePicture(new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                //快门
            }
        }, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.e(TAG, "拍照结果 " + System.currentTimeMillis());
                isCanCapture = true;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Matrix matrix = new Matrix();
                if (CAMERA_DEFAULT == CAMERA_BACK) {
                    matrix.setRotate(nowAngle);
                } else if (CAMERA_DEFAULT == CAMERA_FRONT) {
                    matrix.setRotate(360 - nowAngle);
                    matrix.postScale(-1, 1);
                }

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

                if (callback != null) {
                    if (nowAngle == 90 || nowAngle == 270) {
                        callback.captureResult(bitmap, file.getAbsolutePath(), true);
                    } else {
                        callback.captureResult(bitmap, file.getAbsolutePath(), false);
                    }

                    savePicture(bitmap, file.getAbsolutePath());
                }
            }
        });
    }

    //开始录像
    @Override
    public void startRecord(Context context, Surface surface) {
        mCamera.setPreviewCallback(null);
        videoPath = "";
        int nowAngle = (mAngle + 90) % 360;
        // saveVideoFirstFrame();

        if (isRecording) return;

        if (mCamera == null) {
            openCamera(CAMERA_DEFAULT);
        }
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
        }
        if (mParams == null) {
            mParams = mCamera.getParameters();
        }
        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        mCamera.setParameters(mParams);
        mCamera.unlock();
        mediaRecorder.reset();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        Camera.Size videoSize = CameraUtil.getInstance().getVideoSize(mParams.getSupportedVideoSizes(), mHeight, mWidth, 600);
        Log.i(TAG, "VideoSize  width = " + videoSize.width + "height = " + videoSize.height);
        mediaRecorder.setVideoSize(videoSize.width, videoSize.height);

        if (CAMERA_DEFAULT == CAMERA_FRONT) {
            //手机预览倒立的处理
            if (mCameraAngle == 270) {
                //横屏
                if (nowAngle == 0) {
                    mediaRecorder.setOrientationHint(180);
                } else if (nowAngle == 270) {
                    mediaRecorder.setOrientationHint(270);
                } else {
                    mediaRecorder.setOrientationHint(90);
                }
            } else {
                if (nowAngle == 90) {
                    mediaRecorder.setOrientationHint(270);
                } else if (nowAngle == 270) {
                    mediaRecorder.setOrientationHint(90);
                } else {
                    mediaRecorder.setOrientationHint(nowAngle);
                }
            }
        } else {
            mediaRecorder.setOrientationHint(nowAngle);
        }

        mediaRecorder.setVideoEncodingBitRate(mediaQuality);
        mediaRecorder.setPreviewDisplay(surface);

        String fileName = "video_" + System.currentTimeMillis() + ".mp4";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        videoPath = file.getAbsolutePath();
        mediaRecorder.setOutputFile(videoPath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopRecord(boolean isShort, CameraVideoCallBack callback) {
        if (!isRecording) {
            return;
        }
        if (mediaRecorder != null) {
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);
            try {
                mediaRecorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
                mediaRecorder = null;
                mediaRecorder = new MediaRecorder();
            } finally {
                if (mediaRecorder != null) {
                    mediaRecorder.release();
                }
                mediaRecorder = null;
                isRecording = false;
            }
            stopPreview();
            if (isShort) {
                FileUtil.deleteFile(videoPath);
                videoPath = "";
            }
            if (callback != null) {
                callback.recordResult(videoPath);
            }
        }
    }

    @Override
    public void registerSensor(Context context) {
        mSensorController = SensorController.getInstance(context);
        mSensorController.setCameraSensorListener(new SensorController.CameraSensorListener() {
            @Override
            public void onAngle(int angle) {
                mAngle = angle;
            }
        });
        mSensorController.start();
    }

    @Override
    public void unRegisterSensor() {
        mSensorController.stop();
    }

    /**
     * 相机是否可用
     */
    private boolean isCameraAvailable(int cameraID) {
        boolean isAvailable = false;
        Camera mCamera = null;
        try {
            mCamera = Camera.open(cameraID);
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            e.printStackTrace();
            isAvailable = false;
        } finally {
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
                isAvailable = true;
            }
        }
        return isAvailable;
    }

    /**
     * 相机是否支持聚焦
     */
    public boolean isSupportedFocusMode(List<String> focusList, String focusMode) {
        for (int i = 0; i < focusList.size(); i++) {
            if (focusMode.equals(focusList.get(i))) {
                Log.i(TAG, "FocusMode supported " + focusMode);
                return true;
            }
        }
        Log.i(TAG, "FocusMode not supported " + focusMode);
        return false;
    }

    /**
     * 相机图片是否支持某种格式
     */
    public boolean isSupportedPictureFormats(List<Integer> supportedPictureFormats, int format) {
        for (int i = 0; i < supportedPictureFormats.size(); i++) {
            if (format == supportedPictureFormats.get(i)) {
                Log.i(TAG, "Formats supported " + format);
                return true;
            }
        }
        Log.i(TAG, "Formats not supported " + format);
        return false;
    }

    /**
     * 图片保存到本地
     *
     * @param bitmap
     * @param path
     */
    private void savePicture(final Bitmap bitmap, final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.bitmap2File(bitmap, path);
            }
        }).start();
    }
}
