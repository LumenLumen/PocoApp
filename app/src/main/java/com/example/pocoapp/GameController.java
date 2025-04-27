package com.example.pocoapp;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/*Cette classe est un singleton.*/
public class GameController {

    private static GameController instance ; //singleton
    private Context context; //contexte
    private final Pokemon[][] pkmnATrouver = new Pokemon[3][3]; //Stocke les pkmn à trouver
    private final String[][] caseTrouvee = new String[3][3]; //Stocke les cases gagnées par qui
    private InformationPokemon[][] informations = new InformationPokemon[3][3];
    @SuppressWarnings("unchecked")
    private final ArrayList<Pokemon>[][] pkmnDejaDit = (ArrayList<Pokemon>[][]) new ArrayList[3][3]; //Liste des pkmn déjà dit par case

    /* ================ INITIALISATION ===================
    ======================================================*/

    /*Constructeur privé => Singleton*/
    private GameController (){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                pkmnDejaDit[i][j] = new ArrayList<>();
                caseTrouvee[i][j] = "";
            }
        }
    }
    private String opponentEndpointId;
    public void setOpponentEndpointId(String id) { this.opponentEndpointId = id; }
    public String getOpponentEndpointId() { return opponentEndpointId; }

    private String playerRole = "Rouge"; // Valeur par défaut
    public void setPlayerRole(String role) { this.playerRole = role; }
    public String getPlayerRole() { return playerRole; }


    /*Méthode de classe pour obtenir l'instance de GameController ou la créer*/
    public static synchronized GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    /*Initialisation le contexte (permet notamment de récupérer les fichiers dans assets)*/
    public void initContexte(Context context) {
        this.context = context.getApplicationContext(); // on garde le contexte d'application, évite les fuites de mémoire
    }


    /*Initialise la grille de morpion avec des Pokémons à trouver.*/
    public void initMorpion (){
        int max = 1025; //Nombre de Pokémon dans le Dex
        Random random = new Random();
        int randomNum;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                randomNum = random.nextInt(max) + 1;
                pkmnATrouver[i][j] = getPokemon(randomNum);
                informations[i][j] = new InformationPokemon(pkmnATrouver[i][j]);
            }
        }
    }

    /* ================ GETTERS ==========================
    ======================================================*/

    /*Getter d'un Pokemon d'une case*/
    public Pokemon getPkmnATrouver(int i, int j){
        return pkmnATrouver[i][j] ;
    }

    /*Cherche un Pokémon (dont le numéro est passé en paramètre) dans le fichier de DB.*/
    public Pokemon getPokemon(int randomNum){
        Pokemon pkmn = new Pokemon();

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("dbPoke.txt"))
            );
            // do reading, usually loop until end of file reading
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                i += 1 ;
                if (i==randomNum){
                    String[] ligne = line.split(";");
                    pkmn.setEnglish_name(ligne[0]);
                    pkmn.setFrench_name(ligne[1]);
                    pkmn.setTaille(Float.parseFloat(ligne[3]));
                    pkmn.setPoids(Float.parseFloat(ligne[2]));
                    pkmn.setImage(ligne[4]);

                    String[] types = new String[2];
                    types[0] = ligne[5];
                    types[1] = ligne[6];
                    pkmn.setTypes(types);

                    pkmn.setGeneration(Integer.parseInt(ligne[7]));
                    pkmn.setId(i);
                    break ;
                }
            }
            // line is not visible here.
        } catch (IOException e) {
            System.out.println("Erreur :" + e);
        }

        return pkmn;
    }

    /*Renvoie les informations connues d'un Pokemon sur une case*/
    public InformationPokemon getInfos(int i, int j){
        return this.informations[i][j];
    }

    /* ================ REPONSES ==========================
    ======================================================*/

    /*Renvoie true si qqun a déjà ce Pokemon pour cette case.*/
    public boolean isAlreadySaid (Pokemon pkmn, int i, int j){
        return pkmnDejaDit[i][j].contains(pkmn);
    }

    /*Check la réponse du joueur :
    * Renvoie -1 si la réponse est non valide -> déjà dit/la case est déjà gagnée
    * Renvoie 0 si la réponse est acceptée -> ajouté à la liste de déjà dit
    * Renvoie 1 si c'est la bonne réponse -> édite les cases du morpion */
    public int checkReponse(Pokemon pkmn, int i, int j, String joueur){
        if (isAlreadySaid(pkmn, i, j) || !Objects.equals(caseTrouvee[i][j], "")){
            return -1;
        }
        else if (pkmn.getId() == pkmnATrouver[i][j].getId()) { // <-- Modification clé ici
            caseTrouvee[i][j] = joueur;
            estDevine[i][j] = true;
            return 1;
        }
        else {
            informations[i][j].updateInformations(pkmn);
            pkmnDejaDit[i][j].add(pkmn);
            return 0;
        }
    }

    /*Renvoie le nom du gagnant si la partie est finie, "--" si égalité, sinon une chaine vide.*/
    public String isGameOver (){
        //Si quelqu'un a gagné
        for (int i = 0 ; i < 3 ; i++){
            if (Objects.equals(caseTrouvee[i][0], caseTrouvee[i][1]) && Objects.equals(caseTrouvee[i][2], caseTrouvee[i][1]) && !Objects.equals(caseTrouvee[i][0], "")){
                return caseTrouvee[i][0];
            }
            if (Objects.equals(caseTrouvee[0][i], caseTrouvee[1][i]) && Objects.equals(caseTrouvee[2][i], caseTrouvee[1][i]) && !Objects.equals(caseTrouvee[0][i], "")){
                return caseTrouvee[0][i];
            }
        }

        if (!Objects.equals(caseTrouvee[1][1], "")){  //Diagonales
            if ((Objects.equals(caseTrouvee[0][0], caseTrouvee[1][1]) && Objects.equals(caseTrouvee[0][0], caseTrouvee[2][2])) || (Objects.equals(caseTrouvee[0][2], caseTrouvee[1][1]) && Objects.equals(caseTrouvee[0][2], caseTrouvee[2][0]))){
                return caseTrouvee[1][1];
            }
        }

        //S'il y a une égalité (la grille est pleine et sans vainqueur)
        for (int i = 0 ; i < 3 ; i++){
            for (int j = 0 ; j < 3 ; j++){
                if (Objects.equals(caseTrouvee[i][j], "")){
                    return "" ;
                }
            }
        }
        return "--" ;
    }

    /*Renvoie une liste aléatoire de how_much Pokemon à suggérer au joueur
    * Elle retire les Pokemon déjà dits auparavant*/
    public Pokemon[] suggestionPokemon(int how_much, int i, int j){
        Pokemon[] reponse = new Pokemon[how_much];
        int max = 1025; //Nombre de Pokémon dans le Dex
        Random random = new Random();

        while (how_much > 0){
            how_much--;
            int randomNum = random.nextInt(max) + 1;
            Pokemon pkmn = getPokemon(randomNum);
            if (pkmnDejaDit[i][j].contains(pkmn)){ //Si le Pokemon a déjà été dit, on relance
                how_much++;
            }
            else {
                reponse[how_much] = pkmn ; //Sinon, on l'ajoute à notre réponse
            }
        }

        return reponse;
    }
    private boolean[][] estDevine = new boolean[3][3];

    public boolean estDevine(int row, int col) {
        return estDevine[row][col];
    }

    public void setPokemonDevine(int row, int col, boolean valeur) {
        estDevine[row][col] = valeur;
    }

    /*Simule le tour d'un joueur => robot qui joue aléatoirement*/
    public void tour_bot (){
        Random random = new Random();
        //Générer i et j
        int i = random.nextInt(3);
        int j = random.nextInt(3);


    /*================== FONCTIONS DE RENVOI DES DETAILS =================
    * *********************************************************************/

        //Générer un nombre aléatoire pour le Pokémon à proposer
        int randomNum = random.nextInt(1025) + 1;
        Pokemon pkmn = getPokemon(randomNum);
    //A faire

        while (checkReponse(pkmn, i, j, "bot") == -1){
            randomNum = random.nextInt(1025) + 1;
            pkmn = getPokemon(randomNum);
        }
    }
    /* Cherche un Pokémon par son nom français dans le fichier de DB.*/
    public Pokemon getPokemonByName(String frenchName) {
        if (context == null || frenchName == null || frenchName.trim().isEmpty()) {
            return null; // Contexte non initialisé ou nom invalide
        }

        Pokemon pkmn = null; // Initialiser à null

        try {
            // Ouvre le fichier dbPoke.txt depuis les assets
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("dbPoke.txt"))
            );

            String line;
            int currentId = 0;
            while ((line = reader.readLine()) != null) {
                currentId++; // Garder une trace de l'ID si nécessaire
                String[] data = line.split(";");
                frenchName = frenchName.trim()
                        .toLowerCase(Locale.FRENCH);
                // Index 1 correspond au nom français dans notre structure de fichier
                if (data.length > 7 && data[1] != null && data[1].equalsIgnoreCase(frenchName.trim())) {
                    // Pokémon trouvé ! Créez et remplissez l'objet Pokemon
                    pkmn = new Pokemon();
                    pkmn.setEnglish_name(data[0]);
                    pkmn.setFrench_name(data[1]);
                    try { // Ajouter des try-catch pour le parsing
                        pkmn.setTaille(Float.parseFloat(data[2].replace(',', '.'))); // Gérer la virgule potentielle
                        pkmn.setPoids(Float.parseFloat(data[3].replace(',', '.'))); // Gérer la virgule potentielle
                        pkmn.setGeneration(Integer.parseInt(data[7]));
                    } catch (NumberFormatException e) {
                        System.err.println("Erreur de format Nombre pour " + data[1] + ": " + e.getMessage());
                        // Optionnel : retourner null ou continuer avec des valeurs par défaut
                        return null; // Ou pkmn = null; break;
                    }
                    pkmn.setImage(data[4]);

                    String[] types = new String[2];
                    types[0] = (data[5] != null && !data[5].equalsIgnoreCase("null")) ? data[5].toLowerCase() : null; // Mettre en minuscule et gérer "null"
                    types[1] = (data[6] != null && !data[6].equalsIgnoreCase("null")) ? data[6].toLowerCase() : null; // Mettre en minuscule et gérer "null"
                    pkmn.setTypes(types);

                    pkmn.setId(currentId); // Assigner l'ID basé sur la ligne
                    break; // Sortir de la boucle une fois trouvé
                }
            }
            reader.close(); // Fermer le reader

        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture de dbPoke.txt pour getPokemonByName: " + e);
            // Gérer l'erreur, peut-être retourner null
            return null;
        }

        return pkmn; // Retourne le Pokémon trouvé ou null s'il n'a pas été trouvé
    }
    public void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                pkmnATrouver[i][j] = null;
                caseTrouvee[i][j] = "";
                pkmnDejaDit[i][j].clear();
                informations[i][j] = null;
                estDevine[i][j] = false;
            }
        }
    }
    public String getCaseGagnee(int row, int col) {
        return caseTrouvee[row][col];
    }

}

