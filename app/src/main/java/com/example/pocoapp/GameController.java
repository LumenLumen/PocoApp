package com.example.pocoapp;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

/*Cette classe est un singleton.*/
public class GameController {

    private static GameController instance ;
    private Context context;
    private Pokemon[][] pkmnATrouver = new Pokemon[3][3];
    @SuppressWarnings("unchecked")
    private ArrayList<Pokemon>[][] pkmnDejaDit = (ArrayList<Pokemon>[][]) new ArrayList[3][3];

    /*Constructeur privé => Singleton*/
    private GameController (){
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                pkmnDejaDit[i][j] = new ArrayList<>();
            }
        }
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
                    break ;
                }
            }
            // line is not visible here.
        } catch (IOException e) {
            System.out.println("Erreur :" + e);
        }

        return pkmn;
    }
}
