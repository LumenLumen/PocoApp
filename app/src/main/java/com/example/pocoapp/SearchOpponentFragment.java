package com.example.pocoapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.*;

import java.nio.charset.StandardCharsets;

public class SearchOpponentFragment extends Fragment {

    private ProgressBar loadingSpinner;
    private TextView statusText;
    private ImageView opponentFoundIcon;

    private ConnectionsClient connectionsClient;
    private static final String SERVICE_ID = "com.example.pocoapp.NEARBY_SERVICE";
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private String opponentEndpointId = null;
    private boolean isHost = false;
    private boolean isInitiator = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_opponent, container, false);
        View btnMute = requireActivity().findViewById(R.id.btnMute);
        if (btnMute != null) btnMute.setVisibility(View.GONE);

        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        statusText = view.findViewById(R.id.statusText);
        opponentFoundIcon = view.findViewById(R.id.opponentFoundIcon);
        opponentFoundIcon.setVisibility(View.GONE);

        connectionsClient = Nearby.getConnectionsClient(requireContext());
        isHost = true;  // celui qui découvre est l’hôte
        startDiscovery();
        return view;
    }
    private void startDiscovery() {
        statusText.setText("Recherche d’adversaire...");
        connectionsClient.startDiscovery(
                SERVICE_ID,
                new EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
                        connectionsClient.requestConnection("Player", endpointId, connectionLifecycleCallback);
                    }

                    @Override
                    public void onEndpointLost(@NonNull String endpointId) {
                        statusText.setText("Adversaire perdu");
                    }
                },
                new DiscoveryOptions(STRATEGY)
        );
    }

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            if (result.getStatus().isSuccess()) {
                opponentEndpointId = endpointId;
                GameController.getInstance().setOpponentEndpointId(endpointId);
                connectionsClient.stopDiscovery();

                // Déterminer qui est Rouge ou Vert
                String myRole = isHost ? "Rouge" : "Vert";
                GameController.getInstance().setPlayerRole(myRole);
                showRolePopupAndStartGame(myRole);
            } else {
                statusText.setText("Connexion échouée");
            }
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            statusText.setText("Adversaire déconnecté");
        }
    };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            String message = new String(payload.asBytes(), StandardCharsets.UTF_8);
            if ("NEXT_TURN".equals(message)) {
                requireActivity().runOnUiThread(() -> {
                   // ((GameActivity) requireActivity()).onNextTurnReceived();
                });
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {}
    };

    private void showRolePopupAndStartGame(String role) {
        new AlertDialog.Builder(getContext())
                .setTitle("Rôle attribué")
                .setMessage("Tu es le joueur " + role)
                .setCancelable(false)
                .setPositiveButton("Commencer", (dialog, which) -> {
                    Intent intent = new Intent(getContext(), GameActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                }).show();
    }
}