package com.example.pocoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PokemonGridFragment extends Fragment {
    private RecyclerView recyclerView;
    private PokemonGridAdapter adapter;
    private List<String> pokemonImages = new ArrayList<>();

    public PokemonGridFragment() {
        // Constructeur vide requis
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon_grid, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4)); // Grille 4x4

        adapter = new PokemonGridAdapter(getContext(), pokemonImages, pokemonName -> {
            Toast.makeText(getContext(), "Sélectionné : " + pokemonName, Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(adapter);

        fetchPokemon(); // Récupération des Pokémon

        return view;
    }

    private void fetchPokemon() {
        // Simulation d'images de Pokémon (à remplacer par l'API)
        pokemonImages.add("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png");
        pokemonImages.add("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/2.png");
        pokemonImages.add("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/3.png");
        // Ajouter d'autres images...

        adapter.notifyDataSetChanged();
    }
}
