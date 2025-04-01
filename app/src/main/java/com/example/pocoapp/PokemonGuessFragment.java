package com.example.pocoapp;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PokemonGuessFragment extends Fragment {

    private RecyclerView pokemonGrid;
    private PokemonGridAdapter adapter;
    private EditText guessInput;
    private List<String> pokemonImages = new ArrayList<>();
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

        pokemonGrid = rootView.findViewById(R.id.pokemon_grid);  // Assurez-vous que cette vue existe dans le layout de fragment_pokemon_guess.xml

        if (getArguments() != null) {
            row = getArguments().getInt("row");
            col = getArguments().getInt("col");
        }

        pokemonGrid.setLayoutManager(new GridLayoutManager(getContext(), 4));

        adapter = new PokemonGridAdapter(getContext(), pokemonImages, pokemonName -> {
            if (getActivity() instanceof GameActivity) {
                getParentFragmentManager().popBackStack(); // Ferme le fragment
            }
        });

        pokemonGrid.setAdapter(adapter);

        // Initialiser le GestureDetector pour capter les événements de swipe sur le RecyclerView
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Si un swipe est détecté de droite à gauche (glissement de droite à gauche)
                if (e2.getX() > e1.getX()) {
                    getParentFragmentManager().popBackStack(); // Ferme le fragment
                    return true;
                }
                return false;
            }
        });

        // Ajouter un OnTouchListener au RecyclerView pour gérer le swipe
        pokemonGrid.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        return rootView;
    }
}
