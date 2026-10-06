package com.anyguessr.anyguessrbackend.gameplay.domain;

public record MapId(String value) {

    public MapId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Map ID must not be blank"
            );
        }
    }
}