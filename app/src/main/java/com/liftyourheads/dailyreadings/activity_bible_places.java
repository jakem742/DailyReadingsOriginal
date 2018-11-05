package com.liftyourheads.dailyreadings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import static com.liftyourheads.dailyreadings.activity_date.readings;

public class activity_bible_places extends AppCompatActivity {

    int readingNum;
    ViewPager bibleMapsViewPager;
    FragmentPagerAdapter adapterViewPager;
    int curReadingNum = 0;
    TextView bibleTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bible_places);

        bibleTitleTextView = findViewById(R.id.bibleTitleTextView);

        //Get initial reading
        readingNum = getIntent().getIntExtra("readingNum", 0);

        Log.i("Reading Number",Integer.toString(readingNum));

        //Initialise the viewpager
        bibleMapsViewPager = findViewById(R.id.bibleMapsViewPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        bibleMapsViewPager.setAdapter(adapterViewPager);

        //Initialise at selected page
        bibleMapsViewPager.setCurrentItem((readingNum));
        bibleTitleTextView.setText(readings[readingNum].getFullName());

        bibleMapsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                //Update header
                bibleTitleTextView.setText(readings[position].getFullName());
                Log.i("Reading Name", "Set to " + readings[position].getFullName());

                curReadingNum = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages.
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for a particular page.
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return fragment_bible_places_map.newInstance(0);
                case 1:
                    return fragment_bible_places_map.newInstance(1);
                case 2:
                    return fragment_bible_places_map.newInstance(2);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Tab " + position;
        }

    }

    public void returnToMain(View view) {

        Intent myIntent = new Intent(this, activity_bible.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.putExtra("fromActivity","maps");
        myIntent.putExtra("activeReading", readingNum);
        Bundle options =
                ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slidein_left, R.anim.slideout_right).toBundle();

        overridePendingTransition(R.anim.slidein_left, R.anim.slideout_right);

        if (Build.VERSION.SDK_INT > 15) startActivityIfNeeded(myIntent,0, options);
        else startActivity(myIntent);
        finish();

    }


}
