package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.pokeApi.PokeApi;
import com.enriclop.pokebot.servicio.PokemonService;
import com.enriclop.pokebot.servicio.UserService;
import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;

import java.util.List;

public class TwitchConnection {
    Twirk twirk;

    UserService userService;
    PokemonService pokemonService;

    Pokemon wildPokemon;

    public TwitchConnection(UserService userService, PokemonService pokemonService) {
        this.userService = userService;
        this.pokemonService = pokemonService;

        connect();
    }

    public void connect() {
        twirk = new TwirkBuilder(SETTINGS.CHANNEL_NAME, SETTINGS.BOT_USERNAME, SETTINGS.OAUTH_TOKEN).build();
        try {
            twirk.connect();
            commands();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commands() {
        twirk.addIrcListener( new TwirkListener() {
            public void onPrivMsg(TwitchUser sender, TwitchMessage message) {

                switch (message.getContent()) {
                    case "!cum" -> {
                        twirk.channelMessage("Me corro en " + sender.getDisplayName());
                        start(sender);
                    }
                    case "!leaderboard" -> leaderboard();
                    default -> {
                    }
                    case "!pokemon" -> spawnPokemon();
                    case "!catch" -> catchPokemon(sender);
                }

            }
        } );
    }

    public void start (TwitchUser sender) {
        if (userService.getUserByUsername(sender.getDisplayName()) == null) {
            User newUser = new User(sender.getDisplayName().toLowerCase());
            userService.saveUser(newUser);
        }
    }

    public void leaderboard() {
        List<User> users = userService.getUsers();

        users.sort((u1, u2) -> u2.getPokemons().size() - u1.getPokemons().size());

        if (users.size() > 10) {
            users = users.subList(0, 10);
        }

        StringBuilder leaderboard = new StringBuilder("Leaderboard: ");
        for (User user : users) {
            leaderboard.append ((users.indexOf(user) + 1) + ". " + user.getUsername() + " " + user.getPokemons().size() + " Pokemon ");
        }

        twirk.channelMessage(leaderboard.toString());
    }

    public void spawnPokemon() {
        int countPokemon = 1025;

        int randomPokemon = (int) (Math.random() * countPokemon) + 1;

        Pokemon newPokemon = PokeApi.getPokemonById(randomPokemon);

        if (newPokemon != null) {
            wildPokemon = newPokemon;
            twirk.channelMessage("Ha aparecido un " + firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");
        }
    }

    public void catchPokemon(TwitchUser sender){
        if (wildPokemon != null) {

            int random = (int) (Math.random() * 100) + 1;

            System.out.println(random);

            if (random < 30) {
                twirk.channelMessage("No has podido capturar el " + firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");
                return;
            }

            twirk.channelMessage("Has capturado un " + firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");

            start(sender);
            User user = userService.getUserByUsername(sender.getDisplayName());
            System.out.println(user);
            wildPokemon.setUser(user);
            pokemonService.savePokemon(wildPokemon);

            wildPokemon = null;
        } else {
            twirk.channelMessage("No hay ningun pokemon salvaje!");
        }
    }

    public String firstLetterToUpperCase(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
}
