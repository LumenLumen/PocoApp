package com.example.pocoapp;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/*Cette classe est un singleton.*/
public class GameController {

    private static GameController instance ; //signeton
    private Context context; //contexte
    private Pokemon[][] pkmnATrouver = new Pokemon[3][3]; //Stocke les pkmn à trouver
    private String[][] caseTrouvee = new String[3][3]; //Stocke les cases gagnées par qui
    @SuppressWarnings("unchecked")
    private ArrayList<Pokemon>[][] pkmnDejaDit = (ArrayList<Pokemon>[][]) new ArrayList[3][3]; //Liste des pkmn déjà dit par case

    /*Constructeur privé => Singleton*/
    private GameController (){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                pkmnDejaDit[i][j] = new ArrayList<>();
                caseTrouvee[i][j] = "";
            }
        }
        initMorpion();
    }

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

    /*Getter d'un Pokemon d'une case*/
    public Pokemon getPkmnATrouver(int i, int j){
        return pkmnATrouver[i][j] ;
    }

    /*Initialise la grille de morpion avec des Pokémons à trouver.*/
    public void initMorpion (){
        int max = 1025; //Nombre de Pokémon dans le Dex
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int randomNum = random.nextInt(max) + 1;
                pkmnATrouver[i][j] = getPokemon(randomNum);
            }
        }
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
                    pkmn.setTaille(Float.parseFloat(ligne[2]));
                    pkmn.setPoids(Float.parseFloat(ligne[3]));
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

    /*Renvoie true si qqun à déjà ce Pokemon pour cette case.*/
    public boolean isAlreadySaid (Pokemon pkmn, int i, int j){
        return pkmnDejaDit[i][j].contains(pkmn);
    }

    /*Check la réponse du joueur :
    * Renvoie -1 si la réponse est non valide -> déjà dit/la case est déjà gagnée
    * Renvoie 0 si la réponse est acceptée -> ajouté à la liste de déjà dit
    * Renvoie 1 si c'est la bonne réponse -> édite les cases du morpion */
    public int checkReponse(Pokemon pkmn, int i, int j, String joueur){
        if (isAlreadySaid(pkmn, i, j) || !Objects.equals(caseTrouvee[i][j], "")){
            return -1 ;
        }
        else if (pkmn == pkmnATrouver[i][j]){
            caseTrouvee[i][j] = joueur ;
            return 1 ;
        }
        else {
            pkmnDejaDit[i][j].add(pkmn);
            return 0 ;
        }
    }

    /*Renvoie true si la partie est finie*/
    public boolean isGameOver (){
        for (int i = 0 ; i < 3 ; i++){
            if (Objects.equals(caseTrouvee[i][0], caseTrouvee[i][1]) && Objects.equals(caseTrouvee[i][2], caseTrouvee[i][1]) && !Objects.equals(caseTrouvee[i][0], "")){
                return true;
            }
            if (Objects.equals(caseTrouvee[0][i], caseTrouvee[1][i]) && Objects.equals(caseTrouvee[2][i], caseTrouvee[1][i]) && !Objects.equals(caseTrouvee[0][i], "")){
                return true;
            }
        }

        if (!Objects.equals(caseTrouvee[1][1], "")){
            if (Objects.equals(caseTrouvee[0][0], caseTrouvee[1][1]) && Objects.equals(caseTrouvee[0][0], caseTrouvee[2][2])){
                return true ;
            }
            else return Objects.equals(caseTrouvee[0][2], caseTrouvee[1][1]) && Objects.equals(caseTrouvee[0][2], caseTrouvee[2][0]);
        }
        return false ;
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

    /*================== FONCTIONS DE RENVOI DES DETAILS =================
    * *********************************************************************/

    //A faire 

}
