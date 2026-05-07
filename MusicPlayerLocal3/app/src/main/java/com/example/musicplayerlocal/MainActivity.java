package com.example.musicplayerlocal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.musicplayerlocal.data.MusicRepository;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // UI
    private ListView listaMusicas;
    private TextView nomeMusica, tempoAtual, tempoTotal;
    private SeekBar seekBar;
    private Button botaoPlay, botaoPause, botaoStop, botaoNext, botaoPrev;

    // Dados
    private final ArrayList<String> nomes = new ArrayList<>();
    private final ArrayList<String> caminhos = new ArrayList<>();
    private int musicaAtual = -1;

    // Player
    private MediaPlayer mediaPlayer;

    // Tempo
    private final Handler handler = new Handler();
    private Runnable atualizarTempo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Views
        listaMusicas = findViewById(R.id.listaMusicas);
        nomeMusica   = findViewById(R.id.nomeMusica);
        tempoAtual   = findViewById(R.id.tempoAtual);
        tempoTotal   = findViewById(R.id.tempoTotal);
        seekBar      = findViewById(R.id.seekBar);

        botaoPlay  = findViewById(R.id.botaoPlay);
        botaoPause = findViewById(R.id.botaoPause);
        botaoStop  = findViewById(R.id.botaoStop);
        botaoNext  = findViewById(R.id.botaoNext);
        botaoPrev  = findViewById(R.id.botaoPrev);

        configurarLista();
        configurarBotoes();
        configurarSeekBar();
        pedirPermissao();
    }

    // ================= LISTA =================

    private void configurarLista() {
        listaMusicas.setOnItemClickListener((parent, view, position, id) -> {
            musicaAtual = position;
            tocarMusica(position);
        });
    }

    private void carregarLista() {
        caminhos.clear();
        caminhos.addAll(MusicRepository.getAllMusicPaths(this));

        nomes.clear();
        for (String p : caminhos) {
            nomes.add(new File(p).getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_activated_1,
                nomes
        );
        listaMusicas.setAdapter(adapter);
    }

    // ================= PLAYER =================

    private void tocarMusica(int index) {
        pararMusica();

        mediaPlayer = MediaPlayer.create(this, Uri.parse(caminhos.get(index)));
        mediaPlayer.start();

        nomeMusica.setText(nomes.get(index));
        listaMusicas.setItemChecked(index, true);

        // tempo total
        tempoTotal.setText(formatarTempo(mediaPlayer.getDuration()));

        // seekbar
        seekBar.setMax(mediaPlayer.getDuration());

        iniciarAtualizacaoTempo();
    }

    private void pararMusica() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        finalizarAtualizacaoTempo();
        tempoAtual.setText("0:00");
        tempoTotal.setText("0:00");
        seekBar.setProgress(0);
    }

    // ================= BOTÕES =================

    private void configurarBotoes() {

        botaoPlay.setOnClickListener(v -> {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                iniciarAtualizacaoTempo();
            }
        });

        botaoPause.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });

        botaoStop.setOnClickListener(v -> pararMusica());

        botaoNext.setOnClickListener(v -> {
            if (!caminhos.isEmpty()) {
                musicaAtual = (musicaAtual + 1) % caminhos.size();
                tocarMusica(musicaAtual);
            }
        });

        botaoPrev.setOnClickListener(v -> {
            if (!caminhos.isEmpty()) {
                musicaAtual = (musicaAtual - 1 + caminhos.size()) % caminhos.size();
                tocarMusica(musicaAtual);
            }
        });
    }

    // ================= SEEKBAR + TEMPO =================

    private void configurarSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    tempoAtual.setText(formatarTempo(progress));
                }
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void iniciarAtualizacaoTempo() {
        finalizarAtualizacaoTempo();

        atualizarTempo = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int pos = mediaPlayer.getCurrentPosition();
                    tempoAtual.setText(formatarTempo(pos));
                    seekBar.setProgress(pos);
                    handler.postDelayed(this, 1000);
                }
            }
        };

        handler.post(atualizarTempo);
    }

    private void finalizarAtualizacaoTempo() {
        if (atualizarTempo != null) {
            handler.removeCallbacks(atualizarTempo);
        }
    }

    private String formatarTempo(int millis) {
        int segundos = (millis / 1000) % 60;
        int minutos = (millis / 1000) / 60;
        return String.format("%d:%02d", minutos, segundos);
    }

    // ================= PERMISSÃO =================

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
            carregarLista();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            carregarLista();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararMusica();
    }
}

