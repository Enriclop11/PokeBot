package com.enriclop.pokebot.controller;

import com.enriclop.pokebot.modelo.User;
import com.enriclop.pokebot.servicio.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public User getUserByIdOrUsername(String id) {
        try {
            Integer idInt = Integer.parseInt(id);
            return userService.getUserById(idInt);
        } catch (NumberFormatException e) {
            return userService.getUserByUsername(id);
        }
    }
}
