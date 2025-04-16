package com.example.pocoapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class GridFragment extends Fragment {

    private Button[][] gridButtons = new Button[3][3];

    public GridFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                String buttonID = "btn_" + row + "_" + col;
                int resID = getResources().getIdentifier(buttonID, "id", requireContext().getPackageName());
                gridButtons[row][col] = rootView.findViewById(resID);

                if (gridButtons[row][col] != null) {
                    int finalRow = row;
                    int finalCol = col;

                    // Affiche "?" si le Pokémon n'est pas encore deviné, sinon son nom
                    if (GameController.getInstance().estDevine(row, col)) {
                        gridButtons[row][col].setText(GameController.getInstance().getPkmnATrouver(row, col).getFrench_name());
                    } else {
                        gridButtons[row][col].setText("?");
                    }

                    gridButtons[row][col].setOnClickListener(v -> {
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        FragmentTransaction transaction = fragmentManager.beginTransaction();

                        PokemonGuessFragment pokemonGuessFragment = PokemonGuessFragment.newInstance(finalRow, finalCol);
                        transaction.replace(R.id.fragment_grid, pokemonGuessFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    });
                }
            }
        }

        return rootView;
    }
}
