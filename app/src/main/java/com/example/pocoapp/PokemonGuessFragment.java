package com.example.pocoapp;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PokemonGuessFragment extends Fragment {

    private RecyclerView pokemonGrid;
    private PokemonGridAdapter adapter;
    private Map<String, ImageView> typeIcons = new HashMap<>();

    private List<String> pokemonImages = new ArrayList<>();
    private List<String> pokemonNames = new ArrayList<>();

    private GestureDetector gestureDetector;
    private AutoCompleteTextView input;
    private Button submitButton;

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
        input = rootView.findViewById(R.id.pokemonAutoCompleteTextView);
        submitButton = rootView.findViewById(R.id.submitButton);

        if (getArguments() != null) {
            row = getArguments().getInt("row");
            col = getArguments().getInt("col");
        }

        pokemonGrid.setLayoutManager(new GridLayoutManager(getContext(), 4));

        adapter = new PokemonGridAdapter(getContext(), pokemonImages, pokemonNames, pokemonName -> {
            handleGuess(pokemonName);
        });

        pokemonGrid.setAdapter(adapter);

        // Swipe vers le bas pour quitter
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffY = e2.getY() - e1.getY();
                if (diffY > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                    return true;
                }
                return false;
            }
        });

        rootView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Chargement des suggestions
        loadPokemonSuggestions();

        // Vérification via la barre de saisie
        submitButton.setOnClickListener(v -> {
            String guess = input.getText().toString().trim();
            if (!guess.isEmpty()) {
                handleGuess(guess);
            }
        });

        return rootView;
    }
    private void updateTypeDisplay(String[] proposedTypes, String[] correctTypes) {
        for (Map.Entry<String, ImageView> entry : typeIcons.entrySet()) {
            String type = entry.getKey();
            ImageView icon = entry.getValue();

            if (contains(correctTypes, type)) {
                icon.setAlpha(1.0f); // type correct
            } else if (contains(proposedTypes, type)) {
                icon.setAlpha(0.3f); // type incorrect proposé
            } else {
                icon.setAlpha(1.0f); // pas proposé = neutre
            }
        }
    }

    private boolean contains(String[] array, String value) {
        for (String s : array) {
            if (s != null && s.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
    private void handleGuess(String guessedName) {
        Pokemon target = GameController.getInstance().getPkmnATrouver(row, col);
        if (guessedName.equalsIgnoreCase(target.getFrench_name())) {
            GameController.getInstance().setPokemonDevine(row, col, true);
            Toast.makeText(getContext(), "Bravo ! C'était le bon Pokémon !", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Raté ! Ce n'était pas le bon Pokémon.", Toast.LENGTH_SHORT).show();
        }
        requireActivity().getSupportFragmentManager().popBackStack();
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
