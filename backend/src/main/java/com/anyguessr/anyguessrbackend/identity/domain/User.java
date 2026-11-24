// A player account; id is null until saved, and passwordHash is BCrypt.
package com.anyguessr.anyguessrbackend.identity.domain;

import com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity.UserEntity;
/**
 * 
 * Represents a user account in the application 
 * @param id the local user ID, or null before the user is saved
 * @param username the user's display name
 * @param email the user's email address
 * @param passwordHash the hashed password, or null for an external account
 * @param location the user's location
 */
public record User(
    Long id,
    String username,
    String email,
    String passwordHash,
    String location
) {
}
