package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.apis.PokeApi;
import com.enriclop.pokebot.dto.Command;
import com.enriclop.pokebot.modelo.Items;
import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.security.Settings;
import com.enriclop.pokebot.servicio.ItemsService;
import com.enriclop.pokebot.servicio.MoveService;
import com.enriclop.pokebot.servicio.PokemonService;
import com.enriclop.pokebot.servicio.UserService;
import com.enriclop.pokebot.twitchConnection.settings.Prices;
import com.enriclop.pokebot.utilities.Utilities;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.chat.events.channel.FollowEvent;
import com.github.twitch4j.common.events.domain.EventUser;
import com.github.twitch4j.helix.domain.Chatter;
import com.github.twitch4j.helix.domain.ChattersList;
import com.github.twitch4j.pubsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.pubsub.events.RewardRedeemedEvent;
import com.github.twitch4j.util.PaginationUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
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

    @Autowired
    Settings settings;

    @Autowired
    Prices prices;

    Pokemon wildPokemon;

    TwitchClient twitchClient;

    EventManager eventManager;

    Combat activeCombat;

    Spawn spawn;

    List<Command> commands;

    List<Command> rewards;

    OAuth2Credential streamerCredential;

    com.github.twitch4j.helix.domain.User channel;

    public TwitchConnection() {
        settings = new Settings();

        commands = List.of(
                new Command("leaderboard", true),
                new Command("pokemon", true),
                new Command("catch", true),
                new Command("combat", true),
                new Command("mypokemon", true),
                new Command("refreshusername", true),
                new Command("buy", true),
                new Command("items", true),
                new Command("points", true),
                new Command("help", true),
                new Command("lookprices", true),
                new Command("linkdiscord", true),
                new Command("watchtime", true)
        );

        rewards = List.of(
                new Command("pokeball", true),
                new Command("superball", true),
                new Command("ultraball", true),
                new Command("masterball", true),
                new Command("test", true)
        );

       
    }

    public void connect() {

        if (twitchClient != null) {
            twitchClient.close();
        }

         twitchClient = TwitchClientBuilder.builder()
                 .withDefaultAuthToken(new OAuth2Credential(settings.botUsername, settings.getoAuthTokenBot()))
                 .withEnableHelix(true)
                 .withEnableChat(true)
                 .withEnablePubSub(true)
                 .withChatAccount(new OAuth2Credential(settings.botUsername, settings.getoAuthTokenBot()))
                 .build();

         twitchClient.getChat().joinChannel(settings.channelName);

         channel = getUserDetails(settings.channelName);

         streamerCredential = new OAuth2Credential("twitch", settings.getoAuthTokenChannel());

         twitchClient.getPubSub().listenForChannelPointsRedemptionEvents(null, channel.getId());
         twitchClient.getPubSub().listenForSubscriptionEvents(streamerCredential, channel.getId());
         
         twitchClient.getClientHelper().enableFollowEventListener(settings.channelName);

         commands();

         new SetWatchTime(this).start();
    }

    public void sendMessage(String message) {
        twitchClient.getChat().sendMessage(settings.channelName, message);
    }

    public void commands() {
        eventManager = twitchClient.getEventManager();

        eventManager.onEvent(ChannelMessageEvent.class, event -> {
            String command = event.getMessage().split(" ")[0];

            if (command.startsWith("!")) {
                command = command.substring(1);
            } else {
                return;
            }

            String finalCommand = command.toLowerCase();
            useCommand(commands.stream().filter(c -> c.getCustomName().equals(finalCommand)).findFirst().orElse(null), event);
        });

        eventManager.onEvent(RewardRedeemedEvent.class, event -> {
            String reward = event.getRedemption().getReward().getTitle();

            String finalReward = reward.toLowerCase();
            useReward(rewards.stream().filter(r -> r.getCustomName().equals(finalReward)).findFirst().orElse(null), event);
        });

        eventManager.onEvent(FollowEvent.class , event -> {
            followingReward(event.getUser().getId());
        });

        eventManager.onEvent(ChannelSubscribeEvent.class, event -> {
            subReward(event.getData().getUserId());
        });

    }

    public void useCommand(Command command, ChannelMessageEvent event) {

        if (command == null) return;
        if (!command.isActive()) return;

        switch (command.getName()) {
            case "help" -> sendMessage("Comandos disponibles: !leaderboard !pokemon !catch !combat !mypokemon !refreshusername !buy !items !points !lookprices !linkDiscord !watchtime");
            case "leaderboard" -> leaderboard();
            case "pokemon" -> spawnPokemon();
            case "catch" -> trowPokeball(event);
            case "combat" -> startCombat(event);
            case "mypokemon" -> lookPokemon(event);
            case "refreshusername" -> refreshUsername(event.getUser());
            case "buy" -> buyItem(event);
            case "items" -> lookItems(event);
            case "points" -> myPoints(event);
            case "lookprices" -> lookPrices(event);
            case "linkdiscord" -> linkDiscord(event);
            case "watchtime" -> sendMessage("Tu tiempo de visualización: " + userService.getUserByTwitchId(event.getUser().getId()).getWatchTime());
        }
    }

    public void linkDiscord(ChannelMessageEvent event){
        System.out.println("Linking Discord");
        start(event.getUser().getId());
        User user = userService.getUserByTwitchId(event.getUser().getId());

        if (event.getMessage().split(" ").length < 2) {
            sendMessage("Introduce tu nombre de usuario de Discord!");
            return;
        }

        user.setDcUsername(event.getMessage().split(" ")[1]);
        userService.saveUser(user);
        sendMessage("Discord vinculado!");
    }

    public void useReward(Command command, RewardRedeemedEvent event){
        if (command == null) return;
        if (!command.isActive()) return;

        switch (command.getName()) {
            case "pokeball" -> {
                catchPokemon(event.getRedemption().getUser().getId(), 100);
            }
            case "superball" -> {
                catchPokemon(event.getRedemption().getUser().getId(), 70);
            }
            case "ultraball" -> {
                catchPokemon(event.getRedemption().getUser().getId(), 50);
            }
            case "masterball" -> {
                catchPokemon(event.getRedemption().getUser().getId(), 0);
            }
            //dox the user ip
            case "test" -> sendMessage("https://www.youtube.com/watch?v=BbeeuzU5Qc8");
        }
    }

    public Collection<Chatter> getChatters() {

        return PaginationUtil.getPaginated(
                cursor -> {
                    try {
                        return twitchClient.getHelix().getChatters(settings.getTokenChannel(), channel.getId(), channel.getId(), 1000, cursor).execute();
                    } catch (Exception e) {
                        System.out.println("Error al obtener los chatters");
                        return null;
                    }
                },
                ChattersList::getChatters,
                call -> call.getPagination() != null ? call.getPagination().getCursor() : null
        );
    }

    public List<User> getChattersUsers() {
        Collection<Chatter> chatters = getChatters();
        List<User> users = new ArrayList<>();
        for (Chatter chatter : chatters) {
            User user = userService.getUserByTwitchId(chatter.getUserId());
            if (user != null) {
                users.add(user);
            } else {
                start(chatter.getUserId());
                users.add(userService.getUserByTwitchId(chatter.getUserId()));
            }
        }
        return users;
    }


    public void setSpawn(Boolean active, int cdMinutes) {
        if (active) {
            spawn = new Spawn(this, cdMinutes);
            spawn.start();
        } else {
            if (spawn != null) spawn.active = false;
        }
    }

    public void start (String twitchId) {
        if (userService.getUserByTwitchId(twitchId) == null) {
            com.github.twitch4j.helix.domain.User user = getUserDetails(Integer.parseInt(twitchId));
            User newUser = new User(user.getId(), user.getDisplayName().toLowerCase(), user.getProfileImageUrl());
            userService.saveUser(newUser);
        }
    }

    public void followingReward(String twitchId) {
        start(twitchId);

        User user = userService.getUserByTwitchId(twitchId);

        sendMessage("Gracias por seguirme " + user.getUsernameDisplay() + "!");

        Items items = user.getItems();
        items.addSuperball();
        items.addSuperball(10);
        items.addUltraball(10);

        userService.saveUser(user);
    }

    public void subReward(String twitchId){
        start(twitchId);

        User user = userService.getUserByTwitchId(twitchId);

        sendMessage("Gracias por suscribirte " + user.getUsernameDisplay() + "!");

        Items items = user.getItems();
        items.addSuperball(50);
        items.addUltraball(20);
        items.addMasterball(5);

        userService.saveUser(user);
    }

    public com.github.twitch4j.helix.domain.User getUserDetails(String username) {
        com.github.twitch4j.helix.domain.User[] user = new com.github.twitch4j.helix.domain.User[1];

        twitchClient.getHelix().getUsers(null, null, List.of(username)).execute().getUsers().forEach(u -> {
            user[0] = u;
        });

        return user[0];
    }

    public com.github.twitch4j.helix.domain.User getUserDetails(int id) {
        com.github.twitch4j.helix.domain.User[] users = new com.github.twitch4j.helix.domain.User[1];

        twitchClient.getHelix().getUsers(null, List.of(String.valueOf(id)), null).execute().getUsers().forEach(user -> {
            users[0] = user;
        });

        return users[0];
    }

    public void refreshUsername (EventUser sender) {
        try {
            User user = userService.getUserByTwitchId(sender.getId());
            if (user != null && !user.getUsername().equals(sender.getName().toLowerCase())) {
                user.setUsername(sender.getName().toLowerCase());
                userService.saveUser(user);
            }
        } catch (Exception e) {
            start(sender.getId());
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

        twitchClient.getChat().sendMessage(settings.channelName,leaderboard.toString());
    }

    public void spawnPokemon() {
        Pokemon newPokemon = PokeApi.getRandomPokemon();

        if (newPokemon != null) {
            wildPokemon = newPokemon;

            sendMessage("Ha aparecido un " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " salvaje!");
        }
    }

    public void trowPokeball(ChannelMessageEvent event){
        start(event.getUser().getId());

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
                        catchPokemon(event.getUser().getId(), 70);
                    } else {
                        sendMessage("No tienes Super Balls!");
                    }
                }
                case "ultraball" -> {
                    if (pokeballs.getUltraball() > 0) {
                        pokeballs.useUltraball();
                        itemsService.saveItem(pokeballs);
                        catchPokemon(event.getUser().getId(), 50);
                    } else {
                        sendMessage("No tienes Ultra Balls!");
                    }
                }
                case "masterball" -> {
                    if (pokeballs.getMasterball() > 0) {
                        pokeballs.useMasterball();
                        itemsService.saveItem(pokeballs);
                        catchPokemon(event.getUser().getId(), 0);
                    } else {
                        sendMessage("No tienes Master Balls!");
                    }
                }
                default -> catchPokemon(event.getUser().getId(), 100);
            }

        } else {
            sendMessage("No hay ningun pokemon salvaje!");
        }
    }

    public void catchPokemon(String idTwitch, int probability) {
        if (wildPokemon == null) {
            sendMessage("No hay ningun pokemon salvaje!");
            return;
        }

        int random = (int) (Math.random() * probability) + 1;

        if (random < 30) {
            User user = userService.getUserByTwitchId(idTwitch);


            wildPokemon.setUser(user);
            pokemonService.savePokemon(wildPokemon);


            sendMessage(Utilities.firstLetterToUpperCase(user.getUsername()) + " ha capturado un " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + "!");

            wildPokemon = null;
        } else {
            sendMessage("El " + Utilities.firstLetterToUpperCase(wildPokemon.getName()) + " se ha escapado de la pokeball!");
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
            String username = event.getMessage().split(" ")[1];

            if (username.startsWith("@")) {
                username = username.substring(1);
            }

            player2 = userService.getUserByUsername(username);

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
        } else if (player1.getPokemons().isEmpty()) {
            sendMessage("No tienes ningun pokemon!");
            return;
        } else if (player2.getPokemons().isEmpty()) {
            sendMessage("El " +  Utilities.firstLetterToUpperCase(player2.getUsername()) + " no tiene ningun pokemon!");
            return;
        }

        Pokemon pokemon1 = getSelectedPokemon(player1);
        Pokemon pokemon2 = getSelectedPokemon(player2);

        if (pokemon1 == null) {
            sendMessage("No tienes ningun pokemon seleccionado!");
            return;
        } else if (pokemon2 == null) {
            sendMessage("El " +  Utilities.firstLetterToUpperCase(player2.getUsername()) + " no tiene ningun pokemon seleccionado!");
            return;
        }

        activeCombat = new Combat(player1, player2, userService, twitchClient, settings, pokemon1, pokemon2);
        activeCombat.start();
    }

    public Pokemon getSelectedPokemon(User user) {
        try {
            return pokemonService.getPokemonById(user.getPokemonSelected());
        } catch (Exception e) {
            return null;
        }
    }

    public void lookPokemon (ChannelMessageEvent event){
        start(event.getUser().getId());
        sendMessage("Tus pokemon: " + settings.domain + "/pokemon/" + event.getUser().getName().toLowerCase());
    }

    public void lookPrices (ChannelMessageEvent event){
        start(event.getUser().getId());
        sendMessage("Precios: Superball: " + prices.getSuperballPrice() + " Ultraball: " + prices.getUltraballPrice() + " Masterball: " + prices.getMasterballPrice());
    }

    public void buyItem(ChannelMessageEvent event) {
        start(event.getUser().getId());

        String item = event.getMessage().split(" ")[1];

        int amount;
        try{
            amount = Integer.parseInt(event.getMessage().split(" ")[2]);
            if (amount < 1) amount = 1;
            if (amount > 100) amount = 100;
        } catch (Exception e) {
            amount = 1;
        }

        User user = userService.getUserByTwitchId(event.getUser().getId());

        Items items = user.getItems();

        switch (item) {
            case "superball" -> {
                if (user.getScore() >= (prices.getSuperballPrice() * amount)) {
                    items.addSuperball(amount);
                    user.minusScore(prices.getSuperballPrice() * amount);
                    itemsService.saveItem(items);
                    userService.saveUser(user);
                    sendMessage("Has comprado una Superball!");
                } else {
                    sendMessage("No tienes suficiente dinero!");
                }
            }
            case "ultraball" -> {
                if (user.getScore() >= (prices.getUltraballPrice() * amount)) {
                    items.addUltraball(amount);
                    user.minusScore(prices.getUltraballPrice() * amount);
                    itemsService.saveItem(items);
                    userService.saveUser(user);
                    sendMessage("Has comprado una Ultraball!");
                } else {
                    sendMessage("No tienes suficiente dinero!");
                }
            }
            case "masterball" -> {
                if (user.getScore() >= (prices.getMasterballPrice() * amount)) {
                    items.addMasterball(amount);
                    user.minusScore(prices.getMasterballPrice() * amount);
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
        start(event.getUser().getId());

        User user = userService.getUserByTwitchId(event.getUser().getId());

        Items items = user.getItems();

        sendMessage("Tus items: Superball: " + items.getSuperball() + " Ultraball: " + items.getUltraball() + " Masterball: " + items.getMasterball());
    }

    public void myPoints(ChannelMessageEvent event) {
        start(event.getUser().getId());

        User user = userService.getUserByTwitchId(event.getUser().getId());

        sendMessage("Tienes " + user.getScore() + " puntos!");
    }
}
