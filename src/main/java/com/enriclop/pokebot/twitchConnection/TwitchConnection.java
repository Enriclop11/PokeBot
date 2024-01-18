package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.apis.PokeApi;
import com.enriclop.pokebot.modelo.Items;
import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.servicio.ItemsService;
import com.enriclop.pokebot.servicio.MoveService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
@Setter
public class TwitchConnection extends Thread {

    @Autowired
    UserService userService;

    @Autowired
    PokemonService pokemonService;

    @Autowired
    ItemsService itemsService;

    @Autowired
    MoveService moveService;

    Pokemon wildPokemon;

    TwitchClient twitchClient;
    EventManager eventManager;

    Combat activeCombat;

    public TwitchConnection() {
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

    public void sendMessage(String message) {
        twitchClient.getChat().sendMessage(SETTINGS.CHANNEL_NAME, message);
    }

    public void commands() {
        eventManager = twitchClient.getEventManager();

        eventManager.onEvent(ChannelMessageEvent.class, event -> {


            String command = event.getMessage().split(" ")[0];

            switch (command) {
                case "!leaderboard" -> leaderboard();
                case "!pokemon" -> spawnPokemon();
                case "!catch" -> trowPokeball(event);
                case "!combat" -> startCombat(event);
                case "!mypokemon" -> lookPokemon(event);
                case "!refreshusername" -> refreshUsername(event.getUser());
                case "!buy" -> buyItem(event);
                case "!items" -> lookItems(event);
                case "!points" -> myPoints(event);
            }
        });
    }

    public void start (EventUser sender) {
        if (userService.getUserByTwitchId(sender.getId()) == null) {
            twitchClient.getHelix().getUsers(null, List.of(sender.getId()), null).execute().getUsers().forEach(user -> {
                User newUser = new User(user.getId(), user.getDisplayName().toLowerCase(), user.getProfileImageUrl());
                userService.saveUser(newUser);
            });
        }
    }

    public void refreshUsername (EventUser sender) {
        try {
            User user = userService.getUserByTwitchId(sender.getId());
            if (user != null && !user.getUsername().equals(sender.getName().toLowerCase())) {
                user.setUsername(sender.getName().toLowerCase());
                userService.saveUser(user);
            }
        } catch (Exception e) {
            start(sender);
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

            sendMessage("Ha aparecido un " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");
        }
    }

    public void trowPokeball(ChannelMessageEvent event){
        start(event.getUser());

        if (wildPokemon != null) {

            String pokeball;

            try {
                pokeball = event.getMessage().split(" ")[1];
            } catch (Exception e) {
                pokeball = "pokeball";
            }

            Items pokeballs = userService.getUserByTwitchId(event.getUser().getId()).getItems();

            switch (pokeball) {
                case "superball" -> {
                    if (pokeballs.getSuperball() > 0) {
                        pokeballs.useSuperball();
                        itemsService.saveItem(pokeballs);
                        catchPokemon(event, 70);
                    } else {
                        sendMessage("No tienes Super Balls!");
                    }
                }
                case "ultraball" -> {
                    if (pokeballs.getUltraball() > 0) {
                        pokeballs.useUltraball();
                        itemsService.saveItem(pokeballs);
                        catchPokemon(event, 50);
                    } else {
                        sendMessage("No tienes Ultra Balls!");
                    }
                }
                case "masterball" -> {
                    if (pokeballs.getMasterball() > 0) {
                        pokeballs.useMasterball();
                        itemsService.saveItem(pokeballs);
                        catchPokemon(event, 0);
                    } else {
                        sendMessage("No tienes Master Balls!");
                    }
                }
                default -> catchPokemon(event, 100);
            }

        } else {
            sendMessage("No hay ningun pokemon salvaje!");
        }
    }

    public void catchPokemon(ChannelMessageEvent event, int probability) {
        int random = (int) (Math.random() * probability) + 1;

        if (random < 30) {
            User user = userService.getUserByTwitchId(event.getUser().getId());


            wildPokemon.setUser(user);
            pokemonService.savePokemon(wildPokemon);


            sendMessage("Has capturado un " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + "!");

            wildPokemon = null;
        } else {
            sendMessage("El " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " ha escapado!");
        }

    }

    public void startCombat(ChannelMessageEvent event) {

        if (activeCombat != null && activeCombat.active) {
            sendMessage("Ya hay un combate en curso!");
            return;
        }

        if (event.getMessage().split(" ").length < 2) {
            sendMessage("Elige un usuario para combatir!");
            return;
        } else if (event.getMessage().split(" ").length < 3) {
            sendMessage("Elige un pokemon para combatir!");
            return;
        }

        User player1;
        User player2;

        try {
            player1 = userService.getUserByTwitchId(event.getUser().getId());
        } catch (Exception e) {
            sendMessage("No tienes ningun pokemon!");
            return;
        }

        try {
            player2 = userService.getUserByUsername(event.getMessage().split(" ")[1]);

            if (player2 == null) {
                sendMessage("El usuario no existe!");
                return;
            }
        } catch (Exception e) {
            sendMessage("El usuario no existe!");
            return;
        }

        if (player1 == player2) {
            sendMessage("No puedes combatir contra ti mismo!");
            return;
        } else if (player1.getPokemons().size() == 0) {
            sendMessage("No tienes ningun pokemon!");
            return;
        } else if (player2.getPokemons().size() == 0) {
            sendMessage("El " +  Utilities.firstLetterToUpperCase(player2.getUsername()) + " no tiene ningun pokemon!");
            return;
        }

        Pokemon pokemon1;

        try {
            int pokemonPosition = Integer.parseInt(event.getMessage().split(" ")[2])-1;
            pokemon1 = player1.getPokemons().get(pokemonPosition);
        } catch (Exception e) {
            sendMessage("El pokemon no existe!");
            return;
        }

        activeCombat = new Combat(pokemon1, player2, userService, twitchClient);
        activeCombat.start();
    }

    public void lookPokemon (ChannelMessageEvent event){
        start(event.getUser());
        sendMessage("Tus pokemon: " + SETTINGS.DOMAIN + "/pokemon/" + event.getUser().getName().toLowerCase());
    }

    public void buyItem(ChannelMessageEvent event) {
        start(event.getUser());

        String item = event.getMessage().split(" ")[1];

        User user = userService.getUserByTwitchId(event.getUser().getId());

        Items items = user.getItems();

        switch (item) {
            case "superball" -> {
                if (user.getScore() >= 500) {
                    items.addSuperball();
                    user.addScore(-500);
                    itemsService.saveItem(items);
                    userService.saveUser(user);
                    sendMessage("Has comprado una Superball!");
                } else {
                    sendMessage("No tienes suficiente dinero!");
                }
            }
            case "ultraball" -> {
                if (user.getScore() >= 1000) {
                    items.addUltraball();
                    user.addScore(-1000);
                    itemsService.saveItem(items);
                    userService.saveUser(user);
                    sendMessage("Has comprado una Ultraball!");
                } else {
                    sendMessage("No tienes suficiente dinero!");
                }
            }
            case "masterball" -> {
                if (user.getScore() >= 10000) {
                    items.addMasterball();
                    user.addScore(-10000);
                    itemsService.saveItem(items);
                    userService.saveUser(user);
                    sendMessage("Has comprado una Masterball!");
                } else {
                    sendMessage("No tienes suficiente dinero!");
                }
            }
            default -> sendMessage("El item no existe!");
        }
    }

    public void lookItems(ChannelMessageEvent event) {
        start(event.getUser());

        User user = userService.getUserByTwitchId(event.getUser().getId());

        Items items = user.getItems();

        sendMessage("Tus items: Superball: " + items.getSuperball() + " Ultraball: " + items.getUltraball() + " Masterball: " + items.getMasterball());
    }

    public void myPoints(ChannelMessageEvent event) {
        start(event.getUser());

        User user = userService.getUserByTwitchId(event.getUser().getId());

        sendMessage("Tienes " + user.getScore() + " puntos!");
    }

    /*
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
     */
}
