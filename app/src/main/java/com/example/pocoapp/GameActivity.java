package com.example.pocoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
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
    private int timeLeft = 60; // Fixé à 60s

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameController = GameController.getInstance();
        gameController.initContexte(getApplicationContext());
        gameController.initMorpion();

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
        guessFragment.setOnGuessCompleteListener(() -> {
            nextPlayerAfterGuess();
        });
        transaction.replace(R.id.fragment_grid, guessFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public String getCurrentPlayer() {

        return currentPlayer == 1 ? "Rouge" : "Vert";
    }

    private void startTimer() {
        if (isTimerRunning && countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(60_000, 1_000) { // 60 secondes
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

    // Ajoutez ces variables membres
    private boolean isBotTurn = false;

    // Modifiez la méthode changePlayer()
    private void changePlayer() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        updatePlayerTurn();
        GameController.getInstance().setPlayerRole(currentPlayer == 1 ? "Rouge" : "Vert");

        if(currentPlayer == 2) {
            isBotTurn = true;
            new Handler().postDelayed(this::handleBotTurn, 2000); // 2s de délai
        } else {
            isBotTurn = false;
        }

        startTimer();
    }

    // Ajoutez cette méthode pour gérer le tour du bot
    private void handleBotTurn() {
        if(isBotTurn) {
            GameController.getInstance().tour_bot();
            checkVictory();

            // Si la partie continue, repasse au joueur
            if(GameController.getInstance().isGameOver().isEmpty()) {
                changePlayer();
            }
        }
    }

    // Modifiez checkVictory()
    private void checkVictory() {
        String winner = GameController.getInstance().isGameOver();
        String currentPlayerRole = getCurrentPlayer();  // On récupère le rôle du joueur actuel
        if (!winner.isEmpty()) {
            boolean isPlayerWinner = winner.equals(currentPlayerRole); // Vérifie si le rôle du gagnant correspond au rôle du joueur actuel

            Intent intent = new Intent(this, ResultateActivity.class);
            intent.putExtra("isPlayerWinner", isPlayerWinner);
            startActivity(intent);
            finish();
        }
    }


    private void updatePlayerTurn() {
        findViewById(R.id.player1_turn).setAlpha(currentPlayer == 1 ? 1.0f : 0.3f);
        findViewById(R.id.player2_turn).setAlpha(currentPlayer == 2 ? 1.0f : 0.3f);
    }

    private void toggleSound() {
        isMuted = !isMuted;

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isMuted", isMuted);
        editor.apply();

        if (mediaPlayer != null) {
            if (isMuted) {
                mediaPlayer.pause();
                Log.d("DEBUG", "Musique en pause");
            } else {
                mediaPlayer.start();
                Log.d("DEBUG", "Musique relancée");
            }
        }

        updateMuteIcon();
    }

    private void updateMuteIcon() {
        btnMute.setImageResource(isMuted ? R.drawable.baseline_music_off_24 : R.drawable.baseline_music_note_24);
    }

    public void onBackToMenuClick(View view) {
        GameController.getInstance().resetGame();

        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        startActivity(intent);

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

    public void nextPlayerAfterGuess() {
        changePlayer();


        // Rafraîchir la grille
        Fragment gridFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_grid);
        if (gridFragment instanceof GridFragment) {
            ((GridFragment) gridFragment).refreshGrid();
        }
        checkVictory();
        startTimer();

    }
    public void handleCorrectGuess() {
        nextPlayerAfterGuess();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (isTimerRunning && countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isTimerRunning) {
            startTimer();
        }

        if (mediaPlayer != null && !isMuted) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }
}
