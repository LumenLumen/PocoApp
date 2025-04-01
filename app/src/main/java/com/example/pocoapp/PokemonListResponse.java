package com.example.pocoapp;

import java.util.List;
// sert à stocker la liste des Pokémon récupérés depuis l'API.
public class PokemonListResponse {
    private List<PokemonEntry> results;

    public List<PokemonEntry> getResults() {
        return results;
    }

    public void setResults(List<PokemonEntry> results) {
        this.results = results;
    }

    public static class PokemonEntry {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
        // Méthode pour extraire l'ID du Pokémon depuis l'URL
        public int getId() {
            String[] parts = url.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        }
    }
}
