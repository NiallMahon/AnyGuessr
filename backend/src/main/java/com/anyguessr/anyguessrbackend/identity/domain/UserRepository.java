package com.anyguessr.anyguessrbackend.identity.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    User save(User user);
}
