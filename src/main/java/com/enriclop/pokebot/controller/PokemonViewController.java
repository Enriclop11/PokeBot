package com.enriclop.pokebot.controller;

import com.enriclop.pokebot.servicio.PokemonService;
import com.enriclop.pokebot.twitchConnection.TwitchConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PokemonViewController {

    @Autowired
    private PokemonService pokemonService;

    @Autowired
    TwitchConnection twitchConnection;

    @GetMapping ("/pokemonSpawn")
    public String pokemonSpawn() {
        return "/pokemon/spawn";
    }
}
