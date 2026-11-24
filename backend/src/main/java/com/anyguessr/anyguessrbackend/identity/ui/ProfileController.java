package com.anyguessr.anyguessrbackend.identity.ui;

import com.anyguessr.anyguessrbackend.identity.application.ProfileService;
import com.anyguessr.anyguessrbackend.identity.application.UserProfileResponse;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Provides endpoints for retrieving user profiles.
 */
@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true") // allow requests from the frontend running on port 5173 and 5174
public class ProfileController {
    // Inject the ProfileService to handle business logic
    private final ProfileService profileService;
    
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Retrieves the profile of the user associated with the current session.
     *
     * @param userId the user's ID stored in the session
     * @return the user's profile
     */
    @GetMapping("/me")
    public UserProfileResponse getProfile(@SessionAttribute("userId") Long userId) {
        return profileService.getProfile(userId);
    }
    /**
     * Retrieves a user's profile by ID.
     *
     * @param id the ID of the user whose profile is requested
     * @return the user's profile
     */
    @GetMapping ("/{id}")
    public UserProfileResponse getProfileById(@PathVariable Long id) {
        return profileService.getProfile(id);
    }
}
