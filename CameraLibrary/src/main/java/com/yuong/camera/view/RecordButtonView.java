package com.yuong.camera.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

import com.yuong.camera.R;

public class RecordButtonView extends View {

    private int outLineColor;
    private int progressLineColor;
    private float progressLineWidth = 15;
    private float progressRadius = 60;
    private Paint mPaint = new Paint();
    private RectF mArcRect = new RectF();
    private RectF mRect = new RectF();
    private float progress = 0;
    private int duration;
    private int minDuration;
    private int recordedTime;
    private RecordCountDownTimer timer;
    private RecordListener recordListener;

    public RecordButtonView(Context context) {
        this(context, null);
    }

    public RecordButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RecordButtonView);
        outLineColor = typedArray.getColor(R.styleable.RecordButtonView_view_out_line_color, Color.BLACK);
        progressLineColor = typedArray.getColor(R.styleable.RecordButtonView_view_line_color, Color.WHITE);
        progressLineWidth = typedArray.getDimension(R.styleable.RecordButtonView_view_line_width, 15);
        progressRadius = typedArray.getDimension(R.styleable.RecordButtonView_view_radius, 60);
        typedArray.recycle();

        mPaint.setAntiAlias(true);
        duration = 15 * 1000;
        minDuration = 1500;
        timer = new RecordCountDownTimer(duration, duration / 360);    //录制定时器
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = (int) (progressRadius * 2 + progressLineWidth);
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(progressLineWidth);
        mPaint.setColor(outLineColor);
        canvas.drawCircle(progressRadius + progressLineWidth / 2, progressRadius + progressLineWidth / 2, progressRadius, mPaint);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mRect.set(2 * progressRadius / 3 + progressLineWidth / 2, 2 * progressRadius / 3 + progressLineWidth / 2, 4 * progressRadius / 3 + progressLineWidth / 2, 4 * progressRadius / 3 + progressLineWidth / 2);
        canvas.drawRect(mRect, mPaint);

        mPaint.setColor(progressLineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(progressLineWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mArcRect.set(progressLineWidth / 2, progressLineWidth / 2, 2 * progressRadius + progressLineWidth / 2, 2 * progressRadius + progressLineWidth / 2);
        canvas.drawArc(mArcRect, -90, progress, false, mPaint);
    }


    private class RecordCountDownTimer extends CountDownTimer {
        RecordCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            updateProgress(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            updateProgress(0);
            recordEnd();
        }
    }

    private void updateProgress(long millisUntilFinished) {
        recordedTime = (int) (duration - millisUntilFinished);
        progress = 360f - millisUntilFinished / (float) duration * 360f;
        if (recordListener != null) {
            recordListener.onDuration(recordedTime);
        }
        invalidate();
    }

    private void recordEnd() {
        if (recordListener != null) {
            if (recordedTime < minDuration)
                recordListener.recordShort();
            else
                recordListener.recordEnd();
        }
    }

    public void start() {
        recordedTime = 0;
        timer.start();
    }

    public void stop() {
        timer.cancel();
        recordEnd();
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setMinDuration(int minDuration) {
        this.minDuration = minDuration;
    }

    public int getDuration() {
        return duration;
    }

    public void setRecordListener(RecordListener listener) {
        this.recordListener = listener;
    }

    public interface RecordListener {
        void recordShort();

        void recordEnd();

        void onDuration(int duration);
    }
}
