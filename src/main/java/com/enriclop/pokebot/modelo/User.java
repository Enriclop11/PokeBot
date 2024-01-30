package com.enriclop.pokebot.modelo;

import com.enriclop.pokebot.utilities.Utilities;
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

    private String dcUsername;

    private Integer score;

    private String avatar;

    private int pokemonSelected;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Pokemon> pokemons = new ArrayList<>();

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "items_id")
    private Items items;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "watchtime_id")
    private WatchTime watchTime;

    public User() {
    }

    public User(String twitchId ,String username, String avatar) {
        this.twitchId = twitchId;
        this.username = username;
        this.score = 0;
        this.avatar = avatar;

        this.items = new Items();
        this.watchTime = new WatchTime();
    }

    public void addScore(Integer score) {
        this.score += score;
    }

    public void minusScore(Integer score) {
        this.score -= score;
    }

    public String getUsernameDisplay() {
        return Utilities.firstLetterToUpperCase(this.username);
    }

}
