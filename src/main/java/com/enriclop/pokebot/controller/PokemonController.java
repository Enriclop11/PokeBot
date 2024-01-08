package com.enriclop.pokebot.controller;

import com.enriclop.pokebot.servicio.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @GetMapping("/pokemons")
    public String getPokemons() {
        return pokemonService.getPokemons().toString();
    }

    @GetMapping("/pokemons/{id}")
    public String getPokemonById(Integer id) {
        return pokemonService.getPokemonById(id).toString();
    }


}
