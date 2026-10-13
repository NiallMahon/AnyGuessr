package com.anyguessr.anyguessrbackend.model;

public record User(
    Long id,
    String email,
    String password,
    String username,
    String location
) {
}