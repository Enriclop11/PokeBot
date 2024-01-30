package com.enriclop.pokebot.discordConnection;

import com.enriclop.pokebot.modelo.Pokemon;
import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.security.Settings;
import com.enriclop.pokebot.servicio.UserService;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
public class DiscordConnection {

    @Autowired
    private Settings settings;

    @Autowired
    private UserService userService;

    DiscordClient client;

    GatewayDiscordClient gateway;

    public DiscordConnection() {
        if (settings == null) {
            settings = new Settings();
        }

        connect();
    }

    public void restart() {
        gateway.logout().block();
        connect();
    }

    public void connect() {
        client = DiscordClient.create(settings.tokenDiscord);

        gateway = client.login().block();

        gateway.on(ReadyEvent.class).subscribe(event -> {
            System.out.println("Logged in as " + event.getSelf().getUsername());
        });

        gateway.on(MessageCreateEvent.class).subscribe(event -> {
            Message message = event.getMessage();

            String command = message.getContent().split(" ")[0];

            switch (command) {
                case "!ping":
                    MessageChannel channel = message.getChannel().block();
                    channel.createMessage("Pong!").block();
                    System.out.println(message.getAuthor().get().getUsername());
                    break;
                case "!help":
                    MessageChannel channel2 = message.getChannel().block();
                    channel2.createMessage("Comandos disponibles: !pokemons").block();
                    break;
                case "!pokemons":
                    lookPC(event);
                    break;
                default:
                    break;
            }
        });
    }

    public void lookPC(MessageCreateEvent event) {
        System.out.println("Looking PC");

        MessageChannel channel = event.getMessage().getChannel().block();

        User user = null;

        try{
            user = userService.getUserByDiscordUsername(event.getMessage().getAuthor().get().getUsername());
        } catch (Exception e) {
            channel.createMessage("No tienes cuenta vinculada").block();
            return;
        }

        if (user.getPokemons().isEmpty()) {
            channel.createMessage("El usuario " + user.getUsernameDisplay() + " no tiene pokemons").block();
            return;
        }

        EmbedCreateSpec embedPokemon = getEmbedPokemon(user, 0);

        List<Button> buttons = new ArrayList<>();
        if (user.getPokemons().size() > 1) {
            buttons.add(Button.primary("page_" + user.getId() + "_2", "▶"));
        }
        buttons.add(Button.secondary("select_" + user.getId() + "_1", "Seleccionar"));

        Mono<Message> createMessageMono = channel.createMessage(MessageCreateSpec.builder()
                .addEmbed(embedPokemon)
                .addComponent(ActionRow.of(buttons))
                .build());

        Mono<Void> tempListener = gateway.on(ButtonInteractionEvent.class, eventButton -> {
            if (eventButton.getCustomId().startsWith("page")) {
                String[] data = eventButton.getCustomId().split("_");
                int userId = Integer.parseInt(data[1]);
                int page = Integer.parseInt(data[2]);

                User user1 = userService.getUserById(userId);

                EmbedCreateSpec embedPokemon1 = getEmbedPokemon(user1, page - 1);

                List<Button> buttons1 = new ArrayList<>();
                if (page > 1) {
                    buttons1.add(Button.primary("page_" + user1.getId() + "_" + (page - 1), "◀"));
                }
                buttons1.add(Button.secondary("select_" + user1.getId() + "_" + (page), "Seleccionar"));
                if (user1.getPokemons().size() > page) {
                    buttons1.add(Button.primary("page_" + user1.getId() + "_" + (page + 1), "▶"));
                }


                return eventButton.edit().withEmbeds(embedPokemon1)
                        .withComponents(ActionRow.of(buttons1));
            } else {
                if (eventButton.getCustomId().startsWith("select")) {
                    String[] data = eventButton.getCustomId().split("_");
                    int userId = Integer.parseInt(data[1]);
                    int pokemonIndex = Integer.parseInt(data[2]) - 1;

                    User user1 = userService.getUserById(userId);
                    user1.setPokemonSelected(user1.getPokemons().get(pokemonIndex).getId());
                    userService.saveUser(user1);
                }

                return Mono.empty();
            }
        }).timeout(Duration.ofMinutes(30))
                .onErrorResume(TimeoutException.class, ignore -> Mono.empty())
                .then();

        createMessageMono.then(tempListener).subscribe();
    }

    public EmbedCreateSpec getEmbedPokemon(User user, int pokemonIndex) {
        Pokemon pokemon = user.getPokemons().get(pokemonIndex);

        return EmbedCreateSpec.builder()
                .title(pokemon.getDisplayName())
                .url("https://www.pokemon.com/es/pokedex/" + pokemon.getName().toLowerCase())
                .author(user.getUsernameDisplay(), null, user.getAvatar())
                .description("Pokemon de " + user.getUsernameDisplay())
                .thumbnail(user.getAvatar())
                .addField("HP", String.valueOf(pokemon.getHp()), true)
                .addField("ATK", String.valueOf(pokemon.getAttack()), true)
                .addField("DEF", String.valueOf(pokemon.getDefense()), true)
                .addField("SPATK", String.valueOf(pokemon.getSpecialAttack()), true)
                .addField("SPDEF", String.valueOf(pokemon.getSpecialDefense()), true)
                .addField("SPD", String.valueOf(pokemon.getSpeed()), true)
                .image(pokemon.getFrontSprite())
                .timestamp(Instant.now())
                .footer("Pokemon " + (user.getPokemons().indexOf(pokemon) + 1) + " / " + user.getPokemons().size(), null)
                .build();
    }

}
