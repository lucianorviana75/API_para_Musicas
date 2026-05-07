package com.example.musicplayerlocal.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.musicplayerlocal.R;
import com.example.musicplayerlocal.player.MusicPlayerManager;

public class MusicService extends Service {

    public static final String ACTION_PLAY   = "ACTION_PLAY";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_PAUSE  = "ACTION_PAUSE";
    public static final String ACTION_STOP   = "ACTION_STOP";
    public static final String ACTION_SEEK   = "ACTION_SEEK";

    public static final String EXTRA_PATH = "EXTRA_PATH";
    public static final String EXTRA_SEEK_POS = "EXTRA_SEEK_POS";

    private static final String CHANNEL_ID = "music_channel";
    private static final int NOTIFICATION_ID = 1;

    private MusicPlayerManager playerManager;
    private String currentPath = null;

    @Override
    public void onCreate() {
        super.onCreate();
        playerManager = new MusicPlayerManager();
        criarCanal();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        switch (intent.getAction()) {

            case ACTION_PLAY:
                currentPath = intent.getStringExtra(EXTRA_PATH);
                if (currentPath != null) {
                    playerManager.play(this, currentPath);
                    startForeground(
                            NOTIFICATION_ID,
                            criarNotificacao("Tocando música", true)
                    );
                }
                break;

            case ACTION_RESUME:
                if (currentPath != null) {
                    playerManager.resume();
                }
                break;

            case ACTION_PAUSE:
                playerManager.pause();
                break;

            case ACTION_SEEK:
                int pos = intent.getIntExtra(EXTRA_SEEK_POS, -1);
                if (pos >= 0) {
                    playerManager.seekTo(pos);
                }
                break;

            case ACTION_STOP:
                playerManager.stop();
                currentPath = null;
                stopForeground(true);
                stopSelf();
                break;
        }

        return START_STICKY;
    }

    private void criarCanal() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Music Player",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            manager.createNotificationChannel(channel);
        }
    }

    private Notification criarNotificacao(String texto, boolean tocando) {

        Intent pauseIntent = new Intent(this, MusicService.class);
        pauseIntent.setAction(ACTION_PAUSE);

        Intent resumeIntent = new Intent(this, MusicService.class);
        resumeIntent.setAction(ACTION_RESUME);

        Intent stopIntent = new Intent(this, MusicService.class);
        stopIntent.setAction(ACTION_STOP);

        PendingIntent pausePending =
                PendingIntent.getService(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE);

        PendingIntent resumePending =
                PendingIntent.getService(this, 1, resumeIntent, PendingIntent.FLAG_IMMUTABLE);

        PendingIntent stopPending =
                PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText(texto)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(tocando)
                .setOnlyAlertOnce(true)
                .addAction(
                        tocando ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        tocando ? "Pause" : "Play",
                        tocando ? pausePending : resumePending
                )
                .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "Stop",
                        stopPending
                )
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}