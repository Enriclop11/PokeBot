package com.enriclop.pokebot.pokeApi;

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
            String hp = stats.get(0).get("base_stat").asText();
            String attack = stats.get(1).get("base_stat").asText();
            String defense = stats.get(2).get("base_stat").asText();
            String specialAttack = stats.get(3).get("base_stat").asText();
            String specialDefense = stats.get(4).get("base_stat").asText();
            String speed = stats.get(5).get("base_stat").asText();

            Pokemon newPokemon = new Pokemon(Name, hp, attack, defense, specialAttack, specialDefense, speed);
            return newPokemon;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
