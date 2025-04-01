package com.example.pocoapp;

import java.util.List;

public class PokemonResponse {
    private List<Pokemon> results;

    public List<Pokemon> getResults() {
        return results;
    }

    public static class Pokemon {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public int getId() {
            String[] parts = url.split("/");
            return Integer.parseInt(parts[parts.length - 1]);
        }
    }
}
