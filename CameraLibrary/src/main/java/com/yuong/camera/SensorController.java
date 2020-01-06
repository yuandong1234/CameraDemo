package com.yuong.camera;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.yuong.camera.utils.AngleUtil;

public class SensorController implements SensorEventListener {
    private static final String TAG = SensorController.class.getSimpleName();

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private static SensorController mInstance;
    private CameraSensorListener mCameraSensorListener;

    public void setCameraSensorListener(CameraSensorListener listener) {
        this.mCameraSensorListener = listener;
    }

    private SensorController(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    public static SensorController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SensorController(context);
        }
        return mInstance;
    }

    public void start() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stop() {
        mSensorManager.unregisterListener(this, mSensor);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        int x = (int) event.values[0];
        int y = (int) event.values[1];
        int z = (int) event.values[2];

        int angle = AngleUtil.getSensorAngle(x, y);
        Log.i(TAG, "angle : " + angle);
        if (mCameraSensorListener != null) {
            mCameraSensorListener.onAngle(angle);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface CameraSensorListener {
        void onAngle(int angle);
    }
}
