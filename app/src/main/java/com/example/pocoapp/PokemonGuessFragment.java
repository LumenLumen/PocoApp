package com.example.pocoapp;

import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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

        // Initialiser l'adaptateur avec les images des Pokémon suggérés
        adapter = new PokemonGridAdapter(getContext(), pokemonImages, pokemonNames, pokemonName -> {
            // Exécution au clic sur un Pokémon dans la grille
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

        // Charger les suggestions de Pokémon
        loadPokemonSuggestions();

        return rootView;
    }

    private void loadPokemonSuggestions() {

        Pokemon[] suggestedPokemons = GameController.getInstance().suggestionPokemon(12, row, col);


        if (suggestedPokemons != null && suggestedPokemons.length > 0) {
            pokemonImages.clear(); // Vider la liste avant de la remplir à nouveau
            pokemonNames.clear();
            for (Pokemon p : suggestedPokemons) {
                if (p != null) {
                    pokemonImages.add(p.getImage()); // Utilisez la méthode appropriée pour obtenir l'image du Pokémon
                    pokemonNames.add(p.getFrench_name());
                }
            }
            // Mettre à jour l'adaptateur pour afficher les Pokémon suggérés
            adapter.notifyDataSetChanged();
        } else {
            // Gérer le cas où aucune suggestion n'est retournée
            Toast.makeText(getContext(), "Aucune suggestion de Pokémon disponible.", Toast.LENGTH_SHORT).show();
        }
    }


    public void verifierPokemonChoisi(Pokemon choisi) {
        Pokemon aDeviner = GameController.getInstance().getPkmnATrouver(row, col);

        if (choisi.getFrench_name().equalsIgnoreCase(aDeviner.getFrench_name())) {
            GameController.getInstance().setPokemonDevine(row, col, true);

            Toast.makeText(getContext(), "Bravo ! C'était le bon Pokémon !", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack(); // Revenir à GridFragment
        } else {
            Toast.makeText(getContext(), "Raté ! Ce n'était pas le bon Pokémon.", Toast.LENGTH_SHORT).show();
        }
    }
}
