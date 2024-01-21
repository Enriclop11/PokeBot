package com.enriclop.pokebot.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class SettingsDTO {

    String channelName;

    String botUsername;

    String oAuthToken;

    String domain;

    int cdMinutes;

    @Nullable
    boolean spawnActive;

    public SettingsDTO() {
        this.cdMinutes = 5;
        this.spawnActive = false;
    }

    public SettingsDTO(int cdMinutes, boolean spawnActive, String channelName, String botUsername, String domain) {
        this.cdMinutes = cdMinutes;
        this.spawnActive = spawnActive;
        this.channelName = channelName;
        this.botUsername = botUsername;
        this.domain = domain;
    }
}
