package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.pokeApi.PokeApi;
import com.enriclop.pokebot.servicio.PokemonService;
import com.enriclop.pokebot.servicio.UserService;
import com.enriclop.pokebot.utilities.Utilities;
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

    Spawn spawnPokemon;

    public TwitchConnection(UserService userService, PokemonService pokemonService) {
        this.userService = userService;
        this.pokemonService = pokemonService;

        connect();
    }

    public void connect() {
        twirk = new TwirkBuilder(SETTINGS.CHANNEL_NAME, SETTINGS.BOT_USERNAME, SETTINGS.OAUTH_TOKEN).build();
        try {
            twirk.connect();

            spawnPokemon = new Spawn(this);
            spawnPokemon.start();

            commands();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void commands() {
        twirk.addIrcListener( new TwirkListener() {
            public void onPrivMsg(TwitchUser sender, TwitchMessage message) {

                String command = message.getContent().split(" ")[0];

                switch (command) {
                    case "!cum" -> twirk.channelMessage("Me corro en " + sender.getDisplayName());
                    case "!leaderboard" -> leaderboard();
                    case "!pokemon" -> spawnPokemon();
                    case "!catch" -> catchPokemon(sender);
                    case "!combat" -> startCombat(sender, message);
                    case "!mypokemon" -> lookPokemon(sender, message);
                    case "!help" -> twirk.channelMessage("Comandos: !leaderboard !pokemon !catch !combat !mypokemon");
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
            twirk.channelMessage("Ha aparecido un " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");
        }
    }

    public void catchPokemon(TwitchUser sender){
        if (wildPokemon != null) {

            int random = (int) (Math.random() * 100) + 1;

            if (random < 30) {
                twirk.channelMessage("No has podido capturar el " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");
                return;
            }

            twirk.channelMessage("Has capturado un " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");

            start(sender);
            User user = userService.getUserByUsername(sender.getDisplayName());
            wildPokemon.setUser(user);
            pokemonService.savePokemon(wildPokemon);

            wildPokemon = null;
        } else {
            twirk.channelMessage("No hay ningun pokemon salvaje!");
        }
    }

    public void startCombat(TwitchUser sender, TwitchMessage message) {

        if (message.getContent().split(" ").length < 2) {
            twirk.channelMessage("Elige un usuario para combatir!");
            return;
        } else if (message.getContent().split(" ").length < 3) {
            twirk.channelMessage("Elige un pokemon para combatir!");
            return;
        }

        User player1;
        User player2;

        try {
            player1 = userService.getUserByUsername(sender.getDisplayName());
        } catch (Exception e) {
            twirk.channelMessage("No tienes ningun pokemon!");
            return;
        }

        try {
            player2 = userService.getUserByUsername(message.getContent().split(" ")[1]);
        } catch (Exception e) {
            twirk.channelMessage("El usuario no existe!");
            return;
        }

        if (player1 == player2) {
            twirk.channelMessage("No puedes combatir contra ti mismo!");
            return;
        } else if (player1.getPokemons().size() == 0) {
            twirk.channelMessage("No tienes ningun pokemon!");
            return;
        } else if (player2.getPokemons().size() == 0) {
            twirk.channelMessage("El " +  Utilities.firstLetterToUpperCase(player2.getUsername()) + " no tiene ningun pokemon!");
            return;
        }

        int pokemonPosition = Integer.parseInt(message.getContent().split(" ")[2]);

        Pokemon pokemon1;

        try {
            pokemon1 = player1.getPokemons().get(pokemonPosition);
        } catch (Exception e) {
            twirk.channelMessage("El pokemon no existe!");
            return;
        }

        Combat combat = new Combat(pokemon1, player2, userService, twirk);
    }

    public void lookPokemon(TwitchUser sender, TwitchMessage message){
        StringBuilder pokemonList = new StringBuilder("Tus pokemon: ");

        start(sender);
        List<Pokemon> pokemons = userService.getUserByUsername(sender.getDisplayName()).getPokemons();

        if (pokemons.size() == 0) {
            twirk.channelMessage("No tienes ningun pokemon!");
            return;
        }

        for (Pokemon pokemon : pokemons) {
            pokemonList.append(pokemons.indexOf(pokemon) + ". " + pokemon.getName() + " " + "HP: " + pokemon.getHp() + " ATK: " + pokemon.getAttack() + " DEF: " + pokemon.getDefense() + " SPATK: " + pokemon.getSpecialAttack() + " SPDEF: " + pokemon.getSpecialDefense() + " SPD: " + pokemon.getSpeed() + "\n");
        }

        twirk.channelMessage("Te envio un mensaje privado con tus pokemon!");
        twirk.whisper(sender, pokemonList.toString());
    }



}
