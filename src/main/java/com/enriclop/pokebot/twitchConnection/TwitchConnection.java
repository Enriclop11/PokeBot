package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.apis.PokeApi;
import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.servicio.PokemonService;
import com.enriclop.pokebot.servicio.UserService;
import com.enriclop.pokebot.utilities.Utilities;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class TwitchConnection {
    UserService userService;
    PokemonService pokemonService;

    Pokemon wildPokemon;

    TwitchClient twitchClient;
    EventManager eventManager;

    Combat activeCombat;

    public TwitchConnection(UserService userService, PokemonService pokemonService) {
        this.userService = userService;
        this.pokemonService = pokemonService;
        connect();
    }

    public void connect() {

         twitchClient = TwitchClientBuilder.builder()
                 .withDefaultAuthToken(new OAuth2Credential(SETTINGS.BOT_USERNAME, SETTINGS.OAUTH_TOKEN))
                 .withEnableHelix(true)
                 .withEnableChat(true)
                 .withChatAccount(new OAuth2Credential(SETTINGS.BOT_USERNAME, SETTINGS.OAUTH_TOKEN))
                 .build();

         twitchClient.getChat().joinChannel(SETTINGS.CHANNEL_NAME);

         commands();
    }

    public void commands() {
        eventManager = twitchClient.getEventManager();

        eventManager.onEvent(ChannelMessageEvent.class, event -> {


            String command = event.getMessage().split(" ")[0];

            switch (command) {
                case "!cum" ->  twitchClient.getChat().sendMessage(event.getChannel().getName(), "Me corro en " + event.getUser().getName());
                case "!leaderboard" -> leaderboard();
                case "!pokemon" -> spawnPokemon();
                case "!catch" -> catchPokemon(event.getUser());
                case "!combat" -> startCombat(event);
                case "!mypokemon" -> lookPokemon(event);
                case "!help" -> twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"Comandos: !leaderboard !pokemon !catch !combat !mypokemon");
            }
        });
    }

    public void start (EventUser sender) {
        if (userService.getUserByUsername(sender.getName()) == null) {
            User newUser = new User(sender.getName().toLowerCase());
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

        twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,leaderboard.toString());
    }

    public void spawnPokemon() {
        Pokemon newPokemon = PokeApi.getRandomPokemon();

        if (newPokemon != null) {
            wildPokemon = newPokemon;

            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"Ha aparecido un " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");
        }
    }

    public void catchPokemon(EventUser sender){
        if (wildPokemon != null) {

            int random = (int) (Math.random() * 100) + 1;

            if (random < 30) {
                twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"No has podido capturar el " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");
                return;
            }

            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"Has capturado un " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");

            start(sender);
            User user = userService.getUserByUsername(sender.getName());
            wildPokemon.setUser(user);
            pokemonService.savePokemon(wildPokemon);

            wildPokemon = null;
        } else {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"No hay ningun pokemon salvaje!");
        }
    }

    public void startCombat(ChannelMessageEvent event) {

        if (activeCombat != null && activeCombat.active) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"Ya hay un combate en curso!");
            return;
        }

        if (event.getMessage().split(" ").length < 2) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"Elige un usuario para combatir!");
            return;
        } else if (event.getMessage().split(" ").length < 3) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"Elige un pokemon para combatir!");
            return;
        }

        User player1;
        User player2;

        try {
            player1 = userService.getUserByUsername(event.getUser().getName());
        } catch (Exception e) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"No tienes ningun pokemon!");
            return;
        }

        try {
            player2 = userService.getUserByUsername(event.getMessage().split(" ")[1]);
        } catch (Exception e) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"El usuario no existe!");
            return;
        }

        if (player1 == player2) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"No puedes combatir contra ti mismo!");
            return;
        } else if (player1.getPokemons().size() == 0) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"No tienes ningun pokemon!");
            return;
        } else if (player2.getPokemons().size() == 0) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"El " +  Utilities.firstLetterToUpperCase(player2.getUsername()) + " no tiene ningun pokemon!");
            return;
        }

        Pokemon pokemon1;

        try {
            int pokemonPosition = Integer.parseInt(event.getMessage().split(" ")[2])-1;
            pokemon1 = player1.getPokemons().get(pokemonPosition);
        } catch (Exception e) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"El pokemon no existe!");
            return;
        }

        activeCombat = new Combat(pokemon1, player2, userService, twitchClient);
        activeCombat.start();
    }

    public void lookPokemon(ChannelMessageEvent event){
        StringBuilder pokemonList = new StringBuilder("Tus pokemon: ");

        start(event.getUser());
        List<Pokemon> pokemons = userService.getUserByUsername(event.getUser().getName()).getPokemons();

        if (pokemons.size() == 0) {
            twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME,"No tienes ningun pokemon!");
            return;
        }

        for (Pokemon pokemon : pokemons) {
            pokemonList.append((pokemons.indexOf(pokemon)+1) + ". " + Utilities.firstLetterToUpperCase(pokemon.getName()) + " " + "HP: " + pokemon.getHp() + " ATK: " + pokemon.getAttack() + " DEF: " + pokemon.getDefense() + " SPATK: " + pokemon.getSpecialAttack() + " SPDEF: " + pokemon.getSpecialDefense() + " SPD: " + pokemon.getSpeed() + "\n");
        }

        System.out.println(pokemonList.toString());

    }

}
