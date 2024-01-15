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

    private String twitchId;

    private String username;

    private Integer score;

    private String avatar;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true)
    private List<Pokemon> pokemons = new ArrayList<>();

    public User() {
    }

    public User(String twitchId ,String username, String avatar) {
        this.twitchId = twitchId;
        this.username = username;
        this.score = 0;
        this.avatar = avatar;
    }

    public void addScore(Integer score) {
        this.score += score;
    }

}
