package com.enriclop.pokebot;

import com.enriclop.pokebot.servicio.PokemonService;
import com.enriclop.pokebot.servicio.UserService;
import com.enriclop.pokebot.twitchConnection.TwitchConnection;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PokebotApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokebotApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(UserService userService, PokemonService pokemonService) {
		return args -> {

			TwitchConnection twitchConnection = new TwitchConnection(userService, pokemonService);
		};
	}

}
