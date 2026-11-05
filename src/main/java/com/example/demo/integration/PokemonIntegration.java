package com.example.demo.integration;

import com.example.demo.entity.Pokemon;
import com.example.demo.events.PokemonSender;
import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PokemonIntegration {
    private static final Logger log = LoggerFactory.getLogger(PokemonIntegration.class);

    private final PokemonSender pokemonSender;
    private final PokeApiClient pokeApiClient;

    public PokemonIntegration(
            PokemonSender pokemonSender,
            PokeApiClient pokeApiClient) {
        this.pokemonSender = pokemonSender;
        this.pokeApiClient = pokeApiClient;
    }

    @Retry(name = "pokeapi", fallbackMethod = "pokemonPadrao")
    public Pokemon buscar(String nome) {
        Pokemon pokemon = pokeApiClient.buscarPokemon(nome);
        pokemonSender.enviar(pokemon);
        return pokemon;
    }

    private Pokemon pokemonPadrao(String nome, FeignException.NotFound ex) {
        log.warn("Pokemon '{}' não encontrado após as tentativas; retornando valor padrão", nome);
        return Pokemon.padrao();
    }

}
