package com.example.pocoapp;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PokemonGuessFragment extends Fragment {

    private RecyclerView pokemonGrid;
    private PokemonGridAdapter adapter;
    private List<String> pokemonImages = new ArrayList<>();
    private List<String> pokemonNames = new ArrayList<>();
    private GestureDetector gestureDetector;

    private int row, col;

    public static PokemonGuessFragment newInstance(int row, int col) {
        PokemonGuessFragment fragment = new PokemonGuessFragment();
        Bundle args = new Bundle();
        args.putInt("row", row);
        args.putInt("col", col);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pokemon_guess, container, false);

        pokemonGrid = rootView.findViewById(R.id.pokemon_list);

        if (getArguments() != null) {
            row = getArguments().getInt("row");
            col = getArguments().getInt("col");
        }

        pokemonGrid.setLayoutManager(new GridLayoutManager(getContext(), 4));

        adapter = new PokemonGridAdapter(getContext(), pokemonImages, pokemonNames, pokemonName -> {
            Pokemon aDeviner = GameController.getInstance().getPkmnATrouver(row, col);
            if (pokemonName.equalsIgnoreCase(aDeviner.getFrench_name())) {
                GameController.getInstance().setPokemonDevine(row, col, true);
                Toast.makeText(getContext(), "Bravo ! C'était le bon Pokémon !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Raté ! Ce n'était pas le bon Pokémon.", Toast.LENGTH_SHORT).show();
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        pokemonGrid.setAdapter(adapter);

        //  GESTURE DETECTOR pour swipe vers le bas
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true; // Obligatoire pour que le fling soit détecté
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;

                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        // Swipe vers le bas
                        getParentFragmentManager().popBackStack();
                        return true;
                    }
                }
                return false;
            }
        });

        // Appliquer au layout global
        rootView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Charger les Pokémon suggérés
        loadPokemonSuggestions();

        return rootView;
    }

    private void loadPokemonSuggestions() {
        Pokemon[] suggestedPokemons = GameController.getInstance().suggestionPokemon(12, row, col);

        if (suggestedPokemons != null && suggestedPokemons.length > 0) {
            pokemonImages.clear();
            pokemonNames.clear();
            for (Pokemon p : suggestedPokemons) {
                if (p != null) {
                    pokemonImages.add(p.getImage());
                    pokemonNames.add(p.getFrench_name());
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getContext(), "Aucune suggestion de Pokémon disponible.", Toast.LENGTH_SHORT).show();
        }
    }
}
