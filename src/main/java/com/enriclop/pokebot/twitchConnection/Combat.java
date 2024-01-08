package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.servicio.UserService;
import com.gikk.twirk.Twirk;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;

public class Combat extends Thread{

    UserService userService;

    Twirk twirk;

    User player1;
    User player2;

    Pokemon pokemon1;
    Pokemon pokemon2;

    TwirkListener listener;

    public Combat(Pokemon pokemon1, User user2, UserService userService, Twirk twirk) {
        this.userService = userService;
        this.twirk = twirk;

        this.pokemon1 = pokemon1;

        this.player1 = pokemon1.getUser();
        this.player2 = user2;
    }

    public void run() {
        twirk.channelMessage("¡" + player1.getUsername() + " ha retado a " + player2.getUsername() + " a un combate!");
        twirk.channelMessage("¡" + player2.getUsername() + " aceptara el combate? !accept <Pokemon>");


        listener = new TwirkListener() {
            public void onPrivMsg(TwitchUser sender, TwitchMessage message) {

                String command = message.getContent().split(" ")[0];

                if (command.equals("!accept")){
                    twirk.channelMessage("¡" + player2.getUsername() + " ha aceptado el combate!");

                    int pokemonPosition = Integer.parseInt(message.getContent().split(" ")[1]);
                    try {
                        pokemon2 = player2.getPokemons().get(pokemonPosition);
                    } catch (Exception e) {
                        twirk.channelMessage("¡" + player2.getUsername() + " no tiene un pokemon en esa posicion!");
                        return;
                    }

                    startCombat();
                }
            }
        };

        twirk.addIrcListener(listener);
    }

    public void startCombat(){
        twirk.removeIrcListener(listener);
    }


}
