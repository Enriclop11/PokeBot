package com.enriclop.pokebot.twitchConnection;

import com.enriclop.pokebot.pokeLogic.TableType;
import com.enriclop.pokebot.modelo.Move;
import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.dto.PokemonCombat;
import com.enriclop.pokebot.security.Settings;
import com.enriclop.pokebot.servicio.UserService;
import com.enriclop.pokebot.utilities.Timer;
import com.enriclop.pokebot.utilities.Utilities;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Combat extends Thread{

    UserService userService;

    TwitchClient twitchClient;

    Settings settings;

    User player1;
    User player2;

    Pokemon pokemon1;
    Pokemon pokemon2;

    List<PokemonCombat> order;

    Boolean accepted = false;

    Boolean active = true;

    Timer timer;

    User winner;

    Boolean started = false;


    public Combat(Pokemon pokemon1, User user2, UserService userService, TwitchClient twitchClient, Settings settings) {
        this.userService = userService;
        this.twitchClient = twitchClient;
        this.settings = settings;

        this.pokemon1 = pokemon1;

        this.player1 = pokemon1.getUser();
        this.player2 = user2;

        timer = new Timer();
        timer.start();
    }

    public void sendMessage(String message) {
        twitchClient.getChat().sendMessage(settings.channelName, message);
    }

    public void run() {
        sendMessage("¡" + Utilities.firstLetterToUpperCase(player2.getUsername())  + " aceptara el combate? !accept <Pokemon>");

        twitchClient.getEventManager().onEvent(ChannelMessageEvent.class, event -> {
            if (!active) return;
            if (accepted) return;
            if (!event.getUser().getId().equals(player2.getTwitchId())) return;
            if (timer.getMinutes() > 1) {
                sendMessage("¡" + Utilities.firstLetterToUpperCase(player2.getUsername()) + " no ha aceptado el combate a tiempo!");
                active = false;
                return;
            }


            String command = event.getMessage().split(" ")[0];

            if (command.equals("!accept")){
                try {
                    int pokemonPosition = Integer.parseInt(event.getMessage().split(" ")[1])-1;
                    pokemon2 = player2.getPokemons().get(pokemonPosition);
                    accepted = true;
                    sendMessage("¡" + Utilities.firstLetterToUpperCase(player2.getUsername()) + " ha aceptado el combate!");
                } catch (Exception e) {
                    sendMessage("¡" + Utilities.firstLetterToUpperCase(player2.getUsername()) + " no tiene un pokemon en esa posicion!");
                    active = false;
                    return;
                }
            }
        });


    }

    public void startCombat(){
        started = true;

        System.out.println("Combat started");

        order = new ArrayList<>();
        if (pokemon1.getSpeed() > pokemon2.getSpeed()) {
            order.add(new PokemonCombat(pokemon1));
            order.add(new PokemonCombat(pokemon2));
        } else if (pokemon1.getSpeed() < pokemon2.getSpeed()) {
            order.add(new PokemonCombat(pokemon2));
            order.add(new PokemonCombat(pokemon1));
        } else {
            if (Math.random() < 0.5) {
                order.add(new PokemonCombat(pokemon1));
                order.add(new PokemonCombat(pokemon2));
            } else {
                order.add(new PokemonCombat(pokemon2));
                order.add(new PokemonCombat(pokemon1));
            }
        }

        wait(5);

        while (order.get(0).getCurrentHp() > 0 && order.get(1).getCurrentHp() > 0){
            wait(5);
            attack(order.get(0), order.get(1));
            if (order.get(1).getCurrentHp() <= 0) break;
            wait(5);
            attack(order.get(1), order.get(0));
        }
        wait(10);

        if (order.get(0).getCurrentHp() <= 0) {
            winner = order.get(1).getUser();
        } else {
            winner = order.get(0).getUser();
        }

        winner = userService.getUserById(winner.getId());
        winner.addScore(100);
        userService.saveUser(winner);

        sendMessage("¡" + Utilities.firstLetterToUpperCase(winner.getUsername()) + " ha ganado el combate!");

        endCombat();
    }

    public void endCombat(){
        order = null;
        active = false;
        winner = null;
    }

    public void attack(PokemonCombat attacker, PokemonCombat defender){
        int move = (int) (Math.random() * attacker.getMoves().size());
        Move selectedMove = attacker.getMoves().get(move);

        if (selectedMove.getAccuracy() < (int) (Math.random() * 100)) {
            sendMessage(attacker.getName() + " ha fallado el ataque !");
            return;
        }

        if (selectedMove.getPower() == 0) {
            sendMessage(attacker.getName() + " ha usado " + selectedMove.getName() + "!");
            return;
        }

        double attack = 0;
        double defense = 0;

        if (selectedMove.getEffectiveAttack().equals("physical")) {
            attack = attacker.getAttack();
            defense = defender.getDefense();
        } else if (selectedMove.getEffectiveAttack().equals("special")) {
            attack = attacker.getSpecialAttack();
            defense = defender.getSpecialDefense();
        } else {
            System.out.println("Error en el ataque");
        }

        double damage = 0;

        if (Math.random() < 0.0417 + (selectedMove.getCrit_rate() / 100.0)) {
            damage = (2*2)/5.0;
        } else {
            damage = (2)/5.0;
        }

        //STAB is the same-type attack bonus. This is equal to 1.5 if the move's type matches any of the user's types, and 1 if otherwise. Internally, it is recognized as an addition of the damage calculated thus far divided by 2, rounded down, then added to the damage calculated thus far.
        if (attacker.getType() == selectedMove.getType() || attacker.getType2() == selectedMove.getType()) {
            damage *= 1.5;
        }

        damage *= selectedMove.getPower();
        damage *= (attack / defense);
        damage /= 50.0;
        damage += 2;

        //calculate type effectiveness
        damage *= TableType.modifierAgainst(selectedMove.getType(), defender.getType());
        if (defender.getType2() != null) damage *= TableType.modifierAgainst(selectedMove.getType(), defender.getType2());

        //random is realized as a multiplication by a random uniformly distributed integer between 217 and 255 (inclusive), followed by an integer division by 255. If the calculated damage thus far is 1, random is always 1.
        damage *= (int) (Math.random() * 39 + 217);
        damage /= 255.0;

        damage *= 10;

        System.out.println(attacker.getName() + " ha usado " + selectedMove.getName() + "!");
        System.out.println("Damage: " + damage);

        sendMessage(attacker.getName() + " ha usado " + selectedMove.getName() + "y ha hecho " + (int) damage +  " de daño !");
        defender.setCurrentHp((int) (defender.getCurrentHp() - damage));

        if (defender.getCurrentHp() < 0) {
            defender.setCurrentHp(0);
        }
    }

    private void wait(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
