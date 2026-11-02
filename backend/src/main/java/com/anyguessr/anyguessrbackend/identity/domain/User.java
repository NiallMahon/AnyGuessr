package com.anyguessr.anyguessrbackend.identity.domain;

public record User(
    Long id,
    String email,
    String password,
    String username,
    String location
) {
}