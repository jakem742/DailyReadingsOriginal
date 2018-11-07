package com.liftyourheads.dailyreadings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static com.liftyourheads.dailyreadings.activity_date.readings;


public class activity_readings extends AppCompatActivity {

    public static Context CONTEXT;

    private Button btn;
    /*** help to toggle between play and pause.*/
    private boolean playPause;
    MediaPlayer mediaPlayer;
    /*** remain false till media is not completed, inside OnCompletionListener make it true. */
    private boolean initialStage = true;
    String audioURLPlaying;
    public static String audioURLPage;
    //String audioKJVURLTemplate = "http://server.firefighters.org/kjv/projects/firefighters/kjv_web/01_Gen/01Gen001.mp3";
    //String audioNETURLTemplate = "http://feeds.bible.org/netaudio/51-Colossians-03.mp3";

    TextView bibleTitleTextView;
    ViewPager bibleContentViewPager;
    FragmentPagerAdapter adapterViewPager;

    int readingNum;
    int curReadingNum = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialise theme
        Utils.onActivityCreateSetTheme(this);
        setContentView(R.layout.activity_readings);

        CONTEXT = this;

        bibleContentViewPager = findViewById(R.id.bibleContentViewPager);
        bibleTitleTextView = findViewById(R.id.bibleTitleTextView);

        //Get initial reading
        readingNum = getIntent().getIntExtra("readingNum", 1);

        //Initialise the header
        updateHeader((readingNum));

        //updateUI(readingNum);
        //initializeMediaPlayer();
        //Todo: Fix audio player
        //initNewMediaPlayer();

        //Initialise the viewpager
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        bibleContentViewPager.setAdapter(adapterViewPager);

        //Initialise at selected page
        bibleContentViewPager.setCurrentItem((readingNum));

        bibleContentViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateHeader(position);
                curReadingNum = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void updateHeader(int reading) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean paragraphs = sharedPreferences.getBoolean("pref_paragraphs", false);

        if (!paragraphs && (readings[reading].getNumChapters() > 1 )) {

            Log.i("Updating Header", "Identified dynamic header");
            if (readings[reading].isMultipleBooks()) {

                String readingName = readings[reading].getBookName("0");

                Log.i("Reading Name", "Set to " + readingName);
                bibleTitleTextView.setText(readingName);


            } else {

                String readingBookName = readings[reading].getBookName(null);
                Integer[] readingBookChapters = readings[reading].getChapters();

                String readingName = readingBookName + " " + readingBookChapters[0].toString();
                Log.i("Reading Name", "Set to " + readingName);
                bibleTitleTextView.setText(readingName);

            }

        } else {

            Log.i("Updating Header", "Identified static header");
            bibleTitleTextView.setText(readings[reading].getFullName());
            Log.i("Reading Name", "Set to " + readings[reading].getFullName());

        }
        //Update the audio URL
        audioURLPage = readings[reading].getAudioURL(0);
        Log.i("Current Audio URL", audioURLPage);

        //checkAudioStream(reading);
    }

    public void checkAudioStream(int reading) {

        Integer audioStreamStatus = readings[reading].getAudioStreamStatus(0);
        //btn = findViewById(R.id.mediaControlButton);

        if ( audioStreamStatus == 404) {

            btn.setVisibility(View.GONE);

        } else if (audioStreamStatus == 10) {       //Custom error code identifying no network access

            btn.setVisibility(View.GONE);

            //Todo: Run check in background
            readings[reading].checkAudioURLStream();

            if (readings[reading].getAudioStreamStatus(0) != 10) {

                checkAudioStream(reading);

            }

        } else if (audioStreamStatus == 200) {      //File found

            btn.setVisibility(View.VISIBLE);

        } else {

            Log.w("AudioStream","Unknown Status! Audio Stream Status : " + audioStreamStatus);

        }

    }

    public void returnToMain(View view) {

        Intent myIntent = new Intent(this, activity_date.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.putExtra("fromActivity","readings");
        myIntent.putExtra("activeReading", bibleContentViewPager.getCurrentItem());
        Bundle options =
                ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slidein_left, R.anim.slideout_right).toBundle();

        overridePendingTransition(R.anim.slidein_left, R.anim.slideout_right);

        if (Build.VERSION.SDK_INT > 15) startActivityIfNeeded(myIntent,0, options);
        else startActivity(myIntent);

    }

    public void goToMap(View view) {

        Integer currentView = bibleContentViewPager.getCurrentItem();

        Intent myIntent = new Intent(this, activity_bible_places.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        myIntent.putExtra("fromActivity", "Readings");
        myIntent.putExtra("readingNum", currentView);

        Bundle options =
                ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slidein_left, R.anim.slideout_right).toBundle();

        overridePendingTransition(R.anim.slidein_left, R.anim.slideout_right);

        if (Build.VERSION.SDK_INT > 15)
            startActivityIfNeeded(myIntent, 0, options);
        else
            startActivity(myIntent);



    }

    /*
    public void initializeMediaPlayer() {


        //// LOAD AUDIO FOR READING ////

        //btn = findViewById(R.id.mediaControlButton);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        btn.setOnClickListener(pausePlay);
        btn.setBackgroundResource(android.R.drawable.ic_media_play);

    }

    private View.OnClickListener pausePlay = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            // TODO Fix icons

            if (audioURLPlaying != null && !audioURLPage.equals(audioURLPlaying)) {

                Log.i("Media Player", "On different page! Resetting.");

                mediaPlayer.stop();
                mediaPlayer.reset();

                initialStage = true;
            }

            if (!playPause) {

                btn.setBackgroundResource(android.R.drawable.ic_media_pause);

                Log.i("Media Player", "Playing");

                if (initialStage) {

                    new Player()
                            .execute(audioURLPage);

                    audioURLPlaying = audioURLPage;

                } else {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                    }
                }
                playPause = true;

            } else {

                btn.setBackgroundResource(android.R.drawable.ic_media_play);

                if (mediaPlayer.isPlaying()) {

                    Log.i("Media Player", "Pausing");
                    mediaPlayer.pause();

                }

                if (!audioURLPage.equals(audioURLPlaying)) {

                    Log.i("Media Player", "On different page! Resetting.");
                    initialStage = true;
                    mediaPlayer.reset();

                }

                playPause = false;

            }

        }

    };


    *
     * preparing mediaplayer will take sometime to buffer the content so prepare it inside the background thread and starting it on UI thread.
     *
     * @author piyush


    class Player extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog progress;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            Boolean prepared;
            try {

                mediaPlayer.setDataSource(params[0]);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        initialStage = true;
                        playPause = false;
                        btn.setBackgroundResource(android.R.drawable.ic_media_play);
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });
                mediaPlayer.prepare();
                prepared = true;


            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                Log.d("IllegalArgument", e.getMessage());
                prepared = false;
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                prepared = false;
                e.printStackTrace();
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (progress.isShowing()) {
                progress.cancel();
            }
            Log.d("MPlayer Prepared", "//" + result);
            mediaPlayer.start();
            btn.setBackgroundResource(android.R.drawable.ic_media_pause);


            initialStage = false;
        }

        public Player() {
            progress = new ProgressDialog(activity_readings.this);
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            this.progress.setMessage("Buffering...");
            this.progress.show();

        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Log.i("Media Player","Pausing");
        if (mediaPlayer != null) {
            btn.setBackgroundResource(android.R.drawable.ic_media_play);
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;

        }

    }*/

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
                    return fragment_bible.newInstance(0);
                case 1:
                    return fragment_bible.newInstance(1);
                case 2:
                    return fragment_bible.newInstance(2);
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

    //Prevent endless back button loop
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            returnToMain(null);
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    //// AUDIO VARIABLES ////
    /*
    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private int mCurrentState;

    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;


    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {

                Bundle audio = new Bundle();
                audio.putStringArray("URLs",readings[curReadingNum].getAudioURLS());
                audio.putString("Book",readings[curReadingNum].getBookName(null));

                int[] chapters = new int[readings[curReadingNum].getNumChapters()];
                for (int i = 0; i < readings[curReadingNum].getNumChapters(); i++) {
                    chapters[i] = readings[curReadingNum].getChapter(i);
                }

                audio.putIntArray("Chapters",chapters);

                mMediaControllerCompat = new MediaControllerCompat(activity_readings.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                MediaControllerCompat.setMediaController(activity_readings.this, mMediaControllerCompat);
                MediaControllerCompat.getMediaController(activity_readings.this).getTransportControls().playFromMediaId(readings[curReadingNum].getAudioURL(0), audio);

            } catch( RemoteException e ) {

            }
        }
    };

    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if( state == null ) {
                return;
            }

            switch( state.getState() ) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    mCurrentState = STATE_PLAYING;
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = STATE_PAUSED;
                    break;
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( MediaControllerCompat.getMediaController(activity_readings.this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ) {
            MediaControllerCompat.getMediaController(activity_readings.this).getTransportControls().pause();
        }

        mMediaBrowserCompat.disconnect();
    }

    public void initNewMediaPlayer() {

        //btn = findViewById(R.id.mediaControlButton);
        btn.setBackgroundResource(android.R.drawable.ic_media_play);

        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MediaPlayerService.class),
                mMediaBrowserCompatConnectionCallback, getIntent().getExtras());

        mMediaBrowserCompat.connect();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if( mCurrentState == STATE_PAUSED ) {
                    MediaControllerCompat.getMediaController(activity_readings.this).getTransportControls().play();
                    mCurrentState = STATE_PLAYING;
                } else {
                    if( MediaControllerCompat.getMediaController(activity_readings.this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ) {
                        MediaControllerCompat.getMediaController(activity_readings.this).getTransportControls().pause();
                    }

                    mCurrentState = STATE_PAUSED;
                }
            }
        });


    }*/



}