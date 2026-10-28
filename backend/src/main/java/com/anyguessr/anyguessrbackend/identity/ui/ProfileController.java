package com.anyguessr.anyguessrbackend.identity.ui;

import com.anyguessr.anyguessrbackend.identity.application.ProfileService;
import com.anyguessr.anyguessrbackend.identity.application.UserProfileResponse;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174"}) // allow requests from the frontend running on port 5173 and 5174
public class ProfileController {
    // Inject the ProfileService to handle business logic
    private final ProfileService profileService;
    
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
    // Mapping to get the profile of a user by their ID
    @GetMapping("/{id}")
    public UserProfileResponse getProfile(@PathVariable Long id) {
        return profileService.getProfile(id);
    }
}
