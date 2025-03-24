package com.example.pocoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PokemonHintFragment extends Fragment {
    private TextView hintTextView;
    private EditText guessEditText;
    private Button submitButton;

    public PokemonHintFragment() {
        // Constructeur vide requis
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokemon_hint, container, false);

        hintTextView = view.findViewById(R.id.hintTextView);
        guessEditText = view.findViewById(R.id.guessEditText);
        submitButton = view.findViewById(R.id.submitButton);

        // Exemple d'indice
        hintTextView.setText("Indice : Type Feu 🔥");

        submitButton.setOnClickListener(v -> {
            String guess = guessEditText.getText().toString().trim();
            if (!guess.isEmpty()) {
                // Vérifier si le Pokémon est bon
            }
        });

        return view;
    }
}
