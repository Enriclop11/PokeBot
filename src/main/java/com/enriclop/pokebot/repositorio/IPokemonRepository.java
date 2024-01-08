package com.enriclop.pokebot.repositorio;

import com.enriclop.pokebot.modelo.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPokemonRepository extends JpaRepository<Pokemon, Integer> {

}
