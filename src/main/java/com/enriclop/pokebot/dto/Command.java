package com.enriclop.pokebot.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Command {

    String name;

    @Nullable
    boolean active;

}
