package com.example.demo.integration;

import com.example.demo.entity.Pokemon;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pokeapi", url = "${pokeapi.url}")
public interface PokeApiClient {

	@GetMapping("/pokemon/{nome}")
	Pokemon buscarPokemon(@PathVariable("nome") String nome);

}
