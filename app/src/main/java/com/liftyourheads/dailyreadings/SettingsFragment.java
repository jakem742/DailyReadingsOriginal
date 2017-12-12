package com.liftyourheads.dailyreadings;

import android.content.SharedPreferences;
import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.os.Bundle;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

    }

//    @Override
//    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//
//    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        System.out.println("Settings key changed: " + key);
        if(key.equals("pref_translation")) {

            Log.i("Preferences", "Updating readings to use " + sharedPreferences.getString(key, "unknown"));

            for (int i = 0; i < 3; i++) {

                BibleMainActivity.readings[i].generateAudioURL();
                BibleMainActivity.readings[i].updateVerses();

            }

        } else if (key.equals("pref_theme")) {

            Log.i("Preferences", "Updating theme to use #" + sharedPreferences.getString(key, "0"));

            //Tell main activity to restart on resume
            BibleMainActivity.forceRestart = true;

        }

    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

    }


}