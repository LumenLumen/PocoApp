package com.example.pocoapp;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class GridFragment extends Fragment {

    private Button[][] gridButtons = new Button[3][3];

    public GridFragment() {
        // Constructeur vide requis
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

        // Initialiser les boutons de la grille
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                String buttonID = "btn_" + row + "_" + col;
                int resID = getResources().getIdentifier(buttonID, "id", requireContext().getPackageName());
                gridButtons[row][col] = rootView.findViewById(resID);

                if (gridButtons[row][col] != null) {
                    int finalRow = row;
                    int finalCol = col;
                    gridButtons[row][col].setOnClickListener(v -> {
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        // Créer une nouvelle instance de PokemonGuessFragment avec les paramètres row et col
                        PokemonGuessFragment pokemonGuessFragment = PokemonGuessFragment.newInstance(finalRow, finalCol);

                        // Remplacer le fragment actuel par PokemonGuessFragment
                        transaction.replace(R.id.fragment_grid, pokemonGuessFragment);
                        transaction.addToBackStack(null);  // Permet le retour en arrière
                        transaction.commit();
                        fragmentManager.addOnBackStackChangedListener(() -> {
                            if (fragmentManager.getBackStackEntryCount() == 0) {
                                View btnMute = requireActivity().findViewById(R.id.btnMute);
                                if (btnMute != null) {
                                    btnMute.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    });
                }
            }
        }

        return rootView;
    }




}
