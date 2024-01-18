package com.enriclop.pokebot.apis;

import com.enriclop.pokebot.enums.Types;
import com.enriclop.pokebot.modelo.Move;
import com.enriclop.pokebot.modelo.Pokemon;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PokeApi {

    private static int countPokemon = 1025;

    public static Pokemon getPokemonById(Integer id, boolean isShiny) {
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

            Types type = Types.valueOf(jsonNode.get("types").get(0).get("type").get("name").asText().toUpperCase());
            System.out.println(type);
            System.out.println(jsonNode.get("types").get(0).get("type").get("name").asText());
            Types type2 = null;
            try {
                type2 = Types.valueOf(jsonNode.get("types").get(1).get("type").get("name").asText().toUpperCase());
            } catch (Exception ignored) {
            }

            String frontSprite;

            if (isShiny) {
                frontSprite = jsonNode.get("sprites").get("front_shiny").asText();
            } else {
                frontSprite = jsonNode.get("sprites").get("front_default").asText();
            }

            List<Move> moves = new ArrayList<>();


            int numMoves = jsonNode.get("moves").size();

            List<Integer> usedMoves = new ArrayList<>();

            while (moves.size() < 4) {
                int randomMove = (int) (Math.random() * numMoves);

                if (usedMoves.size() == numMoves) {
                    break;
                } else if (usedMoves.contains(randomMove)) {
                    continue;
                }

                usedMoves.add(randomMove);

                Move newMove = getMove(jsonNode.get("moves").get(randomMove).get("move").get("url").asText());

                if (newMove == null) {
                    continue;
                } else if (newMove.getPower() == 0) {
                    continue;
                } else if (newMove.getAccuracy() == 0) {
                    continue;
                }

                moves.add(newMove);
            }


            return new Pokemon(Name, type, type2, hp, attack, defense, specialAttack, specialDefense, speed, frontSprite, isShiny, moves);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Move getMove(String url) {
        try {
            URL moveUrl = new URL(url);
            HttpURLConnection con = (HttpURLConnection) moveUrl.openConnection();
            con.setRequestMethod("GET");

            String response = con.getResponseMessage();
            System.out.println(response);

            JsonNode jsonNode = new ObjectMapper().readTree(moveUrl);

            String name = jsonNode.get("name").asText();
            Types type = Types.valueOf(jsonNode.get("type").get("name").asText().toUpperCase());
            int power = jsonNode.get("power").asInt();
            int accuracy = jsonNode.get("accuracy").asInt();
            String effectiveAttack = jsonNode.get("damage_class").get("name").asText();

            int crit_rate = 0;
            try {
                crit_rate = jsonNode.get("meta").get("crit_rate").asInt();
            } catch (Exception ignored) {
            }

            return new Move(name, effectiveAttack, type, power, accuracy, crit_rate);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static Pokemon getRandomPokemon(){
        int randomPokemon = (int) (Math.random() * countPokemon) + 1;

        boolean isShiny = (int) (Math.random() * 4096) == 0;

        return PokeApi.getPokemonById(randomPokemon, isShiny);
    }

    public static Pokemon getRandomShinyPokemon(){
        int randomPokemon = (int) (Math.random() * countPokemon) + 1;

        return PokeApi.getPokemonById(randomPokemon, true);
    }
}
