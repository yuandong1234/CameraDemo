package com.yuong.camera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.yuong.camera.R;
import com.yuong.camera.callback.CameraCallBack;
import com.yuong.camera.callback.CameraCaptureCallBack;
import com.yuong.camera.callback.CameraVideoCallBack;
import com.yuong.camera.constant.CameraConfig;
import com.yuong.camera.core.CameraService;
import com.yuong.camera.listener.CameraListener;
import com.yuong.camera.listener.CameraStatusListener;
import com.yuong.camera.listener.CaptureListener;
import com.yuong.camera.preview.PicturePreview;
import com.yuong.camera.preview.VideoPreview;

public class CameraView extends FrameLayout implements View.OnClickListener, SurfaceHolder.Callback, CameraStatusListener,
        CameraListener, CaptureListener, CameraCaptureCallBack, CameraVideoCallBack {
    private static final String TAG = CameraView.class.getSimpleName();

    private VideoView mVideoView;
    private ImageView mImageView;
    private RelativeLayout mTitleBarView;
    private CameraMenuView mCameraMenuView;

    private Context mContext;
    private float mWidth, mHeight;
    private int mPreviewType = CameraConfig.PREVIEW_NONE;
    private CameraCallBack mCameraCallBack;

    public void setCameraCallBack(CameraCallBack callBack) {
        this.mCameraCallBack = callBack;
    }

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
        mImageView = view.findViewById(R.id.image_preview);
        mTitleBarView = view.findViewById(R.id.title_bar);
        ImageView iv_cancel = view.findViewById(R.id.iv_cancel);
        TextView tv_next = view.findViewById(R.id.tv_next);
        mCameraMenuView = view.findViewById(R.id.cameraMenuView);
        mVideoView.getHolder().addCallback(this);
        iv_cancel.setOnClickListener(this);
        tv_next.setOnClickListener(this);
        mCameraMenuView.setCameraListener(this);
        mCameraMenuView.setCaptureListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = mVideoView.getMeasuredWidth();
        mHeight = mVideoView.getMeasuredHeight();
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_cancel) {
            restart();
        } else if (i == R.id.tv_next) {
            if (mCameraCallBack != null) {
                mCameraCallBack.onNext();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread() {
            @Override
            public void run() {
                CameraService.getInstance().startCamera(CameraView.this);
            }
        }.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraService.getInstance().destroyCamera();
        resetState();
    }

    @Override
    public void onCameraOpened() {
        CameraService.getInstance().startPreview(mContext, mVideoView.getHolder(), mWidth, mHeight);
    }

    @Override
    public void onCameraUnavailable() {
        Toast.makeText(mContext, getResources().getString(R.string.camera_unavailable), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraSwitch() {
        CameraService.getInstance().switchCamera(this);
    }

    @Override
    public void onCameraClose() {
        if (mCameraCallBack != null) {
            mCameraCallBack.onCameraClose();
        }
    }

    @Override
    public void onOpenAlbum() {
        if (mCameraCallBack != null) {
            mCameraCallBack.onOpenAlbum();
        }
    }

    @Override
    public void takePicture() {
        Log.i(TAG, "点击拍照.... " + System.currentTimeMillis());
        CameraService.getInstance().takePicture(mContext, this);
    }

    @Override
    public void recordStart() {
        Log.i(TAG, "开始录像.... " + System.currentTimeMillis());
        CameraService.getInstance().startRecord(mContext, mVideoView.getHolder().getSurface());
        mCameraMenuView.setCaptureMenuVisible(false);
    }

    @Override
    public void recordEnd() {
        Log.i(TAG, "停止录像.... " + System.currentTimeMillis());
        CameraService.getInstance().stopRecord(false, this);
    }

    @Override
    public void recordShort() {
        Log.i(TAG, "停止录像 录像事件过短.... " + System.currentTimeMillis());
        CameraService.getInstance().stopRecord(true, this);
    }


    @Override
    public void captureResult(Bitmap bitmap, String path, boolean isVertical) {
        mPreviewType = CameraConfig.PREVIEW_PICTURE;
        mCameraMenuView.setVisibility(View.GONE);
        mTitleBarView.setVisibility(View.VISIBLE);
        PicturePreview.getInstance().startPreview(mImageView, bitmap);
        if (mCameraCallBack != null) {
            mCameraCallBack.onPicture(path);
        }
    }

    @Override
    public void recordResult(String path) {
        if (!TextUtils.isEmpty(path)) {
            mPreviewType = CameraConfig.PREVIEW_VIDEO;
            mCameraMenuView.setVisibility(View.GONE);
            mTitleBarView.setVisibility(View.VISIBLE);
            VideoPreview.getInstance().startPreview(mVideoView, path);
            if (mCameraCallBack != null) {
                mCameraCallBack.onVideo(path);
            }
        } else {
            mPreviewType = CameraConfig.PREVIEW_NONE;
            restart();
        }
    }

    private void resetState() {
        switch (mPreviewType) {
            case CameraConfig.PREVIEW_PICTURE:
                PicturePreview.getInstance().stopPreview();
                mCameraMenuView.setCaptureAble();
                break;
            case CameraConfig.PREVIEW_VIDEO:
                VideoPreview.getInstance().stopPreview();
                break;
        }
        mCameraMenuView.setVisibility(View.VISIBLE);
        mCameraMenuView.setCaptureMenuVisible(true);
        mTitleBarView.setVisibility(View.GONE);
        mPreviewType = CameraConfig.PREVIEW_NONE;
    }

    private void restart() {
        resetState();
        CameraService.getInstance().startPreview(mContext, mVideoView.getHolder(), mWidth, mHeight);
    }

    public void start() {
        CameraService.getInstance().registerSensor(mContext);
        CameraService.getInstance().startPreview(mContext, mVideoView.getHolder(), mWidth, mHeight);
    }

    public void stop() {
        CameraService.getInstance().unRegisterSensor();
        CameraService.getInstance().stopPreview();
    }

    public boolean onBackPressed() {
        boolean isClose = mPreviewType == CameraConfig.PREVIEW_NONE;
        if (!isClose) {
            restart();
        }
        return isClose;
    }

    public int getPreviewType() {
        return mPreviewType;
    }
}
