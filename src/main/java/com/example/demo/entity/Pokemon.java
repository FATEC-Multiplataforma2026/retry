package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Pokemon(
		Long id,
		@JsonProperty("name") String nome,
		Integer height) {

	public static Pokemon padrao() {
		return new Pokemon(0L, "desconhecido", 0);
	}

}
