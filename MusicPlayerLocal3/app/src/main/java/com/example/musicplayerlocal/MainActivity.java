package com.example.musicplayerlocal;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.content.Intent;
import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listaMusicas;
    TextView nomeMusica, tempoAtual, tempoTotal;
    SeekBar seekBar;
    ImageView capaAlbum;
    Button botaoPlay, botaoPause, botaoStop, botaoNext, botaoPrev, botaoDelete;

    ArrayList<String> musicas = new ArrayList<>();
    ArrayList<String> caminhos = new ArrayList<>();
    int musicaAtual = -1;

    MediaPlayer mediaPlayer;
    Handler handler = new Handler();

    MediaSession mediaSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Inicializar Views ---
        listaMusicas = findViewById(R.id.listaMusicas);
        nomeMusica = findViewById(R.id.nomeMusica);
        tempoAtual = findViewById(R.id.tempoAtual);
        tempoTotal = findViewById(R.id.tempoTotal);
        seekBar = findViewById(R.id.seekBar);
        capaAlbum = findViewById(R.id.capaAlbum);

        botaoPlay = findViewById(R.id.botaoPlay);
        botaoPause = findViewById(R.id.botaoPause);
        botaoStop = findViewById(R.id.botaoStop);
        botaoNext = findViewById(R.id.botaoNext);
        botaoPrev = findViewById(R.id.botaoPrev);
        botaoDelete = findViewById(R.id.botaoDelete);

        // --- Pedir permissão ---
        pedirPermissao();

        // --- MediaSession para Bluetooth ---
        mediaSession = new MediaSession(this, "MusicPlayerSession");
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                KeyEvent keyEvent = mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyEvent.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            if (mediaPlayer != null) mediaPlayer.start();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            tocarProximaMusica();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            tocarMusicaAnterior();
                            break;
                    }
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }
        });
        mediaSession.setActive(true);

        // --- Botões ---
        botaoPlay.setOnClickListener(v -> { if (mediaPlayer != null) mediaPlayer.start(); });
        botaoPause.setOnClickListener(v -> { if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause(); });
        botaoStop.setOnClickListener(v -> pararMusica());
        botaoNext.setOnClickListener(v -> tocarProximaMusica());
        botaoPrev.setOnClickListener(v -> tocarMusicaAnterior());
        botaoDelete.setOnClickListener(v -> deletarMusica());

        // --- Atualizar SeekBar ---
        Runnable atualizarSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    tempoAtual.setText(formatarTempo(mediaPlayer.getCurrentPosition()));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(atualizarSeekBar, 0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tempoAtual.setText(formatarTempo(progress));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void pedirPermissao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 1);
        } else {
            carregarMusicas();
        }
    }

    private void carregarMusicas() {
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = android.provider.MediaStore.Audio.Media.DATA + " LIKE ? OR " +
                android.provider.MediaStore.Audio.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{ "%/Download/%", "%/Music/%" };
        Cursor cursor = getContentResolver().query(uri, null, selection, selectionArgs, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String nome = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DISPLAY_NAME));
                String caminho = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.DATA));
                musicas.add(nome);
                caminhos.add(caminho);
            }
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, musicas);
        listaMusicas.setAdapter(adapter);

        listaMusicas.setOnItemClickListener((parent, view, position, id) -> {
            musicaAtual = position;
            tocarMusica(musicaAtual);
            listaMusicas.setItemChecked(position, true); // seleciona visualmente
        });
    }

    private void tocarMusica(int index) {
        if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.release(); }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            mediaPlayer.setDataSource(caminhos.get(index));
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) { e.printStackTrace(); }

        nomeMusica.setText(musicas.get(index));

        // Capa
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(caminhos.get(index));
            byte[] arte = mmr.getEmbeddedPicture();
            if (arte != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(arte, 0, arte.length);
                capaAlbum.setImageBitmap(bitmap);
            } else {
                capaAlbum.setImageResource(R.mipmap.ic_launcher);
            }
            mmr.release();
        } catch (Exception e) { e.printStackTrace(); capaAlbum.setImageResource(R.mipmap.ic_launcher); }

        seekBar.setMax(mediaPlayer.getDuration());
        tempoTotal.setText(formatarTempo(mediaPlayer.getDuration()));
    }

    private void tocarProximaMusica() {
        if (!caminhos.isEmpty()) {
            musicaAtual = (musicaAtual + 1) % caminhos.size();
            tocarMusica(musicaAtual);
            listaMusicas.setItemChecked(musicaAtual, true);
        }
    }

    private void tocarMusicaAnterior() {
        if (!caminhos.isEmpty()) {
            musicaAtual = (musicaAtual - 1 + caminhos.size()) % caminhos.size();
            tocarMusica(musicaAtual);
            listaMusicas.setItemChecked(musicaAtual, true);
        }
    }

    private void pararMusica() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            seekBar.setProgress(0);
            tempoAtual.setText("0:00");
        }
    }

    private void deletarMusica() {
        if (musicaAtual >= 0 && musicaAtual < caminhos.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Deletar música")
                    .setMessage("Tem certeza que deseja deletar esta música da lista?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = null;
                        }

                        musicas.remove(musicaAtual);
                        caminhos.remove(musicaAtual);

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, musicas);
                        listaMusicas.setAdapter(adapter);

                        if (!caminhos.isEmpty()) {
                            musicaAtual = musicaAtual % caminhos.size();
                            tocarMusica(musicaAtual);
                            listaMusicas.setItemChecked(musicaAtual, true);
                        } else {
                            musicaAtual = -1;
                            nomeMusica.setText("");
                            capaAlbum.setImageResource(R.mipmap.ic_launcher);
                            seekBar.setProgress(0);
                            tempoAtual.setText("0:00");
                            tempoTotal.setText("0:00");
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        }
    }

    private String formatarTempo(int ms) {
        int segundos = (ms / 1000) % 60;
        int minutos = (ms / 1000) / 60;
        return String.format("%d:%02d", minutos, segundos);
    }
}