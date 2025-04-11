package com.example.pocoapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    public static MediaPlayer mediaPlayer; // MediaPlayer partagé avec ResultateActivity
    private boolean isMuted = false;
    private ImageButton btnMute;
    private ImageButton btnLangue;
    private GestureDetector gestureDetector;
    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String language = prefs.getString("Locale.Helper.Selected.Language", "en");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        loadLocale(); // Appliquer la langue avant d'afficher quoi que ce soit
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

        // Initialisation du bouton langue
        btnLangue = findViewById(R.id.btnlangue);

        btnLangue.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.language_menu, popup.getMenu());
            // ⚠️ Forcer l'affichage des icônes
            try {
                Field[] fields = popup.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if ("mPopup".equals(field.getName())) {
                        field.setAccessible(true);
                        Object menuPopupHelper = field.get(popup);
                        Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                        Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                        setForceIcons.invoke(menuPopupHelper, true);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.lang_fr) {
                    LocaleHelper.persistLanguage(MainActivity.this, "fr");
                    recreate(); // redémarre proprement l’activité
                    return true;
                } else if (item.getItemId() == R.id.lang_en) {
                    LocaleHelper.persistLanguage(MainActivity.this, "en");
                    recreate();
                    return true;
                }
                return false;
            });
            popup.show();
        });



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
    //langue
    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Sauvegarder la langue dans les préférences
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putString("Locale.Helper.Selected.Language", lang).apply();


        // Redémarrer l'activité
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

}


