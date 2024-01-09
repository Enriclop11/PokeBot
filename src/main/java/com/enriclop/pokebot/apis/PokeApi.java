package com.enriclop.pokebot.apis;

import com.enriclop.pokebot.modelo.Pokemon;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;

public class PokeApi {

    public static Pokemon getPokemonById(Integer id) {
        try {
            URL url = new URL("https://pokeapi.co/api/v2/pokemon/"+id+"/");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            String response = con.getResponseMessage();
            System.out.println(response);

            JsonNode jsonNode = new ObjectMapper().readTree(url);

            String Name = jsonNode.get("name").asText();
            JsonNode stats = jsonNode.get("stats");
            int hp = Integer.parseInt(stats.get(0).get("base_stat").asText());
            int attack = Integer.parseInt(stats.get(1).get("base_stat").asText());
            int defense = Integer.parseInt(stats.get(2).get("base_stat").asText());
            int specialAttack = Integer.parseInt(stats.get(3).get("base_stat").asText());
            int specialDefense = Integer.parseInt(stats.get(4).get("base_stat").asText());
            int speed = Integer.parseInt(stats.get(5).get("base_stat").asText());
            String frontSprite = jsonNode.get("sprites").get("front_default").asText();
            String backSprite = jsonNode.get("sprites").get("back_default").asText();

            return new Pokemon(Name, hp, attack, defense, specialAttack, specialDefense, speed, frontSprite, backSprite);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Pokemon getRandomPokemon(){
        int countPokemon = 1025;

        int randomPokemon = (int) (Math.random() * countPokemon) + 1;

        return PokeApi.getPokemonById(randomPokemon);
    }
}
