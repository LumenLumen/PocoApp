package com.example.pocoapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Set;

public class SearchOpponentFragment extends Fragment {

    private ProgressBar loadingSpinner;
    private TextView statusText;
    private ImageView opponentFoundIcon;
    private BluetoothAdapter bluetoothAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_opponent, container, false);

        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        statusText = view.findViewById(R.id.statusText);
        opponentFoundIcon = view.findViewById(R.id.opponentFoundIcon);
        opponentFoundIcon.setVisibility(View.GONE); // Cacher l'icône au début

        // Lancer la recherche Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            statusText.setText("Bluetooth désactivé !");
            return view;
        }

        searchForOpponent();

        return view;
    }

    private void searchForOpponent() {
        statusText.setText("Recherche d'un adversaire...");

        new Handler().postDelayed(() -> {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (!pairedDevices.isEmpty()) {
                statusText.setText("Adversaire trouvé !");
                loadingSpinner.setVisibility(View.GONE);
                opponentFoundIcon.setVisibility(View.VISIBLE);


            } else {
                statusText.setText("Aucun adversaire trouvé !");
                loadingSpinner.setVisibility(View.GONE);
            }
        }, 5000); // Simule 5 secondes de recherche
    }
}
