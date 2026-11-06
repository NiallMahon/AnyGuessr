// Registers and logs in players against the users table, hashing passwords with BCrypt.
package com.anyguessr.anyguessrbackend.identity.application;

import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository.UsernameOrEmailTakenException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "That username is already taken"
            );
        }

        User user = new User(
                null,
                email,
                passwordEncoder.encode(request.password()),
                username,
                request.location().trim()
        );

        try {
            return new AuthResponse(userRepository.save(user).id());
        } catch (UsernameOrEmailTakenException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "That username or email is already taken"
            );
        }
    }

    public AuthResponse login(LoginRequest request) {
        if (request.identifier() == null || request.password() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Identifier and password are required"
            );
        }

        User user = userRepository.findByEmail(request.identifier())
                .or(() -> userRepository.findByUsername(request.identifier()))
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
}
