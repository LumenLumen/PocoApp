package com.example.pocoapp;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;
import com.google.android.material.button.MaterialButton;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private ImageButton btnMute;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        enableImmersiveMode();

        // Initialisation de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.logopocollection);

        // Initialisation du détecteur de gestes
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

        // Initialisation du MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.musiquepokemon);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        // Initialisation du bouton mute
        btnMute = findViewById(R.id.btnMute);
        btnMute.setOnClickListener(v -> toggleSound());

        // Gestion des fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragment_multi, new BlankFragment());
        fragmentTransaction.add(R.id.fragment_solo, new Solo());
        fragmentTransaction.commit();

    }

    private void toggleSound() {
        if (isMuted) {
            mediaPlayer.start();
            btnMute.setImageResource(R.drawable.baseline_music_note_24); // Icône musique active
        } else {
            mediaPlayer.pause();
            btnMute.setImageResource(R.drawable.baseline_music_off_24); // Icône musique coupée
        }
        isMuted = !isMuted;
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
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    // Déplacement de la classe SwipeGestureListener en dehors des autres méthodes
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
