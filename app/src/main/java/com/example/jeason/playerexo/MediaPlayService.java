package com.example.jeason.playerexo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;

/**
 * Created by Jeason on 2018/2/7.
 */

public class MediaPlayService extends Service {
    private static final String TAG = MediaPlayService.class.getSimpleName();
    private final IBinder mBinder = new MyBinder();
    private NotificationManager notificationManager;
    private int notificationID = 101;
    private ExoPlayer player;
    private SimpleExoPlayer simpleExoPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationOnStatusBar();
        Log.v(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        if (player == null) {
            Log.v(TAG, "Player has already been relased");
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind");
        notificationManager.cancel(notificationID);
        if (player != null) {
            player.release();
            player = null;
            Log.v(TAG, "Player is relased");
        }
        this.stopSelf();
        return true;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.v(TAG, "onTaskRemoved");
        notificationManager.cancel(notificationID);
        player.release();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    private void notificationOnStatusBar() {
        Log.v(TAG, "notificationOnStatusBar Media is playing");
        Intent intent = new Intent(this, MainActivity.class);
        int requestID = (int) System.currentTimeMillis();
        int flag = PendingIntent.FLAG_CANCEL_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, requestID, intent, flag);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "Media is playing";
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId);
        mBuilder.setSmallIcon(R.drawable.rounded_button)
                .setContentTitle("Player is playing")
                .setContentText("Tap to back to the MainActivity!")
                .setContentIntent(pendingIntent);
        notificationManager.notify(notificationID, mBuilder.build());
    }

    class MyBinder extends Binder {
        MediaPlayService getService() {
            return MediaPlayService.this;
        }
    }

    public void playMedia(MediaSource mediaSource) {
        if (player == null) {
            Renderer[] audioRenders = new Renderer[1];
            audioRenders[0] = new MediaCodecAudioRenderer(MediaCodecSelector.DEFAULT);
            TrackSelector audioTrackSelection = new DefaultTrackSelector();
//            player = ExoPlayerFactory.newInstance(audioRenders, audioTrackSelection);
//            player.setPlayWhenReady(true);
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), audioTrackSelection);
        }
//        player.prepare(mediaSource);
        simpleExoPlayer.prepare(mediaSource);
    }

    public ExoPlayer getPlayer() {
//        return player;
        return simpleExoPlayer;
    }
}
