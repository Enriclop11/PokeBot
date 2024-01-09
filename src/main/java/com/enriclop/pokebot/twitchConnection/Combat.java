package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.servicio.UserService;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

public class Combat extends Thread{

    UserService userService;

    TwitchClient twitchClient;

    User player1;
    User player2;

    Pokemon pokemon1;
    Pokemon pokemon2;

    Boolean accepted = false;

    public Combat(Pokemon pokemon1, User user2, UserService userService, TwitchClient twitchClient) {
        this.userService = userService;
        this.twitchClient = twitchClient;

        this.pokemon1 = pokemon1;

        this.player1 = pokemon1.getUser();
        this.player2 = user2;
    }

    public void run() {
        twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + player1.getUsername() + " ha retado a " + player2.getUsername() + " a un combate!");
        twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + player2.getUsername() + " aceptara el combate? !accept <Pokemon>");

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            if (accepted) return;

            String command = event.getMessage().split(" ")[0];

            if (command.equals("!accept")){
                twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + player2.getUsername() + " ha aceptado el combate!");

                int pokemonPosition = Integer.parseInt(event.getMessage().split(" ")[1]);
                try {
                    pokemon2 = player2.getPokemons().get(pokemonPosition);
                } catch (Exception e) {
                    twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"¡" + player2.getUsername() + " no tiene un pokemon en esa posicion!");
                    return;
                }

                startCombat();
            }
        });


    }

    public void startCombat(){

    }


}
