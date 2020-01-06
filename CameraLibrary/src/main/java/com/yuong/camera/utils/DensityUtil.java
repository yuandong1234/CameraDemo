package com.yuong.camera.utils;

import android.content.Context;
import android.util.TypedValue;

public class DensityUtil {
    private DensityUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    /**
     * dp转px
     *
     * @param context
     * @param value
     * @return
     */
    public static int dp2px(Context context, float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                value, context.getResources().getDisplayMetrics());
    }


    /**
     * sp转px
     *
     * @param context
     * @param value
     * @return
     */
    public static int sp2px(Context context, float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                value, context.getResources().getDisplayMetrics());
    }


    /**
     * px转dp
     *
     * @param context
     * @param value
     * @return
     */
    public static float px2dp(Context context, float value) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (value / scale);
    }


    /**
     * px转sp
     *
     * @param context
     * @param value
     * @return
     */
    public static float px2sp(Context context, float value) {
        return (value / context.getResources().getDisplayMetrics().scaledDensity);
    }
}
