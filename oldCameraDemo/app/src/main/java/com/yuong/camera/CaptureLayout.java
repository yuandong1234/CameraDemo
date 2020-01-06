package com.yuong.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CaptureLayout extends FrameLayout implements View.OnClickListener {
    private static final String TAG = CaptureLayout.class.getSimpleName();
    private RelativeLayout captureMenu;
    private ImageView iv_camera_close;
    private TextView tv_camera_switch, tv_album;
    private TextView tvVideo, tvPicture;
    private View videoIndicator, pictureIndicator, viewIndicator;
    private CaptureButton captureButton;

    private Context mContext;
    private CaptureListener captureListener;
    private CameraListener cameraListener;
    private int mType = CameraConstant.TYPE_PICTURE;
    private int[] videoIndicatorLocation, pictureIndicatorLocation;

    public CaptureLayout(Context context) {
        this(context, null);
    }

    public CaptureLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
        initListener();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_capture, this);
        captureMenu = view.findViewById(R.id.captureMenu);
        iv_camera_close = view.findViewById(R.id.iv_camera_close);
        tv_camera_switch = view.findViewById(R.id.tv_camera_switch);
        tv_album = view.findViewById(R.id.tv_album);
        tvVideo = view.findViewById(R.id.tv_video);
        videoIndicator = view.findViewById(R.id.video_indicator);
        tvPicture = view.findViewById(R.id.tv_picture);
        pictureIndicator = view.findViewById(R.id.picture_indicator);
        viewIndicator = view.findViewById(R.id.view_indicator);
        RelativeLayout layoutVideo = view.findViewById(R.id.layout_video);
        RelativeLayout layoutPicture = view.findViewById(R.id.layout_picture);
        captureButton = view.findViewById(R.id.captureButton);
        layoutVideo.setOnClickListener(this);
        layoutPicture.setOnClickListener(this);
        tvPicture.getPaint().setFakeBoldText(true);
        captureButton.setType(mType);

        videoIndicator.post(new Runnable() {
            @Override
            public void run() {
                videoIndicatorLocation = getViewScreenPosition(videoIndicator);
            }
        });


        pictureIndicator.post(new Runnable() {
            @Override
            public void run() {
                pictureIndicatorLocation = getViewScreenPosition(pictureIndicator);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewIndicator.getLayoutParams();
                params.leftMargin = pictureIndicatorLocation[0];
                //注意状态栏的高度
                if (isFullScreen(mContext)) {
                    params.topMargin = pictureIndicatorLocation[1];
                } else {
                    params.topMargin = pictureIndicatorLocation[1] - ScreenUtil.getStatusHeight(mContext);
                }
                viewIndicator.setLayoutParams(params);
            }
        });
    }

    private void initListener() {
        iv_camera_close.setOnClickListener(this);
        tv_camera_switch.setOnClickListener(this);
        tv_album.setOnClickListener(this);
        captureButton.setCaptureListener(new CaptureListener() {
            @Override
            public void takePicture() {
                Log.e(TAG, "点击拍照.... " + System.currentTimeMillis());
                if (captureListener != null) {
                    captureListener.takePicture();
                }
            }

            @Override
            public void recordStart() {
                Log.e(TAG, "开始录像.... " + System.currentTimeMillis());
                if (captureListener != null) {
                    captureListener.recordStart();
                }
            }

            @Override
            public void recordEnd() {
                Log.e(TAG, "停止录像.... " + System.currentTimeMillis());
                if (captureListener != null) {
                    captureListener.recordEnd();
                }
            }

            @Override
            public void recordShort() {
                Log.e(TAG, "停止录像 录像事件过短.... " + System.currentTimeMillis());
                if (captureListener != null) {
                    captureListener.recordShort();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_picture: {
                if (mType == CameraConstant.TYPE_PICTURE) {
                    return;
                }
                int distance = pictureIndicatorLocation[0] - videoIndicatorLocation[0];
                startAnimation(-distance, 0, CameraConstant.TYPE_PICTURE);
            }
            break;
            case R.id.layout_video: {
                if (mType == CameraConstant.TYPE_VIDEO) {
                    return;
                }
                int distance = pictureIndicatorLocation[0] - videoIndicatorLocation[0];
                startAnimation(0, -distance, CameraConstant.TYPE_VIDEO);
            }
            break;
            case R.id.iv_camera_close:
                if (cameraListener != null) {
                    cameraListener.onCameraClose();
                }
                break;
            case R.id.tv_camera_switch:
                if (cameraListener != null) {
                    cameraListener.onCameraSwitch();
                }
                break;
            case R.id.tv_album:
                if (cameraListener != null) {
                    cameraListener.onOpenAlbum();
                }
                break;
        }
    }

    private void startAnimation(int startX, int endX, final int type) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(viewIndicator, "translationX", startX, endX);
        objectAnimator.setDuration(300);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setCameraType(type);
            }
        });
        objectAnimator.start();
    }

    private void setCameraType(int type) {
        this.mType = type;
        captureButton.setType(type);
        switch (type) {
            case CameraConstant.TYPE_PICTURE:
                tvVideo.getPaint().setFakeBoldText(false);
                tvPicture.getPaint().setFakeBoldText(true);
                break;
            case CameraConstant.TYPE_VIDEO:
                tvVideo.getPaint().setFakeBoldText(true);
                tvPicture.getPaint().setFakeBoldText(false);
                break;
        }
        tvVideo.postInvalidate();
        tvPicture.postInvalidate();
    }

    public void setCaptureEnable() {
        captureButton.setCaptureEnable();
    }

    public void setCaptureMenuVisible(boolean visible) {
        if (visible) {
            captureMenu.setVisibility(View.VISIBLE);
        } else {
            captureMenu.setVisibility(View.INVISIBLE);
        }
    }


    private int[] getViewScreenPosition(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location;
    }

    private boolean isFullScreen(Context context) {
        return (((Activity) context).getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

    public void setCaptureListener(CaptureListener captureListener) {
        this.captureListener = captureListener;
    }

    public void setCameraListener(CameraListener listener) {
        this.cameraListener = listener;
    }
}
