package com.liftyourheads.dailyreadings;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

public class NotificationUtils extends ContextWrapper {

    private NotificationManager mManager;
    public static final String AUDIO_CHANNEL_ID = "com.liftyourheads.dailyreadings.dailyReadingsAudio";
    public static final String AUDIO_CHANNEL_NAME = "Daily Readings Audio Stream";

    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }

    public void createChannels() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create android channel
            NotificationChannel dailyReadingsAudioChannel = new NotificationChannel(AUDIO_CHANNEL_ID,
                    AUDIO_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            getManager().createNotificationChannel(dailyReadingsAudioChannel);

        }
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getAndroidChannelNotification(Context context, String action, MediaSessionCompat mediaSession) {

        if (action.equals("Play")) {
            return MediaStyleHelper.from(context, mediaSession)
                    .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                    .setStyle(
                            new android.support.v4.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(0)
                                    .setMediaSession(mediaSession.getSessionToken()))
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentText("Content Text")
                    .setContentTitle("Content Title")
                    .setChannelId(AUDIO_CHANNEL_ID);

        } else if (action.equals("Pause")) {

            return MediaStyleHelper.from(context, mediaSession)
                    .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                    .setStyle(
                            new android.support.v4.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(0)
                                    .setMediaSession(mediaSession.getSessionToken()))
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentText("Content Text")
                    .setContentTitle("Content Title")
                    .setChannelId(AUDIO_CHANNEL_ID);

        }

        return null;

    }
}