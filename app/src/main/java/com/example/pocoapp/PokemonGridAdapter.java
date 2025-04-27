package com.example.pocoapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PokemonGridAdapter extends RecyclerView.Adapter<PokemonGridAdapter.ViewHolder> {
    private Context context;
    private List<String> pokemonImages;
    private List<String> pokemonNames;

    private OnPokemonClickListener listener;

    // Interface pour gérer les clics sur les Pokémon
    public interface OnPokemonClickListener {
        void onPokemonClick(String pokemonName);
    }

    public PokemonGridAdapter(Context context, List<String> pokemonImages, List<String> pokemonNames, OnPokemonClickListener listener) {
        this.context = context;
        this.pokemonImages = pokemonImages;
        this.pokemonNames = pokemonNames;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pokemon, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = pokemonImages.get(position);
        String pokemonName = pokemonNames.get(position);  // ici on récupère le bon nom

        // Charger l’image avec Glide
        Glide.with(context)
                .load(imageUrl)
                .into(holder.pokemonImageView);

        holder.itemView.setOnClickListener(v -> {
            listener.onPokemonClick(pokemonName);
        });
    }

    private boolean isPlayerTurn() {
        // Vérifie via le GameController si c'est le tour du joueur
        return GameController.getInstance().getPlayerRole()
                .equals(((GameActivity) context).getCurrentPlayer());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pokemonImageView;
        TextView pokemonName; // Ajouté

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pokemonImageView = itemView.findViewById(R.id.pokemonImageView);
            pokemonName = itemView.findViewById(R.id.pokemonName); // Correspond à votre XML
        }
    }
    @Override
    public int getItemCount() {
        return pokemonNames.size();
    }

}
