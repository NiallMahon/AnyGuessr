package com.anyguessr.anyguessrbackend.identity.domain;
// Model class representing a user profile
public record Profile(
    Long id,
    String name,
    String location,
    String initials,
    int gamesPlayed
) {  
}
