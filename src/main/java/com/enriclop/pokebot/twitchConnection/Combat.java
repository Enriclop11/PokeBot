package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.servicio.UserService;
import com.enriclop.pokebot.utilities.Timer;
import com.enriclop.pokebot.utilities.Utilities;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Combat extends Thread{

    UserService userService;

    TwitchClient twitchClient;

    User player1;
    User player2;

    Pokemon pokemon1;
    Pokemon pokemon2;

    List<Pokemon> order;

    Boolean accepted = false;

    Boolean active = true;

    Timer timer;

    User winner;


    private static class PokemonCombat extends Pokemon {
        int currentHp;

        public int getCurrentHp() {
            return currentHp;
        }

        public void setCurrentHp(int currentHp) {
            this.currentHp = currentHp;
        }

        public PokemonCombat(Pokemon pokemon) {
            super(pokemon.getName(), pokemon.getHp(), pokemon.getAttack(), pokemon.getDefense(), pokemon.getSpecialAttack(), pokemon.getSpecialDefense(), pokemon.getSpeed(), pokemon.getFrontSprite(), pokemon.isShiny(), pokemon.getUser());
            currentHp = pokemon.getHp();
        }
    }


    public Combat(Pokemon pokemon1, User user2, UserService userService, TwitchClient twitchClient) {
        this.userService = userService;
        this.twitchClient = twitchClient;

        this.pokemon1 = pokemon1;

        this.player1 = pokemon1.getUser();
        this.player2 = user2;

        timer = new Timer();
        timer.start();
    }

    public void run() {
        twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + Utilities.firstLetterToUpperCase(player2.getUsername())  + " aceptara el combate? !accept <Pokemon>");

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            if (!active) return;
            if (accepted) return;
            if (!event.getUser().getName().equals(player2.getUsername())) return;
            if (timer.getMinutes() > 1) {
                twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + Utilities.firstLetterToUpperCase(player2.getUsername()) + " no ha aceptado el combate a tiempo!");
                active = false;
                return;
            }


            String command = event.getMessage().split(" ")[0];

            if (command.equals("!accept")){
                twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + Utilities.firstLetterToUpperCase(player2.getUsername()) + " ha aceptado el combate!");

                try {
                    int pokemonPosition = Integer.parseInt(event.getMessage().split(" ")[1])-1;
                    pokemon2 = player2.getPokemons().get(pokemonPosition);
                } catch (Exception e) {
                    twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + Utilities.firstLetterToUpperCase(player2.getUsername()) + " no tiene un pokemon en esa posicion!");
                    active = false;
                    return;
                }

                startCombat();
            }
        });


    }

    public void startCombat(){
        order = List.of(pokemon1, pokemon2);

        if (pokemon1.getSpeed() < pokemon2.getSpeed()) order = List.of(pokemon2, pokemon1);

        PokemonCombat pokemon1 = new PokemonCombat(this.order.get(0));
        PokemonCombat pokemon2 = new PokemonCombat(this.order.get(1));

        while (pokemon1.getCurrentHp() > 0 && pokemon2.getCurrentHp() > 0){
            attack(pokemon1, pokemon2);
            if (pokemon2.getCurrentHp() <= 0) break;
            attack(pokemon2, pokemon1);
        }

        if (pokemon1.getCurrentHp() <= 0) {
            winner = pokemon2.getUser();
        } else {
            winner = pokemon1.getUser();
        }

        winner = userService.getUserById(winner.getId());
        winner.addScore(1);
        userService.saveUser(winner);
    }

    public void endCombat(){
        order = null;
        active = false;
        winner = null;
    }

    public void attack(PokemonCombat attacker, PokemonCombat defender){
        double damage =  attacker.getAttack() * (100.0 / (defender.getDefense()));
        if (damage < 0) damage = 0;
        System.out.println((int) damage);
        defender.setCurrentHp((int) (defender.getCurrentHp() - damage));

        if (defender.getCurrentHp() < 0) defender.setCurrentHp(0);
    }


}
