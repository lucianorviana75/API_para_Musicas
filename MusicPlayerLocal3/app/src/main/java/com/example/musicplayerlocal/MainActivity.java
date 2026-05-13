package com.example.musicplayerlocal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.support.v4.media.session.MediaControllerCompat;

import com.example.musicplayerlocal.data.MusicRepository;
import com.example.musicplayerlocal.service.MusicService;
import android.support.v4.media.session.MediaControllerCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity {

    private ListView lista;

    private Button play;
    private Button pause;
    private Button stop;
    private Button next;
    private Button prev;

    private ArrayList<String> caminhos = new ArrayList<>();
    private final ArrayList<String> nomes = new ArrayList<>();
    private MediaControllerCompat mediaController;
    private int musicaAtual = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        lista = findViewById(R.id.listaMusicas);

        play  = findViewById(R.id.botaoPlay);
        pause = findViewById(R.id.botaoPause);
        stop  = findViewById(R.id.botaoStop);
        next  = findViewById(R.id.botaoNext);
        prev  = findViewById(R.id.botaoPrev);

        // LISTA
        lista.setOnItemClickListener((parent, view, position, id) -> {
            tocarMusica(position);
        });

        // NEXT
        next.setOnClickListener(v -> {
            if (caminhos.isEmpty()) return;
            musicaAtual = (musicaAtual + 1) % caminhos.size();
            tocarMusica(musicaAtual);
        });

        // PREV
        prev.setOnClickListener(v -> {
            if (caminhos.isEmpty()) return;
            musicaAtual--;
            if (musicaAtual < 0) {
                musicaAtual = caminhos.size() - 1;
            }
            tocarMusica(musicaAtual);
        });

        // PLAY
        play.setOnClickListener(v -> {
            startService(new Intent(this, MusicService.class)
                    .setAction(MusicService.ACTION_RESUME));
        });

        // PAUSE
        pause.setOnClickListener(v -> {
            startService(new Intent(this, MusicService.class)
                    .setAction(MusicService.ACTION_PAUSE));
        });

        // STOP
        stop.setOnClickListener(v -> {
            startService(new Intent(this, MusicService.class)
                    .setAction(MusicService.ACTION_STOP));
        });

        pedirPermissao();
    }


    private void tocarMusica(int index) {

        musicaAtual = index;

        Intent i = new Intent(this, MusicService.class);
        i.setAction(MusicService.ACTION_PLAY);

        i.putStringArrayListExtra(
                MusicService.EXTRA_PLAYLIST,
                caminhos
        );

        i.putExtra(
                MusicService.EXTRA_INDEX,
                index
        );

        ContextCompat.startForegroundService(this, i);
    }

    private void tocar() {

        // 1️⃣ Inicia o Service normalmente
        Intent i = new Intent(this, MusicService.class);
        i.setAction(MusicService.ACTION_PLAY);
        i.putStringArrayListExtra(MusicService.EXTRA_PLAYLIST, caminhos);
        i.putExtra(MusicService.EXTRA_INDEX, musicaAtual);

        ContextCompat.startForegroundService(this, i);

        // 2️⃣ Aguarda a MediaSession existir e registra o controller
        lista.postDelayed(() -> {
            try {
                MediaControllerCompat mediaController =
                        MediaControllerCompat.getMediaController(this);

                if (mediaController == null) {
                    mediaController = new MediaControllerCompat(
                            this,
                            android.support.v4.media.session.MediaControllerCompat
                                    .getMediaController(this)
                                    .getSessionToken()
                    );
                    MediaControllerCompat.setMediaController(this, mediaController);
                }

                this.mediaController = mediaController;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 300);
    }
    private void carregar() {
        caminhos.clear();
        caminhos.addAll(MusicRepository.getAllMusicPaths(this));

        nomes.clear();
        for (String p : caminhos) {
            nomes.add(new File(p).getName());
        }

        lista.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_activated_1,
                nomes
        ));
    }

    private void pedirPermissao() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                    1
            );
        } else {
            carregar();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int r, @NonNull String[] p, @NonNull int[] g) {
        super.onRequestPermissionsResult(r, p, g);
        if (r == 1 && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) {
            carregar();
        }
    }
}