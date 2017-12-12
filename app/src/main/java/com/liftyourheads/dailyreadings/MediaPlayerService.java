package com.liftyourheads.dailyreadings;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.util.Log;


import java.io.IOException;
import java.util.List;

import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;


////// https://code.tutsplus.com/tutorials/background-audio-in-android-with-mediasessioncompat--cms-27030 ////

public class MediaPlayerService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mMediaPlayer; // For the actual playback
    private MediaSessionCompat mMediaSessionCompat; // Manage metadata and playback controls/states

    public NotificationUtils mNotificationUtils; //Create notification channel

    private String readingBook;
    private int[] readingChapters;
    private String[] readingURLS;
    private int audioIndex;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Take the Intent that is passed to the Service and send it to the MediaButtonReceiver class
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationUtils = new NotificationUtils(this); //Initialise notification channel

        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
    }

    /*  Initialize the MediaPlayer object that we created at the top of the class,
        request partial wake lock, and set the player's volume */

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(1.0f, 1.0f);
    }

    /*  Where we initialize the MediaSessionCompat object and wire it to the media buttons and control
        methods that allow us to handle playback and user input. This method starts by
            1. Creating a ComponentName object that points to the Android support library's MediaButtonReceiver class,
                and uses that to:
            2. Create a new MediaSessionCompat. We then
            3. Pass the MediaSession.Callback object that we created earlier to it, and
            4. Set the flags necessary for receiving media button inputs and control signals. Next, we
            5. Create a new Intent for handling media button inputs on pre-Lollipop devices, and
            6. Set the media session token for our service. */

    private void initMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);

        mMediaSessionCompat.setCallback(mMediaSessionCallback);
        mMediaSessionCompat.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);

        setSessionToken(mMediaSessionCompat.getSessionToken());
    }

    // Listen for headphone change events

    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if( mMediaPlayer.isPlaying() ) {
                    mMediaPlayer.stop();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mMediaPlayer.pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( mMediaPlayer != null ) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( mMediaPlayer != null ) {
                    if( !mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.start();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    //Todo: Change to play next track in queue!

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        /*if( mMediaPlayer != null ) {
            mMediaPlayer.release();
        } */

        if( mMediaPlayer != null ) {

            audioIndex++;
            //Check if player is at the end of the list of URLs
            if (audioIndex < readingURLS.length) {

                try {
                    mMediaPlayer.setDataSource(readingURLS[audioIndex]);
                    initMediaSessionMetadata();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                audioIndex++;

            } else {

                mMediaPlayer.release();

            }
        }
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    //Not important for general audio service, required for class
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    //Listens for changes in the headphone state
    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
                mMediaPlayer.pause();
            }
        }
    };

    // Used for handling playback state when media session actions occur

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {


        // Call a helper method that attempts to retrieve focus, and if it cannot, it will simply return

        @Override
        public void onPlay() {
            super.onPlay();
            if( !successfullyRetrievedAudioFocus() ) {
                return;
            }

            //Set the state to playing
            mMediaSessionCompat.setActive(true);
            setMediaPlaybackState(STATE_PLAYING);

            //Start the mediaPlayer
            showPlayingNotification();
            mMediaPlayer.start();

        }

        @Override
        public void onPause() {
            super.onPause();

            if( mMediaPlayer.isPlaying() ) {
                mMediaPlayer.pause();
                setMediaPlaybackState(STATE_PAUSED);
                showPausedNotification();
            }
        }

        //Todo: Change processing of media file!

        /*@Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);

            /*try {
                AssetFileDescriptor afd = getResources().openRawResourceFd(Integer.valueOf(mediaId));
                if( afd == null ) {
                    return;
                }

                try {
                    mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

                } catch( IllegalStateException e ) {
                    mMediaPlayer.release();
                    initMediaPlayer();
                    mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                }

                afd.close();
                initMediaSessionMetadata();

            } catch (IOException e) {
                return;
            }

            try {
                mMediaPlayer.prepare();
            } catch (IOException e) {}

            //Work with extras here if you want
        } */
        private ProgressDialog progress;

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);

            readingURLS = extras.getStringArray("URLs");
            readingChapters = extras.getIntArray("Chapters");
            readingBook = extras.getString("Book");

            try {
                mMediaPlayer.setDataSource(mediaId);
                audioIndex = 0;

            } catch( IllegalStateException e ) {
                mMediaPlayer.release();
            } catch(IOException e) {
                mMediaPlayer.release();
            }

            initMediaSessionMetadata();

            //try {
            progress = new ProgressDialog(BibleReadingActivity.CONTEXT);

            progress.show();
            mMediaPlayer.prepareAsync();
            progress.cancel();
            //} catch (IOException e) {}

        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);


        }
    };


    /*  Get a reference to the system AudioManager, and attempt to request audio focus for streaming music.
        It will then return a boolean representing whether or not the request succeeded. */

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    /*  A helper method that creates a PlaybackStateCompat.Builder object and gives it the proper actions and state,
        and then builds and associates a PlaybackStateCompat with your MediaSessionCompat object. */

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
        if( state == STATE_PLAYING ) {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackStateBuilder.build());
    }

    private void showPlayingNotification() {
        NotificationCompat.Builder builder = mNotificationUtils.getAndroidChannelNotification(this, "Play", mMediaSessionCompat);
        if( builder == null ) {
            Log.i("Play Notification","No notification found!");
            return;
        }

        mNotificationUtils.getManager().notify(101,builder.build());



        /*builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .setStyle(
                    new MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mMediaSessionCompat.getSessionToken()))
                .setSmallIcon(R.mipmap.ic_launcher);


        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel mediaChannel = new NotificationChannel("com.liftyourheads.dailyreadings.dailyReadingsAudio", "com.liftyourheads.dailyreadings.dailyReadingsAudio", NotificationManager.IMPORTANCE_HIGH);
            mediaChannel.setDescription("description");
            mediaChannel.enableVibration(true);
            mediaChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            notificationManager.createNotificationChannel(mediaChannel);
        } else {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //mNotificationUtils.;
            builder.setChannelId("com.liftyourheads.dailyreadings.dailyReadingsAudio");
            mNotificationUtils.getManager().notify(1, builder.build());

        } else {

            NotificationManagerCompat.from(MediaPlayerService.this).notify(1, builder.build());

        } */

    }

    private void showPausedNotification() {
        NotificationCompat.Builder builder = mNotificationUtils.getAndroidChannelNotification(this, "Pause", mMediaSessionCompat);
        if( builder == null ) {
            Log.i("Pause Notification","No notification found!");
            return;
        }

        mNotificationUtils.getManager().notify(101,builder.build());

        /*builder.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .setStyle(
                    new MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setMediaSession(mMediaSessionCompat.getSessionToken()))
                .setSmallIcon(R.mipmap.ic_launcher);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setChannelId("com.liftyourheads.dailyreadings.dailyReadingsAudio");
            mNotificationUtils.getManager().notify(1, builder.build());

        } else {

            NotificationManagerCompat.from(this).notify(1, builder.build());
        } */

    }


    /*  1. Get a reference to the system service's AudioManager, and
        2. Call abandonAudioFocus() with our AudioFocusChangeListener as a parameter,
            which will notify other apps on the device that you are giving up audio focus. Next,
        3. Unregister the BroadcastReceiver that was set up to listen for headphone changes, and
        4. Release the MediaSessionCompat object. Finally, you will want to
        5. Cancel the playback control notification.     */

    @Override
    public void onDestroy() {
        super.onDestroy();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
        unregisterReceiver(mNoisyReceiver);
        mMediaSessionCompat.release();
        NotificationManagerCompat.from(this).cancel(1);
    }



    private void initMediaSessionMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, readingBook);
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, Integer.toString(readingChapters[audioIndex]));
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, (audioIndex + 1));
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, readingChapters.length);

        mMediaSessionCompat.setMetadata(metadataBuilder.build());
    }


}