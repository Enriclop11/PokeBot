package com.enriclop.pokebot.repositorio;

import com.enriclop.pokebot.modelo.Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IItemsRepository extends JpaRepository<Items, Integer> {
}
