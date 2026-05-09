package com.example.musicplayerlocal.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.musicplayerlocal.R;

public class MusicService extends Service {

    public static final String ACTION_PLAY   = "ACTION_PLAY";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_PAUSE  = "ACTION_PAUSE";
    public static final String ACTION_STOP   = "ACTION_STOP";
    public static final String EXTRA_PATH    = "EXTRA_PATH";

    private static final String CHANNEL_ID = "music_channel";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mediaPlayer;

    /* ===== BINDER ===== */
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        criarCanal();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        switch (intent.getAction()) {

            case ACTION_PLAY:
                startForeground(
                        NOTIFICATION_ID,
                        criarNotificacao("Tocando")
                );

                String path = intent.getStringExtra(EXTRA_PATH);
                if (path != null) {
                    tocar(path);
                }
                break;

            case ACTION_RESUME:
                if (mediaPlayer != null) mediaPlayer.start();
                break;

            case ACTION_PAUSE:
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
                break;

            case ACTION_STOP:
                parar();
                stopForeground(true);
                stopSelf();
                break;
        }

        return START_STICKY;
    }

    /* ===== PLAYER ===== */

    private void tocar(String path) {
        parar();

        mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
        mediaPlayer.start();

        // ✅ QUANDO A MÚSICA ACABAR
        mediaPlayer.setOnCompletionListener(mp -> {
            enviarProximaMusica();
        });
    }
    private void enviarProximaMusica() {
        Intent intent = new Intent("NEXT_MUSIC");
        sendBroadcast(intent);
    }


    private void parar() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /* ===== MÉTODOS USADOS PELA ACTIVITY ===== */

    public int getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public int getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    public void seekTo(int pos) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(pos);
        }
    }

    /* ===== NOTIFICAÇÃO ===== */

    private Notification criarNotificacao(String texto) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MusicPlayerLocal")
                .setContentText(texto)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
    }

    private void criarCanal() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}