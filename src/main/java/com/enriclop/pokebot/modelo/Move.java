package com.enriclop.pokebot.modelo;


import com.enriclop.pokebot.enums.Types;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "move")
@Data
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Types type;

    private String effectiveAttack;

    private int power;

    private int accuracy;

    private int crit_rate;

    @ManyToOne
    @JoinColumn(name = "pokemon_id")
    @JsonIgnore
    private Pokemon pokemon;

    public Move() {
    }

    public Move(String name, String effectiveAttack, Types type, int power, int accuracy, int crit_rate) {
        this.name = name;
        this.effectiveAttack = effectiveAttack;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.crit_rate = crit_rate;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":" + id +
                ", \"name\":\"" + name + '\"' +
                ", \"type\":\"" + type + '\"' +
                ", \"effectiveAttack\":\"" + effectiveAttack + '\"' +
                ", \"power\":" + power +
                ", \"accuracy\":" + accuracy +
                ", \"crit_rate\":" + crit_rate +
                '}';
    }
}
