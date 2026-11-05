package com.example.demo.exception;

public class PokemonEnvioException extends RuntimeException {

	public PokemonEnvioException(String mensagem) {
		super(mensagem);
	}

	public PokemonEnvioException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}

}
