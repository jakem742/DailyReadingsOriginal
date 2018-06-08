package com.liftyourheads.dailyreadings;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class activity_settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialise theme
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_settings);

        /*
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new fragment_settings())
                .commit();
        */

    }

    // When screen rotates
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //Tell main activity to update it's UI
        activity_date.forceUIOrientationCheck = true;

    }


}

