package com.anyguessr.anyguessrbackend.identity.application;

import com.anyguessr.anyguessrbackend.identity.domain.Profile;
import com.anyguessr.anyguessrbackend.identity.domain.ProfileRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    // Service class that handles business logic related to user profiles, such as retrieving and saving profile data.
    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }
    // Retrieves a user profile by their ID, throwing a ResponseStatusException with a 404 status if the profile is not found.
    public UserProfileResponse getProfile(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        return new UserProfileResponse(
                profile.id(),
                profile.name(),
                profile.location(),
                profile.initials(),
                profile.gamesPlayed()
        );
    }

}
