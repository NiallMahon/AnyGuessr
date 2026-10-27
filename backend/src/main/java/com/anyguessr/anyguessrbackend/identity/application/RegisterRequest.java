package com.anyguessr.anyguessrbackend.identity.application;

public record RegisterRequest(
    String username,
    String email,
    String password,
    String location
) {
}

