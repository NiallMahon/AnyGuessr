package com.anyguessr.anyguessrbackend.repository;

import com.anyguessr.anyguessrbackend.model.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryProfileRepository implements ProfileRepository {
    private final Map<Long, Profile> profiles = new HashMap<>();

    // Initialize the repository with a sample profile, in this case from the in memory storage, but in the real application, this would be replaced with the actual database initialization logic.
    public InMemoryProfileRepository(){
        profiles.put(1L, new Profile(1L, "Cillian Shine", "Galway, Ireland", "CS", 67));
    }
    // Retrieves a user profile by their ID from the in-memory storage, returning an Optional that may or may not contain a Profile object.
    @Override 
    public Optional<Profile> findById(Long id) {
        return Optional.ofNullable(profiles.get(id));
    }
    // Saves a user profile to the in-memory storage, returning the saved Profile object. In the real application, this method would handle database persistence.
    @Override
    public Profile save(Profile profile) {
        profiles.put(profile.id(), profile);
        return profile;
    }
    
}
