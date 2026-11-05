package com.example.demo.pokemon;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.demo.entity.Pokemon;
import com.example.demo.integration.PokemonIntegration;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class PokemonServiceTest {

	private static final AtomicInteger chamadasMissingno = new AtomicInteger();

	private static HttpServer server;

	@Autowired
	private PokemonIntegration pokemonService;

	@DynamicPropertySource
	static void pokeApiUrl(DynamicPropertyRegistry registry) throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		server.createContext("/api/v2/pokemon/ditto", exchange -> {
			byte[] body = """
					{"id": 132, "name": "ditto", "height": 3, "weight": 40, "abilities": []}
					""".getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, body.length);
			exchange.getResponseBody().write(body);
			exchange.close();
		});
		server.createContext("/api/v2/pokemon/missingno", exchange -> {
			chamadasMissingno.incrementAndGet();
			exchange.sendResponseHeaders(404, -1);
			exchange.close();
		});
		server.start();
		registry.add("pokeapi.url", () -> "http://localhost:" + server.getAddress().getPort() + "/api/v2");
		registry.add("resilience4j.retry.instances.pokeapi.wait-duration", () -> "1ms");
	}

	@AfterAll
	static void pararServidor() {
		server.stop(0);
	}

	@BeforeEach
	void resetar() {
		chamadasMissingno.set(0);
	}

	@Test
	void mapeiaIdNomeEHeight() {
		Pokemon pokemon = pokemonService.buscar("ditto");

		assertThat(pokemon).isEqualTo(new Pokemon(132L, "ditto", 3));
	}

	@Test
	void tenta3VezesNo404ERetornaPadrao() {
		Pokemon pokemon = pokemonService.buscar("missingno");

		assertThat(pokemon).isEqualTo(Pokemon.padrao());
		assertThat(chamadasMissingno.get()).isEqualTo(3);
	}

}
