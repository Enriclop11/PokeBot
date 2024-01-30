package com.enriclop.pokebot.security;

import com.enriclop.pokebot.dto.AdminUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class Settings {
    public String channelName = "";
    public String tokenChannel = "";

    public String botUsername = "";
    public String tokenBot = "";

    public String tokenDiscord = "";

    public String domain = "";
    public AdminUser adminUser;

    public Settings() {
        adminUser = new AdminUser("admin", "admin");
    }

    public String getoAuthTokenChannel() {
        return "oauth:" + tokenChannel;
    }

    public String getoAuthTokenBot() {
        return "oauth:" + tokenBot;
    }
}
