package com.anyguessr.anyguessrbackend.identity.infrastructure;

import org.springframework.stereotype.Repository;

import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> usersByEmail =
            new ConcurrentHashMap<>();

    public InMemoryUserRepository() {
        save(new User(
                1L,
                "cillian@email.com",
                "password",
                "CillianShine",
                "Galway, Ireland"
        ));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(
                usersByEmail.get(normaliseEmail(email))
        );
    }

    @Override
    public User save(User user) {
        usersByEmail.put(
                normaliseEmail(user.email()),
                user
        );

        return user;
    }

    private String normaliseEmail(String email) {
        return email.trim().toLowerCase();
    }
}