package com.anyguessr.anyguessrbackend.identity.domain;

import java.util.Optional;
// Repository interface for managing different operations related to user profiles, such as retrieving and saving profile data.
public interface ProfileRepository {
    //retrieves a user profile by their ID
    Optional<Profile> findById(Long id);
    //saves a user profile
    Profile save(Profile profile);
}
