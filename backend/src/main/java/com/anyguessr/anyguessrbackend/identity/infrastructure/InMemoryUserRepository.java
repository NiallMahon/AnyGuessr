package com.anyguessr.anyguessrbackend.identity.infrastructure;

import org.springframework.stereotype.Repository;

import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import java.util.concurrent.atomic.AtomicLong;

@Profile("memory")
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> usersById = new ConcurrentHashMap<>();

    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();

    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    
    private final AtomicLong nextID = new AtomicLong(2L); // Start from 2 since we have a sample user with ID 1
    public InMemoryUserRepository() {
        save(new User(
                1L,
                "CillianShine",
                "cillian@email.com",
                "hashed_password"
        ));
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(
                usersById.get(id)
        );
    }


    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                usersByEmail.get(normalise(email))
        );
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                usersByUsername.get(normalise(username))
        );
    }

    @Override
    public User save(User user) {
        User savedUser = user;
        if (user.id() == null) {
            Long newId = nextID.getAndIncrement();
            savedUser = new User(
                    newId,
                    user.username(),
                    user.email(),
                    user.passwordHash()
                );
        }
        usersById.put(
                savedUser.id(),
                savedUser
        );

        if(savedUser.email() != null) {
            usersByEmail.put(
                    normalise(savedUser.email()),
                    savedUser
            );
        }
        if(savedUser.username() != null) {
            usersByUsername.put(
            normalise(savedUser.username()),
            savedUser
);
        }
        return savedUser;
    }

    private String normalise(String value) {
        return value.trim().toLowerCase();
    }
}