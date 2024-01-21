package com.enriclop.pokebot.security;

import com.enriclop.pokebot.dto.AdminUser;
import org.springframework.stereotype.Component;

@Component
public class Settings {
    public String channelName = "";
    public String botUsername = "";
    public String oAuthToken = "";
    public String domain = "";
    public AdminUser adminUser;

    public Settings() {
        adminUser = new AdminUser("admin", "admin");
    }
}
