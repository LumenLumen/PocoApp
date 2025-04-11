package com.example.pocoapp;
import androidx.annotation.Nullable;
// Import the FileWriter class
// Import the IOException class to handle errors

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;

public class GridFragment extends Fragment {

    private Button[][] gridButtons = new Button[3][3];
    private Pokemon[][] lstPoke = new Pokemon[3][3];

    public GridFragment() {
        // Constructeur vide requis
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Pokemon getPokemon(int randomNum){
        Pokemon pkmn = new Pokemon();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getContext().getAssets().open("dbPoke.txt")));

            // do reading, usually loop until end of file reading
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                i += 1 ;
                if (i==randomNum){
                    String[] ligne = line.split(";");
                    pkmn.setEnglish_name(ligne[0]);
                    pkmn.setFrench_name(ligne[1]);
                    pkmn.setTaille(Float.parseFloat(ligne[2]));
                    pkmn.setPoids(Float.parseFloat(ligne[3]));
                    pkmn.setImage(ligne[4]);

                    String[] types = new String[2];
                    types[0] = ligne[5];
                    types[1] = ligne[6];
                    pkmn.setTypes(types);

                    pkmn.setGeneration(Integer.parseInt(ligne[7]));
                    break ;
                }
            }
            // line is not visible here.
        } catch (IOException e) {
            System.out.println("Erreur :" + e);
        }

        return pkmn;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);


        // ================== Initialiser les neufs Pokémons à deviner.
        // Initialisation des Pokémon dans lstPoke

        int max = 1025; //Nombre de Pokémon dans le Dex
        int randomNum = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                randomNum = ThreadLocalRandom.current().nextInt(1, max + 1);
                lstPoke[i][j] = getPokemon(randomNum);
            }
        }

        //===================

        // Initialiser les boutons de la grille
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                String buttonID = "btn_" + row + "_" + col;
                int resID = getResources().getIdentifier(buttonID, "id", requireContext().getPackageName());
                gridButtons[row][col] = rootView.findViewById(resID);

                if (gridButtons[row][col] != null) {
                    int finalRow = row;
                    int finalCol = col;

                    //gridButtons[row][col].setText(lstPoke[row][col].getName());

                    //Action sur clic
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
