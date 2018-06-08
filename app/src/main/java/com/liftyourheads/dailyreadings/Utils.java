package com.liftyourheads.dailyreadings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.StyleRes;
import android.support.v4.content.IntentCompat;
import android.support.v7.preference.PreferenceManager;
//import android.preference.PreferenceManager;

public class Utils {
    private static int sTheme;

    public final static int THEME_DARK = 0;
    public final static int THEME_LIGHT = 1;

    public static void restartActivity(Activity activity) {
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity) {

        //SharedPreferences sharedPreferences = activity.getSharedPreferences("com.liftyourheads.dailyreadings", Context.MODE_PRIVATE);
        PreferenceManager.setDefaultValues(activity, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        int theme = Integer.parseInt(sharedPreferences.getString("pref_theme","0"));


        switch (theme) {
            default:
            case THEME_DARK:
                activity.setTheme(R.style.AppTheme_Dark);
                break;
            case THEME_LIGHT:
                activity.setTheme(R.style.AppTheme);
                break;
        }
    }

    public static float getDimension(Context context, @StyleRes int styleResId, @AttrRes int attr) {
        TypedArray a = context.getTheme().obtainStyledAttributes(styleResId, new int[]{attr});
        float size = a.getDimension(0, 0);
        a.recycle();
        return size;
    }

    public static class Theme {

        public static @StyleRes int resolveTextSize(String choice) {
            switch (Integer.parseInt(choice)) {
                case -1:
                    return R.style.AppTextSize_XSmall;
                case 0:
                default:
                    return R.style.AppTextSize;
                case 1:
                    return R.style.AppTextSize_Medium;
                case 2:
                    return R.style.AppTextSize_Large;
                case 3:
                    return R.style.AppTextSize_XLarge;
            }
        }


    }

}
