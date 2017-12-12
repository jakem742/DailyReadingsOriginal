package com.liftyourheads.dailyreadings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
                activity.setTheme(R.style.DailyReadings_Dark);
                break;
            case THEME_LIGHT:
                activity.setTheme(R.style.DailyReadings_Light);
                break;
        }
    }


}
