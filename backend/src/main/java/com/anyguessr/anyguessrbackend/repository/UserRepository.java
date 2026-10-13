package com.anyguessr.anyguessrbackend.repository;

import com.anyguessr.anyguessrbackend.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    User save(User user);
}
