package com.enriclop.pokebot.controller;

import com.enriclop.pokebot.servicio.PokemonService;
import com.enriclop.pokebot.twitchConnection.TwitchConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @Autowired
    private TwitchConnection twitchConnection;

    @GetMapping("/pokemons")
    public String getPokemons() {
        return pokemonService.getPokemons().toString();
    }

    @GetMapping("/pokemons/{id}")
    public String getPokemonById(Integer id) {
        return pokemonService.getPokemonById(id).toString();
    }

    @GetMapping("/pokemon/wild/sprite")
    public String getWildPokemonSprite() {
        if (twitchConnection.getWildPokemon() != null)
            return twitchConnection.getWildPokemon().getFrontSprite();
        else
            return null;
    }

}
