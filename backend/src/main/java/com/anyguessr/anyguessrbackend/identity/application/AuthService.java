package com.anyguessr.anyguessrbackend.identity.application;

import com.anyguessr.anyguessrbackend.identity.domain.Profile;
import com.anyguessr.anyguessrbackend.identity.domain.ProfileRepository;
import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.crypto.password.PasswordEncoder;



import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    
    

    public AuthService(
            UserRepository userRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }


        User user = new User(
                null,
                request.username().trim(),
                email,
                passwordEncoder.encode(request.password())
        );
        User savedUser = userRepository.save(user);
        Long userId = savedUser.id();

        Profile profile = new Profile(
                userId,
                request.username().trim(),
                request.location().trim(),
                createInitials(request.username()),
                0   
        );

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

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
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