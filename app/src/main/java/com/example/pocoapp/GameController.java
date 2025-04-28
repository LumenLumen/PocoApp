package com.example.pocoapp;

import android.content.Context;
import android.util.Log; // Importation ajoutée pour le débogage

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays; // Importation ajoutée
import java.util.HashMap; // Importation ajoutée
import java.util.HashSet;
import java.util.List; // Importation ajoutée
import java.util.Locale;
import java.util.Map; // Importation ajoutée
import java.util.Objects;
import java.util.Random;

/*Cette classe est un singleton.*/
public class GameController {

    private static GameController instance; // singleton
    private Context context; // contexte

    // Nouvelle structure pour stocker tous les Pokémon en mémoire
    private Map<Integer, Pokemon> pokemonById;
    private Map<String, Pokemon> pokemonByFrenchName;

    private final Pokemon[][] pkmnATrouver = new Pokemon[3][3]; // Stocke les pkmn à trouver dans la grille
    private final String[][] caseTrouvee = new String[3][3]; // Stocke les cases gagnées par qui ("Rouge", "Bleu", ou "")
    private InformationPokemon[][] informations = new InformationPokemon[3][3]; // Indices pour chaque case
    @SuppressWarnings("unchecked")
    private final ArrayList<Pokemon>[][] pkmnDejaDit = (ArrayList<Pokemon>[][]) new ArrayList[3][3]; // Liste des pkmn déjà dits par case

    private static final String TAG = "GameController"; // Tag pour les logs

    /* ================ INITIALISATION ===================
    ======================================================*/

    /*Constructeur privé => Singleton*/
    private GameController() {
        // Initialise les tableaux internes
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                pkmnDejaDit[i][j] = new ArrayList<>();
                caseTrouvee[i][j] = "";
                // informations[i][j] et pkmnATrouver[i][j] seront initialisés dans initMorpion
            }
        }
        // Les maps pokemonById et pokemonByFrenchName sont initialisées dans loadPokemonDatabase()
    }

    private String opponentEndpointId; // Pour le multijoueur
    public void setOpponentEndpointId(String id) { this.opponentEndpointId = id; }
    public String getOpponentEndpointId() { return opponentEndpointId; }

    private String playerRole = "Rouge"; // Rôle du joueur ("Rouge" ou "Bleu")
    public void setPlayerRole(String role) { this.playerRole = role; }
    public String getPlayerRole() { return playerRole; }

    /*Méthode de classe pour obtenir l'instance de GameController ou la créer*/
    public static synchronized GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    /*
     * Initialise le contexte de l'application.
     * Lance aussi le chargement de la base de données Pokémon en mémoire.
     */
    public void initContexte(Context context) {
        // on garde le contexte d'application, évite les fuites de mémoire
        this.context = context.getApplicationContext();
        loadPokemonDatabase(); // Charger la BDD ici
    }

    /*
     * Charge tous les Pokémon depuis dbPoke.txt en mémoire.
     * À appeler une seule fois au démarrage de l'application.
     */
    public void loadPokemonDatabase() {
        if (context == null) {
            Log.e(TAG, "Context not initialized for loading database!");
            return;
        }
        // Vérifier si la base de données est déjà chargée
        if (pokemonById != null && pokemonByFrenchName != null && !pokemonById.isEmpty()) {
            Log.d(TAG, "Pokémon database already loaded.");
            return;
        }

        pokemonById = new HashMap<>();
        pokemonByFrenchName = new HashMap<>();
        BufferedReader reader = null;
        long startTime = System.currentTimeMillis(); // Pour mesurer le temps de chargement

        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("dbPoke.txt"))
            );
            String line;
            int currentId = 0;
            while ((line = reader.readLine()) != null) {
                currentId++;
                String[] data = line.split(";");

                // Valider la longueur minimale des données
                if (data.length >= 8) { // Assurez-vous que c'est le bon nombre minimum de colonnes
                    Pokemon pkmn = new Pokemon();
                    pkmn.setId(currentId); // L'ID est basé sur le numéro de ligne (à partir de 1)

                    // Assurer que les chaînes ne sont pas nulles ou vides et les nettoyer
                    pkmn.setEnglish_name(data[0].trim());
                    pkmn.setFrench_name(data[1].trim());

                    try {
                        // Gérer la virgule potentielle pour les floats et vérifier la longueur des indices
                        if (data.length > 2) pkmn.setPoids(Float.parseFloat(data[2].trim().replace(',', '.'))); else pkmn.setPoids(0);
                        if (data.length > 3) pkmn.setTaille(Float.parseFloat(data[3].trim().replace(',', '.'))); else pkmn.setTaille(0);
                        if (data.length > 4) pkmn.setImage(data[4].trim()); else pkmn.setImage("");


                        String[] types = new String[2];
                        // CORRECTION : Convertir les types en minuscule et gérer null/vide/chaîne "null"
                        types[0] = (data.length > 5 && data[5] != null && !data[5].trim().isEmpty() && !data[5].trim().equalsIgnoreCase("null")) ? data[5].trim().toLowerCase(Locale.FRENCH) : null;
                        types[1] = (data.length > 6 && data[6] != null && !data[6].trim().isEmpty() && !data[6].trim().equalsIgnoreCase("null")) ? data[6].trim().toLowerCase(Locale.FRENCH) : null;
                        pkmn.setTypes(types);

                        if (data.length > 7) pkmn.setGeneration(Integer.parseInt(data[7].trim())); else pkmn.setGeneration(0);


                        // Stocker dans les maps pour une recherche rapide
                        pokemonById.put(pkmn.getId(), pkmn);
                        // Utiliser le nom français en minuscule comme clé pour la recherche par nom
                        if (pkmn.getFrench_name() != null && !pkmn.getFrench_name().isEmpty()) {
                            pokemonByFrenchName.put(pkmn.getFrench_name().toLowerCase(Locale.FRENCH), pkmn);
                        } else {
                            Log.w(TAG, "Skipping Pokemon with empty French name at ID: " + pkmn.getId());
                        }

                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Skipping line " + currentId + " due to number format error: " + e.getMessage() + " - Line data: " + line);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e(TAG, "Skipping line " + currentId + " due to unexpected data format (ArrayIndexOutOfBounds): " + e.getMessage() + " - Line data: " + line);
                    } catch (Exception e) { // Attraper d'autres exceptions potentielles
                        Log.e(TAG, "Skipping line " + currentId + " due to parsing error: " + e.getMessage() + " - Line data: " + line, e);
                    }
                } else {
                    Log.w(TAG, "Skipping incomplete line " + currentId + ": " + line);
                }
            }
            long endTime = System.currentTimeMillis();
            Log.d(TAG, "Pokémon database loaded successfully. Total " + pokemonById.size() + " entries in " + (endTime - startTime) + " ms.");

        } catch (IOException e) {
            Log.e(TAG, "Error reading dbPoke.txt: " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader: " + e.getMessage(), e);
                }
            }
        }
    }


    /*Initialise la grille de morpion avec des Pokémons à trouver.*/
    public void initMorpion() {
        if (pokemonById == null || pokemonById.isEmpty()) {
            Log.e(TAG, "Database not loaded! Cannot initialize morpion.");
            // Tenter de charger la BDD si possible, ou afficher une erreur bloquante
            loadPokemonDatabase();
            if (pokemonById == null || pokemonById.isEmpty()) {
                // Gérer le cas où le chargement a échoué (ex: afficher un message d'erreur à l'utilisateur et quitter)
                return;
            }
        }

        int maxId = pokemonById.size(); // Le nombre de Pokémon chargés
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Pokemon chosenPkmn = null;
                int randomNum;
                // Boucle pour choisir un Pokémon valide et non-déjà-cible
                do {
                    randomNum = random.nextInt(maxId) + 1; // Choisir un ID valide
                    chosenPkmn = getPokemon(randomNum); // Cherche dans la map
                    // On vérifie si le Pokémon est valide et s'il n'est pas déjà une cible dans la grille
                } while (chosenPkmn == null || isPokemonAlreadyInGrid(chosenPkmn)); // Boucle tant que la cible est nulle ou déjà présente

                pkmnATrouver[i][j] = chosenPkmn;
                // Initialise InformationPokemon pour cette case
                informations[i][j] = new InformationPokemon(pkmnATrouver[i][j]);
                Log.d(TAG, "Case [" + i + "," + j + "] cible: " + pkmnATrouver[i][j].getFrench_name() + " (ID: " + pkmnATrouver[i][j].getId() + ")");
                // Réinitialiser les Pokémon déjà dits pour cette nouvelle partie/grille
                if (pkmnDejaDit[i][j] == null) {
                    pkmnDejaDit[i][j] = new ArrayList<>();
                } else {
                    pkmnDejaDit[i][j].clear();
                }
                caseTrouvee[i][j] = ""; // Réinitialiser l'état gagné de la case
                estDevine[i][j] = false; // Réinitialiser l'état deviné
            }
        }
        Log.d(TAG, "Grille Morpion initialisée.");
    }

    // Méthode d'aide pour vérifier si un Pokémon est déjà une cible dans la grille actuelle
    private boolean isPokemonAlreadyInGrid(Pokemon pkmn) {
        if (pkmn == null) return true; // Un Pokémon null ne peut pas être une cible
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                // Comparer par ID pour vérifier si c'est le même Pokémon
                if (pkmnATrouver[r][c] != null && pkmnATrouver[r][c].getId() == pkmn.getId()) {
                    return true; // Trouvé dans la grille
                }
            }
        }
        return false; // Pas trouvé dans la grille
    }


    /* ================ GETTERS ==========================
    ======================================================*/

    /*Getter d'un Pokemon d'une case*/
    public Pokemon getPkmnATrouver(int i, int j){
        // Ajouter une vérification de bornes et de nullité
        if (i < 0 || i >= 3 || j < 0 || j >= 3) {
            Log.e(TAG, "Attempted to get target Pokemon for invalid cell [" + i + "," + j + "]");
            return null;
        }
        return pkmnATrouver[i][j] ;
    }

    /*
     * Cherche un Pokémon par son ID dans la base de données en mémoire.
     * Ne lit PAS le fichier.
     */
    public Pokemon getPokemon(int id){
        if (pokemonById == null || pokemonById.isEmpty()) {
            Log.e(TAG, "Database not loaded when calling getPokemon(int)!");
            return null;
        }
        return pokemonById.get(id); // Recherche rapide dans la map
    }

    /*
     * Cherche un Pokémon par son nom français dans la base de données en mémoire.
     * Ne lit PAS le fichier. Gère la casse.
     */
    public Pokemon getPokemonByName(String frenchName) {
        if (pokemonByFrenchName == null || pokemonByFrenchName.isEmpty() || frenchName == null || frenchName.trim().isEmpty()) {
            Log.e(TAG, "Database not loaded or invalid name for lookup when calling getPokemonByName!");
            return null;
        }
        // Recherche rapide dans la map en utilisant le nom en minuscule
        return pokemonByFrenchName.get(frenchName.trim().toLowerCase(Locale.FRENCH));
    }

    /*Renvoie les informations connues d'un Pokemon sur une case*/
    public InformationPokemon getInfos(int i, int j){
        // Ajouter une vérification de bornes et de nullité
        if (i < 0 || i >= 3 || j < 0 || j >= 3 || informations[i][j] == null) {
            Log.e(TAG, "Attempted to get infos for invalid or uninitialized cell [" + i + "," + j + "]");
            return null;
        }
        return this.informations[i][j];
    }

    /* ================ REPONSES ==========================
    ======================================================*/

    /*
     * Renvoie true si ce Pokémon a déjà été dit pour cette case.
     * Compare par ID.
     */
    public boolean isAlreadySaid (Pokemon pkmn, int i, int j){
        if (pkmn == null) {
            Log.d(TAG, "isAlreadySaid called with null Pokemon.");
            return true; // Un Pokémon null est considéré comme "déjà dit" ou invalide
        }
        // Ajouter une vérification de bornes pour la liste
        if (i < 0 || i >= 3 || j < 0 || j >= 3) {
            Log.e(TAG, "Attempted to check isAlreadySaid for invalid cell [" + i + "," + j + "]");
            return true; // Considérer comme déjà dit pour une case invalide
        }

        // CORRECTION : Comparer par ID si la méthode equals() de Pokemon ne le fait pas.
        // Si votre Pokemon.equals() compare par ID, le .contains() suffit.
        // Pour être sûr, on peut parcourir et comparer par ID.
        for (Pokemon saidPkmn : pkmnDejaDit[i][j]) {
            if (saidPkmn != null && saidPkmn.getId() == pkmn.getId()) {
                Log.d(TAG, "Pokemon ID " + pkmn.getId() + " ('" + pkmn.getFrench_name() + "') already said for [" + i + "," + j + "]");
                return true;
            }
        }
        Log.d(TAG, "Pokemon ID " + pkmn.getId() + " ('" + pkmn.getFrench_name() + "') not already said for [" + i + "," + j + "]");
        return false;
    }

    /*
     * Check la réponse du joueur :
     * Renvoie -1 si la réponse est non valide -> Pokémon null, déjà dit, ou case déjà gagnée.
     * Renvoie 0 si la réponse est acceptée (fausse) -> ajouté à la liste de déjà dit, infos updatées.
     * Renvoie 1 si c'est la bonne réponse -> édite les cases du morpion, marque comme deviné.
     */
    public int checkReponse(Pokemon pkmn, int i, int j, String joueur){
        // Ajouter des logs pour suivre ce qui arrive
        Log.d(TAG, "Checking response for [" + i + "," + j + "]. Guess: " + (pkmn != null ? pkmn.getFrench_name() + " (ID:" + pkmn.getId() + ")" : "NULL") + ", Player: " + joueur);
        Log.d(TAG, "Target: " + (pkmnATrouver[i][j] != null ? pkmnATrouver[i][j].getFrench_name() + " (ID:" + pkmnATrouver[i][j].getId() + ")" : "NULL"));
        Log.d(TAG, "Case gagnée par: '" + caseTrouvee[i][j] + "'");

        // Vérifier si les indices de la case sont valides
        if (i < 0 || i >= 3 || j < 0 || j >= 3) {
            Log.e(TAG, "checkReponse called with invalid cell indices [" + i + "," + j + "]");
            return -1; // Indices invalides
        }

        // Vérification si la case est déjà gagnée
        if (!Objects.equals(caseTrouvee[i][j], "")){
            Log.d(TAG, "Case [" + i + "," + j + "] already won by " + caseTrouvee[i][j]);
            return -1 ; // Case déjà gagnée
        }

        // Vérification si le Pokémon deviné est valide ou a déjà été dit pour cette case
        if (pkmn == null || isAlreadySaid(pkmn, i, j)){
            Log.d(TAG, "Pokemon is NULL or already said.");
            return -1 ; // Réponse invalide (Pokémon non trouvé ou déjà dit)
        }

        // CORRECTION MAJEURE : Comparer le Pokémon deviné avec le Pokémon cible par leur ID unique
        // Assurez-vous que pkmnATrouver[i][j] n'est pas null avant d'appeler getId()
        if (pkmnATrouver[i][j] != null && pkmn.getId() == pkmnATrouver[i][j].getId()){
            Log.d(TAG, "Correct guess for [" + i + "," + j + "]!");
            caseTrouvee[i][j] = joueur ; // Marque la case comme gagnée par le joueur
            estDevine[i][j] = true; // Marque la case comme devinée
            return 1 ; // Bonne réponse
        }
        else {
            Log.d(TAG, "Wrong guess for [" + i + "," + j + "]. Updating infos.");
            // Mettre à jour les informations connues pour cette case
            if (informations[i][j] != null) {
                informations[i][j].updateInformations(pkmn); // Utilise l'objet InformationPokemon corrigé
            } else {
                Log.e(TAG, "InformationPokemon object is NULL for [" + i + "," + j + "]!");
            }
            // Ajouter le Pokémon à la liste des Pokémon déjà dits pour cette case
            // S'assure que la liste existe
            if(pkmnDejaDit[i][j] == null) {
                pkmnDejaDit[i][j] = new ArrayList<>();
            }
            pkmnDejaDit[i][j].add(pkmn);
            return 0 ; // Mauvaise réponse valide (nouvel indice donné)
        }
    }

    /*Renvoie le nom du gagnant si la partie est finie, "--" si égalité, sinon une chaine vide.*/
    public String isGameOver (){
        // Vérifications des lignes, colonnes et diagonales pour un gagnant
        // (Votre logique actuelle est correcte pour cela)

        // Si quelqu'un a gagné sur une ligne
        for (int i = 0 ; i < 3 ; i++){
            if (!Objects.equals(caseTrouvee[i][0], "") &&
                    Objects.equals(caseTrouvee[i][0], caseTrouvee[i][1]) &&
                    Objects.equals(caseTrouvee[i][1], caseTrouvee[i][2])) {
                return caseTrouvee[i][0];
            }
        }
        // Si quelqu'un a gagné sur une colonne
        for (int i = 0 ; i < 3 ; i++){
            if (!Objects.equals(caseTrouvee[0][i], "") &&
                    Objects.equals(caseTrouvee[0][i], caseTrouvee[1][i]) &&
                    Objects.equals(caseTrouvee[1][i], caseTrouvee[2][i])) {
                return caseTrouvee[0][i];
            }
        }

        // Si quelqu'un a gagné sur les diagonales
        if (!Objects.equals(caseTrouvee[1][1], "")) {
            // Diagonale principale
            if (Objects.equals(caseTrouvee[0][0], caseTrouvee[1][1]) && Objects.equals(caseTrouvee[1][1], caseTrouvee[2][2])){
                return caseTrouvee[1][1];
            }
            // Anti-diagonale
            if (Objects.equals(caseTrouvee[0][2], caseTrouvee[1][1]) && Objects.equals(caseTrouvee[1][1], caseTrouvee[2][0])){
                return caseTrouvee[1][1];
            }
        }


        // S'il y a une égalité (la grille est pleine et sans vainqueur)
        for (int i = 0 ; i < 3 ; i++){
            for (int j = 0 ; j < 3 ; j++){
                if (Objects.equals(caseTrouvee[i][j], "")){
                    return "" ; // Grille pas encore pleine, la partie continue
                }
            }
        }
        Log.d(TAG, "Grille pleine, égalité.");
        return "--" ; // Grille pleine, pas de vainqueur = égalité
    }

    /*
     * Renvoie une liste aléatoire de `how_much` Pokemon à suggérer au joueur pour la case (i,j).
     * Retire les Pokemon déjà dits pour cette case. Utilise la base de données en mémoire.
     */
    public Pokemon[] suggestionPokemon(int how_much, int i, int j){
        if (pokemonById == null || pokemonById.isEmpty()) {
            Log.e(TAG, "Database not loaded! Cannot provide suggestions.");
            return new Pokemon[0]; // Retourne un tableau vide
        }

        Pokemon[] reponse = new Pokemon[how_much];
        int maxId = pokemonById.size(); // Le nombre total de Pokémon
        Random random = new Random();
        int count = 0; // Compteur pour le nombre de suggestions trouvées

        // HashSet temporaire pour s'assurer que les suggestions *dans cette liste* sont uniques
        HashSet<Integer> suggestedIds = new HashSet<>();


        // Boucle pour trouver 'how_much' suggestions uniques et valides
        while (count < how_much){
            int randomNum = random.nextInt(maxId) + 1; // Choisir un ID aléatoire
            Pokemon pkmn = getPokemon(randomNum); // Recherche très rapide dans la map

            // Vérifier si le Pokémon est valide, n'a pas déjà été dit pour cette case,
            // et n'est pas déjà dans la liste de suggestions que l'on construit.
            if (pkmn != null && !isAlreadySaid(pkmn, i, j) && !suggestedIds.contains(pkmn.getId())){
                reponse[count] = pkmn ; // Ajouter à la réponse
                suggestedIds.add(pkmn.getId()); // Marquer comme ajouté à la suggestion actuelle
                count++; // Incrémenter le compteur de suggestions trouvées
            }
            // Si le Pokémon est null, déjà dit, ou déjà suggéré dans cette liste, la boucle continue.
        }

        return reponse;
    }

    private boolean[][] estDevine = new boolean[3][3]; // Pour suivre si un Pokémon a été deviné dans une case


    /* Renvoie true si le Pokémon cible a été deviné pour cette case. */
    public boolean estDevine(int row, int col) {
        // Ajouter une vérification de bornes
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            Log.e(TAG, "Attempted to check estDevine for invalid cell [" + row + "," + col + "]");
            return false; // Retourner false pour les indices invalides
        }
        return estDevine[row][col];
    }

    /* Marque si le Pokémon cible a été deviné pour cette case. */
    public void setPokemonDevine(int row, int col, boolean valeur) {
        // Ajouter une vérification de bornes
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            Log.e(TAG, "Attempted to set estDevine for invalid cell [" + row + "," + col + "]");
            return; // Ne rien faire pour les indices invalides
        }
        estDevine[row][col] = valeur;
    }

    /*
     * Simule le tour d'un joueur robot.
     * Choisit une case vide aléatoire et propose un Pokémon aléatoire non déjà dit.
     * Utilise la base de données en mémoire.
     */
    public void tour_bot (){
        if (pokemonById == null || pokemonById.isEmpty()) {
            Log.e(TAG, "Database not loaded! Bot cannot play.");
            return;
        }

        Random random = new Random();
        int i, j;
        int maxId = pokemonById.size();

        // Tenter de trouver une case qui n'est pas encore gagnée par quelqu'un
        // Limiter les tentatives pour éviter une boucle infinie si toutes les cases sont prises
        int attempts = 0;
        final int MAX_ATTEMPTS = 20; // Essayer un nombre raisonnable de fois
        do {
            i = random.nextInt(3);
            j = random.nextInt(3);
            attempts++;
            if (attempts > MAX_ATTEMPTS) {
                Log.w(TAG, "Bot failed to find an empty cell after " + MAX_ATTEMPTS + " attempts.");
                return; // Ne peut pas jouer s'il ne trouve pas de case
            }
        } while (!Objects.equals(caseTrouvee[i][j], "")); // Tant que la case n'est pas vide, on cherche une autre


        Pokemon pkmn = null;
        attempts = 0;
        // Générer un Pokémon aléatoire qui n'a pas été dit dans cette case et qui est valide
        do {
            int randomNum = random.nextInt(maxId) + 1;
            pkmn = getPokemon(randomNum); // Recherche très rapide dans la map
            attempts++;
            if (attempts > MAX_ATTEMPTS) {
                Log.w(TAG, "Bot failed to find a valid, not-already-said Pokemon for cell [" + i + "," + j + "] after " + MAX_ATTEMPTS + " attempts.");
                return; // Ne peut pas jouer s'il ne trouve pas de Pokémon valide
            }
        } while (pkmn == null || isAlreadySaid(pkmn, i, j)); // Tant que le Pokémon est null ou déjà dit pour cette case, on en cherche un autre

        // Une fois qu'on a une case vide et un Pokémon valide et pas encore dit, on fait la réponse
        Log.d(TAG, "Bot turn: Guessing " + pkmn.getFrench_name() + " (ID: " + pkmn.getId() + ") for cell [" + i + "," + j + "]");
        int result = checkReponse(pkmn, i, j, "Bot"); // Le bot joue en tant que "Bot"
        Log.d(TAG, "Bot guess result: " + result);

        // Le bot ne fait rien de spécial selon le résultat dans cette implémentation simple
        // Si c'est correct (result 1), la caseTrouvee est mise à jour
        // Si c'est faux (result 0), les infos sont mises à jour et le pkmn est ajouté à pkmnDejaDit
        // Si c'est invalide (-1), le do-while du dessus l'a empêché normalement
    }

    /* Réinitialise l'état complet du jeu pour une nouvelle partie. */
    public void resetGame() {
        Log.d(TAG, "Resetting game state.");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                pkmnATrouver[i][j] = null; // Supprimer les références aux anciens Pokémon cibles
                caseTrouvee[i][j] = "";
                // S'assurer que la liste existe avant de la vider
                if (pkmnDejaDit[i][j] != null) {
                    pkmnDejaDit[i][j].clear();
                } else {
                    pkmnDejaDit[i][j] = new ArrayList<>(); // Re-créer si null
                }
                informations[i][j] = null; // Supprimer les anciennes infos
                estDevine[i][j] = false;
            }
        }
        // initMorpion() devra être appelé après resetGame() pour commencer une nouvelle partie
    }

    /* Renvoie le propriétaire de la case gagnée ("Rouge", "Bleu", ou ""). */
    public String getCaseGagnee(int row, int col) {
        // Ajouter une vérification de bornes
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            Log.e(TAG, "Attempted to getCaseGagnee for invalid cell [" + row + "," + col + "]");
            return ""; // Retourner vide pour les indices invalides
        }
        return caseTrouvee[row][col];
    }
}