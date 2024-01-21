package com.enriclop.pokebot.controller;

import com.enriclop.pokebot.dto.AdminUser;
import com.enriclop.pokebot.dto.Command;
import com.enriclop.pokebot.dto.SettingsDTO;
import com.enriclop.pokebot.security.Settings;
import com.enriclop.pokebot.twitchConnection.TwitchConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class SettingsController {

    @Autowired
    TwitchConnection twitchConnection;

     @Autowired
    Settings settings;

    @GetMapping("/settings")
    public String settings(Model model) {
        int cdMinutes;
        boolean spawnActive;

        if (twitchConnection.getSpawn() != null) {
            cdMinutes = twitchConnection.getSpawn().cdMinutes;
            spawnActive = twitchConnection.getSpawn().active;
        } else {
            cdMinutes = 5;
            spawnActive = false;
        }



        SettingsDTO settings = new SettingsDTO(cdMinutes, spawnActive, this.settings.channelName, this.settings.botUsername, this.settings.domain);
        model.addAttribute("settings", settings);
        return "settings/settings";
    }

    @PostMapping("/settings")
    public String changeSettings(@ModelAttribute("settings") SettingsDTO settings) {

        if (settings.getCdMinutes() < 1) settings.setCdMinutes(1);

        twitchConnection.setSpawn(settings.isSpawnActive(), settings.getCdMinutes());

        boolean changed = false;

        if (settings.getChannelName() == null) settings.setChannelName("");


        if (!settings.getChannelName().equals("") && !this.settings.channelName.equals(settings.getChannelName())) {

            if (twitchConnection.getTwitchClient() != null) twitchConnection.getTwitchClient().getChat().leaveChannel(this.settings.channelName);

            this.settings.channelName = settings.getChannelName();
            changed = true;
        }

        if (!settings.getBotUsername().equals("") && !this.settings.botUsername.equals(settings.getBotUsername())) {
            this.settings.botUsername = settings.getBotUsername();
            changed = true;
        }

        if (settings.getOAuthToken() != null && !settings.getOAuthToken().equals("")  && !this.settings.oAuthToken.equals(settings.getOAuthToken()) ) {
            System.out.println(settings.getOAuthToken());
            this.settings.oAuthToken = settings.getOAuthToken();
            changed = true;
        }

        if (changed) {
            twitchConnection.connect();
        }

        this.settings.domain = settings.getDomain();

        return "redirect:/settings";
    }

    @GetMapping("/settings/admin")
    public String admin(Model model) {
        model.addAttribute("adminUser", settings.adminUser);
        return "settings/admin";
    }

    @PostMapping("/settings/admin")
    public String changeAdmin(@ModelAttribute("adminUser") AdminUser adminUser) {
        settings.adminUser = adminUser;
        return "redirect:/settings/admin";
    }

    @GetMapping("/settings/commands")
    public String commands(Model model) {
        model.addAttribute("commands", twitchConnection.getCommands());
        return "settings/commands";
    }

    @GetMapping("/settings/commands/{command}")
    public String command(@PathVariable String command, Model model) {
        List<Command> commands = twitchConnection.getCommands();

        Command commandObj = null;
        for (Command c : commands) {
            if (c.getName().equals(command)) {
                commandObj = c;
                break;
            }
        }

        model.addAttribute("command", commandObj);
        return "redirect:/settings/commands";
    }

    @PostMapping("/settings/commands")
    public String changeCommands(@ModelAttribute("command") Command command) {
        System.out.println(command.getName());
        System.out.println(command.isActive());

        List<Command> commands = twitchConnection.getCommands();

        for (Command c : commands) {
            if (c.getName().equals(command.getName())) {
                c.setActive(command.isActive());
                break;
            }
        }

        twitchConnection.setCommands(commands);

        return "redirect:/settings/commands";
    }



}
