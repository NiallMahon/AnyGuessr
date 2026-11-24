package com.anyguessr.anyguessrbackend.identity.application;

import com.anyguessr.anyguessrbackend.identity.domain.Profile;
import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;
import com.anyguessr.anyguessrbackend.stats.domain.UserId;
import com.anyguessr.anyguessrbackend.stats.application.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
/**
 * Builds user profiles from account details and game statistics.
 */
@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final StatsService statsService;
    
    public ProfileService(UserRepository userRepository, StatsService statsService) {
        this.userRepository = userRepository;
        this.statsService = statsService;
    }
    /**
     * Retrieves a user's public profile, including their games-played count.
     *
     * @param id the local ID of the user
     * @return the user's profile
     * @throws ResponseStatusException if the user cannot be found
     */
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
