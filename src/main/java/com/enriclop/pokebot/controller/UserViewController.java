package com.enriclop.pokebot.controller;

import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.servicio.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class UserViewController {

    @Autowired
    private UserService userService;

    @GetMapping ("/leaderboard")
    public String leaderboard(Model model) {
        List<User> users = userService.getUsers();
        users.forEach(user -> {
            user.setUsername(user.getUsername().substring(0, 1).toUpperCase() + user.getUsername().substring(1));
        });
        users.sort((o1, o2) -> o2.getPokemons().size() - o1.getPokemons().size());

        model.addAttribute("users", users);

        return "user/leaderboard";
    }

    @GetMapping ("/leaderboard/score")
    public String leaderboardScore(Model model) {
        List<User> users = userService.getUsers();
        users.forEach(user -> {
            user.setUsername(user.getUsername().substring(0, 1).toUpperCase() + user.getUsername().substring(1));
        });
        users.sort((o1, o2) -> o2.getScore() - o1.getScore());

        model.addAttribute("users", users);

        return "user/leaderboard";
    }
}
