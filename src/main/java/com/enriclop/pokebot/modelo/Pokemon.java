package com.enriclop.pokebot.modelo;

import com.enriclop.pokebot.enums.Types;
import com.enriclop.pokebot.utilities.Utilities;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "pokemon")
@Data
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Types type;

    @Enumerated(EnumType.STRING)
    private Types type2;

    private int hp;

    private int attack;

    private int defense;

    private int specialAttack;

    private int specialDefense;

    private int speed;

    private String frontSprite;

    private boolean isShiny;



    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "pokemon", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Move> moves = new ArrayList<>();

    public Pokemon() {
    }

    public Pokemon(Pokemon pokemon) {
        this.name = pokemon.getName();
        this.type = pokemon.getType();
        this.type2 = pokemon.getType2();
        this.hp = pokemon.getHp();
        this.attack = pokemon.getAttack();
        this.defense = pokemon.getDefense();
        this.specialAttack = pokemon.getSpecialAttack();
        this.specialDefense = pokemon.getSpecialDefense();
        this.speed = pokemon.getSpeed();
        this.frontSprite = pokemon.getFrontSprite();
        this.isShiny = pokemon.isShiny();
        this.user = pokemon.getUser();
        this.moves = pokemon.getMoves();
    }

    public Pokemon(String name, int hp, int attack, int defense, int specialAttack, int specialDefense, int speed, String frontSprite, boolean isShiny) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.frontSprite = frontSprite;
        this.isShiny = isShiny;
    }

    public Pokemon(String name, int hp, int attack, int defense, int specialAttack, int specialDefense, int speed, String frontSprite, boolean isShiny, User user) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.frontSprite = frontSprite;
        this.isShiny = isShiny;
        this.user = user;
    }

    public Pokemon(String name, Types type1, Types type2, int hp, int attack, int defense, int specialAttack, int specialDefense, int speed, String frontSprite, boolean isShiny, List<Move> moves) {
        this.name = name;
        this.type= type1;
        this.type2 = type2;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.frontSprite = frontSprite;
        this.isShiny = isShiny;

        for (Move move : moves) {
            move.setPokemon(this);
        }
        this.moves = moves;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return '{' +
                "\"id\":" + id +
                ", \"name\":\"" + name + '\"' +
                ", \"type\":\"" + type + '\"' +
                ", \"type2\":\"" + type2 + '\"' +
                ", \"hp\":" + hp +
                ", \"attack\":" + attack +
                ", \"defense\":" + defense +
                ", \"specialAttack\":" + specialAttack +
                ", \"specialDefense\":" + specialDefense +
                ", \"speed\":" + speed +
                ", \"frontSprite\":\"" + frontSprite + '\"' +
                ", \"isShiny\":" + isShiny +
                ", \"moves\":" + moves +
                '}';
    }

    public String getDisplayName() {
        return Utilities.firstLetterToUpperCase(this.name);
    }
}
