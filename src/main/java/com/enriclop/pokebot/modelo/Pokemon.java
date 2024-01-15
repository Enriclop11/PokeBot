package com.enriclop.pokebot.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "pokemon")
@Data
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

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

    public Pokemon() {
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

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return '{' +
                "\"id\":" + id +
                ", \"name\":\"" + name + '\"' +
                ", \"hp\":" + hp +
                ", \"attack\":" + attack +
                ", \"defense\":" + defense +
                ", \"specialAttack\":" + specialAttack +
                ", \"specialDefense\":" + specialDefense +
                ", \"speed\":" + speed +
                ", \"frontSprite\":\"" + frontSprite + '\"' +
                ", \"isShiny\":" + isShiny +
                '}';
    }
}
