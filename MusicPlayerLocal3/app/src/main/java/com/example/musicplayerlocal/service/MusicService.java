package com.example.musicplayerlocal.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.example.musicplayerlocal.R;

import java.util.ArrayList;

public class MusicService extends Service {

    /* ===== AÇÕES ===== */
    public static final String ACTION_PLAY   = "ACTION_PLAY";
    public static final String ACTION_RESUME = "ACTION_RESUME";
    public static final String ACTION_PAUSE  = "ACTION_PAUSE";
    public static final String ACTION_STOP   = "ACTION_STOP";

    /* ===== EXTRAS ===== */
    public static final String EXTRA_PLAYLIST = "EXTRA_PLAYLIST";
    public static final String EXTRA_INDEX    = "EXTRA_INDEX";

    private static final String CHANNEL_ID = "music_channel";
    private static final int NOTIFICATION_ID = 1;

    private MediaPlayer mediaPlayer;
    private MediaSessionCompat mediaSession;
    private AudioManager audioManager;

    private ArrayList<String> playlist = new ArrayList<>();
    private int musicaAtual = 0;


    /* ====================== CICLO DE VIDA ====================== */

    @Override
    public void onCreate() {
        super.onCreate();

        criarCanal();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = new MediaPlayer();

        mediaSession = new MediaSessionCompat(this, "MusicPlayerLocal");

        mediaSession.setCallback(new MediaSessionCompat.Callback() {

            @Override
            public void onPlay() {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    atualizarEstado(PlaybackStateCompat.STATE_PLAYING);
                }
            }

            @Override
            public void onPause() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    atualizarEstado(PlaybackStateCompat.STATE_PAUSED);
                }
            }

            @Override
            public void onSkipToNext() {
                proxima();
            }

            @Override
            public void onSkipToPrevious() {
                anterior();
            }

            @Override
            public void onStop() {
                parar();
            }
        });

        mediaSession.setActive(true);
        atualizarEstado(PlaybackStateCompat.STATE_STOPPED);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        switch (intent.getAction()) {

            case ACTION_PLAY:
                startForeground(NOTIFICATION_ID, criarNotificacao());

                playlist = intent.getStringArrayListExtra(EXTRA_PLAYLIST);
                musicaAtual = intent.getIntExtra(EXTRA_INDEX, 0);

                if (playlist != null && !playlist.isEmpty()) {
                    tocar(playlist.get(musicaAtual));
                }
                break;

            case ACTION_RESUME:
                mediaSession.getController().getTransportControls().play();
                break;

            case ACTION_PAUSE:
                mediaSession.getController().getTransportControls().pause();
                break;

            case ACTION_STOP:
                parar();
                stopForeground(true);
                stopSelf();
                break;
        }

        return START_STICKY;
    }

    /* ====================== PLAYER ====================== */

    private void tocar(String path) {
        parar();

        audioManager.requestAudioFocus(
                focus -> {},
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );

        mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(mp -> proxima());
        atualizarEstado(PlaybackStateCompat.STATE_PLAYING);
    }

    private void proxima() {
        if (playlist == null || playlist.isEmpty()) return;
        musicaAtual = (musicaAtual + 1) % playlist.size();
        tocar(playlist.get(musicaAtual));
    }

    private void anterior() {
        if (playlist == null || playlist.isEmpty()) return;
        musicaAtual = (musicaAtual - 1 + playlist.size()) % playlist.size();
        tocar(playlist.get(musicaAtual));
    }

    private void parar() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        atualizarEstado(PlaybackStateCompat.STATE_STOPPED);
    }

    /* ====================== MEDIA STATE ====================== */

    private void atualizarEstado(int state) {
        PlaybackStateCompat playbackState =
                new PlaybackStateCompat.Builder()
                        .setActions(
                                PlaybackStateCompat.ACTION_PLAY |
                                        PlaybackStateCompat.ACTION_PAUSE |
                                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                        PlaybackStateCompat.ACTION_STOP
                        )
                        .setState(
                                state,
                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                                1.0f
                        )
                        .build();

        mediaSession.setPlaybackState(playbackState);
    }

    /* ====================== NOTIFICAÇÃO ====================== */

    private Notification criarNotificacao() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MusicPlayerLocal")
                .setContentText("Reproduzindo música")
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
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}