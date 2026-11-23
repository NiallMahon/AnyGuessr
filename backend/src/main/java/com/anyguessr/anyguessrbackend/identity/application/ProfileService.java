package com.anyguessr.anyguessrbackend.identity.application;

import com.anyguessr.anyguessrbackend.identity.domain.Profile;
import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;
import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final StatsService statsService;
    // Service class that handles business logic related to user profiles, such as retrieving and saving profile data.
    public ProfileService(UserRepository userRepository, StatsService statsService) {
        this.userRepository = userRepository;
        this.statsService = statsService;
    }
    // Retrieves a user profile by their ID, throwing a ResponseStatusException with a 404 status if the profile is not found.
    public UserProfileResponse getProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        int gamesplayed = statsService.getGamesPlayed(new UserId(user.id()));
        return new UserProfileResponse(
                user.id(),
                user.username(),
                user.location(),
                Profile.initialsOf(user.username()),
                gamesplayed
        );
    }

}
