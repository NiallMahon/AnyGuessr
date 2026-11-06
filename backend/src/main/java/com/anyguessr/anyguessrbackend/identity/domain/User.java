// A player account; id is null until saved, and passwordHash is BCrypt.
package com.anyguessr.anyguessrbackend.identity.domain;

public record User(
    Long id,
    String email,
    String passwordHash,
    String username,
    String location
) {
}
