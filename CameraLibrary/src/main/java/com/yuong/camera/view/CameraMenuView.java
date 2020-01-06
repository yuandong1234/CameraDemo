package com.yuong.camera.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yuong.camera.R;
import com.yuong.camera.constant.CameraConfig;
import com.yuong.camera.listener.CameraListener;
import com.yuong.camera.listener.CaptureListener;
import com.yuong.camera.utils.ActivityUtil;
import com.yuong.camera.utils.ScreenUtil;


public class CameraMenuView extends FrameLayout implements View.OnClickListener, CaptureListener {
    private static final String TAG = CameraMenuView.class.getSimpleName();
    private RelativeLayout mCameraMenu;
    private TextView mTvVideo, mTvPicture;
    private View mVideoIndicator, mPictureIndicator, mViewIndicator;
    private CameraButtonView mCameraButtonView;

    private Context mContext;
    private int[] mVideoIndicatorLocation, mPictureIndicatorLocation;
    private int mType = CameraConfig.TYPE_PICTURE;
    private CameraListener mCameraListener;
    private CaptureListener mCaptureListener;


    public void setCameraListener(CameraListener cameraListener) {
        this.mCameraListener = cameraListener;
    }

    public void setCaptureListener(CaptureListener captureListener) {
        this.mCaptureListener = captureListener;
    }

    public CameraMenuView(Context context) {
        this(context, null);
    }

    public CameraMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_camera_menu_view, this);
        mCameraMenu = view.findViewById(R.id.camera_menu);
        ImageView iv_camera_close = view.findViewById(R.id.iv_camera_close);
        TextView tv_camera_switch = view.findViewById(R.id.tv_camera_switch);
        TextView tv_album = view.findViewById(R.id.tv_album);
        mTvVideo = view.findViewById(R.id.tv_video);
        mVideoIndicator = view.findViewById(R.id.video_indicator);
        mTvPicture = view.findViewById(R.id.tv_picture);
        mPictureIndicator = view.findViewById(R.id.picture_indicator);
        mViewIndicator = view.findViewById(R.id.view_indicator);
        RelativeLayout layoutVideo = view.findViewById(R.id.layout_video);
        RelativeLayout layoutPicture = view.findViewById(R.id.layout_picture);
        mCameraButtonView = view.findViewById(R.id.cameraButtonView);
        iv_camera_close.setOnClickListener(this);
        tv_camera_switch.setOnClickListener(this);
        tv_album.setOnClickListener(this);
        layoutVideo.setOnClickListener(this);
        layoutPicture.setOnClickListener(this);
        mCameraButtonView.setCaptureListener(this);
        mTvPicture.getPaint().setFakeBoldText(true);
        mCameraButtonView.setType(mType);

        mVideoIndicator.post(new Runnable() {
            @Override
            public void run() {
                mVideoIndicatorLocation = ScreenUtil.getViewScreenPosition(mVideoIndicator);
            }
        });


        mPictureIndicator.post(new Runnable() {
            @Override
            public void run() {
                mPictureIndicatorLocation = ScreenUtil.getViewScreenPosition(mPictureIndicator);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mViewIndicator.getLayoutParams();
                params.leftMargin = mPictureIndicatorLocation[0];
                //注意状态栏的高度
                if (ActivityUtil.isFullScreen((Activity) mContext)) {
                    params.topMargin = mPictureIndicatorLocation[1];
                } else {
                    params.topMargin = mPictureIndicatorLocation[1] - ScreenUtil.getStatusHeight(mContext);
                }
                mViewIndicator.setLayoutParams(params);
            }
        });
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.layout_picture) {
            if (mType == CameraConfig.TYPE_PICTURE) {
                return;
            }
            int distance = mPictureIndicatorLocation[0] - mVideoIndicatorLocation[0];
            startAnimation(-distance, 0, CameraConfig.TYPE_PICTURE);

        } else if (i == R.id.layout_video) {
            if (mType == CameraConfig.TYPE_VIDEO) {
                return;
            }
            int distance = mPictureIndicatorLocation[0] - mVideoIndicatorLocation[0];
            startAnimation(0, -distance, CameraConfig.TYPE_VIDEO);

        } else if (i == R.id.iv_camera_close) {
            if (mCameraListener != null) {
                mCameraListener.onCameraClose();
            }

        } else if (i == R.id.tv_camera_switch) {
            if (mCameraListener != null) {
                mCameraListener.onCameraSwitch();
            }

        } else if (i == R.id.tv_album) {
            if (mCameraListener != null) {
                mCameraListener.onOpenAlbum();
            }
        }
    }

    private void startAnimation(int startX, int endX, final int type) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mViewIndicator, "translationX", startX, endX);
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
        mCameraButtonView.setType(type);
        switch (type) {
            case CameraConfig.TYPE_PICTURE:
                mTvVideo.getPaint().setFakeBoldText(false);
                mTvPicture.getPaint().setFakeBoldText(true);
                break;
            case CameraConfig.TYPE_VIDEO:
                mTvVideo.getPaint().setFakeBoldText(true);
                mTvPicture.getPaint().setFakeBoldText(false);
                break;
        }
        mTvVideo.postInvalidate();
        mTvPicture.postInvalidate();
    }

    @Override
    public void takePicture() {
        Log.i(TAG, "点击拍照.... " + System.currentTimeMillis());
        if (mCaptureListener != null) {
            mCaptureListener.takePicture();
        }
    }

    @Override
    public void recordStart() {
        Log.i(TAG, "开始录像.... " + System.currentTimeMillis());
        if (mCaptureListener != null) {
            mCaptureListener.recordStart();
        }
    }

    @Override
    public void recordEnd() {
        Log.i(TAG, "停止录像.... " + System.currentTimeMillis());
        if (mCaptureListener != null) {
            mCaptureListener.recordEnd();
        }
    }

    @Override
    public void recordShort() {
        Log.i(TAG, "停止录像 录像事件过短.... " + System.currentTimeMillis());
        if (mCaptureListener != null) {
            mCaptureListener.recordShort();
        }
    }

    public void setCaptureAble() {
        mCameraButtonView.setCaptureAble(true);
    }

    public void setCaptureMenuVisible(boolean visible) {
        if (visible) {
            mCameraMenu.setVisibility(View.VISIBLE);
        } else {
            mCameraMenu.setVisibility(View.INVISIBLE);
        }
    }
}
