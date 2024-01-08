package com.enriclop.pokebot.modelo;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String username;

    private Integer score;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true)
    private List<Pokemon> pokemons = new ArrayList<>();

    public User() {
    }

    public User(String username) {
        this.username = username;
        this.score = 0;
    }

}
