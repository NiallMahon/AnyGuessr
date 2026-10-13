package com.anyguessr.anyguessrbackend.service;

import com.anyguessr.anyguessrbackend.dto.AuthResponse;
import com.anyguessr.anyguessrbackend.dto.LoginRequest;
import com.anyguessr.anyguessrbackend.dto.RegisterRequest;
import com.anyguessr.anyguessrbackend.model.Profile;
import com.anyguessr.anyguessrbackend.model.User;
import com.anyguessr.anyguessrbackend.repository.ProfileRepository;
import com.anyguessr.anyguessrbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    // ID 1 belongs to the seeded account.
    private final AtomicLong nextUserId = new AtomicLong(2);

    public AuthService(
            UserRepository userRepository,
            ProfileRepository profileRepository
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }

        Long userId = nextUserId.getAndIncrement();

        User user = new User(
                userId,   
                request.email().trim().toLowerCase(Locale.ROOT),
                request.password(),
                request.username().trim(),
                request.location().trim()
        );

        Profile profile = new Profile(
                userId,
                request.username().trim(),
                request.location().trim(),
                createInitials(request.username()),
                0   
        );

        userRepository.save(user);
        profileRepository.save(profile);

        return new AuthResponse(userId);
    }

    public AuthResponse login(LoginRequest request) {
        if (request.identifier() == null || request.password() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Identifier and password are required"
            );
        }

        User user = userRepository.findByEmail(request.identifier())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid identifier or password"
                ));

        if (!user.password().equals(request.password())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        return new AuthResponse(user.id());
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.location() == null
                || request.location().isBlank()
                || request.email() == null
                || request.email().isBlank()
                || request.password() == null
                || request.password().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All registration fields are required"
            );
        }
    }

    private String createInitials(String name) {
        return Arrays.stream(name.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .limit(2)
                .map(part -> part.substring(0, 1)
                        .toUpperCase(Locale.ROOT))
                .collect(Collectors.joining());
    }
}