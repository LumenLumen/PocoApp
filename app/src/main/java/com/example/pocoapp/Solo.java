package com.example.pocoapp;

import android.content.Intent; // Ajout de l'import
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class Solo extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solo, container, false);
        Button btnFragment = view.findViewById(R.id.btnFragment);

        // Ajoute un écouteur de clic
        btnFragment.setOnClickListener(v -> {
            // Démarre ResultateActivity
            //Intent intent = new Intent(requireActivity(), GameActivity.class);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, new AledClass());

            transaction.commit();
        });

        return view;
    }
}
