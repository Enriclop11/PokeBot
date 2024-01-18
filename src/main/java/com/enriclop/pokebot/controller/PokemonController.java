package com.enriclop.pokebot.controller;

import com.enriclop.pokebot.dto.PokemonCombat;
import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.servicio.PokemonService;
import com.enriclop.pokebot.twitchConnection.TwitchConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PokemonController {

    @Autowired
    private PokemonService pokemonService;

    @Autowired
    private TwitchConnection twitchConnection;

    @GetMapping("/pokemons")
    public List<Pokemon> getPokemons() {
        return pokemonService.getPokemons();
    }

    @GetMapping("/pokemons/{id}")
    public Pokemon getPokemonById(Integer id) {
        return pokemonService.getPokemonById(id);
    }

    @GetMapping("/pokemon/wild/sprite")
    public String getWildPokemonSprite() {
        if (twitchConnection.getWildPokemon() != null)
            return twitchConnection.getWildPokemon().getFrontSprite();
        else
            return null;
    }

    @GetMapping("/pokemon/combat")
    public List<PokemonCombat> getCombat() {
        if (twitchConnection.getActiveCombat() != null && !twitchConnection.getActiveCombat().getStarted() && twitchConnection.getActiveCombat().getAccepted()) {
            twitchConnection.getActiveCombat().startCombat();
            return null;
        } else if (twitchConnection.getActiveCombat() !=  null && twitchConnection.getActiveCombat().getOrder() != null) {
            return twitchConnection.getActiveCombat().getOrder();
        } else
            return null;
    }

    @PostMapping("/pokemon/combat")
    public void endCombat() {
        twitchConnection.sendMessage(twitchConnection.getActiveCombat().getWinner().getUsername() + " ha ganado el combate!");
        twitchConnection.getActiveCombat().endCombat();
    }
}