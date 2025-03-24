package com.example.pocoapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PokeApi {
    @GET("pokemon/{name}")
    Call<PokemonResponse> getPokemon(@Path("name") String pokemonName);

    @GET("pokemon")
    Call<PokemonListResponse> getPokemonList(@Query("limit") int limit, @Query("offset") int offset);

    // Ajoute cette méthode pour obtenir les informations d'un Pokémon depuis RetrofitClient
    static PokeApi create() {
        return RetrofitClient.getClient().create(PokeApi.class);
    }
}
