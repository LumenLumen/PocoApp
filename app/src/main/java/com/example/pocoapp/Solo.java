package com.example.pocoapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Button;
public class Solo extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_solo, container, false);
        Button btnFragment = view.findViewById(R.id.btnFragment);

        // Ajoute un écouteur de clic
        btnFragment.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fragment cliqué !", Toast.LENGTH_SHORT).show();
        });
        return view;
    }
}
