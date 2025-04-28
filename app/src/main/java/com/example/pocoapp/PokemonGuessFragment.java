package com.example.pocoapp;

import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PokemonGuessFragment extends Fragment {

    private RecyclerView pokemonGrid;
    private PokemonGridAdapter adapter;
    private Map<String, ImageView> typeIcons = new HashMap<>();
    private List<String> pokemonImages = new ArrayList<>();
    private List<String> pokemonNames = new ArrayList<>();
    private GestureDetector gestureDetector;
    private EditText input;
    private Button submitButton;
    private TextView weightHintTextView;
    private TextView sizeHintTextView;
    private TextView generationHintTextView;
    private int row, col;


    // Liste de tous les types (pour itérer facilement)
    private static final ArrayList<String> ALL_TYPES = new ArrayList<>(Arrays.asList(
            "feu", "eau", "plante", "electrik", "acier", "combat", "dragon", "fee",
            "glace", "insecte", "normal", "poison", "psy", "roche",
            "sol", "spectre", "tenebres", "vol"
    ));

    // Interface pour communiquer avec l'activité parente
    public interface OnGuessCompleteListener {
        void onGuessComplete();
    }

    private OnGuessCompleteListener listener;

    // Méthode pour définir le listener
    public void setOnGuessCompleteListener(OnGuessCompleteListener listener) {
        this.listener = listener;
    }

    public static PokemonGuessFragment newInstance(int row, int col) {
        PokemonGuessFragment fragment = new PokemonGuessFragment();
        Bundle args = new Bundle();
        args.putInt("row", row);
        args.putInt("col", col);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pokemon_guess, container, false);

        // Initialisation des vues standard
        pokemonGrid = rootView.findViewById(R.id.pokemon_list);
        input = rootView.findViewById(R.id.pokemonAutoCompleteTextView);
        submitButton = rootView.findViewById(R.id.submitButton);

        // Initialisation des vues d'indices
        weightHintTextView = rootView.findViewById(R.id.weightHintTextView);
        sizeHintTextView = rootView.findViewById(R.id.sizeHintTextView);
        generationHintTextView = rootView.findViewById(R.id.generationHintTextView);

        // Récupération des arguments
        if (getArguments() != null) {
            row = getArguments().getInt("row");
            col = getArguments().getInt("col");
        } else {
            row = 0; // Valeurs par défaut ou afficher une erreur
            col = 0;
        }

        // Initialisation du RecyclerView
        pokemonGrid.setLayoutManager(new GridLayoutManager(getContext(), 4));
        adapter = new PokemonGridAdapter(getContext(), pokemonImages, pokemonNames, pokemonName -> {
            // Gérer le clic sur une suggestion (même logique que le bouton valider)
            handleGuess(pokemonName);
        });
        pokemonGrid.setAdapter(adapter);

        // Initialisation du GestureDetector pour le swipe
        setupGestureDetector();
        rootView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Initialisation de la map des icônes de type
        initializeTypeIconsMap(rootView); // Passer rootView pour trouver les vues

        // Chargement initial des suggestions
        loadPokemonSuggestions();

        // Listener pour le bouton Valider
        submitButton.setOnClickListener(v -> {
            if (!isMyTurn()) {
                Toast.makeText(getContext(), "Ce n'est pas votre tour !", Toast.LENGTH_SHORT).show();
                closeFragment(); // Retour à la grille
                return;
            }

            String guess = input.getText().toString().trim();
            if (!guess.isEmpty()) {
                handleGuess(guess);
            } else {
                Toast.makeText(getContext(), "Veuillez entrer un nom de Pokémon.", Toast.LENGTH_SHORT).show();
            }
        });


        // Affichage initial des indices connus
        updateHints();

        return rootView;
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffY = e2.getY() - e1.getY();
                if (diffY > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    closeFragment(); // Utiliser une méthode dédiée pour la fermeture
                    return true;
                }
                return false;
            }
        });
    }

    // Méthode pour remplir la map typeIcons
    private void initializeTypeIconsMap(View rootView) {
        typeIcons.clear();
        for (String typeName : ALL_TYPES) {
            String resourceName = "type_" + typeName;
            int resourceId = getResources().getIdentifier(resourceName, "id", requireContext().getPackageName());

            if (resourceId != 0) {
                ImageView icon = rootView.findViewById(resourceId);
                if (icon != null) {
                    icon.setAlpha(0.3f); // Transparent par défaut
                    typeIcons.put(typeName.toLowerCase(), icon);
                }
            }
        }
    }


    // Méthode pour mettre à jour TOUS les indices à l'écran
    private void updateHints() {
        if (getContext() == null) return; // Sécurité si le fragment est détaché

        InformationPokemon info = GameController.getInstance().getInfos(row, col);
        if (info == null) {
            System.err.println("Erreur: Impossible de récupérer InformationPokemon pour [" + row + "," + col + "]");
            return; // Ne rien faire si les infos ne sont pas dispos
        }

        // --- Mise à jour Poids ---
        float poidsMin = info.getPoids_min();
        float poidsMax = info.getPoids_max();
        if (poidsMin == poidsMax && poidsMax != 0 && poidsMax != 1000F) { // Condition pour éviter l'état initial
            weightHintTextView.setText(String.format(java.util.Locale.US,"Poids: %.1f kg ✓", poidsMin));
        } else {
            // Afficher la flèche seulement si on a une info pertinente
            String hintText = "Poids: ?";
            if (poidsMin > 0F) hintText = String.format(java.util.Locale.US, "Poids: > %.1f kg", poidsMin);
            if (poidsMax < 1000F) {
                if (poidsMin > 0F) hintText += String.format(java.util.Locale.US," et < %.1f kg", poidsMax);
                else hintText = String.format(java.util.Locale.US,"Poids: < %.1f kg", poidsMax);
            }
            weightHintTextView.setText(hintText);
        }

        // --- Mise à jour Taille ---
        float tailleMin = info.getTaille_min();
        float tailleMax = info.getTaille_max();
        if (tailleMin == tailleMax && tailleMax != 0 && tailleMax != 200F) { // Condition pour éviter l'état initial
            sizeHintTextView.setText(String.format(java.util.Locale.US,"Taille: %.1f m ✓", tailleMin));
        } else {
            String hintText = "Taille: ?";
            if (tailleMin > 0F) hintText = String.format(java.util.Locale.US,"Taille: > %.1f m", tailleMin);
            if (tailleMax < 200F) {
                if (tailleMin > 0F) hintText += String.format(java.util.Locale.US," et < %.1f m", tailleMax);
                else hintText = String.format(java.util.Locale.US,"Taille: < %.1f m", tailleMax);
            }
            sizeHintTextView.setText(hintText);
        }

        // --- Mise à jour Génération ---
        int genMin = info.getGeneration_min();
        int genMax = info.getGeneration_max();
        if (genMin == genMax && genMin != 0 && genMin != 10) {
            generationHintTextView.setText(String.format(java.util.Locale.US,"Génération: %d ✓", genMin));
        } else {
            String hintText = "Génération: ?";
            if (genMin > 1) hintText = String.format(java.util.Locale.US,"Génération: > %d", genMin);
            if (genMax < 9) {
                if (genMin > 1) hintText += String.format(java.util.Locale.US," et < %d", genMax);
                else hintText = String.format(java.util.Locale.US,"Génération: < %d", genMax);
            }
            generationHintTextView.setText(hintText);
        }

        // --- Mise à jour des Types (Opacité et Highlighting) ---
        updateTypeHintsUI(info);
    }


    // Méthode spécifique pour les types (appelée par updateHints)
    private void updateTypeHintsUI(InformationPokemon info) {
        // Tous transparents au départ
        for (ImageView icon : typeIcons.values()) {
            icon.setAlpha(0.3f);
            icon.setBackground(null);
        }

        // Puis on affiche seulement les types trouvés
        for (String type : info.getTypes_identiques()) {
            if (type == null) continue;
            ImageView icon = typeIcons.get(type.toLowerCase());
            if (icon != null) {
                icon.setAlpha(1.0f);
                icon.setBackgroundResource(R.drawable.type_correct_background);
            }
        }
    }



    private boolean isMyTurn() {
        String myRole = GameController.getInstance().getPlayerRole();
        String currentPlayer = ((GameActivity) requireActivity()).getCurrentPlayer();
        return myRole.equals(currentPlayer);
    }




    // Logique de gestion de la réponse du joueur
    private void handleGuess(String guessedName) {
        if (getContext() == null) return;

        Pokemon guessedPokemon = GameController.getInstance().getPokemonByName(guessedName);

        if (guessedPokemon == null) {
            Toast.makeText(getContext(), "Pokémon '" + guessedName + "' non trouvé.", Toast.LENGTH_SHORT).show();
            return;
        }

        String playerRole = ((GameActivity) requireActivity()).getCurrentPlayer();
        int result = GameController.getInstance().checkReponse(guessedPokemon, row, col, playerRole);

        switch (result) {
            case 1: // Bonne réponse
                Toast.makeText(getContext(), "Bravo ! C'était " + guessedPokemon.getFrench_name() + " !", Toast.LENGTH_LONG).show();
                ((GameActivity) requireActivity()).nextPlayerAfterGuess();
                closeFragment();
                break;
            case 0: // Mauvaise réponse, mais valide
                Toast.makeText(getContext(), "Raté ! Ce n'est pas " + guessedPokemon.getFrench_name() + ".", Toast.LENGTH_SHORT).show();
                ((GameActivity) requireActivity()).nextPlayerAfterGuess();
                updateHints();
                closeFragment();
                break;
            case -1: // Réponse invalide
                Toast.makeText(getContext(), "'" + guessedPokemon.getFrench_name() + "' a déjà été proposé ou la case est déjà prise.", Toast.LENGTH_SHORT).show();
                input.setText("");
                break;
        }
    }

    // Recharger les suggestions de Pokémon
    private void loadPokemonSuggestions() {
        if (getContext() == null) return;

        // Utilise la méthode existante qui filtre déjà les Pokémon dits
        Pokemon[] suggestedPokemons = GameController.getInstance().suggestionPokemon(12, row, col);
        if (suggestedPokemons != null && suggestedPokemons.length > 0) {
            pokemonImages.clear();
            pokemonNames.clear();
            for (Pokemon p : suggestedPokemons) {
                if (p != null) {
                    pokemonImages.add(p.getImage());
                    pokemonNames.add(p.getFrench_name());
                }
            }
            adapter.notifyDataSetChanged();
        } else {
            // Peut-être juste vider la liste visuellement si pas de suggestions ?
            pokemonImages.clear();
            pokemonNames.clear();
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Aucune nouvelle suggestion de Pokémon disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    // Méthode pour fermer proprement le fragment
    private void closeFragment() {
        // Rafraîchir le GridFragment existant
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_grid, new GridFragment());
        transaction.commit();

        // Retirer le fragment actuel de la pile
        requireActivity().getSupportFragmentManager().popBackStackImmediate();
    }

}