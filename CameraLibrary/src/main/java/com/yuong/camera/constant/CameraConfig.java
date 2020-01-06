package com.yuong.camera.constant;

public class CameraConfig {
    //操作
    public final static int TYPE_PICTURE = 1;//拍照
    public final static int TYPE_VIDEO = 2;//录像

    //预览
    public final static int PREVIEW_NONE = 0;//无操作
    public final static int PREVIEW_PICTURE = 1;//拍照
    public final static int PREVIEW_VIDEO = 2;//录像

    //录制视频比特率
    public static final int MEDIA_QUALITY_HIGH = 20 * 100000;
    public static final int MEDIA_QUALITY_MIDDLE = 16 * 100000;
    public static final int MEDIA_QUALITY_LOW = 12 * 100000;
    public static final int MEDIA_QUALITY_POOR = 8 * 100000;
    public static final int MEDIA_QUALITY_FUNNY = 4 * 100000;
    public static final int MEDIA_QUALITY_DESPAIR = 2 * 100000;
    public static final int MEDIA_QUALITY_SORRY = 1 * 80000;

    //拍照
    public static final int CAMERA_REQUEST_CODE = 1000;
    public static final int CAMERA_RESULT_CODE = 1001;
    public static final String CAMERA_RESULT = "camera_result";
    public static final String CAMERA_RESULT_TYPE = "camera_result_type";

    //图片选择
    public static final String PICTURE_MAX_SIZE = "picture_max_size";
    public static final String PICTURE_SELECTED_RESULT = "picture_selected_result";
    public static final int PICTURE_SELECTOR_REQUEST_CODE = 1002;
    public static final int PICTURE_SELECTOR_RESULT_CODE = 1003;

}
