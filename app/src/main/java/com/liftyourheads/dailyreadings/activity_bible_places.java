package com.liftyourheads.dailyreadings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import static com.liftyourheads.dailyreadings.activity_date.readings;

public class activity_bible_places extends AppCompatActivity {

    int readingNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bible_places);

        TextView bibleTitleTextView = findViewById(R.id.bibleTitleTextView);



        //Get initial reading
        readingNum = getIntent().getIntExtra("readingNum", 1);


        bibleTitleTextView.setText(readings[readingNum-1].getFullName());
    }

    public void returnToMain(View view) {

        Intent myIntent = new Intent(this, activity_date.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.putExtra("fromActivity","readings");
        myIntent.putExtra("activeReading", readingNum);
        Bundle options =
                ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slidein_left, R.anim.slideout_right).toBundle();

        overridePendingTransition(R.anim.slidein_left, R.anim.slideout_right);

        if (Build.VERSION.SDK_INT > 15) startActivityIfNeeded(myIntent,0, options);
        else startActivity(myIntent);

    }

}
