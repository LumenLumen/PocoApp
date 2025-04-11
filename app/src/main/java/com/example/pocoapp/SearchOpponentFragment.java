package com.example.pocoapp;

import android.content.Context;
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
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import java.nio.charset.StandardCharsets;

public class SearchOpponentFragment extends Fragment {

    private ProgressBar loadingSpinner;
    private TextView statusText;
    private ImageView opponentFoundIcon;

    private ConnectionsClient connectionsClient;
    private static final String SERVICE_ID = "com.example.pocoapp.NEARBY_SERVICE";
    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private String opponentEndpointId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_opponent, container, false);
        View btnMute = requireActivity().findViewById(R.id.btnMute);
        if (btnMute != null) {
            btnMute.setVisibility(View.GONE);
        }
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        statusText = view.findViewById(R.id.statusText);
        opponentFoundIcon = view.findViewById(R.id.opponentFoundIcon);
        opponentFoundIcon.setVisibility(View.GONE); // Cacher l'icône au début

        connectionsClient = Nearby.getConnectionsClient(requireContext());

        startDiscovery(); // Démarrer la recherche d'adversaire

        return view;
    }

    private void startDiscovery() {
        statusText.setText(getString(R.string.search_opponent));

        connectionsClient.startDiscovery(
                SERVICE_ID,
                new com.google.android.gms.nearby.connection.EndpointDiscoveryCallback() {
                    @Override
                    public void onEndpointFound(@NonNull String endpointId, @NonNull com.google.android.gms.nearby.connection.DiscoveredEndpointInfo info) {
                        Log.d("Nearby", "Adversaire trouvé : " + endpointId);
                        statusText.setText(getString(R.string.opponent_found_connecting));

                        connectionsClient.requestConnection("Joueur", endpointId, connectionLifecycleCallback);
                    }

                    @Override
                    public void onEndpointLost(@NonNull String endpointId) {
                        Log.d("Nearby", "Adversaire perdu");
                        statusText.setText(getString(R.string.opponent_not_found));
                    }
                },
                new com.google.android.gms.nearby.connection.DiscoveryOptions(STRATEGY)
        );
    }

    private final ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            Log.d("Nearby", "Connexion initiée avec " + endpointId);
            connectionsClient.acceptConnection(endpointId, payloadCallback);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            if (result.getStatus().isSuccess()) {
                Log.d("Nearby", "Connexion réussie !");
                statusText.setText(getString(R.string.opponent_connected));
                loadingSpinner.setVisibility(View.GONE);
                opponentFoundIcon.setVisibility(View.VISIBLE);
                opponentEndpointId = endpointId;
            } else {
                Log.d("Nearby", "Échec de la connexion");
                statusText.setText(getString(R.string.connection_failed));
            }
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            Log.d("Nearby", "Déconnexion de " + endpointId);
            statusText.setText(getString(R.string.opponent_disconnected));
            opponentEndpointId = null;
        }
    };

    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            String receivedMessage = new String(payload.asBytes(), StandardCharsets.UTF_8);
            Log.d("Nearby", "Message reçu : " + receivedMessage);
            statusText.setText(getString(R.string.message_received, receivedMessage));
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {
            // Gérer l'avancement du transfert si nécessaire
        }
    };

    public void sendMessage(String message) {
        if (opponentEndpointId != null) {
            Payload payload = Payload.fromBytes(message.getBytes(StandardCharsets.UTF_8));
            connectionsClient.sendPayload(opponentEndpointId, payload);
            Log.d("Nearby", "Message envoyé : " + message);
        }
    }
}
