package com.enriclop.pokebot.dto;

import com.enriclop.pokebot.modelo.Pokemon;

public class PokemonCombat extends Pokemon {

    int currentHp;

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public PokemonCombat(Pokemon pokemon) {
        super(pokemon);
        currentHp = pokemon.getHp();
    }

    @Override
    public String toString() {
        return '{' +
                "\"id\":" + getId() +
                ", \"name\":\"" + getName() + '\"' +
                ", \"type\":\"" + getType() + '\"' +
                ", \"type2\":\"" + getType2() + '\"' +
                ", \"hp\":" + getHp() +
                ", \"attack\":" + getAttack() +
                ", \"defense\":" + getDefense() +
                ", \"specialAttack\":" + getSpecialAttack() +
                ", \"specialDefense\":" + getSpecialDefense() +
                ", \"speed\":" + getSpeed() +
                ", \"frontSprite\":\"" + getFrontSprite() + '\"' +
                ", \"isShiny\":" + isShiny() +
                ", \"currentHp\":" + currentHp +
                '}';
    }
}
