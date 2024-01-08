package com.enriclop.pokebot.controller;

import com.enriclop.pokebot.servicio.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String getUsers() {
        return userService.getUsers().toString();
    }

    @GetMapping("/users/{id}")
    public String getUserByIdOrUsername(String id) {
        try {
            Integer idInt = Integer.parseInt(id);
            return userService.getUserById(idInt).toString();
        } catch (NumberFormatException e) {
            return userService.getUserByUsername(id).toString();
        }
    }
}
