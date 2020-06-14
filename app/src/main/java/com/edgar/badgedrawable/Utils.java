package com.edgar.badgedrawable;

import android.content.Context;

/**
 * Created by Edgar on 2020/6/13.
 */
public class Utils {

    public static int dp2Px(Context context, float value) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (scale * value + 0.5f);
    }
}
