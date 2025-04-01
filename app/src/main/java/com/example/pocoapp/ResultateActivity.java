package com.example.pocoapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.androidgamesdk.GameActivity;

public class ResultateActivity extends AppCompatActivity {
    private ImageView resultGif;
    private LottieAnimationView confettiAnimation;
    private Button btnRejouer, btnMenu;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultate);

        Log.d("DEBUG", "Lancement de ResultateActivity");

        // Initialisation des vues
        resultGif = findViewById(R.id.resultGif);
        confettiAnimation = findViewById(R.id.confettiAnimation);
        btnRejouer = findViewById(R.id.btnRejouer);
        btnMenu = findViewById(R.id.btnMenu);
        resultText = findViewById(R.id.resultText);


        boolean isWin = getIntent().getBooleanExtra("IS_WIN", false);
        Log.d("DEBUG", "isWin = " + isWin);

        if (isWin) {
            resultText.setText("Gagné !");
            showVictoryScreen();
        } else {
            resultText.setText("Perdu !");
            showDefeatScreen();
        }

        btnRejouer.setOnClickListener(v -> restartGame());
        btnMenu.setOnClickListener(v -> goToMenu());

    }

    private void showVictoryScreen() {
        Log.d("DEBUG", "Affichage de l'écran de victoire");
        Glide.with(this).asGif().load(R.drawable.victory).into(resultGif);
        confettiAnimation.setVisibility(View.VISIBLE);
        confettiAnimation.postDelayed(() -> confettiAnimation.playAnimation(), 100);
    }

    private void showDefeatScreen() {
        Log.d("DEBUG", "Affichage de l'écran de défaite");
        Glide.with(this).asGif().load(R.drawable.defaite).into(resultGif);
        confettiAnimation.setVisibility(View.GONE);
    }

    private void restartGame() {
        Intent intent = new Intent(ResultateActivity.this, GameActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToMenu() {
        Intent intent = new Intent(ResultateActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isMuted = prefs.getBoolean("isMuted", false);

        Log.d("DEBUG", "ResultateActivity - onResume() called");

        if (MainActivity.mediaPlayer == null) {
            Log.e("ERROR", "MediaPlayer est null dans ResultateActivity !");
        } else {
            if (isMuted) {
                MainActivity.mediaPlayer.pause();
                Log.d("DEBUG", "Musique en pause car mute activé");
            } else {
                if (!MainActivity.mediaPlayer.isPlaying()) {
                    MainActivity.mediaPlayer.start();
                    Log.d("DEBUG", "Musique relancée");
                } else {
                    Log.d("DEBUG", "Musique déjà en lecture");
                }
            }
        }
    }





}