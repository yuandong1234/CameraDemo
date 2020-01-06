package com.yuong.camera.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yuong.camera.R;
import com.yuong.camera.constant.CameraConfig;
import com.yuong.camera.listener.CaptureListener;

public class CameraButtonView extends FrameLayout implements View.OnClickListener, RecordButtonView.RecordListener {
    private static final String TAG = CameraButtonView.class.getSimpleName();
    private LinearLayout mTimeView;
    private View mSpotView;
    private TextView mTvTime;
    private FrameLayout mCaptureView, mVideoView;
    private View mCapture, mVideo;
    private RecordButtonView mRecordButtonView;

    private Context mContext;
    private CaptureListener mCaptureListener;

    public void setCaptureListener(CaptureListener captureListener) {
        this.mCaptureListener = captureListener;
    }

    public CameraButtonView(Context context) {
        this(context, null);
    }

    public CameraButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_camera_button_view, this);
        mTimeView = view.findViewById(R.id.time_view);
        mSpotView = view.findViewById(R.id.view_spot);
        mTvTime = view.findViewById(R.id.tv_time);
        mCaptureView = view.findViewById(R.id.capture_view);
        mCapture = view.findViewById(R.id.view_capture);
        mVideoView = view.findViewById(R.id.video_view);
        mVideo = view.findViewById(R.id.view_video);
        mRecordButtonView = view.findViewById(R.id.recordButtonView);
        mTimeView.setVisibility(View.INVISIBLE);
        mVideoView.setVisibility(View.GONE);
        mRecordButtonView.setVisibility(View.GONE);
        mCapture.setOnClickListener(this);
        mVideo.setOnClickListener(this);
        mRecordButtonView.setOnClickListener(this);
        mRecordButtonView.setRecordListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.view_capture) {
            Log.i(TAG, "点击拍照.... " + System.currentTimeMillis());
            takePicture();

        } else if (i == R.id.view_video) {
            Log.i(TAG, "开始录像.... " + System.currentTimeMillis());
            startRecord();

        } else if (i == R.id.recordButtonView) {
            Log.i(TAG, "停止录像.... " + System.currentTimeMillis());
            stopRecord();

        }
    }

    public void setType(int type) {
        mTimeView.setVisibility(View.INVISIBLE);
        if (type == CameraConfig.TYPE_PICTURE) {
            mCaptureView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
        } else {
            mCaptureView.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
            mRecordButtonView.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
        }
    }

    private void takePicture() {
        startCaptureAnimation();
        setCaptureAble(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mCaptureListener != null) {
                    mCaptureListener.takePicture();
                }
            }
        }).start();
    }

    private void startRecord() {
        startRecordAnimation(mVideo, mRecordButtonView);
        mTimeView.setVisibility(View.VISIBLE);
        if (mCaptureListener != null) {
            mCaptureListener.recordStart();
        }
        mRecordButtonView.start();
        startRecordSpotAnimation();
    }

    private void stopRecord() {
        mRecordButtonView.stop();
    }

    private void resetState() {
        startRecordAnimation(mRecordButtonView, mVideo);
        mTimeView.setVisibility(View.GONE);
        mSpotView.clearAnimation();
    }

    @Override
    public void recordShort() {
        Toast.makeText(mContext, getResources().getString(R.string.record_time_short), Toast.LENGTH_SHORT).show();
        resetState();
        if (mCaptureListener != null) {
            mCaptureListener.recordShort();
        }
    }

    @Override
    public void recordEnd() {
        resetState();
        if (mCaptureListener != null) {
            mCaptureListener.recordEnd();
        }
    }

    @Override
    public void onDuration(int duration) {
        float total = mRecordButtonView.getDuration() / 1000f;
        duration = duration * 10 / 1000;
        float recordTime = duration / 10f;
        String time = recordTime + "  |  " + total;
        mTvTime.setText(time);
    }


    private void startCaptureAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mCapture, "scaleX", 1, 0.9f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mCapture, "scaleY", 1, 0.9f);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(200);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(200);

        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(mCapture, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(mCapture, "scaleY", 0.9f, 1f);
        scaleX2.setInterpolator(new LinearInterpolator());
        scaleX2.setDuration(50);
        scaleX2.setStartDelay(200);
        scaleY2.setInterpolator(new LinearInterpolator());
        scaleY2.setDuration(50);
        scaleY2.setStartDelay(200);

        set.play(scaleX).with(scaleY).with(scaleX2).with(scaleY2);
        set.start();
    }

    private void startRecordAnimation(final View oldView, View newView) {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(oldView, "scaleX", 1, 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(oldView, "scaleY", 1, 0);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(200);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(200);

        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(newView, "scaleX", 0, 1);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(newView, "scaleY", 0, 1);

        scaleX2.setInterpolator(new LinearInterpolator());
        scaleX2.setDuration(300);
        scaleY2.setInterpolator(new LinearInterpolator());
        scaleY2.setDuration(300);

        set.play(scaleX).with(scaleY).with(scaleX2).with(scaleY2);
        newView.setVisibility(View.VISIBLE);
        set.start();

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                oldView.setVisibility(View.GONE);
                oldView.setScaleX(1.0f);
                oldView.setScaleY(1.0f);
            }
        });
    }

    private void startRecordSpotAnimation() {
        AlphaAnimation alpha = new AlphaAnimation(0.0f, 1.0f);
        alpha.setDuration(1000);
        alpha.setRepeatCount(-1);
        alpha.setFillBefore(true);
        mSpotView.startAnimation(alpha);
    }

    public void setCaptureAble(boolean able) {
        mCapture.setEnabled(able);
    }
}
