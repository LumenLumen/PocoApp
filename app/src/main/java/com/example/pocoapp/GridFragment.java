package com.example.pocoapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

public class GridFragment extends Fragment {

    private Button[][] gridButtons = new Button[3][3];
    private Context context;
    private List<String> pokemonImages;
    private List<String> pokemonNames;
    private PokemonGridAdapter.OnPokemonClickListener listener;


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
                        //gridButtons[row][col].setText(GameController.getInstance().getPkmnATrouver(row, col).getFrench_name());
                        String spriteName = GameController.getInstance().getPkmnATrouver(row, col).getImage();
                        int spriteResId = getResources().getIdentifier(spriteName, "drawable", requireContext().getPackageName());
                        if (spriteResId != 0) {
                            gridButtons[row][col].setBackgroundResource(spriteResId);
                            gridButtons[row][col].setText(""); // On enlève le texte
                        }
                    } else {
                       // gridButtons[row][col].setText("?");
                        gridButtons[row][col].setText(GameController.getInstance().getPkmnATrouver(row, col).getFrench_name());

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
    @Override
    public void onResume() {
        super.onResume();
        refreshGrid();
    }

    public void refreshGrid() {
        final int GRID_SIZE = 3;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                refreshButton(row, col);
            }
        }
    }

    private void refreshButton(int row, int col) {
        Button button = gridButtons[row][col];
        Pokemon pokemon = GameController.getInstance().getPkmnATrouver(row, col);

        if (GameController.getInstance().estDevine(row, col)) {
            // Pokemon has been found
            showPokemonImage(button, pokemon);
            setWinnerBackground(button, GameController.getInstance().getCaseGagnee(row, col));
        } else {
            // Pokemon not yet found
            gridButtons[row][col].setText("?");

            button.setBackgroundResource(android.R.color.transparent);
        }
    }

    private void showPokemonImage(Button button, Pokemon pokemon) {
        button.setText(""); // Remove text
        String imageUrl = pokemon.getImage(); // URL to the image on the internet

        Glide.with(requireContext())
                .load(imageUrl)
                .override(120, 120) // Resize if needed
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("Glide", "Error loading image", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        button.setForeground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void setWinnerBackground(Button button, String winner) {
        if ("Rouge".equals(winner)) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light));
        } else {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light));
        }
    }
}
