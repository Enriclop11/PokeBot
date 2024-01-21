package com.enriclop.pokebot.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    Settings settings;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return User.withUsername(settings.adminUser.getUsername())
                .password(settings.adminUser.getPassword())
                .roles("ADMIN")
                .build();
    }
}
