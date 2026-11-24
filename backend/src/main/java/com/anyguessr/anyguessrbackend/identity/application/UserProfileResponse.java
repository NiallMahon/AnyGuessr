package com.anyguessr.anyguessrbackend.identity.application;
/**
 * 
 * Represents a DTO Data Transfer Object for user profile response
 * @param id the local user ID, or null before the user is saved
 * @param username the user's display name
 * @param email the user's email address
 * @param passwordHash the hashed password, or null for an external account
 * @param location the user's location
*/

public record UserProfileResponse(
        Long id,
        String name,
        String location,
        String initials,
        int gamesPlayed
) {
}