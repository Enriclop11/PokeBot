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

    private String hp;

    private String attack;

    private String defense;

    private String specialAttack;

    private String specialDefense;

    private String speed;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Pokemon() {
    }

    public Pokemon(String name, String hp, String attack, String defense, String specialAttack, String specialDefense, String speed) {
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
