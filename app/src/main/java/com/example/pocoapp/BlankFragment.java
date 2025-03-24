package com.example.pocoapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BlankFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blank, container, false);

        // Récupère le bouton dans la vue
        Button btnFragment = view.findViewById(R.id.btnFragment);

        // Ajoute un écouteur de clic
        btnFragment.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, new SearchOpponentFragment());


            // Ajoute la transaction à la back stack pour pouvoir revenir en arrière
            transaction.addToBackStack(null);
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

        return view;
    }




}
