package com.example.pocoapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PokemonGridAdapter extends RecyclerView.Adapter<PokemonGridAdapter.ViewHolder> {
    private Context context;
    private List<String> pokemonImages;
    private OnPokemonClickListener listener;

    // Interface pour gérer les clics sur les Pokémon
    public interface OnPokemonClickListener {
        void onPokemonClick(String pokemonName);
    }

    public PokemonGridAdapter(Context context, List<String> pokemonImages, OnPokemonClickListener listener) {
        this.context = context;
        this.pokemonImages = pokemonImages;
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

        // Charger l’image avec Glide
        Glide.with(context)
                .load(imageUrl)
                .into(holder.pokemonImageView);

        holder.itemView.setOnClickListener(v -> {
            Toast.makeText(context, "Pokémon sélectionné !", Toast.LENGTH_SHORT).show();
            listener.onPokemonClick("Pokémon " + position); // Remplace par un vrai nom plus tard
        });
    }

    @Override
    public int getItemCount() {
        return pokemonImages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView pokemonImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            pokemonImageView = itemView.findViewById(R.id.pokemonImageView);
        }
    }
}
