package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.servicio.UserService;
import com.enriclop.pokebot.utilities.Timer;
import com.enriclop.pokebot.utilities.Utilities;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import java.util.List;

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

        for (Pokemon pokemon : order) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + Utilities.firstLetterToUpperCase(pokemon.getUser().getUsername()) + " ha sacado a " + Utilities.firstLetterToUpperCase(pokemon.getName()) + "!");
        }

        if (pokemon1.getSpeed() < pokemon2.getSpeed()) order = List.of(pokemon2, pokemon1);

        while (pokemon1.getHp() > 0 && pokemon2.getHp() > 0){
            attack(order.get(0), order.get(1));
            if (order.get(1).getHp() <= 0) break;
            attack(order.get(1), order.get(0));
        }

        active = false;
    }

    public void attack(Pokemon attacker, Pokemon defender){
        double damage =  attacker.getAttack() * (100.0 / (defender.getDefense()));
        if (damage < 0) damage = 0;
        System.out.println(damage);
        defender.setHp((int) (defender.getHp() - damage));

        if (defender.getHp() < 0) defender.setHp(0);

        twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + Utilities.firstLetterToUpperCase(attacker.getUser().getUsername()) + " ha atacado a " + Utilities.firstLetterToUpperCase(defender.getUser().getUsername()) + " y le ha hecho " + (int) damage + " de daño! " + Utilities.firstLetterToUpperCase(defender.getName()) + " tiene " + defender.getHp() + " de vida!");

        if (defender.getHp() <= 0) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + Utilities.firstLetterToUpperCase(defender.getUser().getUsername()) + " ha perdido!");
        }
    }


}
