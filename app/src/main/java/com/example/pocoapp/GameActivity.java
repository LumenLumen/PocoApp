package com.example.pocoapp;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Arrays;

public class GameActivity extends AppCompatActivity {
    private Button[][] gridButtons = new Button[3][3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Récupérer les boutons de la grille
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                String buttonID = "btn_" + row + "_" + col;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                gridButtons[row][col] = findViewById(resID);

                // Ajouter un écouteur de clic
                int finalRow = row;
                int finalCol = col;
                gridButtons[row][col].setOnClickListener(v -> openPokemonGuess(finalRow, finalCol));
            }
        }
    }

    private void openPokemonGuess(int row, int col) {
        Intent intent = new Intent(this, PokemonGuessActivity.class);
        intent.putExtra("row", row);
        intent.putExtra("col", col);
        startActivity(intent);
    }
}
