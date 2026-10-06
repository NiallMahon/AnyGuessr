package com.anyguessr.anyguessrbackend.gameplay.domain;

public record RegionId(String value) {

    public RegionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Region ID must not be blank");
        }
    }
}