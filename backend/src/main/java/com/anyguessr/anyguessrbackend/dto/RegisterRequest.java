package com.anyguessr.anyguessrbackend.dto;

public record RegisterRequest(
    String username,
    String email,
    String password,
    String location
) {
}

