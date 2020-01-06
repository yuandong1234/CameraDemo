package com.yuong.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

public class CameraView extends FrameLayout implements View.OnClickListener, SurfaceHolder.Callback, CameraStatusListener,
        CameraListener, CaptureListener, TakePictureCallBack, RecordVideoCallBack {
    private static final String TAG = CameraView.class.getSimpleName();

    private RelativeLayout titleBarLayout;
    private ImageView ivCancel;
    private TextView tvConfirm;

    private VideoView mVideoView;
    private ImageView mPhoto;
    private CaptureLayout captureLayout;

    private Context mContext;
    private float mWidth, mHeight;

    private int mPreviewType = CameraConstant.PREVIEW_NONE;
    private Bitmap captureBitmap;
    private MediaPlayer mMediaPlayer;


    private CameraCallBack mCallBack;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        setWillNotDraw(false);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_camera_view, this);
        mVideoView = view.findViewById(R.id.video_preview);
        mPhoto = view.findViewById(R.id.image_photo);
        captureLayout = view.findViewById(R.id.captureLayout);
        titleBarLayout = view.findViewById(R.id.title_bar_layout);
        ivCancel = view.findViewById(R.id.iv_cancel);
        tvConfirm = view.findViewById(R.id.tv_confirm);
        mVideoView.getHolder().addCallback(this);
        ivCancel.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
        captureLayout.setCameraListener(this);
        captureLayout.setCaptureListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cancel:
                resetState();
                break;
            case R.id.tv_confirm:
                Log.e(TAG, "下一步。。。。");
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = mVideoView.getMeasuredWidth();
        mHeight = mVideoView.getMeasuredHeight();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread() {
            @Override
            public void run() {
                CameraHelper.getInstance().startCamera(CameraView.this);
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraHelper.getInstance().destroyCamera();
    }

    @Override
    public void onCameraOpened() {
        CameraHelper.getInstance().startPreview(mContext, mVideoView.getHolder(), mWidth, mHeight);
    }

    @Override
    public void onCameraSwitch() {
        CameraHelper.getInstance().switchCamera(this);
    }

    @Override
    public void onCameraClose() {
        if (mCallBack != null) {
            mCallBack.onCameraClose();
        }
    }

    @Override
    public void onOpenAlbum() {

    }

    @Override
    public void takePicture() {
        Log.e(TAG, "点击拍照.... " + System.currentTimeMillis());
        CameraHelper.getInstance().takePicture(mContext, this);
    }

    @Override
    public void captureResult(Bitmap bitmap, String path, boolean isVertical) {
        Log.e(TAG, "拍照结果 " + System.currentTimeMillis());
        if (mCallBack != null) {
            mCallBack.onPicture(path);
        }
        preViewPicture(bitmap, isVertical);
    }

    @Override
    public void recordStart() {
        Log.e(TAG, "开始录像.... " + System.currentTimeMillis());
        CameraHelper.getInstance().startRecord(mContext, mVideoView.getHolder().getSurface());
        captureLayout.setCaptureMenuVisible(false);
    }

    @Override
    public void recordEnd() {
        CameraHelper.getInstance().stopRecord(false, this);
    }

    @Override
    public void recordShort() {
        CameraHelper.getInstance().stopRecord(true, this);
    }

    @Override
    public void recordResult(String path) {
        Log.e(TAG, "录像结果.... " + System.currentTimeMillis());
        preViewVideo(path);
    }

    private void preViewPicture(Bitmap bitmap, boolean isVertical) {
//        if (isVertical) {
//            mPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        } else {
//            mPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        }
        captureBitmap = bitmap;
        mPreviewType = CameraConstant.PREVIEW_PICTURE;
        mPhoto.setImageBitmap(bitmap);
        mPhoto.setVisibility(VISIBLE);
        captureLayout.setVisibility(View.GONE);
        titleBarLayout.setVisibility(View.VISIBLE);
    }

    private void resetState() {
        captureLayout.setVisibility(View.VISIBLE);
        captureLayout.setCaptureEnable();
        titleBarLayout.setVisibility(View.GONE);
        mPhoto.setVisibility(View.GONE);
        stopVideo();
        mPreviewType = CameraConstant.PREVIEW_NONE;
        captureLayout.setCaptureMenuVisible(true);
        mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        CameraHelper.getInstance().startPreview(mContext, mVideoView.getHolder(), mWidth, mHeight);
    }

    private void preViewVideo(final String path) {
        mPreviewType = CameraConstant.PREVIEW_VIDEO;
        mPhoto.setVisibility(View.GONE);
        captureLayout.setVisibility(View.GONE);
        titleBarLayout.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void run() {
                try {
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(path);
                    mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                            .OnVideoSizeChangedListener() {
                        @Override
                        public void
                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight());
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopVideo() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void updateVideoViewSize(float videoWidth, float videoHeight) {
        if (videoWidth > videoHeight) {
            LayoutParams videoViewParam;
            int height = (int) ((videoHeight / videoWidth) * getWidth());
            videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, height);
            videoViewParam.gravity = Gravity.CENTER;
            mVideoView.setLayoutParams(videoViewParam);
        }
    }


    public void start() {
        CameraHelper.getInstance().registerSensor(mContext);
        CameraHelper.getInstance().startPreview(mContext, mVideoView.getHolder(), mWidth, mHeight);
    }

    public void stop() {
        CameraHelper.getInstance().unRegisterSensor();
    }


    public boolean onBackPressed() {
        boolean isClose = mPreviewType == CameraConstant.PREVIEW_NONE;
        if (!isClose) {
            resetState();
        }
        return isClose;
    }

    public void setCallBack(CameraCallBack callBack) {
        this.mCallBack = callBack;
    }

}
