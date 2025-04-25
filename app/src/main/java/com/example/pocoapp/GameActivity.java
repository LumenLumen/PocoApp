package com.example.pocoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class GameActivity extends AppCompatActivity {
    private GameController gameController;
    private View redIndicator, greenIndicator;
    public static MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private ImageButton btnMute;
    private TextView timerText;

    private int currentPlayer = 1; // 1 = Rouge, 2 = Vert
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private int timeLeft = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        GameController.getInstance().initContexte(getApplicationContext());
        GameController.getInstance().initMorpion();
        enableImmersiveMode();

        timerText = findViewById(R.id.timerText);
        btnMute = findViewById(R.id.btnMute);

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        isMuted = prefs.getBoolean("isMuted", false);

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.bourgenvol);
            mediaPlayer.setLooping(true);
            if (!isMuted) mediaPlayer.start();
        }

        updateMuteIcon();
        btnMute.setOnClickListener(v -> toggleSound());

        updatePlayerTurn();
        startTimer();
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

    public void handleCorrectGuess() {
        changePlayer(); // Change de joueur uniquement si la réponse est correcte
    }

    private void startTimer() {
        if (isTimerRunning) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(15_000, 1_000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                timerText.setText(String.valueOf(timeLeft));
            }

            @Override
            public void onFinish() {
                changePlayer();
            }
        }.start();

        isTimerRunning = true;
    }

    private void changePlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        updatePlayerTurn();
        timeLeft = 15;
        startTimer();
    }

    private void updatePlayerTurn() {
        findViewById(R.id.player1_turn).setAlpha(currentPlayer == 1 ? 1.0f : 0.3f);
        findViewById(R.id.player2_turn).setAlpha(currentPlayer == 2 ? 1.0f : 0.3f);
    }

    private void toggleSound() {
        isMuted = !isMuted;

        // Sauvegarde du statut du son dans les préférences
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isMuted", isMuted);
        editor.apply();

        if (mediaPlayer != null) {
            if (isMuted) {
                mediaPlayer.pause();
                Log.d("DEBUG", "MainActivity - Musique en pause");
            } else {
                mediaPlayer.start();
                Log.d("DEBUG", "MainActivity - Musique relancée");
            }
        }

        updateMuteIcon();
    }

    private void updateMuteIcon() {
        btnMute.setImageResource(isMuted ? R.drawable.baseline_music_off_24 : R.drawable.baseline_music_note_24);
    }
    public void onBackToMenuClick(View view) {
        // Crée un Intent pour revenir à l'écran principal
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        startActivity(intent); // Démarre l'activité principale

        // Facultatif : Ajoute un effet de transition entre les activités
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);


        finish();
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
        if (isTimerRunning) countDownTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isTimerRunning) startTimer();
    }
}
