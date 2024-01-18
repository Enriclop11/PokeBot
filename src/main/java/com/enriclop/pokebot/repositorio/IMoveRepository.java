package com.enriclop.pokebot.repositorio;


import com.enriclop.pokebot.modelo.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IMoveRepository extends JpaRepository<Move, Integer>{
}
