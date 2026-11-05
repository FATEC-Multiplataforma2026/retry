package com.example.demo.controller;

import com.example.demo.entity.Pokemon;
import com.example.demo.integration.PokemonIntegration;
import com.example.demo.events.PokemonSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pokemons")
public class PokemonController {
    private final PokemonIntegration pokemonService;

    public PokemonController(PokemonIntegration pokemonService, PokemonSender pokemonSender) {
        this.pokemonService = pokemonService;
    }

    @GetMapping("/{nome}")
    public Pokemon buscar(@PathVariable String nome) {
        return pokemonService.buscar(nome);
    }

}
