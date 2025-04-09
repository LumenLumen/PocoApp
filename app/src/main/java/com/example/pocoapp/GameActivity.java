package com.example.pocoapp;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class GameActivity extends AppCompatActivity {

    public static MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private ImageButton btnMute;
    private int currentPlayer = 1; // 1 pour Joueur 1, 2 pour Joueur 2
    private TextView timerText;
    private CountDownTimer countDownTimer;
    private int timeLeft = 15; // 15 secondes pour chaque joueur
    private boolean isTimerRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        enableImmersiveMode();
        timerText = findViewById(R.id.timerText);
        startTimer();
        // Initialisation du son
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        isMuted = prefs.getBoolean("isMuted", false);
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.bourgenvol);
            mediaPlayer.setLooping(true);
            if (!isMuted) mediaPlayer.start();
        }

        // Initialisation du bouton mute
        btnMute = findViewById(R.id.btnMute);
        updateMuteIcon();
        btnMute.setOnClickListener(v -> toggleSound());
        // Initialisation des joueurs
        updatePlayerTurn();

        // Ajouter la grille comme fragment principal
        showGridFragment();
    }

    private void showGridFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_grid, new GridFragment());
        transaction.commit();
    }

    public void showPokemonGuessFragment(int row, int col) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        PokemonGuessFragment guessFragment = PokemonGuessFragment.newInstance(row, col);
        transaction.replace(R.id.fragment_grid, guessFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private void startTimer() {
        if (isTimerRunning) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeLeft * 1000, 1000) {  // 1000ms = 1 seconde
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) millisUntilFinished / 1000;
                timerText.setText(String.valueOf(timeLeft));
            }

            @Override
            public void onFinish() {
                // Changer de joueur lorsque le timer expire
                changePlayer();
            }
        }.start();
        isTimerRunning = true;
    }

    private void changePlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        updatePlayerTurn();  // Met à jour l'affichage du joueur courant

        // Redémarre le timer pour l'autre joueur
        timeLeft = 15;  // Réinitialiser le timer
        startTimer();
    }

    private void updatePlayerTurn() {
        // Si c'est le tour du Joueur 1
        if (currentPlayer == 1) {
            // Joueur 1 visible, Joueur 2 transparent
            findViewById(R.id.player1_turn).setAlpha(1.0f);  // Joueur 1 opaque
            findViewById(R.id.player2_turn).setAlpha(0.3f);  // Joueur 2 transparent
        } else {
            // Joueur 1 transparent, Joueur 2 visible
            findViewById(R.id.player1_turn).setAlpha(0.3f);  // Joueur 1 transparent
            findViewById(R.id.player2_turn).setAlpha(1.0f);  // Joueur 2 opaque
        }
    }


    private void toggleSound() {
        isMuted = !isMuted;
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putBoolean("isMuted", isMuted).apply();

        if (mediaPlayer != null) {
            if (isMuted) mediaPlayer.pause();
            else mediaPlayer.start();
        }

        updateMuteIcon();
    }

    private void updateMuteIcon() {
        if (btnMute != null) {
            btnMute.setImageResource(isMuted ? R.drawable.baseline_music_off_24 : R.drawable.baseline_music_note_24);
        }
    }

    private void enableImmersiveMode() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> enableImmersiveMode());
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (isTimerRunning) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTimerRunning) {
            startTimer();
        }
    }
}
