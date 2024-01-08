package com.enriclop.pokebot.servicio;

import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.repositorio.IPokemonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PokemonService {

    @Autowired
    private IPokemonRepository pokemonRepository;

    public PokemonService(IPokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    public List<Pokemon> getPokemons() {
        return pokemonRepository.findAll();
    }

    public Pokemon savePokemon(Pokemon pokemon) {
        return pokemonRepository.save(pokemon);
    }

    public Pokemon getPokemonById(Integer id) {
        return pokemonRepository.findById(id).get();
    }

    public void deletePokemonById(Integer id) {
        pokemonRepository.deleteById(id);
    }
}
