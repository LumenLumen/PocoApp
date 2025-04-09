package com.example.pocoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PokemonGuessActivity extends AppCompatActivity {

    private RecyclerView pokemonGrid;
    private PokemonGridAdapter adapter;
    private EditText guessInput;
    private List<String> pokemonImages = new ArrayList<>();
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_guess);

        pokemonGrid = findViewById(R.id.pokemonGrid);
        guessInput = findViewById(R.id.guessInput);

        pokemonGrid.setLayoutManager(new GridLayoutManager(this, 4));

        adapter = new PokemonGridAdapter(this, pokemonImages, pokemonName -> {
            Intent intent = new Intent();
            intent.putExtra("chosenPokemon", pokemonName);
            setResult(RESULT_OK, intent);
            finish();
        });

        pokemonGrid.setAdapter(adapter);

        loadPokemon();

        // Swipe pour revenir au jeu
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e2.getX() > e1.getX()) { // Swipe vers la droite
                    finish();
                    return true;
                }
                return false;
            }
        });
        gestureDetector = new GestureDetector(this, new SwipeGestureListener());

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    finish(); // Swipe vers la droite -> Ferme l'activité
                }
                return true;
            }
            return false;
        }
    }

    private void loadPokemon() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pokeapi.co/api/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        /*PokeApi pokeAPI = retrofit.create(PokeApi.class);

        pokeAPI.getPokemonList(20, 0).enqueue(new Callback<PokemonListResponse>() {
            @Override
            public void onResponse(@NonNull Call<PokemonListResponse> call, @NonNull Response<PokemonListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PokemonListResponse.PokemonEntry> pokemonList = response.body().getResults();
                    for (PokemonListResponse.PokemonEntry p : pokemonList) {
                        String imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + p.getId() + ".png";
                        pokemonImages.add(imageUrl);
                        System.out.println("Image ajoutée : " + imageUrl); // Debug
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    System.out.println("Erreur API: " + response.code() + " - " + response.message()); // Debug
                    Toast.makeText(PokemonGuessActivity.this, "Erreur API: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PokemonListResponse> call, @NonNull Throwable t) {
                System.out.println("Erreur Réseau: " + t.getMessage()); // Debug
                Toast.makeText(PokemonGuessActivity.this, "Erreur de connexion", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

}
