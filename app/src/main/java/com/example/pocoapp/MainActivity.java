package com.example.pocoapp;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {
    public static MediaPlayer mediaPlayer; // MediaPlayer partagé avec ResultateActivity
    private boolean isMuted = false;
    private ImageButton btnMute;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        enableImmersiveMode();

        // Initialisation du gestureDetector pour détecter les swipes
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        // Initialisation de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.logopocollection);

        // Chargement de l'état du son depuis les préférences
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        isMuted = prefs.getBoolean("isMuted", false);

        // Initialisation du MediaPlayer UNE SEULE FOIS
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.musiquepokemon);
            mediaPlayer.setLooping(true);
            Log.d("DEBUG", "MainActivity - MediaPlayer initialisé");

            if (!isMuted) {
                mediaPlayer.start();
                Log.d("DEBUG", "MainActivity - Musique lancée");
            }
        } else {
            Log.d("DEBUG", "MainActivity - MediaPlayer existant");
        }

        // Initialisation du bouton mute
        btnMute = findViewById(R.id.btnMute);
        updateMuteIcon(); // Met à jour l'icône au démarrage
        btnMute.setOnClickListener(v -> toggleSound());

        // Gestion des fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragment_multi, new BlankFragment());
        fragmentTransaction.add(R.id.fragment_solo, new Solo());

        fragmentTransaction.commit();
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
        if (btnMute != null) {
            btnMute.setImageResource(isMuted ? R.drawable.baseline_music_off_24 : R.drawable.baseline_music_note_24);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.d("DEBUG", "MainActivity - Musique mise en pause (veille)");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        isMuted = prefs.getBoolean("isMuted", false);

        if (mediaPlayer != null && !isMuted) {
            mediaPlayer.start();
            Log.d("DEBUG", "MainActivity - Musique relancée après veille");
        }

        updateMuteIcon();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void enableImmersiveMode() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                enableImmersiveMode();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return (gestureDetector != null && gestureDetector.onTouchEvent(event)) || super.onTouchEvent(event);
    }

    // Gestion du swipe pour revenir en arrière
    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 != null && e2 != null) {
                float diffX = e2.getX() - e1.getX();
                if (diffX > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    getSupportFragmentManager().popBackStack();
                    return true;
                }
            }
            return false;
        }
    }
}
