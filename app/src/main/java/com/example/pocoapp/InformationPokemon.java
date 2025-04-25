package com.example.pocoapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class InformationPokemon {

    private HashSet<String> types_identiques; //Types identiques au Pokemon recherché
    private HashSet<String> types_differents; //Types différents du Pokemon recherché
    private float poids_min; //Poids min
    private float poids_max; //Poids max
    private float taille_min; //Taille min
    private float taille_max; //Taille max
    private int generation_min; //Gen min
    private int generation_max; //Gen max
    private Pokemon aTrouver;

    public InformationPokemon (Pokemon aTrouver){
        this.aTrouver = aTrouver;

        types_identiques = new HashSet<String>();
        types_differents = new HashSet<String>();

        poids_max = 1000F;
        poids_min = 0F;
        taille_max = 200F;
        taille_min = 0F;
        generation_max = 9;
        generation_min = 1;
    }

    public void updateInformations (Pokemon submission){

        //Update de poids min et max
        if (submission.getPoids() == this.aTrouver.getPoids()){
            this.poids_max = this.poids_min = submission.getPoids();
        } else if (submission.getPoids() > this.aTrouver.getPoids() && submission.getPoids() < this.poids_max) {
            this.poids_max = submission.getPoids();
        } else if (submission.getPoids() < this.aTrouver.getPoids() && submission.getPoids() > this.poids_min) {
            this.poids_min = submission.getPoids();
        }

        //Update de taille min et max
        if (submission.getTaille() == this.aTrouver.getTaille()){
            this.taille_max = this.taille_min = submission.getTaille();
        } else if (submission.getTaille() > this.aTrouver.getTaille() && submission.getTaille() < this.taille_max) {
            this.taille_max = submission.getTaille();
        } else if (submission.getTaille() < this.aTrouver.getTaille() && submission.getTaille() > this.taille_min) {
            this.taille_min = submission.getTaille();
        }

        //Update de gen min et max
        if (submission.getGeneration() == this.aTrouver.getGeneration()){
            this.generation_max = this.generation_min = submission.getGeneration();
        } else if (submission.getGeneration() > this.aTrouver.getGeneration() && submission.getGeneration() < this.taille_max) {
            this.generation_max = submission.getGeneration();
        } else if (submission.getGeneration() < this.aTrouver.getGeneration() && submission.getGeneration() > this.taille_min) {
            this.generation_min = submission.getGeneration();
        }

        //Update des types
        if (Objects.equals(submission.getTypes()[0], this.aTrouver.getTypes()[0]) || Objects.equals(submission.getTypes()[0], this.aTrouver.getTypes()[1])){
            this.types_identiques.add(submission.getTypes()[0]);
        }
        if (Objects.equals(submission.getTypes()[1], this.aTrouver.getTypes()[0]) || Objects.equals(submission.getTypes()[1], this.aTrouver.getTypes()[1])){
            this.types_identiques.add(submission.getTypes()[1]);
        }

        if (this.types_identiques.size() == 2){
            ArrayList<String> types = new ArrayList<>(Arrays.asList("normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground", "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy"));
            types.removeIf(this.types_identiques::contains);
            this.types_differents.addAll(types);
            return; //On connait les types alors on mets fin à l'exécution de la fonction.
        }

        if (!Objects.equals(submission.getTypes()[0], this.aTrouver.getTypes()[0]) && !Objects.equals(submission.getTypes()[0], this.aTrouver.getTypes()[1])){
            this.types_differents.add(submission.getTypes()[0]);
        }
        if (!Objects.equals(submission.getTypes()[1], this.aTrouver.getTypes()[0]) && !Objects.equals(submission.getTypes()[1], this.aTrouver.getTypes()[1])){
            this.types_differents.add(submission.getTypes()[1]);
        }

    }

    public HashSet<String> getTypes_identiques() {
        return types_identiques;
    }

    public HashSet<String> getTypes_differents() {
        return types_differents;
    }

    public float getPoids_min() {
        return poids_min;
    }

    public void setPoids_min(float poids_min) {
        this.poids_min = poids_min;
    }

    public float getPoids_max() {
        return poids_max;
    }

    public void setPoids_max(float poids_max) {
        this.poids_max = poids_max;
    }

    public float getTaille_min() {
        return taille_min;
    }

    public void setTaille_min(float taille_min) {
        this.taille_min = taille_min;
    }

    public float getTaille_max() {
        return taille_max;
    }

    public void setTaille_max(float taille_max) {
        this.taille_max = taille_max;
    }

    public int getGeneration_min() {
        return generation_min;
    }

    public void setGeneration_min(int generation_min) {
        this.generation_min = generation_min;
    }

    public int getGeneration_max() {
        return generation_max;
    }

    public void setGeneration_max(int generation_max) {
        this.generation_max = generation_max;
    }

}
