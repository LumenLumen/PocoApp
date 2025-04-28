package com.example.pocoapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class InformationPokemon {

    private HashSet<String> types_identiques;
    private HashSet<String> types_differents;
    private float poids_min;
    private float poids_max;
    private float taille_min;
    private float taille_max;
    private int generation_min;
    private int generation_max;
    private Pokemon aTrouver;

    public InformationPokemon(Pokemon aTrouver) {
        this.aTrouver = aTrouver;
        types_identiques = new HashSet<>();
        types_differents = new HashSet<>();
        poids_max = 1000F;
        poids_min = 0F;
        taille_max = 200F;
        taille_min = 0F;
        generation_max = 9;
        generation_min = 1;
    }

    public void updateInformations(Pokemon submission) {
        if (submission == null || submission.getTypes() == null || aTrouver == null || aTrouver.getTypes() == null) return;

        // Poids
        if (submission.getPoids() == this.aTrouver.getPoids()) {
            this.poids_min = this.poids_max = submission.getPoids();
        } else if (submission.getPoids() > this.aTrouver.getPoids() && submission.getPoids() < this.poids_max) {
            this.poids_max = submission.getPoids();
        } else if (submission.getPoids() < aTrouver.getPoids() && submission.getPoids() > poids_min) {
            this.poids_min = submission.getPoids();
        }

        // Taille
        if (submission.getTaille() == this.aTrouver.getTaille()) {
            this.taille_min = this.taille_max = submission.getTaille();
        } else if (submission.getTaille() > this.aTrouver.getTaille() && submission.getTaille() < this.taille_max) {
            this.taille_max = submission.getTaille();
        } else if (submission.getTaille() < this.aTrouver.getTaille() && submission.getTaille() > this.taille_min) {
            this.taille_min = submission.getTaille();
        }

        System.out.println(this.taille_max);
        System.out.println(this.taille_min);
        System.out.println(this.poids_max);
        System.out.println(this.poids_min);

        // Génération
        if (submission.getGeneration() == aTrouver.getGeneration()) {
            generation_min = generation_max = submission.getGeneration();
        } else if (submission.getGeneration() > aTrouver.getGeneration() && submission.getGeneration() < generation_max) {
            generation_max = submission.getGeneration();
        } else if (submission.getGeneration() < aTrouver.getGeneration() && submission.getGeneration() > generation_min) {
            generation_min = submission.getGeneration();
        }

        // Récupération des types traduits
        String targetType1 = translateTypeToFrench(aTrouver.getTypes()[0]);
        String targetType2 = translateTypeToFrench(aTrouver.getTypes()[1]);
        String subType1 = translateTypeToFrench(submission.getTypes()[0]);
        String subType2 = translateTypeToFrench(submission.getTypes()[1]);

        // Comparaison propre
        if (subType1 != null) {
            if (subType1.equals(targetType1) || subType1.equals(targetType2)) {
                types_identiques.add(subType1);
            } else {
                types_differents.add(subType1);
            }
        }
        if (subType2 != null && !subType2.equals(subType1)) { // éviter de comparer deux fois si type1 = type2
            if (subType2.equals(targetType1) || subType2.equals(targetType2)) {
                types_identiques.add(subType2);
            } else {
                types_differents.add(subType2);
            }
        }

        // Vérification finale : tous les types trouvés ?
        HashSet<String> targetTypesSet = new HashSet<>();
        if (targetType1 != null) targetTypesSet.add(targetType1);
        if (targetType2 != null) targetTypesSet.add(targetType2);

        if (types_identiques.containsAll(targetTypesSet)) {
            // Tous les types trouvés => tout le reste est incorrect
            ArrayList<String> allTypes = new ArrayList<>(Arrays.asList(
                    "feu", "eau", "plante", "electrik", "acier", "combat", "dragon", "fee",
                    "glace", "insecte", "normal", "poison", "psy", "roche",
                    "sol", "spectre", "tenebres", "vol"
            ));
            allTypes.removeAll(types_identiques);
            types_differents.addAll(allTypes);
        }
    }


    // Traduction anglais -> français
    private String translateTypeToFrench(String englishType) {
        if (englishType == null) return null;
        switch (englishType.toLowerCase()) {
            case "fire": return "feu";
            case "water": return "eau";
            case "grass": return "plante";
            case "electric": return "electrik";
            case "steel": return "acier";
            case "fighting": return "combat";
            case "dragon": return "dragon";
            case "fairy": return "fee";
            case "ice": return "glace";
            case "bug": return "insecte";
            case "normal": return "normal";
            case "poison": return "poison";
            case "psychic": return "psy";
            case "rock": return "roche";
            case "ground": return "sol";
            case "ghost": return "spectre";
            case "dark": return "tenebres";
            case "flying": return "vol";
            default: return englishType.toLowerCase();
        }
    }

    // Getters
    public HashSet<String> getTypes_identiques() { return types_identiques; }
    public HashSet<String> getTypes_differents() { return types_differents; }
    public float getPoids_min() { return poids_min; }
    public float getPoids_max() { return poids_max; }
    public float getTaille_min() { return taille_min; }
    public float getTaille_max() { return taille_max; }
    public int getGeneration_min() { return generation_min; }
    public int getGeneration_max() { return generation_max; }
}
