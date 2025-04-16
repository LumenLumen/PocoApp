package com.example.pocoapp;

import androidx.annotation.NonNull;

public class Pokemon {
    private int id ;
    private String english_name;
    private String french_name ;
    private String[] types;
    private float poids;
    private float taille;
    private int generation;
    private String image;

    // Getter et Setter pour 'id'
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getter et Setter pour 'name'
    public String getFrench_name() {
        return french_name;
    }

    public void setFrench_name(String name) {
        this.french_name = name;
    }

    public String getEnglish_name() {
        return english_name;
    }

    public void setEnglish_name(String name) {
        this.english_name = name;
    }

    // Getter et Setter pour 'types'
    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    // Getter et Setter pour 'poids'
    public float getPoids() {
        return poids;
    }

    public void setPoids(float poids) {
        this.poids = poids;
    }

    // Getter et Setter pour 'taille'
    public float getTaille() {
        return taille;
    }

    public void setTaille(float taille) {
        this.taille = taille;
    }

    // Getter et Setter pour 'generation'
    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    // Getter et Setter pour 'image'
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
