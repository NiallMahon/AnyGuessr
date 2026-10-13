package com.anyguessr.anyguessrbackend.dto;
// DTO (Data Transfer Object) for user profile response
public record UserProfileResponse(
        Long id,
        String name,
        String location,
        String initials,
        int gamesPlayed
) {
}