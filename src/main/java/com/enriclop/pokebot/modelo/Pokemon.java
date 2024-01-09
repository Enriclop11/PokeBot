package com.enriclop.pokebot.modelo;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Pokemon() {
    }

    public Pokemon(String name, int hp, int attack, int defense, int specialAttack, int specialDefense, int speed) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "Pokemon{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hp='" + hp + '\'' +
                ", attack='" + attack + '\'' +
                ", defense='" + defense + '\'' +
                ", specialAttack='" + specialAttack + '\'' +
                ", specialDefense='" + specialDefense + '\'' +
                ", speed='" + speed + '\'' +
                ", user=" + user.getId() +
                '}';
    }
}
