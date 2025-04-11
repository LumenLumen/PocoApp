package com.example.pocoapp;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class AledClass extends Fragment {

    private EditText requete;
    private TextView pokemon;
    private RequestQueue requestQueue;

    public static AledClass newInstance() {
        return new AledClass();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialisation de la RequestQueue
        requestQueue = Volley.newRequestQueue(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Initialiser les vues
        View view = inflater.inflate(R.layout.aled, container, false);
        requete = view.findViewById(R.id.requete);
        Button response = view.findViewById(R.id.response);
        pokemon = view.findViewById(R.id.pokemon);

        // Ajouter un écouteur au bouton
        response.setOnClickListener(v -> sendData());

        return view;
    }

    private void sendData() {

        String url = "https://pokeapi.co/api/v2/pokemon/";
        String text = requete.getText().toString().trim(); // Récupérer le texte de l'EditText

        if (text.isEmpty()) {
            pokemon.setText("Veuillez entrer un nom de Pokémon.");
            return;
        }

        // Modifier l'URL pour inclure le nom du Pokémon
        String pokemonUrl = url + text.toLowerCase();

        // Création de l'objet JSON à envoyer
        JSONObject data = new JSONObject();
        try {
            data.put("pokemon", text);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(data);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, pokemonUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String predictedPoke = response.getString("weight");
                            pokemon.setText("Poids du Pokémon : " + predictedPoke);
                            Log.d("DEBUG", "Poids : " + predictedPoke);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            pokemon.setText("Erreur de réponse JSON");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pokemon.setText("Erreur de connexion : " + error.toString());
                    }
                }
        );

        pokemon.setVisibility(View.VISIBLE);

        requestQueue.add(jsonObjectRequest);
    }

}