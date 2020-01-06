package com.yuong.camera;

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

public class CaptureButton extends FrameLayout implements View.OnClickListener, RecordProgressView.RecordListener {
    private static final String TAG = CaptureButton.class.getSimpleName();
    private FrameLayout layout_capture;
    private View view_capture;

    private LinearLayout ll_video;
    private View view_video_spot;
    private TextView tv_video_time;
    private FrameLayout layout_video;
    private View view_video;
    private RecordProgressView recordProgressView;

    private Context mContext;
    private CaptureListener captureListener;

    public CaptureButton(Context context) {
        this(context, null);
    }

    public CaptureButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_capture_button, this);
        ll_video = view.findViewById(R.id.ll_video);
        view_video_spot = view.findViewById(R.id.view_video_spot);
        tv_video_time = view.findViewById(R.id.tv_video_time);
        layout_capture = view.findViewById(R.id.layout_capture);
        view_capture = view.findViewById(R.id.view_capture);
        layout_video = view.findViewById(R.id.layout_video);
        view_video = view.findViewById(R.id.view_video);
        recordProgressView = view.findViewById(R.id.recordProgressView);
        view_capture.setOnClickListener(this);
        view_video.setOnClickListener(this);
        recordProgressView.setOnClickListener(this);
        recordProgressView.setCountdownProgressListener(this);
        ll_video.setVisibility(View.INVISIBLE);
        layout_video.setVisibility(View.GONE);
        recordProgressView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_capture:
                Log.e(TAG, "点击拍照.... " + System.currentTimeMillis());
                takePicture();
                break;
            case R.id.view_video:
                Log.e(TAG, "点击录像.... " + System.currentTimeMillis());
                startRecord();
                break;
            case R.id.recordProgressView:
                Log.e(TAG, "停止录像.... " + System.currentTimeMillis());
                stopRecord();
                break;
        }
    }

    @Override
    public void recordShort() {
        Toast.makeText(mContext, "录制时间过短!", Toast.LENGTH_SHORT).show();
        if (captureListener != null) {
            captureListener.recordShort();
        }
    }

    @Override
    public void recordEnd() {
        resetState();
        if (captureListener != null) {
            captureListener.recordEnd();
        }
    }

    @Override
    public void onDuration(int duration) {
        float total = recordProgressView.getDuration() / 1000f;
        duration = duration * 10 / 1000;
        float recordTime = duration / 10f;
        String time = recordTime + "  |  " + total;
        tv_video_time.setText(time);
    }

    public void setType(int type) {
        ll_video.setVisibility(View.INVISIBLE);
        if (type == CameraConstant.TYPE_PICTURE) {
            layout_capture.setVisibility(View.VISIBLE);
            layout_video.setVisibility(View.GONE);
        } else {
            layout_capture.setVisibility(View.GONE);
            layout_video.setVisibility(View.VISIBLE);
            recordProgressView.setVisibility(View.GONE);
            view_video.setVisibility(View.VISIBLE);
        }
    }

    public void setCaptureListener(CaptureListener listener) {
        this.captureListener = listener;
    }

    private void takePicture() {
        startCaptureAnimation();
        view_capture.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (captureListener != null) {
                    captureListener.takePicture();
                }
            }
        }).start();
    }

    private void startCaptureAnimation() {
        AnimatorSet set = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view_capture, "scaleX", 1, 0.9f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view_capture, "scaleY", 1, 0.9f);
        scaleX.setInterpolator(new LinearInterpolator());
        scaleX.setDuration(200);
        scaleY.setInterpolator(new LinearInterpolator());
        scaleY.setDuration(200);

        ObjectAnimator scaleX2 = ObjectAnimator.ofFloat(view_capture, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleY2 = ObjectAnimator.ofFloat(view_capture, "scaleY", 0.9f, 1f);
        scaleX2.setInterpolator(new LinearInterpolator());
        scaleX2.setDuration(50);
        scaleX2.setStartDelay(200);
        scaleY2.setInterpolator(new LinearInterpolator());
        scaleY2.setDuration(50);
        scaleY2.setStartDelay(200);

        set.play(scaleX).with(scaleY).with(scaleX2).with(scaleY2);
        set.start();
    }


    private void startRecord() {
        startRecordAnimation(view_video, recordProgressView);
        ll_video.setVisibility(View.VISIBLE);
        if (captureListener != null) {
            captureListener.recordStart();
        }
        recordProgressView.start();
        startRecordSpotAnimation();
    }

    private void stopRecord() {
        recordProgressView.stop();
    }

    private void resetState() {
        startRecordAnimation(recordProgressView, view_video);
        ll_video.setVisibility(View.GONE);
        view_video_spot.clearAnimation();
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
        view_video_spot.startAnimation(alpha);
    }


    public void setCaptureEnable() {
        view_capture.setEnabled(true);
    }

}
