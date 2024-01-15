package com.enriclop.pokebot.controller;

import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.servicio.PokemonService;
import com.enriclop.pokebot.servicio.UserService;
import com.enriclop.pokebot.twitchConnection.TwitchConnection;
import com.enriclop.pokebot.utilities.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PokemonViewController {

    @Autowired
    private PokemonService pokemonService;

    @Autowired
    private UserService userService;

    @Autowired
    TwitchConnection twitchConnection;

    @GetMapping ("/pokemonSpawn")
    public String pokemonSpawn() {
        return "pokemon/spawn";
    }

    @GetMapping ("/pokemon/{username}")
    public String pokemon(@PathVariable String username, Model model) {
        User user = userService.getUserByUsername(username);

        if (user == null) {
            return "user/pokedex";
        }

        user.setUsername(Utilities.firstLetterToUpperCase(user.getUsername()));

        user.getPokemons().forEach(pokemon -> {
            pokemon.setName(Utilities.firstLetterToUpperCase(pokemon.getName()));
        });

        model.addAttribute("user", user);
        return "user/pokedex";
    }

    @GetMapping("/pokemon/{username}/shiny")
    public String shinyPokemon(@PathVariable String username, Model model) {
        User user = userService.getUserByUsername(username);

        if (user == null) {
            return "user/pokedex";
        }

        user.setUsername(Utilities.firstLetterToUpperCase(user.getUsername()));

        user.getPokemons().forEach(pokemon -> {
            pokemon.setName(Utilities.firstLetterToUpperCase(pokemon.getName()));
        });

        user.getPokemons().removeIf(pokemon -> !pokemon.isShiny());

        model.addAttribute("user", user);
        return "user/pokedex";
    }

    @GetMapping("/battle")
    public String battle() {
        return "pokemon/combat";
    }
}
